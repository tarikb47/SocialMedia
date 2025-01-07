package com.example.hook.presentation.home.settings.editProfile

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.hook.R
import com.example.hook.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()

    }

    private fun setListeners() {
        binding.logOut.setOnClickListener {
            viewModel.clearData()
            val navController =
                (requireActivity().supportFragmentManager.findFragmentById(R.id.onboarding_fragment) as NavHostFragment).navController
            navController.navigate(R.id.action_global_to_loginFragment)
        }
       /* binding.addNewPhoto.setOnClickListener {
            openGallery()
        }*/

    }

    private fun setObservers() {
        lifecycleScope.launch {
            viewModel.profileState.collectLatest { result ->
                when (result) {
                    is ProfileState.Error -> {

                    }

                    ProfileState.Idle -> {}
                    ProfileState.Loading -> {}
                    ProfileState.PhotoChanged -> {}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val PICK_IMAGE_REQUEST = 100

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let { viewModel.uploadPhotoToFirebase(it) }
        }
    }
}