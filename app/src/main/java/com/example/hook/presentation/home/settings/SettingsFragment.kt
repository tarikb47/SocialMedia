package com.example.hook.presentation.home.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.hook.R
import com.example.hook.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.userState.collectLatest { user ->
                user?.let {
                    binding.username.text = it.username
                    binding.phoneNumber.text = it.phoneNumber ?: "N/A"
                    binding.email.text = it.email
                    Glide.with(requireContext())
                        .load(it.photoUrl)
                        .transform(CircleCrop())
                        .into(binding.imageProfile)

                }
            }
        }

        viewModel.getUser()
    }

    private fun setupClickListeners() {
        binding.edit.setOnClickListener {
            findNavController().navigate(R.id.action_nav_settings_to_edit_profile)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
