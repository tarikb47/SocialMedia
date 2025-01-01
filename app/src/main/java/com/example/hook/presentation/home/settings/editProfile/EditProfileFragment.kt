package com.example.hook.presentation.home.settings.editProfile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.hook.R
import com.example.hook.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    companion object {
        fun newInstance() = EditProfileFragment()
    }
    private var _binding : FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.logOut.setOnClickListener{
            val navController = (requireActivity().supportFragmentManager.findFragmentById(R.id.onboarding_fragment) as NavHostFragment).navController
            viewModel.clearData()
            navController.navigate(R.id.action_global_to_loginFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}