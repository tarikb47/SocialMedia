package com.example.hook.presentation.home.chats.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hook.R
import com.example.hook.databinding.ChatsContainerBinding
import com.example.hook.domain.model.Chat
import com.example.hook.presentation.authentication.helpers.TimeFormater

class ChatsRecyclerAdapter(        private val onChatClicked: (Chat) -> Unit

) : RecyclerView.Adapter<ChatsRecyclerAdapter.ChatViewHolder>() {
    private val timeFormater = TimeFormater()
    private var chats: List<Chat> = emptyList()
    inner class ChatViewHolder(private val binding: ChatsContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.lastMessage.text = chat.lastMessage
            binding.time.text =timeFormater.getRelativeTime(chat.timestamp)
            binding.username.text = chat.username
            Glide.with(binding.root.context)
                .load(chat.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.add_new_icon)
                .error(R.drawable.add_new_icon)
                .into(binding.imageProfile)
            binding.root.setOnClickListener{
                onChatClicked(chat)
            }
        }


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatsContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }
    fun updateChats(updatedChats: List<Chat>) {
        this.chats = updatedChats
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = chats.size
}
