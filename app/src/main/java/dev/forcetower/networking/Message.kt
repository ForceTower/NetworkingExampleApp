package dev.forcetower.networking

data class Message(
    val id: String,
    val content: String,
    val sender: String
)