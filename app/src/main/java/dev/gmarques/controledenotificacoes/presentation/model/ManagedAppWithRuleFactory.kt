package dev.gmarques.controledenotificacoes.presentation.model

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule

/**
 * Cria instâncias de [ManagedAppWithRule] combinando informações do app instalado, estado de gerenciamento e regra vigente.
 */
object ManagedAppWithRuleFactory {

    /**
     * Cria um [ManagedAppWithRule] a partir de um aplicativo instalado, seu registro gerenciado e a regra correspondente.
     */
    fun create(
        installedApp: InstalledApp,
        managedApp: ManagedApp,
        rule: Rule,
    ): ManagedAppWithRule {
        return ManagedAppWithRule(
            name = installedApp.name,
            packageName = installedApp.packageName,
            rule = rule,
            hasPendingNotifications = managedApp.hasPendingNotifications,
            uninstalled = installedApp.uninstalled,
        )
    }
}
