package com.example.hook.presentation.home.contacts.addContact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.common.enums.FieldType
import com.example.hook.databinding.FragmentAddContactBinding
import com.example.hook.presentation.authentication.helpers.ToastHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddContactFragment : Fragment() {

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddContactViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.addContact.setOnClickListener {
            handleAddContact()
        }
        binding.contactCredential.doOnTextChanged{_,_,_,_ ->
            viewModel.clearState()
        }
        binding.close.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleAddContact() {
        val credential = binding.contactCredential.text.toString().trim()
        val nickname = binding.nickname.text.toString().trim()

        viewModel.checkContact(credential, nickname)
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.contactState.collectLatest { state ->
                when (state) {
                    is ContactState.Idle -> {
                        binding.usernameInputLayout.error = null
                    }

                    is ContactState.Loading -> {
                        binding.registerProgressBar.visibility = View.VISIBLE
                    }

                    is ContactState.Success -> {
                        binding.registerProgressBar.visibility = View.GONE
                        showSuccessMessage("Contact added successfully.")
                        viewModel.clearState()
                    }

                    is ContactState.Error -> {
                        binding.registerProgressBar.visibility = View.GONE
                       handleError(state.message)
                        viewModel.clearState()
                    }

                    is ContactState.InputError -> {
                        binding.registerProgressBar.visibility = View.GONE
                        binding.usernameInputLayout.error = state.message.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun handleError(error: Throwable) {
        binding.registerProgressBar.visibility = View.GONE
        ToastHelper.showError(requireContext(), error.message ?: "An error occurred", layoutInflater)
    }

    private fun showSuccessMessage(message: String) {
        ToastHelper.showSuccess(requireContext(), message, layoutInflater)
    }
}
