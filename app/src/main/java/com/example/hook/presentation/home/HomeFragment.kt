package com.example.hook.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hook.R
import com.example.hook.databinding.FragmentHomeBinding
import com.example.hook.ui.contacts.ContactsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.log

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.fragments.forEach { fragment ->
            Log.d("HomeFragment", "Child fragment: $fragment")
        }
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.child_nav_host_fragment) as? NavHostFragment

        if (navHostFragment == null) {
            return
        } else {
        }

        val navController = navHostFragment.navController
        binding.navigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.add_contact -> {
                    binding.navigation.visibility = View.GONE
                }

                else -> {
                    binding.navigation.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        viewModel.setUserOffline()

    }

    /*override fun onStop() {
        super.onStop()
        viewModel.setUserOffline()
    }
*/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
