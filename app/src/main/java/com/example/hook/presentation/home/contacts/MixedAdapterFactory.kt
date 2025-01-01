package com.example.hook.presentation.home.contacts

import androidx.navigation.NavController
import com.example.hook.data.remote.home.contacts.UserActivityRepository
import com.example.hook.ui.contacts.MixedAdapter
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class MixedAdapterFactory @Inject constructor(
    private val userActivityRepository: UserActivityRepository
) {
    fun create(navController: NavController, scope: CoroutineScope): MixedAdapter {
        return MixedAdapter(navController, userActivityRepository, scope)
    }
}