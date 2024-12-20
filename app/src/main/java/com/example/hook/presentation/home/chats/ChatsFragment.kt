package com.example.hook.presentation.home.chats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.ui.setupWithNavController
import com.example.hook.databinding.FragmentChatsBinding
import com.example.hook.presentation.home.chats.adapters.ChatsViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator


class ChatsFragment : Fragment() {


    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatsViewModel by viewModels()
    lateinit var chatsViewPagerAdapter: ChatsViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ChatsFragment", "ChatsFragment is displayed")

        /*  val bottomNavigationView: BottomNavigationView =
              binding.navigation*/

      /*  ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { v: View, insets: WindowInsetsCompat ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                0
            )
            insets
        }*/
        chatsViewPagerAdapter = ChatsViewPagerAdapter(this)
        binding.viewPager.adapter = chatsViewPagerAdapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All"
                1 -> "Groups"
                2 -> "Channels"
                else -> {""}
            }
        }.attach()
    }
}
