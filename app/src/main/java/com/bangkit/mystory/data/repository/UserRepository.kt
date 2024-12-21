package com.bangkit.mystory.data.repository

import ApiService
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.bangkit.mystory.data.local.UserEntity
import com.bangkit.mystory.data.local.UserPreferences
import com.bangkit.mystory.data.paging.StoryPagingSource
import com.bangkit.mystory.data.remote.response.ListStoryItem
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

    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(token, apiService) }
        ).liveData
    }

    suspend fun addNewStory(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        lat: Double? = null,
        lon: Double? = null
    ): UploadResponse {
        return apiService.addNewStory("Bearer $token", image, description, lat, lon)
    }

    suspend fun getStoriesWithLocation(token: String) = apiService.getStoriesWithLocation(token)

    fun getUserData() = userPreferences.getLogin().asLiveData()

    suspend fun saveUserData(user: UserEntity) = userPreferences.setLogin(user)

    suspend fun logout() = userPreferences.deleteLogin()
}
