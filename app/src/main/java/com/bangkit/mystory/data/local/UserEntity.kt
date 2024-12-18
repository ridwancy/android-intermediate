package com.bangkit.mystory.data.local

data class UserEntity(
    val userId: String,
    val name: String,
    val email: String,
    val token: String,
    val isLogin: Boolean
)
