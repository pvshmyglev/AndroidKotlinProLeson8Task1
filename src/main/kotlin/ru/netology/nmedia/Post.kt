package ru.netology.nmedia

data class Post(

    val id: Long,
    val author: String = "",
    val authorAvatar: String = "",
    val authorId: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,

)