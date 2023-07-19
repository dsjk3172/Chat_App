package com.example.chat_app

import java.io.Serializable

data class RegisterModel(
    var id: String,
    var pw: String
)

data class RegisterResult(
    var message: Boolean
)

data class LoginModel(
    var id: String,
    var pw: String
)

data class LoginResult(
    var UID: Int
)

data class User(
    val UID: Int,
    val userid: String,
    val password: String,
    val username: String,
): Serializable

data class Room(
    val sender_id : Int,
    val receiver_id : Int
)

data class RoomNumber(
    val number: Int
)


data class Message(
    val sender_id: Int,
    val receiver_id: Int,
    val text: String,
    var time: String
)

data class UserId(
    val id: String
)