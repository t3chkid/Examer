package com.example.examer.viewmodels

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.examer.auth.AuthenticationResult
import com.example.examer.auth.AuthenticationService
import com.example.examer.data.domain.ExamerUser
import com.example.examer.di.ExamerApplication
import com.example.examer.usecases.CredentialsValidationUseCase
import com.example.examer.usecases.ExamerCredentialsValidationUseCase
import com.example.examer.utils.PasswordManager
import com.example.examer.viewmodels.ProfileScreenViewModel.UpdateAttribute.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.security.KeyStore


interface ProfileScreenViewModel {
    enum class UpdateAttribute { NAME, EMAIL, PASSWORD }
    enum class UiState { UPDATE_SUCCESS, UPDATE_FAILURE, LOADING, IDLE }

    val uiState: State<UiState>
    fun updateAttributeForCurrentUser(updateAttribute: UpdateAttribute, newValue: String)
    fun updateProfilePicture(imageBitmap: ImageBitmap)
    fun isValidEmail(email: String): Boolean
    fun isValidPassword(password: String): Boolean
}

class ExamerProfileScreenViewModel(
    application: Application,
    private val authenticationService: AuthenticationService,
    private val passwordManager: PasswordManager,
    private val credentialsValidationUseCase: CredentialsValidationUseCase,
) : AndroidViewModel(application), ProfileScreenViewModel,
    CredentialsValidationUseCase by credentialsValidationUseCase {

    private val _uiState = mutableStateOf(ProfileScreenViewModel.UiState.IDLE)
    override val uiState: State<ProfileScreenViewModel.UiState> = _uiState

    override fun updateAttributeForCurrentUser(
        updateAttribute: ProfileScreenViewModel.UpdateAttribute,
        newValue: String
    ) {
        viewModelScope.launch {
            val currentUser = authenticationService.currentUser.value!!
            // set the ui state to loading
            _uiState.value = ProfileScreenViewModel.UiState.LOADING
            try {
                // update the attribute using authentication service
                val result = authenticationService.updateAttributeForUser(
                    currentUser,
                    updateAttributeType = when (updateAttribute) {
                        NAME -> AuthenticationService.UpdateAttributeType.NAME
                        EMAIL -> AuthenticationService.UpdateAttributeType.EMAIL
                        PASSWORD -> AuthenticationService.UpdateAttributeType.PASSWORD
                    },
                    newValue = newValue,
                    password = passwordManager.getPasswordForUser(currentUser) // can throw exception
                )
                // set the ui state based on the result
                _uiState.value = when (result) {
                    is AuthenticationResult.Failure -> ProfileScreenViewModel.UiState.UPDATE_FAILURE
                    is AuthenticationResult.Success -> ProfileScreenViewModel.UiState.UPDATE_SUCCESS
                }
            } catch (exception: IllegalArgumentException) {
                // indicates that the PasswordManager#getPasswordForUser()
                // threw an exception because the password of the current
                // user was not saved using the password manager.
                _uiState.value = ProfileScreenViewModel.UiState.UPDATE_FAILURE
            }
        }
    }

    override fun updateProfilePicture(imageBitmap: ImageBitmap) {
        TODO("Not yet implemented")
    }
}