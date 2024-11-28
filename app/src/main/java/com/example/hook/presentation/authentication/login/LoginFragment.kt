package com.example.hook.presentation.authentication.login

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.databinding.CustomToastBinding
import com.example.hook.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        setupObservers()
        setupListeners()
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setupListeners() {
        binding.goToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_RegisterFragment)
        }
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString().trim()
            val password = binding.loginPasswordEditText.editText?.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                showError("Please enter valid email and password")
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> {
                        Log.d("Tarik", "setupObservers: Loading faza")
                    }

                    is LoginState.Success -> {
                        showSuccess("Login successfully")
                        findNavController().navigate(R.id.action_loginFragment_to_HomeFragment)
                    }

                    is LoginState.Error -> {
                        showError(state.message)
                    }

                    is LoginState.Idle -> {
                    }
                }
            }
        }
    }

    private var isToastVisible = false
    private fun showToast(message: String) {
        if (isToastVisible) return
        isToastVisible = true
        val toastBinding = CustomToastBinding.inflate(layoutInflater)
        toastBinding.toastMessage.text = message
        Toast(requireContext()).apply {
            duration = Toast.LENGTH_LONG
            view = toastBinding.root
            setGravity(Gravity.TOP, 0, 100)
            show()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            isToastVisible = false
        }, 3500)
    }

    private fun showError(message: String) {
        showToast(message)
    }

    private fun showSuccess(message: String) {
        showToast(message)
    }

}

