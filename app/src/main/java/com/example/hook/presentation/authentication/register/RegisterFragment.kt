package com.example.hook.presentation.authentication.register
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.common.exception.FacebookLoginCancelledException
import com.example.hook.common.exception.FacebookSignInFailedException
import com.example.hook.databinding.FragmentRegisterBinding
import com.example.hook.presentation.authentication.helpers.ToastHelper
import com.example.hook.presentation.authentication.helpers.UserInput
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        setObservers()
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setListeners() {
        binding.signUpButton.setOnClickListener {
            val userInput = getUserInput()
            viewModel.validateUserInput(
                username = userInput.username,
                email = userInput.email,
                phone = userInput.phone,
                password = userInput.password
            )
        }
        binding.fbButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }
        binding.googleButton.setOnClickListener {
            googleSignInClient = viewModel.getGoogleSignInClient()
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
        }
        binding.goToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }


    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterState.CredentialSignInSuccess -> showCredentialSignInSuccess()
                    RegisterState.Initial -> {}
                    RegisterState.Loading -> showLoading()
                    RegisterState.ValidInput -> {
                       val userInput = getUserInput()
                        showValidInput(userInput.username, userInput.email, userInput.phone, userInput.password)
                    }
                    is RegisterState.Error -> {
                        handleError(state.error)
                        viewModel.resetValidationState()
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
    //registeonactivitycallback
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


    private fun showValidInput(username: String, email: String, phone: String, password: String) {
        binding.registerProgressBar.visibility = View.GONE
        showSuccessMessage("Proceeding to phone verification.")
        val action = RegisterFragmentDirections.actionRegisterFragmentToPhoneVerification(
            username,
            email,
            phone,
            password
        )

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(action)
        }, 3500)
    }

    private fun showLoading() {
        binding.registerProgressBar.visibility = View.VISIBLE
    }


    private fun showCredentialSignInSuccess() {
        binding.registerProgressBar.visibility = View.GONE
        showSuccessMessage("Successfully logged in.")
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_registerFragment_to_Home)
        }, 3500)
    }
    private fun handleError(error: Throwable) {
        binding.registerProgressBar.visibility = View.GONE
        ToastHelper.showError(requireContext(), error.message ?: "An error occurred", layoutInflater)
    }

    private fun showSuccessMessage(message: String) {
        ToastHelper.showSuccess(requireContext(), message, layoutInflater)
    }
    private fun getUserInput(): UserInput {
        val email = binding.registrationEmailEditText.text.toString().trim()
        val password = binding.registrationPasswordEditText.editText?.text.toString().trim()
        val username = binding.registrationUsernameEditText.text.toString().trim()
        val phone = binding.registrationPhoneNumberEditText.text.toString().trim()

        return UserInput(username, email, phone, password)
    }


    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    }
}
