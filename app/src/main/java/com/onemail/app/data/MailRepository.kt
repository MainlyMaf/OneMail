package com.onemail.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.random.Random

class MailRepository(private val api: MailTmApi = MailClient.api) {

    suspend fun createAccount(): AccountData = withContext(Dispatchers.IO) {
        val allDomains = MailParser.domains(api.getDomains())
            .filter { !it.domain.isNullOrBlank() }
        val domain = allDomains.firstOrNull { it.isActive == true }?.domain
            ?: allDomains.firstOrNull()?.domain
            ?: throw IOException("Сервис не вернул доступных доменов")

        var lastError: Throwable? = null

        repeat(3) {
            val login = randomLogin()
            val password = randomPassword()
            val address = "$login@$domain"
            try {
                val created = MailParser.account(
                    api.createAccount(
                        CreateAccountRequest(address = address, password = password)
                    )
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
            } catch (e: IOException) {
                // Повторяем только при конфликте адреса, остальное — сразу наверх.
                if (e.message?.contains("exists", ignoreCase = true) == true) {
                    lastError = e
                } else {
                    throw e
                }
            }
        }

        throw lastError ?: IOException("Не удалось создать почтовый ящик")
    }

    suspend fun obtainToken(address: String, password: String): String = withContext(Dispatchers.IO) {
        MailParser.token(
            api.getToken(TokenRequest(address = address, password = password))
        )
    }

    suspend fun fetchMessages(token: String): List<MessageSummary> = withContext(Dispatchers.IO) {
        MailParser.messages(api.getMessages(bearer(token)))
    }

    suspend fun fetchMessage(token: String, id: String): MessageDetail = withContext(Dispatchers.IO) {
        MailParser.message(api.getMessage(bearer(token), id))
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