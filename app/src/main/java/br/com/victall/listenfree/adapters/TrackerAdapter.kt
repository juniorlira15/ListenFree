package br.com.victall.listenfree.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.victall.listenfree.R
import br.com.victall.listenfree.databinding.ItemTrackBinding
import br.com.victall.listenfree.models.Track
import br.com.victall.listenfree.player.PlayerManager

class TrackAdapter(
    private var tracks: List<Track>,
    private val onClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var highlightedTrackId: String? = null

    inner class TrackViewHolder(val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.binding.apply {
            tvTrackTitle.text = track.name
            tvTrackDuration.text = formatDuration(track.duration)
            root.setOnClickListener {
                PlayerManager.play(holder.itemView.context, track, tracks)
                onClick(track)
            }

            val isHighlighted = track.id == highlightedTrackId
            tvTrackTitle.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    if (isHighlighted) R.color.spotify_green else android.R.color.white
                )
            )
            tvTrackDuration.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    if (isHighlighted) R.color.spotify_green else android.R.color.darker_gray
                )
            )

            lottieView.visibility = if (isHighlighted) android.view.View.VISIBLE else android.view.View.GONE
            if (isHighlighted) {
                lottieView.playAnimation()
            } else {
                lottieView.cancelAnimation()
            }
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newList: List<Track>) {
        tracks = newList
        notifyDataSetChanged()
    }

    fun setHighlightTrack(trackId: String) {
        highlightedTrackId = trackId
        notifyDataSetChanged()
    }

    fun getTracks(): List<Track> = tracks

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}
