package com.bangkit.mystory.ui.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.mystory.data.local.UserEntity
import com.bangkit.mystory.data.remote.response.StoryResponse
import com.bangkit.mystory.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MapsViewModel (private val userRepository: UserRepository) : ViewModel() {
    private val _storyResponse = MutableLiveData<StoryResponse>()
    val storyResponse: LiveData<StoryResponse> = _storyResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getStoriesWithLocation(token: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = userRepository.getStoriesWithLocation(token)
                Log.d(TAG, "onSuccess: ${response.message}")
                _isLoading.postValue(false)
                _storyResponse.postValue(response)
            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, StoryResponse::class.java)
                val errorMessage = errorBody.message
                _isLoading.postValue(false)
                Log.d(TAG, "onError: $errorMessage")
            }
        }
    }


    companion object {
        private const val TAG = "MainViewModel"
    }
}