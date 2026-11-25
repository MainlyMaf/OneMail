package com.onemail.app.util

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.userMessage(): String = when (this) {
    is UnknownHostException -> "Нет соединения с интернетом. Проверьте сеть."
    is SocketTimeoutException -> "Сервер не отвечает. Попробуйте ещё раз."
    is ConnectException -> "Не удалось подключиться к серверу. Проверьте интернет."
    is HttpException -> when (code()) {
        401, 403 -> "Сессия истекла. Создайте новую почту."
        404 -> "Ящик больше не существует на сервере."
        422 -> "Не удалось создать адрес. Попробуйте ещё раз."
        429 -> "Слишком много запросов. Подождите минуту."
        else -> "Ошибка сервера (${code()}). Попробуйте позже."
    }
    is JsonParseException -> "Сервис вернул неожиданный ответ. Попробуйте позже."
    is IllegalStateException -> "Сервис вернул неожиданный ответ. Попробуйте позже."
    is IOException -> message?.takeIf { it.isNotBlank() } ?: "Ошибка сети. Проверьте подключение."
    else -> message?.takeIf { it.isNotBlank() } ?: "Неизвестная ошибка."
}