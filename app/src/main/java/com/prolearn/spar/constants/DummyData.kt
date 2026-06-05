package com.prolearn.spar.constants

import com.prolearn.spar.domain.model.User

object DummyAuth {
    val CURRENT_USER = User(
        id = "dummy_001",
        name = "Arjun Sharma",
        firstName = "Arjun",
        email = "arjun@prolearn.ai",
        examTarget = "JEE Advanced",
        avatarInitials = "AS"
    )

    fun validateLogin(email: String, password: String): Boolean =
        email.isNotBlank() && password.length >= 6

    fun createUser(name: String, email: String, exam: String) = User(
        id = "dummy_${System.currentTimeMillis()}",
        name = name,
        firstName = name.split(" ").first(),
        email = email,
        examTarget = exam,
        avatarInitials = name.split(" ").take(2).map { it.first() }.joinToString("")
    )
}
