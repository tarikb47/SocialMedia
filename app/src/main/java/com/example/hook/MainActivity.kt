package com.example.hook

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.hook.data.local.dao.ContactDao
import com.example.hook.data.remote.home.contacts.ContactsRepository
import com.example.hook.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    @Inject
    lateinit var contactsRepository: ContactsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
      /*  CoroutineScope(Dispatchers.Main).launch {
            contactsRepository.populateMockContactsForAllUsers()
        }*/


        /*     ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                 val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                 v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                 insets
             }*/
        val navHostFragment = supportFragmentManager.findFragmentById(binding.onboardingFragment.id) as NavHostFragment
        navController = navHostFragment.navController

    }
}