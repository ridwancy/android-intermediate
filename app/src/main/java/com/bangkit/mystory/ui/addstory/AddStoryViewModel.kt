package com.bangkit.mystory.ui.addstory

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
        description: RequestBody
    ): LiveData<Result<UploadResponse>> = liveData(Dispatchers.IO) {
        try {
            val response = userRepository.addNewStory(token, image, description)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}