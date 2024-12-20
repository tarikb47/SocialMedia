package com.example.hook.presentation.home.contacts

import com.example.hook.data.local.entity.ContactEntity

sealed class RecyclerViewItem {
    data class ContactItem(val contact: ContactEntity) : RecyclerViewItem()
    data class MenuItem(val title: String, val iconResId: Int) : RecyclerViewItem()
}
