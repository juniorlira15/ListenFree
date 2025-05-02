package br.com.victall.listenfree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.victall.listenfree.R
import br.com.victall.listenfree.databinding.ItemPodcastBinding
import br.com.victall.listenfree.models.Podcast
import com.bumptech.glide.Glide

class PodcastAdapter(
    private val onPodcastClick: (Podcast) -> Unit,
    private val onFavoriteClick: (Podcast) -> Unit
) : ListAdapter<Podcast, PodcastAdapter.PodcastViewHolder>(PodcastDiffCallback()) {

    private val selectedPodcasts = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val binding = ItemPodcastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PodcastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PodcastViewHolder(private val binding: ItemPodcastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(podcast: Podcast) {
            binding.apply {
                tvPodcastTitle.text = podcast.name
                tvPodcastAuthor.text = podcast.author

                Glide.with(ivPodcastCover)
                    .load(podcast.imageUrl)
                    .placeholder(R.drawable.placeholder_podcast)
                    .error(R.drawable.placeholder_podcast)
                    .into(ivPodcastCover)

                btnFavorite.isSelected = selectedPodcasts.contains(podcast.id)

                root.setOnClickListener {
                    onPodcastClick(podcast)
                }

                btnFavorite.setOnClickListener {
                    val isSelected = !btnFavorite.isSelected
                    btnFavorite.isSelected = isSelected
                    
                    if (isSelected) {
                        selectedPodcasts.add(podcast.id)
                    } else {
                        selectedPodcasts.remove(podcast.id)
                    }
                    onFavoriteClick(podcast)
                }
            }
        }
    }

    private class PodcastDiffCallback : DiffUtil.ItemCallback<Podcast>() {
        override fun areItemsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Podcast, newItem: Podcast): Boolean {
            return oldItem == newItem
        }
    }
}
