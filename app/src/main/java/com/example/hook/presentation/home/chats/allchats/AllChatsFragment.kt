package com.example.hook.presentation.home.chats.allchats

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hook.R
import com.example.hook.databinding.FragmentAllChatsBinding
import com.example.hook.presentation.home.chats.adapters.ChatsRecyclerAdapter
import com.example.hook.domain.model.Chat
import com.example.hook.presentation.home.chats.allchats.AllChatsViewModel.ChatsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllChatsFragment : Fragment() {

    companion object {
        fun newInstance() = AllChatsFragment()
    }

    private var _binding: FragmentAllChatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AllChatsViewModel by viewModels()
    private var adapter = ChatsRecyclerAdapter()
    private lateinit var currentUser : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCurrentUserVid()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        lifecycleScope.launch {
            viewModel.chatsState.collectLatest { state ->
                when (state) {
                    is ChatsState.Initial -> {
                    }

                    is ChatsState.Loading -> {
                    }

                    is ChatsState.Error -> {
                        showError(state.error)
                    }

                    is ChatsState.UpdatedChats -> {
                        adapter.updateChats(state.chats)
                    }

                    is ChatsState.CurrentUserId -> {currentUser = state.userId
                        viewModel.fetchChats(currentUser)
                    }

                }
            }


        }
    }


    private fun showError(error: Throwable) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

