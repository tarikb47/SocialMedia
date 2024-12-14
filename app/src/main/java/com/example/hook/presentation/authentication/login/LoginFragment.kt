package com.example.hook.presentation.authentication.login

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.common.exception.BlankFieldsException
import com.example.hook.common.exception.FacebookLoginCancelledException
import com.example.hook.common.exception.FacebookSignInFailedException
import com.example.hook.databinding.FragmentLoginBinding
import com.example.hook.presentation.authentication.helpers.ToastHelper
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        setupObservers()
        setupListeners()
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
                handleError(BlankFieldsException())
            }
        }
        binding.fbButton.setOnClickListener { initiateFacebookLogin() }
        binding.googleButton.setOnClickListener { initiateGoogleLogin() }
        binding.forgotPass.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString().trim()
            viewModel.resetPassword(email)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GOOGLE_SIGN_IN_REQUEST_CODE -> {
                viewModel.handleGoogleSignIn(data)
            }

            else -> {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> {
                    }

                    is LoginState.Success -> {
                        showSuccessMessage("Login successfully")
                        findNavController().navigate(R.id.action_loginFragment_to_HomeFragment)
                    }

                    is LoginState.Error -> {
                        handleError(state.message)
                    }

                    is LoginState.Idle -> {
                    }

                    is LoginState.EmailSent -> showSuccessMessage("Reset password email sent.")
                    is LoginState.FacebookSignIn -> {
                        showSuccessMessage("Login successfully")
                        findNavController().navigate(R.id.action_loginFragment_to_HomeFragment)
                    }

                    is LoginState.Initial -> {}
                    is LoginState.GoogleSignIn -> {
                        showSuccessMessage("Login successfully")
                        findNavController().navigate(R.id.action_loginFragment_to_HomeFragment)
                    }
                }
            }

        }
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    handleError(FacebookLoginCancelledException())
                }

                override fun onError(error: FacebookException) {
                    handleError(FacebookSignInFailedException())
                }
            }
        )
    }

    private fun handleError(error: Throwable) {
        ToastHelper.showError(
            requireContext(),
            error.message ?: "An error occurred",
            layoutInflater
        )
    }

    private fun showSuccessMessage(message: String) {
        ToastHelper.showSuccess(requireContext(), message, layoutInflater)
    }


    private fun initiateFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            listOf(LIST_OF_EMAIL, "public_profile")
        )
    }

    private fun initiateGoogleLogin() {
        googleSignInClient = viewModel.getGoogleSignInClient()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            GOOGLE_SIGN_IN_REQUEST_CODE
        )
    }

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
        private const val LIST_OF_EMAIL = "email"
    }
}

