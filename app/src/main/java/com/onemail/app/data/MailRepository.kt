package com.onemail.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.random.Random

class MailRepository(private val api: MailTmApi = MailClient.api) {

    suspend fun createAccount(): AccountData = withContext(Dispatchers.IO) {
        val domains = api.getDomains().domains.filter {
            it.isActive == true && !it.domain.isNullOrBlank()
        }
        val domain = domains.firstOrNull()?.domain
            ?: throw IOException("Сервис не вернул доступных доменов")

        var lastError: Throwable? = null

        repeat(3) {
            val login = randomLogin()
            val password = randomPassword()
            val address = "$login@$domain"
            try {
                val created = api.createAccount(
                    CreateAccountRequest(address = address, password = password)
                )
                val accountId = created.id
                    ?: throw IOException("Сервер не вернул идентификатор ящика")
                val token = obtainToken(address, password)
                return@withContext AccountData(
                    id = accountId,
                    address = address,
                    password = password,
                    token = token
                )
            } catch (e: HttpException) {
                lastError = e
                if (e.code() != 422 && e.code() != 429) throw e
            }
        }

        throw lastError ?: IOException("Не удалось создать почтовый ящик")
    }

    suspend fun obtainToken(address: String, password: String): String = withContext(Dispatchers.IO) {
        api.getToken(TokenRequest(address = address, password = password)).token
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("Сервер не вернул токен авторизации")
    }

    suspend fun fetchMessages(token: String): List<MessageSummary> = withContext(Dispatchers.IO) {
        api.getMessages(bearer(token)).messages
    }

    suspend fun fetchMessage(token: String, id: String): MessageDetail = withContext(Dispatchers.IO) {
        api.getMessage(bearer(token), id)
    }

    suspend fun deleteAccount(token: String, id: String) {
        withContext(Dispatchers.IO) {
            api.deleteAccount(bearer(token), id)
        }
    }

    private fun bearer(token: String): String = "Bearer $token"

    private fun randomLogin(): String {
        val letters = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val builder = StringBuilder()
        builder.append(letters[Random.nextInt(letters.length)])
        repeat(11) {
            val alphabet = if (Random.nextBoolean()) letters else digits
            builder.append(alphabet[Random.nextInt(alphabet.length)])
        }
        return builder.toString()
    }

    private fun randomPassword(): String {
        val alphabet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789!@#%"
        val builder = StringBuilder()
        repeat(16) {
            builder.append(alphabet[Random.nextInt(alphabet.length)])
        }
        return builder.toString()
    }
}
