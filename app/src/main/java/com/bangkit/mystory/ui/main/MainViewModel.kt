package com.bangkit.mystory.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.mystory.data.local.UserEntity
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
}