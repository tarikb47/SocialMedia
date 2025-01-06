package com.example.hook.presentation.home.chats.allchats.chat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hook.R
import com.example.hook.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    private val currentUserId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    init {
        Log.d("MessagesAdapter", "Adapter created with currentUserId: $currentUserId")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("MessagesAdapter", "onCreateViewHolder called with viewType: $viewType")
        return when (viewType) {
            VIEW_TYPE_SENT -> SentMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            )
            VIEW_TYPE_RECEIVED -> ReceivedMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("MessagesAdapter", "onBindViewHolder called for position: $position")
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> {
                Log.d("MessagesAdapter", "Binding SentMessageViewHolder for position $position")
                holder.bind(message)
            }
            is ReceivedMessageViewHolder -> {
                Log.d("MessagesAdapter", "Binding ReceivedMessageViewHolder for position $position")
                holder.bind(message)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)

        fun bind(message: Message) {
            Log.d("MessagesAdapter", "SentMessageViewHolder.bind called for message: ${message.text}, Timestamp: ${message.timestamp}")
            messageText.text = message.text
            messageTimestamp.text = formatTimestamp(message.timestamp)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)

        fun bind(message: Message) {
            Log.d("MessagesAdapter", "ReceivedMessageViewHolder.bind called for message: ${message.text}, Timestamp: ${message.timestamp}")
            messageText.text = message.text
            messageTimestamp.text = formatTimestamp(message.timestamp)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        Log.d("MessagesAdapter", "Timestamp formatted: $timestamp -> $formattedTime")
        return formattedTime
    }

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    // DiffUtil callback for Message items
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Check if items are the same (e.g., check by unique ID or timestamp)
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Check if the contents of the messages are the same
            return oldItem == newItem
        }
    }
}
