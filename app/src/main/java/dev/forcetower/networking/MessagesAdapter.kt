package dev.forcetower.networking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.forcetower.networking.databinding.ItemMessageBinding

class MessagesAdapter : ListAdapter<Message, MessagesAdapter.MessageHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        return MessageHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.binding.message = getItem(position)
    }

    inner class MessageHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    private object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}