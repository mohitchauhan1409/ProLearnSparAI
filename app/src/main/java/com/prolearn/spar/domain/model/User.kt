package com.prolearn.spar.domain.model

data class User(
    val id: String,
    val name: String,
    val firstName: String,
    val email: String,
    val examTarget: String,
    val avatarInitials: String
)
