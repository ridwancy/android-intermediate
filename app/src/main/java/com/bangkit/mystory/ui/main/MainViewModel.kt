package com.bangkit.mystory.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bangkit.mystory.data.local.UserEntity
import com.bangkit.mystory.data.remote.response.ListStoryItem
import com.bangkit.mystory.data.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // Mendapatkan data pengguna yang sedang login
    fun getLogin(): LiveData<UserEntity> {
        return userRepository.getUserData()
    }

    // Menghapus data login saat logout
    fun deleteLogin() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun getStories(token: String): LiveData<Result<List<ListStoryItem>>> {
        return liveData {
            try {
                val response = userRepository.getStories(token)
                Log.d("MainViewModel", "Fetched Stories: ${response.listStory.size}")
                emit(Result.success(response.listStory))
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching stories: ${e.message}")
                emit(Result.failure(e))
            }
        }
    }

}