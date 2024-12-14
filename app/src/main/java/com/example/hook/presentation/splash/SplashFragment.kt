package com.example.hook.presentation.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    companion object {
        fun newInstance() = SplashFragment()
    }

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateImageIn()

        lifecycleScope.launch {
            viewModel.splashState.collectLatest { initialized ->
                if (initialized) {
                    animateImageOut {
                        navigateToOnboarding()
                    }
                }
            }
        }
        viewModel.initializeApp()

    }

    private fun navigateToOnboarding() {
        findNavController().navigate(R.id.action_splash_to_onboarding)
    }

    private fun animateImageIn() {
        val slideIn = ObjectAnimator.ofFloat(binding.logoImageView, "translationY", 1000f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 0f, 1f)
        AnimatorSet().apply {
            playTogether(slideIn, fadeIn)
            duration = 700
            start()
        }
    }

    private fun animateImageOut(onEnd: () -> Unit) {
        val slideOut = ObjectAnimator.ofFloat(binding.logoImageView, "translationY", 0f, -1000f)
        val fadeOut = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 1f, 0f)
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            duration = 700
            start()
            addListener(onEnd = { onEnd() })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}