package com.example.hook.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.navArgument
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hook.R
import com.example.hook.common.result.Result
import com.example.hook.data.remote.home.contacts.UserActivityRepository
import com.example.hook.databinding.ContactContainerBinding
import com.example.hook.databinding.DropdownContactItemBinding
import com.example.hook.presentation.home.contacts.ContactsFragmentDirections
import com.example.hook.presentation.home.contacts.ContactsViews
import com.example.hook.presentation.home.contacts.RecyclerViewItem
import kotlinx.coroutines.flow.collectLatest
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MixedAdapter(
    private val navController: NavController,
    private val userActivityRepository: UserActivityRepository,
    private val scope: CoroutineScope
) : ListAdapter<RecyclerViewItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {

    var isMenuVisible = false

    fun showMenu() {
        if (!isMenuVisible) {
            isMenuVisible = true
            val currentList = menuItems + currentList.filterIsInstance<RecyclerViewItem.ContactItem>()
            submitList(currentList)
        }
    }

    fun hideMenu() {
        if (isMenuVisible) {
            isMenuVisible = false
            val currentList = currentList.filterIsInstance<RecyclerViewItem.ContactItem>()
            submitList(currentList)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecyclerViewItem.ContactItem -> ContactsViews.CONTACT.ordinal
            is RecyclerViewItem.MenuItem -> ContactsViews.MENU.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (ContactsViews.values()[viewType]) {
            ContactsViews.CONTACT -> {
                val binding = ContactContainerBinding.inflate(inflater, parent, false)
                ContactViewHolder(binding, userActivityRepository, scope)
            }

            ContactsViews.MENU -> {
                val binding = DropdownContactItemBinding.inflate(inflater, parent, false)
                MenuViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is RecyclerViewItem.ContactItem -> (holder as ContactViewHolder).bind(item, navController)
            is RecyclerViewItem.MenuItem -> (holder as MenuViewHolder).bind(item, navController)
        }
    }

    class ContactViewHolder(
        private val binding: ContactContainerBinding,
        private val userActivityRepository: UserActivityRepository,
        private val scope: CoroutineScope
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: RecyclerViewItem.ContactItem, navController: NavController) {
            binding.contact = contact.contact
            Glide.with(binding.imageProfile.context)
                .load(contact.contact.photoUrl)
                .placeholder(R.drawable.add_new_icon)
                .circleCrop()
                .into(binding.imageProfile)
            binding.root.setOnClickListener {
                val action = ContactsFragmentDirections.actionNavContactsToDirectChat(contact.contact.userId)
                navController.navigate(action)
            }
            scope.launch {
                userActivityRepository.observeUserStatus(contact.contact.userId).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val activityStatus = result.data
                            binding.activityStatus.text = when (activityStatus) {
                                true -> "Online"
                                is Timestamp -> "Last active: ${formatTimestamp(activityStatus)}"
                                else -> activityStatus.toString()
                            }
                        }
                        is Result.Error -> {
                            binding.activityStatus.text = "Unknown"
                        }
                    }
                }
            }
        }

        private fun formatTimestamp(timestamp: Timestamp): String {
            val millis = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
            val date = Date(millis)
            val formatter = SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault())
            return formatter.format(date)
        }
    }

    class MenuViewHolder(private val binding: DropdownContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(menuItem: RecyclerViewItem.MenuItem, navController: NavController) {
            binding.menuText.text = menuItem.title
            binding.menuIcon.setImageResource(menuItem.iconResId)
            binding.root.setOnClickListener {
                when (menuItem.title) {
                    "Find People Nearby" -> {  }
                    "Invite Friends" -> {  }
                    "Contact Categories" -> {  }
                    "Add New Contact" -> navController.navigate(R.id.action_nav_contacts_to_add_contact)
                }
            }
        }
    }

    private class ItemDiffCallback : DiffUtil.ItemCallback<RecyclerViewItem>() {
        override fun areItemsTheSame(oldItem: RecyclerViewItem, newItem: RecyclerViewItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RecyclerViewItem, newItem: RecyclerViewItem): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        val menuItems = listOf(
            RecyclerViewItem.MenuItem("Find People Nearby", R.drawable.vector2),
            RecyclerViewItem.MenuItem("Invite Friends", R.drawable.vector1),
            RecyclerViewItem.MenuItem("Contact Categories", R.drawable.sss),
            RecyclerViewItem.MenuItem("Add New Contact", R.drawable.ic_add_plus_blue)
        )
    }
}
