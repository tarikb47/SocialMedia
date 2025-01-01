package com.example.hook.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hook.data.remote.home.contacts.UserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.hook.common.result.Result
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val userActivityRepository: UserActivityRepository
) : ViewModel() {

    fun setUserOnline(){
        viewModelScope.launch {
            Log.d("Tarik", "launchalo")
            userActivityRepository.userOnline().collectLatest {result->
                Log.d("Tarik", "Collecting result")
                when(result){
                    is Result.Error -> Log.d("Tarik", "${result.error}")
                    is Result.Success -> Log.d("Tarik","Online")
                }
            }
        }

    }
    fun setUserOffline(){
        userActivityRepository.userOffline()
    }
}