package com.example.hook.presentation.home.chats

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hook.R
import com.example.hook.data.local.entity.UserEntity
import com.example.hook.databinding.ChatsContainerBinding

class ViewHolder (private val binding: ChatsContainerBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserEntity) {
        Glide.with(binding.imageProfile.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.add_new_icon)
            .error(R.drawable.add_new_icon)
            .circleCrop()
            .into(binding.imageProfile)

        binding.user = user
    }
}