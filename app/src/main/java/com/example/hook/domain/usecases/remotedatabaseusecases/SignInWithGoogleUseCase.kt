package com.example.hook.domain.usecases.remotedatabaseusecases

import android.content.Context
import android.content.Intent
import com.example.hook.R
import com.example.hook.common.exception.GoogleSignInFailedException
import com.example.hook.common.ext.asFlow
import com.example.hook.common.ext.mapSuccess
import com.example.hook.domain.repository.AuthRepository
import javax.inject.Inject
import com.example.hook.common.result.Result
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map


class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository, @ApplicationContext private val context: Context
) {
    fun setupGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Flow<Result<Boolean>> {
        return getGoogleSignInAccount(data).flatMapLatest { accountResult ->
            when (accountResult) {
                is Result.Success -> {
                    val credential = GoogleAuthProvider.getCredential(accountResult.data.idToken, null)
                    authRepository.signInWithCredential(credential)
                }
                is Result.Error -> flowOf(Result.Error(accountResult.error))
            }
        }
    }

    private fun getGoogleSignInAccount(data: Intent?): Flow<Result<GoogleSignInAccount>> =
        GoogleSignIn.getSignedInAccountFromIntent(data).asFlow().mapSuccess { it }
}



