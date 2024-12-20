package com.example.hook.presentation.home.chats.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.databinding.ChatsContainerBinding

class ChatsRecyclerAdapter : RecyclerView.Adapter<com.example.hook.presentation.home.chats.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): com.example.hook.presentation.home.chats.ViewHolder {
        val binding =
            ChatsContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return com.example.hook.presentation.home.chats.ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: com.example.hook.presentation.home.chats.ViewHolder,
        position: Int
    ) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
val messages = listOf(
    UserEntity(

        1,
        "",
        "Tarik",
        "one",
        phoneNumber = "aa",
        firebaseToken = "aa",
        photoUrl = "https://firebasestorage.googleapis.com/v0/b/hook-c47b9.firebasestorage.app/o/default.webp?alt=media&token=e34658b5-e46f-4a1a-85f1-8f57acde4dc8",

    ),
    UserEntity(
        2,
        "",
        "Tarik",
        "one",
        phoneNumber = "aa",
        firebaseToken = "aa",
        "https://firebasestorage.googleapis.com/v0/b/hook-c47b9.firebasestorage.app/o/default.webp?alt=media&token=e34658b5-e46f-4a1a-85f1-8f57acde4dc8"

    ),
    UserEntity(
        2,
        "",
        "Tarik",
        "one",
        phoneNumber = "aa",
        firebaseToken = "aa",
        "https://firebasestorage.googleapis.com/v0/b/hook-c47b9.firebasestorage.app/o/default.webp?alt=media&token=e34658b5-e46f-4a1a-85f1-8f57acde4dc8"

    ),
    UserEntity(
        4,
        "",
        "Tarik",
        "one",
        phoneNumber = "aa",
        firebaseToken = "aa",
        "https://firebasestorage.googleapis.com/v0/b/hook-c47b9.firebasestorage.app/o/default.webp?alt=media&token=e34658b5-e46f-4a1a-85f1-8f57acde4dc8"
    ))