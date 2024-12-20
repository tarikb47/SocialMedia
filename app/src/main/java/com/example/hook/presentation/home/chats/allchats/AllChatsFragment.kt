package com.example.hook.presentation.home.chats.allchats

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hook.R
import com.example.hook.databinding.FragmentAllChatsBinding
import com.example.hook.databinding.FragmentLoginBinding
import com.example.hook.presentation.home.chats.adapters.ChatsRecyclerAdapter

class AllChatsFragment : Fragment() {

    companion object {
        fun newInstance() = AllChatsFragment()
    }
     var adapter = ChatsRecyclerAdapter()
    private var _binding: FragmentAllChatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AllChatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllChatsBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        return binding.root
    }
}