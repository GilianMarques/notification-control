package dev.gmarques.controledenotificacoes.domain.framework

interface VibratorProvider {
    fun error()
    fun success()
    fun interaction()
    fun tick()
    fun sineAnimation()
}
