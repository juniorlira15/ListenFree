package br.com.victall.listenfree.models

import java.io.Serializable

/**
 * Representa um episódio de podcast no aplicativo ListenFree.
 * 
 * @property id Identificador único do episódio
 * @property title Título do episódio
 * @property description Descrição detalhada do episódio
 * @property duration Duração do episódio em segundos
 * @property releaseDate Data de lançamento do episódio
 * @property audioUrl URL para o arquivo de áudio do episódio
 * @property imageUrl URL para a imagem de capa do episódio
 * @property podcastId ID do podcast ao qual este episódio pertence
 * @property isDownloaded Indica se o episódio está baixado localmente
 * @property downloadPath Caminho local do arquivo baixado (se aplicável)
 * @property lastPlayedPosition Posição de reprodução em segundos
 * @property isCompleted Indica se o episódio foi completamente reproduzido
 */
data class PodcastEpisode(
    val id: String,
    val title: String,
    val description: String,
    val duration: Long, // em segundos
    val releaseDate: String, // formato ISO 8601
    val audioUrl: String,
    val imageUrl: String,
    val podcastId: String,
    var isDownloaded: Boolean = false,
    var downloadPath: String? = null,
    var lastPlayedPosition: Long = 0,
    var isCompleted: Boolean = false
) : Serializable {

    /**
     * Formata a duração do episódio em um formato legível (HH:MM:SS)
     */
    fun getFormattedDuration(): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Verifica se o episódio está em progresso (foi iniciado mas não concluído)
     */
    fun isInProgress(): Boolean {
        return lastPlayedPosition > 0 && !isCompleted
    }

    /**
     * Calcula a porcentagem de progresso do episódio
     */
    fun getProgressPercentage(): Int {
        if (duration == 0L) return 0
        return ((lastPlayedPosition.toFloat() / duration.toFloat()) * 100).toInt()
    }

    /**
     * Atualiza a posição de reprodução e verifica se o episódio foi concluído
     * 
     * @param position Nova posição de reprodução em segundos
     * @param threshold Porcentagem de duração que considera o episódio como concluído (padrão: 90%)
     */
    fun updatePlaybackPosition(position: Long, threshold: Float = 0.9f) {
        lastPlayedPosition = position
        
        // Considera o episódio como concluído se o usuário ouviu pelo menos 90% da duração
        if (position > 0 && duration > 0 && (position.toFloat() / duration.toFloat()) >= threshold) {
            isCompleted = true
        }
    }

    /**
     * Marca o episódio como concluído
     */
    fun markAsCompleted() {
        isCompleted = true
        lastPlayedPosition = duration
    }

    /**
     * Reseta o progresso do episódio
     */
    fun resetProgress() {
        lastPlayedPosition = 0
        isCompleted = false
    }
} 