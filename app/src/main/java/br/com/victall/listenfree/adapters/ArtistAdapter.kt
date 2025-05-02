package br.com.victall.listenfree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.victall.listenfree.databinding.ItemArtistBinding
import br.com.victall.listenfree.models.Artist
import com.bumptech.glide.Glide

class ArtistAdapter(
    private val onArtistSelected: (Artist, Boolean) -> Unit
) : ListAdapter<Artist, ArtistAdapter.ArtistViewHolder>(ArtistDiffCallback()) {

    private val selectedArtists = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtistViewHolder(
        private val binding: ItemArtistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.apply {
                tvArtistName.text = artist.name
                
                Glide.with(root.context)
                    .load(artist.imageUrl)
                    .circleCrop()
                    .into(ivArtist)

                root.isChecked = selectedArtists.contains(artist.id)
                
                root.setOnClickListener {
                    val isSelected = !root.isChecked
                    root.isChecked = isSelected
                    
                    if (isSelected) {
                        selectedArtists.add(artist.id)
                    } else {
                        selectedArtists.remove(artist.id)
                    }
                    
                    onArtistSelected(artist, isSelected)
                }
            }
        }
    }

    class ArtistDiffCallback : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }
    }

    fun getSelectedCount() = selectedArtists.size
} 