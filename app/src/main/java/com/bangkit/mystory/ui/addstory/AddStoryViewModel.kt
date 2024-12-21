package com.bangkit.mystory.ui.addstory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bangkit.mystory.data.remote.response.UploadResponse
import com.bangkit.mystory.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun uploadStory(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        lat: Double?,
        lon: Double?
    ): LiveData<Result<UploadResponse>> = liveData(Dispatchers.IO) {
        try {
            Log.d("AddStoryViewModel", "Uploading story with lat=$lat, lon=$lon")
            val response = userRepository.addNewStory(token, image, description, lat, lon)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}