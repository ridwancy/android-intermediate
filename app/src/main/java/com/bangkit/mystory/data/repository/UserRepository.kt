package com.bangkit.mystory.data.repository

import ApiService
import androidx.lifecycle.asLiveData
import com.bangkit.mystory.data.local.UserEntity
import com.bangkit.mystory.data.local.UserPreferences
import com.bangkit.mystory.data.remote.response.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences) {

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreferences: UserPreferences
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreferences)
            }.also { instance = it }
    }

    suspend fun registerUser(name: String, email: String, password: String) =
        apiService.register(name, email, password)

    suspend fun loginUser(email: String, password: String) =
        apiService.login(email, password)

    suspend fun getStories(token: String) = apiService.getStories("Bearer $token")

    suspend fun addNewStory(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        latitude: Double? = null,
        longitude: Double? = null
    ): UploadResponse {
        return apiService.addNewStory("Bearer $token", image, description, latitude, longitude)
    }

    fun getUserData() = userPreferences.getLogin().asLiveData()

    suspend fun saveUserData(user: UserEntity) = userPreferences.setLogin(user)

    suspend fun logout() = userPreferences.deleteLogin()
}
