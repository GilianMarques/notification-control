package dev.gmarques.controledenotificacoes.domain.framework.contracts

interface VibratorProvider {
    fun error()
    fun success()
    fun interaction()

    /**Uma univca vibração bem curta*/
    fun tick()

    /**Uma vibração especifica para a animação da tela de notificações quando esta vazia*/
    fun sineAnimation()
}
