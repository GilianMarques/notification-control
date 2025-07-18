package dev.gmarques.controledenotificacoes.framework.notification_listener_service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.domain.framework.AlarmScheduler
import dev.gmarques.controledenotificacoes.domain.framework.RuleEnforcer
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.isSatisfiedBy
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.Rule.Type
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAppInBlockPeriod
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:16.
 */
class RuleEnforcerImpl @Inject constructor(
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val updateManagedAppUseCase: dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase,
    private val insertAppNotificationUseCase: InsertAppNotificationUseCase,
    @ApplicationContext private val context: Context,
) : RuleEnforcer {

    private lateinit var appNotification: AppNotification
    private lateinit var activeNotification: ActiveStatusBarNotification
    private lateinit var callback: RuleEnforcer.Callback

    override fun enforceOnNotification(
        activeNotification: ActiveStatusBarNotification,
        appNotification: AppNotification,
        callback: RuleEnforcer.Callback,
    ) = runBlocking {

        this@RuleEnforcerImpl.activeNotification = activeNotification
        this@RuleEnforcerImpl.appNotification = appNotification
        this@RuleEnforcerImpl.callback = callback


        val managedApp = getManagedAppByPackageIdUseCase(appNotification.packageId)

        if (managedApp != null) {
            val rule = getRuleByIdUseCase(managedApp.ruleId)
                ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")

            enforceRuleAndCondition(rule, managedApp)

        } else callback.appNotManaged(activeNotification)

    }

    private fun enforceRuleAndCondition(
        rule: Rule,
        managedApp: ManagedApp,
    ) {

        val condition = rule.condition
        val appInBlockPeriod = rule.isAppInBlockPeriod()

        if (condition == null) {
            if (appInBlockPeriod) saveAndBlockNotification(rule, managedApp)
            else callback.allowNotification(activeNotification)
            return
        }

        val shouldAllowNotification = shouldAllowNotification(
            ruleType = rule.type,
            conditionType = condition.type,
            isConditionSatisfied = condition.isSatisfiedBy(appNotification),
            isAppInBlockPeriod = appInBlockPeriod
        )

        if (shouldAllowNotification) callback.allowNotification(activeNotification)
        else saveAndBlockNotification(rule, managedApp)
    }

    @TestOnly
    fun shouldAllowNotification(
        ruleType: Type,
        conditionType: Condition.Type,
        isConditionSatisfied: Boolean,
        isAppInBlockPeriod: Boolean,
    ): Boolean {

        if (ruleType == Type.RESTRICTIVE && isAppInBlockPeriod) {
            return when (conditionType) {
                Condition.Type.ONLY_IF -> !isConditionSatisfied
                Condition.Type.EXCEPT -> isConditionSatisfied
            }
        }

        if (ruleType == Type.PERMISSIVE && !isAppInBlockPeriod) {
            return when (conditionType) {
                Condition.Type.ONLY_IF -> isConditionSatisfied
                Condition.Type.EXCEPT -> !isConditionSatisfied
            }
        }

        Log.w(
            "USUK",
            "RuleEnforcerImpl.shouldAllowNotification: notificaçção permitida pq nao caiu em nenhuma pré-condição"
        )

        return true
    }

    private fun saveAndBlockNotification(
        rule: Rule,
        managedApp: ManagedApp,
    ) {
        val nextUnlockTime = rule.nextAppUnlockPeriodFromNow()

        // snooze isnt supported in this sdk, calling cancel
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            callback.cancelNotification(activeNotification, appNotification, rule, managedApp)
        } else when (rule.action) {

            Rule.Action.SNOOZE -> {
                val snoozeFor = nextUnlockTime - System.currentTimeMillis()
                callback.snoozeNotification(activeNotification, snoozeFor)
            }

            Rule.Action.CANCEL -> {
                callback.cancelNotification(activeNotification, appNotification, rule, managedApp)
            }
        }

        runBlocking {
            alarmScheduler.scheduleAlarm(appNotification.packageId, nextUnlockTime)
            updateManagedAppUseCase(managedApp.copy(hasPendingNotifications = true))
            saveNotificationOnHistory()
            saveLargeIconOnStorage()
        }

    }

    fun saveNotificationOnHistory() = runBlocking {

        if (appNotification.title.isEmpty() && appNotification.content.isEmpty()) return@runBlocking

        insertAppNotificationUseCase(appNotification)

        activeNotification.notification.contentIntent?.let {
            PendingIntentCache.add(appNotification.pendingIntentId(), activeNotification.notification.contentIntent!!)
        }

    }

    fun saveLargeIconOnStorage() {

        try {
            val icon = activeNotification.largeIcon ?: return
            val drawable = icon.loadDrawable(context) ?: return

            val bitmap = (drawable as BitmapDrawable).bitmap

            val file = File(context.cacheDir, appNotification.bitmapId())

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            // Log.d("USUK", "RuleEnforcerImpl.saveLargeIcon: largeIcon for ${activeNotification.packageName} saved")
        } catch (e: Exception) {
            // Log.e("USUK", "RuleEnforcerImpl.saveLargeIcon: failure while saving notification's large icon from ${activeNotification.packageName}")
            e.stackTrace
        }

    }


}

