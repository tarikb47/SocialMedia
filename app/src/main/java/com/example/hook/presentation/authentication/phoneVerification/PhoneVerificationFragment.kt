package com.example.hook.presentation.authentication.phoneVerification

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hook.R
import com.example.hook.databinding.FragmentPhoneVerificationBinding
import com.example.hook.presentation.authentication.helpers.ToastHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneVerification : Fragment() {
    private val args: PhoneVerificationArgs by navArgs()
    private var _binding: FragmentPhoneVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PhoneVerificationViewModel by viewModels()
    private var verificationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendVerificationCode()
        binding.digitOne.requestFocus()

        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.digitOne, InputMethodManager.SHOW_IMPLICIT)
        setupCodeInputBehavior()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCodeInputBehavior() {
        val codeInputs = listOf(
            binding.digitOne,
            binding.digitTwo,
            binding.digitThree,
            binding.digitFour,
            binding.digitFive,
            binding.digitSix
        )

        codeInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.isNullOrEmpty()) {
                        if (index < codeInputs.size - 1) {
                            codeInputs[index + 1].requestFocus()
                        } else if (index == codeInputs.size - 1) {
                            submitVerificationCode()
                        }
                    }

                }

                override fun afterTextChanged(p0: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isNotEmpty()) {
                        editText.text?.clear()
                    } else if (index > 0) {
                        codeInputs[index - 1].requestFocus()
                        codeInputs[index - 1].text?.clear()
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun sendVerificationCode() {
        val email = args.email
        val username = args.username
        val phoneNumber = args.phoneNumber
        val password = args.password
        viewModel.sendVerificationCode(phoneNumber)
        lifecycleScope.launch {
            viewModel.verificationState.collect { state ->
                when (state) {
                    is VerificationState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is VerificationState.CodeSent -> verificationId = state.verificationId
                    VerificationState.CompletedVerification -> {}
                    VerificationState.SignedInWithPhoneNumber -> {
                        viewModel.registerUser(username, email, phoneNumber, password)
                    }

                    VerificationState.Idle -> {}
                    VerificationState.UserRegistered -> {
                        showSuccessMessage("Registered successfully, proceed with login.")
                        navigateToNextScreen()
                    }

                    is VerificationState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        handleError(state.error)
                        setCodeInputEnabled(true)
                    }
                }
            }
        }
    }

    private fun submitVerificationCode() {
        val enteredCode =
            binding.digitOne.text.toString() + binding.digitTwo.text.toString() + binding.digitThree.text.toString() + binding.digitFour.text.toString() + binding.digitFive.text.toString() + binding.digitSix.text.toString()

        if (enteredCode.length == 6) {
            setCodeInputEnabled(false)
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                waitForVerificationIdAndSubmit(enteredCode)
            }
        }
    }

    private suspend fun waitForVerificationIdAndSubmit(enteredCode: String) {
        val timeoutMillis = 10_000
        val startTime = System.currentTimeMillis()
        while (verificationId == null && (System.currentTimeMillis() - startTime) < timeoutMillis) {
            kotlinx.coroutines.delay(100)
        }
        if (verificationId != null) {
            viewModel.phoneSignUWithCode(verificationId!!, enteredCode)
        } else {
            binding.progressBar.visibility = View.GONE
            handleError(Throwable("Verification ID not received in time. Please try again."))
            setCodeInputEnabled(true)
        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.verificationFragment_to_loginFragment)
    }

    private fun handleError(error: Throwable) {
        ToastHelper.showError(
            requireContext(), error.message ?: "An error occurred", layoutInflater
        )
    }

    private fun showSuccessMessage(message: String) {
        ToastHelper.showSuccess(requireContext(), message, layoutInflater)
    }

    private fun setCodeInputEnabled(enabled: Boolean) {
        val codeInputs = listOf(
            binding.digitOne,
            binding.digitTwo,
            binding.digitThree,
            binding.digitFour,
            binding.digitFive,
            binding.digitSix
        )
        codeInputs.forEach { it.isEnabled = enabled }
        if (!enabled) {
            binding.digitOne.clearFocus()
        }
    }
}
