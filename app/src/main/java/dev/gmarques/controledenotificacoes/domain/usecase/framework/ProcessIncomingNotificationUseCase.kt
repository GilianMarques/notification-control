/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dev.gmarques.controledenotificacoes.domain.usecase.framework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.service.notification.StatusBarNotification
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gmarques.controledenotificacoes.AppLogger
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor.PerformAction.Allow
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor.PerformAction.Cancel
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor.PerformAction.Snooze
import dev.gmarques.controledenotificacoes.domain.framework.contracts.alarms.ReportNotificationAlarmScheduler
import dev.gmarques.controledenotificacoes.domain.implementations.IncomingNotificationProcessorImpl
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.bitmapId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationExtensionFun.pendingIntentId
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationFactory
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nextAppUnlockPeriodFromNow
import dev.gmarques.controledenotificacoes.domain.usecase.app_notification.InsertAppNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.GetManagedAppByPackageIdUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.managed_apps.UpdateManagedAppUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GetRuleByIdUseCase
import dev.gmarques.controledenotificacoes.framework.PendingIntentCache
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 18 de julho de 2025 as 16:39.
 *
 * Processa uma notificação recebida no dispositivo pelo [NotificationListener]
 * para determinar se ela deve ser permitida ou bloqueada e executa as ações relacionadas
 * ao processo como manter historico, fazer cache de bitmap, agendar alarme, etc...
 *
 * usa [IncomingNotificationProcessorImpl] para o processamento da notificação
 */
class ProcessIncomingNotificationUseCase @Inject constructor(
    private val getManagedAppByPackageIdUseCase: GetManagedAppByPackageIdUseCase,
    private val getRuleByIdUseCase: GetRuleByIdUseCase,
    private val reportNotificationAlarmScheduler: ReportNotificationAlarmScheduler,
    private val updateManagedAppUseCase: UpdateManagedAppUseCase,
    private val insertAppNotificationUseCase: InsertAppNotificationUseCase,
    private val incomingNotificationProcessor: IncomingNotificationProcessor,
    @ApplicationContext private val context: Context,
) {

    operator fun invoke(sbn: StatusBarNotification): ProcessingResult = runBlocking {

        val targetNotification = ActiveStatusBarNotificationFactory.create(sbn)

        val managedApp = getManagedAppByPackageIdUseCase(sbn.packageName) ?: run {
            return@runBlocking ProcessingResult.AppNotManaged(targetNotification)
        }

        val rule = getRule(managedApp)


        val actionToPerform = incomingNotificationProcessor.processNotification(
            AppNotificationFactory.create(targetNotification),
            rule,
            managedApp,
        )

        return@runBlocking when (actionToPerform) {
            Allow -> {
                AppLogger.d("Allow not: ${targetNotification.title}", rule)
                if (rule.keepFullHistory) saveNotification(targetNotification)
                ProcessingResult.AllowNotification(targetNotification)
            }

            Cancel -> {
                AppLogger.d("Cancel not: ${targetNotification.title}", rule)
                saveNotification(targetNotification)
                scheduleReportNotification(rule, targetNotification)
                setHasPendingNotificationsForManagedApp(managedApp)
                ProcessingResult.CancelNotification(targetNotification)
            }

            Snooze -> {
                AppLogger.d("Snooze not: ${targetNotification.title}", rule)
                saveNotification(targetNotification)
                setHasPendingNotificationsForManagedApp(managedApp)
                val until = rule.nextAppUnlockPeriodFromNow()
                if (until < 0) error("O periodo de adiamento deve ser positivo. verifique se a regra é permablock e o proximo periodo. rule: $rule ")
                ProcessingResult.SnoozeNotification(targetNotification, until)
            }
        }

    }


    /**Retorna uma regra ou lança uma exceção*/
    private fun getRule(managedApp: ManagedApp): Rule = runBlocking {
        return@runBlocking getRuleByIdUseCase(managedApp.ruleId)
            ?: error("Um app gerenciado deve ter uma regra. Isso é um Bug $managedApp")
    }


    /**
     * Salva a notificação no banco de dados, armazena em cache seu `PendingIntent` e
     * salva uma cópia em cache do bitmap do ícone grande da notificação, se houver.
     *
     * Esta função é executada em um `runBlocking` para garantir que as operações de banco de dados e
     * sistema de arquivos sejam concluídas antes que a função retorne.
     * As exceções durante o salvamento do bitmap são registradas, mas não interrompem o fluxo.
     *
     * @param targetNotification A notificação a ser salva.
     */
    private fun saveNotification(targetNotification: ActiveStatusBarNotification) = runBlocking {

        if (!isValidNotification(targetNotification)) return@runBlocking

        val appNotification = AppNotificationFactory.create(targetNotification)

        insertAppNotificationUseCase(appNotification)

        targetNotification.notification.contentIntent?.let {
            PendingIntentCache.add(appNotification.pendingIntentId(), it)
        }

        try {
            val bitmap = (targetNotification.largeIcon?.loadDrawable(context) as BitmapDrawable).bitmap
            val file = File(context.cacheDir, appNotification.bitmapId())

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

        } catch (e: Exception) {
            Log.w(
                "USUK",
                "ProcessIncomingNotificationUseCase.saveNotification: error saving largeIcon from:\n$targetNotification\nerror: ${e.stackTrace} "
            )
        }

    }

    /**
     * Verifica se uma notificação é válida para ser salva.
     * Uma notificação é considerada inválida se não tiver título, conteúdo e ícone grande.
     * @param appNotification A notificação a ser validada.
     * @return True se a notificação for válida, false caso contrário.
     */
    private fun isValidNotification(appNotification: ActiveStatusBarNotification): Boolean {
        return !(appNotification.title.isEmpty() &&
                appNotification.content.isEmpty() &&
                appNotification.largeIcon == null)
    }

    /**
     * Define o sinalizador `hasPendingNotifications` como verdadeiro para o aplicativo gerenciado fornecido.
     *
     * Esta função é executada em um `runBlocking` para garantir que a atualização do banco de dados
     * seja concluída antes que a função retorne.
     *
     * @param managedApp O aplicativo gerenciado a ser atualizado.
     */
    private fun setHasPendingNotificationsForManagedApp(managedApp: ManagedApp) = runBlocking {
        updateManagedAppUseCase(managedApp.copy(hasPendingNotifications = true))
    }

    /**
     * Agenda uma notificação de relatório para a regra e notificação de destino fornecidas.
     *
     * A notificação de relatório será acionada no próximo período de desbloqueio do aplicativo,
     * conforme definido pela regra.
     *
     * @param rule A regra que define o período de desbloqueio do aplicativo.
     * @param targetNotification A notificação de destino para a qual o relatório será agendado.
     */
    private fun scheduleReportNotification(rule: Rule, targetNotification: ActiveStatusBarNotification) {
        val nextUnlockTime = rule.nextAppUnlockPeriodFromNow()
        reportNotificationAlarmScheduler.scheduleAlarm(targetNotification.packageName, nextUnlockTime)
    }

    sealed class ProcessingResult {
        data class AllowNotification(val targetNotification: ActiveStatusBarNotification) : ProcessingResult()
        data class CancelNotification(val targetNotification: ActiveStatusBarNotification) : ProcessingResult()
        data class SnoozeNotification(val targetNotification: ActiveStatusBarNotification, val until: Long) :
            ProcessingResult()

        data class AppNotManaged(val targetNotification: ActiveStatusBarNotification) : ProcessingResult()

    }
}