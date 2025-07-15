package dev.gmarques.controledenotificacoes.domain.data

/**
 * Um objeto utilitário para gerenciar propriedades de preferências individuais.
 *
 * Esta classe simplifica o acesso e a modificação de valores de preferências armazenados no dispositivo.
 * Ela lida com a leitura, escrita e verificação de valores padrão para uma preferência específica,
 * eliminando a necessidade de interagir diretamente com UseCases ou Repositórios para cada operação.
 *
 * @param T O tipo de dado da propriedade da preferência.
 * @property key A chave única que identifica esta preferência no armazenamento.
 * @property defaultValue O valor padrão para esta preferência, usado se nenhum valor estiver salvo.
 * @property preferenceReader Uma função lambda para ler o valor da preferência do armazenamento.
 *                            Recebe a `key` e o `defaultValue` e retorna o valor lido ou o padrão.
 * @property preferenceSaver Uma função lambda para salvar o valor da preferência no armazenamento.
 *                           Recebe a `key` e o novo `value` a ser salvo.
 */
class PreferenceProperty<T>(
    private val key: String,
    private val defaultValue: T,
    private val preferenceReader: (String, T) -> T,
    private val preferenceSaver: (String, T) -> Unit,
) {
    // Cache para o valor da preferência. Usado para refletir alterações em tempo real
    // e evitar leituras desnecessárias do armazenamento.
    private var cachedValue: T? = null

    /**
     * O valor atual da preferência.
     * Se um valor estiver em cache, ele é retornado. Caso contrário, o valor é lido
     * do armazenamento usando `preferenceReader`, armazenado em cache e então retornado.
     */
    val value: T
        @Synchronized get() = cachedValue ?: preferenceReader(key, defaultValue).also { cachedValue = it }

    /**
     * Verifica se o valor atual da preferência é igual ao seu valor padrão.
     * @return `true` se o valor atual for o padrão, `false` caso contrário.
     */
    fun isDefault(): Boolean = value == defaultValue

    /**Salva o valor padrao da preferencia no armazenamento*/
    @Synchronized
    fun reset() {
        cachedValue = defaultValue
        preferenceSaver(key, defaultValue)
    }

    /**
     * Atualiza o valor da preferência com o novo valor fornecido.
     * O novo valor é armazenado em cache e salvo no armazenamento do dispositivo.
     */
    @Synchronized
    fun set(value: T) {
        cachedValue = value
        preferenceSaver(key, value)
    }
}