package com.bangkit.mystory.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.mystory.data.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.registerUser(name, email, password)
                if (!response.error) {
                    _isSuccess.postValue(true)
                } else {
                    _errorMessage.postValue(response.message)
                }
            } catch (e: HttpException) {
                _errorMessage.postValue(e.message())
            } catch (e: Exception) {
                _errorMessage.postValue("Unexpected error occurred")
            }
        }
    }
}