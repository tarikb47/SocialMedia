package com.example.hook.presentation.authentication.register

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import com.example.hook.databinding.FragmentRegisterBinding
import com.example.hook.domain.model.User
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.ml.ModelManager
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        setObservers()
        setListeners()
        observeFacebookLoginState()
        setupGoogleSignInClient() // This initializes googleSignInClient
        observeGoogleSignInState()
        // setupPhoneNumberEditText()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupPhoneNumberEditText() {
        binding.registrationPhoneNumberEditText.apply {
            val phonePrefix = "+3876"
            setText(phonePrefix)
            setSelection(phonePrefix.length)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.toString().startsWith(phonePrefix)) {
                        setText(phonePrefix)
                        setSelection(phonePrefix.length)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun setListeners() {
        binding.signUpButton.setOnClickListener {
            val email = binding.registrationEmailEditText.text.toString().trim()
            val password = binding.registrationPasswordEditText.editText?.text.toString().trim()
            val username = binding.registrationUsernameEditText.text.toString().trim()
            val phone = binding.registrationPhoneNumberEditText.text.toString().trim()
            viewModel.validateUserInput(username, email, phone, password)
        }
        binding.fbButton.setOnClickListener {
            setupFacebookLogin()
        }
        binding.googleButton.setOnClickListener {
            initiateGoogleSignIn()
        }
        binding.goToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.validationState.collect { result ->
                when (result) {
                    is ValidationResult.Success -> {
                        val email = binding.registrationEmailEditText.text.toString().trim()
                        val password =
                            binding.registrationPasswordEditText.editText?.text.toString().trim()
                        val username = binding.registrationUsernameEditText.text.toString().trim()
                        val phone = binding.registrationPhoneNumberEditText.text.toString().trim()
                        val user = User(username, email, phone, password)
                        viewModel.saveUserLocally(user)
                        showSuccessAndNavigate(
                            "Proceeding to phone authentication",
                            R.id.action_registerFragment_to_phoneVerification
                        )

                    }

                    is ValidationResult.Idle -> {
                    }

                    is ValidationResult.BlankFields -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.PhoneNumberRegistered -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.InvalidPassword -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.UsernameTaken -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.InvalidPhoneNumber -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.WeakPassword -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                    is ValidationResult.EmailTaken -> {
                        showError(result.message)
                        viewModel.resetValidationState()
                    }

                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            if (::callbackManager.isInitialized) {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun showSuccess(message: String) {
        showToast(message)
    }

    private fun showError(message: String) {
        showToast(message)
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

    private lateinit var callbackManager: CallbackManager

    private fun setupFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken
                    viewModel.loginWithFacebook(token)
                }

                override fun onCancel() {
                    showError("Facebook login cancelled")
                }

                override fun onError(error: FacebookException) {
                    showError("Facebook login error: ${error.message}")
                }
            })
    }


    private fun observeFacebookLoginState() {
        lifecycleScope.launchWhenStarted {
            viewModel.facebookLoginState.collect { state ->
                when (state) {
                    is FacebookLoginResult.Loading -> {
                        Log.d("Tarik", "observeFacebookLoginState: loading")
                    }


                    is FacebookLoginResult.Success -> {
                        Log.d("Tarik", "observeFacebookLoginState: success")
                        showSuccess("Facebook login successful")
                        Log.d("Tarik", "observeFacebookLoginState: $state")
                        findNavController().navigate(R.id.action_registerFragment_to_Home)
                    }

                    is FacebookLoginResult.Error -> {
                        showError(state.message)
                        Log.d("Tarik", "observeFacebookLoginState: ${state.message}")
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun showSuccessAndNavigate(message: String, destinationId: Int) {
        showSuccess(message)
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(destinationId)
        }, 3500)
    }

    private fun observeGoogleSignInState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.googleSignInState.collect { state ->
                when (state) {
                    is GoogleSignInResult.Loading -> {
                        // Show loading spinner if necessary
                    }

                    is GoogleSignInResult.Success -> {
                        showSuccess("Google login successful")
                        findNavController().navigate(R.id.action_registerFragment_to_Home)
                    }

                    is GoogleSignInResult.Error -> {
                        showError(state.message)
                    }

                    GoogleSignInResult.Idle -> Unit // No-op
                    else -> {}
                }
            }
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                showError("Failed to retrieve Google ID token")
            }
        } catch (e: ApiException) {
            showError("Google Sign-In failed: ${e.message}")
        }
    }

    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()

            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun initiateGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

}
