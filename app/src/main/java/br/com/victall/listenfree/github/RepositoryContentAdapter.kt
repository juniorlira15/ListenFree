package br.com.victall.listenfree.github

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.victall.listenfree.R
import br.com.victall.listenfree.databinding.ItemRepositoryContentBinding

class RepositoryContentAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onUploadClick: (String) -> Unit
) : ListAdapter<String, RepositoryContentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRepositoryContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRepositoryContentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }

            binding.btnUpload.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUploadClick(getItem(position))
                }
            }
        }

        fun bind(item: String) {
            binding.apply {
                tvName.text = item.replace("/","")
                ivIcon.setImageResource(
                    if (item.endsWith("/")) R.drawable.ic_folder
                    else R.drawable.ic_file
                )
                val isFolder = item.endsWith("/")
                btnDelete.visibility = if (isFolder) android.view.View.VISIBLE else android.view.View.GONE
                btnUpload.visibility = if (isFolder) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 