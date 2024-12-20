package com.example.hook.presentation.home.chats.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hook.presentation.home.chats.allchats.AllChatsFragment
import com.example.hook.presentation.home.chats.channels.ChannelsFragment
import com.example.hook.presentation.home.chats.groups.GroupsFragment

class ChatsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllChatsFragment()
            1 -> GroupsFragment()
            2 -> ChannelsFragment()
            else -> {Fragment()}
        }
    }
}