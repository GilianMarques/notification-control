package dev.gmarques.controledenotificacoes.framework.model


data class RemoteConfigValues(
    val blockApp: Boolean = false,
    val contactEmail: String? = null,
    val playStoreAppLink: String? = null,
    val privacyUrl: String? = null,
)