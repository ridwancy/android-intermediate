package com.bangkit.mystory.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.mystory.data.local.UserEntity
import com.bangkit.mystory.data.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoginSuccessful = MutableLiveData<Boolean>()
    val isLoginSuccessful: LiveData<Boolean> = _isLoginSuccessful

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.loginUser(email, password)
                if (!response.error) {
                    val user = UserEntity(
                        userId = response.loginResult.userId,
                        name = response.loginResult.name,
                        email = email,
                        token = response.loginResult.token,
                        isLogin = true
                    )
                    userRepository.saveUserData(user)
                    _isLoginSuccessful.postValue(true)
                } else {
                    _errorMessage.postValue(response.message)
                }
            } catch (e: HttpException) {
                _errorMessage.postValue("Login failed")
            } catch (e: Exception) {
                _errorMessage.postValue("Unexpected error: ${e.message}")
            }
        }
    }
}