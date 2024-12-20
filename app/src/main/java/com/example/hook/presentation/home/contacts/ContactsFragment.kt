package com.example.hook.presentation.home.contacts

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hook.R

import com.example.hook.databinding.FragmentContactsBinding
import com.example.hook.ui.contacts.ContactsViewModel
import com.example.hook.ui.contacts.MixedAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var layoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ChatsFragment", "ChatsFragment is displayed")
        layoutManager = LinearLayoutManager(requireContext())
         val adapter = MixedAdapter(findNavController())

        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.contacts.collectLatest { contacts ->
                adapter.submitList(contacts)
            }
        }
        binding.add.setOnClickListener() {
            if (!adapter.isMenuVisible) {
                adapter.showMenu()
                binding.recyclerView.post {
                    layoutManager.smoothScrollToPosition(
                        binding.recyclerView,
                        RecyclerView.State(),
                        0
                    )
                }
            }

        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && adapter.isMenuVisible) {
                    adapter.hideMenu()
                }

            }
        })


        viewModel.loadContacts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}