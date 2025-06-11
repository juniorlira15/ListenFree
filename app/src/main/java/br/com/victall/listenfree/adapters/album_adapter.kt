package br.com.victall.listenfree.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.victall.listenfree.R
import br.com.victall.listenfree.databinding.ItemAlbumBinding
import br.com.victall.listenfree.models.Album
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class AlbumAdapter(
    private var albums: List<Album>,
    private val onClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {
    private val TAG = "AlbumAdapter"

    inner class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        Log.d(TAG, "Binding album at position $position: ${album.titulo}")
        
        holder.binding.apply {
            tvAlbumTitle.text = album.titulo
            tvArtistTitle.text = album.artist

            
            // Carrega a imagem com placeholder e tratamento de erro
            Glide.with(ivAlbumCover.context)
                .load(album.imageUrl)
                .placeholder(R.drawable.placeholder_album)
                .error(R.drawable.placeholder_album)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(ivAlbumCover)

            root.setOnClickListener { 
                Log.d(TAG, "Album clicked: ${album.titulo}")
                onClick(album) 
            }
        }
    }

    override fun getItemCount(): Int = albums.size

    fun updateList(newList: List<Album>) {
        Log.d(TAG, "Updating album list. New size: ${newList.size}")
        albums = newList
        notifyDataSetChanged()
    }
}
