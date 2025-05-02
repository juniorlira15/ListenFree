package br.com.victall.listenfree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.victall.listenfree.databinding.ItemPodcastEpisodeBinding
import br.com.victall.listenfree.models.PodcastEpisode
import com.bumptech.glide.Glide

/**
 * Adaptador para exibir episódios de podcast em um RecyclerView
 */
class PodcastEpisodeAdapter(
    private val onEpisodeClick: (PodcastEpisode) -> Unit,
    private val onDownloadClick: (PodcastEpisode) -> Unit
) : ListAdapter<PodcastEpisode, PodcastEpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemPodcastEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EpisodeViewHolder(
        private val binding: ItemPodcastEpisodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: PodcastEpisode) {
            binding.apply {
                tvEpisodeTitle.text = episode.title
                tvEpisodeDuration.text = episode.getFormattedDuration()
                
                // Exibe o progresso se o episódio estiver em progresso
                if (episode.isInProgress()) {
                    progressBar.visibility = android.view.View.VISIBLE
                    progressBar.progress = episode.getProgressPercentage()
                    tvProgress.text = "${episode.getProgressPercentage()}%"
                } else {
                    progressBar.visibility = android.view.View.GONE
                    tvProgress.visibility = android.view.View.GONE
                }
                
                // Exibe ícone de download se o episódio estiver baixado
                if (episode.isDownloaded) {
                    btnDownload.setImageResource(android.R.drawable.ic_menu_save)
                } else {
                    btnDownload.setImageResource(android.R.drawable.stat_sys_download)
                }
                
                // Carrega a imagem do episódio
                Glide.with(root.context)
                    .load(episode.imageUrl)
                    .into(ivEpisodeCover)
                
                // Configura os cliques
                root.setOnClickListener {
                    onEpisodeClick(episode)
                }
                
                btnDownload.setOnClickListener {
                    onDownloadClick(episode)
                }
            }
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<PodcastEpisode>() {
        override fun areItemsTheSame(oldItem: PodcastEpisode, newItem: PodcastEpisode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PodcastEpisode, newItem: PodcastEpisode): Boolean {
            return oldItem == newItem
        }
    }
} 