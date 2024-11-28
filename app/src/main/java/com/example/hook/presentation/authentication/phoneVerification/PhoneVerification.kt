package com.example.hook.presentation.authentication.phoneVerification

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.databinding.CustomToastBinding
import com.example.hook.databinding.FragmentPhoneVerificationBinding
import com.example.hook.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneVerification : Fragment() {
    private var _binding: FragmentPhoneVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PhoneVerificationViewModel by viewModels()
    private var verificationId: String? = null

    /*
        private var user: User? = null
    */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneVerificationBinding.inflate(inflater, container, false)

        binding.digitOne.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.digitOne, InputMethodManager.SHOW_IMPLICIT)

        setupCodeInputBehavior()
        viewModel.getUserFromLocal(1)
        lifecycleScope.launchWhenStarted {
            viewModel.userLocalFetchStateFlow.collect { state ->
                when (state) {
                    is UserLocalState.Idle -> {}
                    is UserLocalState.Loading -> {}
                    is UserLocalState.Success -> {
                        sendVerificationCode(state.user.phoneNumber)
                    }

                    is UserLocalState.Error -> {
                        Log.d("Tarik", "onCreateView: Nece da fetcha sa lokalne")
                    }
                }

            }
        }

        /*
        user = arguments?.let { PhoneVerificationArgs.fromBundle(it).user }
        user?.let { sendVerificationCode(it.phoneNumber) }*/
        return binding.root
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

    private fun sendVerificationCode(phonenumber: String) {
        val phoneNumber = "+38761068883"
        lifecycleScope.launch {
            viewModel.sendVerificationCode(phonenumber)
            val user = viewModel.getFetchedUser()
            viewModel.verificationState.collect { state ->
                when (state) {
                    is VerificationState.Loading -> {
                    }

                    is VerificationState.CodeSent -> {
                        verificationId = state.verificationId
                    }

                    is VerificationState.Completed -> {
                        viewModel.registerUser(user!!)
                        navigateToNextScreen()
                        showSuccess()
                    }

                    is VerificationState.Error -> {
                        showError(state.message)
                    }

                    else -> showError("unresolved")
                }
            }
        }
    }

    private fun submitVerificationCode() {
        val enteredCode = binding.digitOne.text.toString() +
                binding.digitTwo.text.toString() +
                binding.digitThree.text.toString() +
                binding.digitFour.text.toString() +
                binding.digitFive.text.toString() +
                binding.digitSix.text.toString()
        val user = viewModel.getFetchedUser()
        if (enteredCode.length == 6 && verificationId != null) {
            viewModel.verifyCode(verificationId!!, enteredCode, user!!)

        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.verificationFragment_to_homeFragment)
    }

    private fun showSuccess() {
        showToast("Registrovan.")
    }

    private fun showError(message: String) {
        showToast("failalo")
    }

    private fun showToast(message: String) {
        val toastBinding = CustomToastBinding.inflate(layoutInflater)
        toastBinding.toastMessage.text = message
        Toast(requireContext()).apply {
            duration = Toast.LENGTH_LONG
            view = toastBinding.root
            setGravity(Gravity.TOP, 0, 100)
            show()
        }
    }
}
