package ru.netology.nmedia

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

private const val BASE_URL = "http://localhost:9999/api/"

val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()
val gson = Gson()

val coroutineScope = CoroutineScope(EmptyCoroutineContext)

suspend fun <T> makeRequest(addressResource: String, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        Request.Builder()
            .url("$BASE_URL$addressResource")
            .build()
            .let { client.newCall(it) }
            .execute()
            .let { response ->
                val body = response.body?.string() ?: run {
                    error("No body")
                }
                gson.fromJson<T>(body, typeToken.type)
            }
    }

suspend fun getPosts() =
    makeRequest("posts", object : TypeToken<List<Post>>() {})

suspend fun getAuthor(id : Long) =
    makeRequest("authors/$id", object : TypeToken<Author>() {})

fun main() {
    coroutineScope.launch {
        val posts = getPosts().map{ post ->
           async {
            val authorById = getAuthor(post.authorId)
            post.copy(author = authorById.name, authorAvatar = authorById.avatar)
           }
        }.awaitAll()

        posts.forEach{
            println(it)
        }
    }

    Thread.sleep(15_000)
}