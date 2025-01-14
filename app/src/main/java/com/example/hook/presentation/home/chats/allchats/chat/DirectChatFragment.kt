package com.example.hook.presentation.home.chats.allchats.chat

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hook.databinding.FragmentDirectChatBinding
import com.example.hook.domain.model.Message
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.log

@AndroidEntryPoint
class DirectChatFragment : Fragment() {
    private var _binding: FragmentDirectChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DirectChatViewModel by viewModels()
    private val args: DirectChatFragmentArgs by navArgs()
    private val contact: String by lazy { args.contactId }
    private lateinit var currentUser: String
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCurrentUserVid()
        setObservers()
        setListeners()
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                bottom = imeInsets.bottom.coerceAtLeast(systemBarsInsets.bottom)
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setListeners() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                val newMessage = Message(
                    text = message,
                    senderId = currentUser,
                    timestamp = System.currentTimeMillis()
                )
                viewModel.sendMessage(
                    listOf(currentUser, contact),
                    newMessage,
                    currentUser,
                    contact
                )
                binding.messageInput.text.clear()
            }

        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.messageState.collectLatest { state ->
                when (state) {
                    is MessageState.CurrentUserId -> {
                        currentUser = state.userId
                        messagesAdapter = MessagesAdapter(currentUser)
                        val layoutManager = LinearLayoutManager(requireContext())
                        binding.messagesRv.layoutManager = layoutManager
                        binding.messagesRv.adapter = messagesAdapter
                        viewModel.listenForNewMessages(currentUser, contact)
                        viewModel.fetchMessages(currentUser, contact)
                        viewModel.getChatDetails(contact)
                        viewModel.observeActivity(contact)
                    }

                    is MessageState.Error -> {}
                    MessageState.Initial -> {}
                    MessageState.Loading -> {}
                    MessageState.MessageSent -> {
                        binding.messagesRv.scrollToPosition(messagesAdapter.itemCount - 1)
                    }

                    is MessageState.MessagesLoaded -> {
                        updateMessages(state.messages)
                    }

                    is MessageState.ContactDetailsFetched -> {
                        Glide.with(requireContext())
                            .load(state.userProfile.photoUrl)
                            .circleCrop()
                            .into(binding.imageProfile)
                        binding.username.text = state.userProfile.username
                    }

                    is MessageState.UserActivityStatus -> {
                        binding.ativity.text = state.status
                    }
                }
            }


        }
    }

    private fun updateMessages(messages: List<Message>) {
        messagesAdapter.submitList(messages) {
            if (messages.isNotEmpty()) {
                binding.messagesRv.post {
                    val layoutManager = binding.messagesRv.layoutManager as LinearLayoutManager
                    Log.d("Tarik", "RecyclerView size: ${binding.messagesRv.adapter?.itemCount}")
                    layoutManager.scrollToPositionWithOffset(messages.size - 1, 0)
                    Log.d("Tarik", "Scrolling to position: ${messages.size - 1}")
                }
            }
        }
    }
}


