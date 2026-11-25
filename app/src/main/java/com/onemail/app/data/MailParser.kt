package com.onemail.app.data

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.IOException

/**
 * Терпимый разбор ответов Mail.tm: сервис может вернуть как hydra-коллекцию
 * (`{"hydra:member": [...]}`), так и «голый» JSON-массив.
 */
object MailParser {

    private val gson = Gson()

    fun domains(payload: JsonElement): List<DomainItem> {
        val array = collection(payload, listOf("hydra:member", "member", "domains", "items"))
        return array.mapNotNull { element ->
            when {
                element.isJsonPrimitive -> DomainItem(domain = element.asString, isActive = true)
                element.isJsonObject -> gson.fromJson(element, DomainItem::class.java)
                    ?.takeIf { !it.domain.isNullOrBlank() }
                else -> null
            }
        }
    }

    fun messages(payload: JsonElement): List<MessageSummary> {
        val array = collection(payload, listOf("hydra:member", "member", "messages", "items"))
        return array.mapNotNull { element ->
            if (element.isJsonObject) {
                gson.fromJson(element, MessageSummary::class.java)
                    ?.takeIf { it.id.isNotBlank() }
            } else {
                null
            }
        }
    }

    fun message(payload: JsonElement): MessageDetail {
        val obj = payload.asObject()
        val sender = obj.get("from")
            ?.takeIf { it.isJsonObject }
            ?.let { gson.fromJson(it, Sender::class.java) }
        val html = obj.get("html")?.let { element ->
            when {
                element.isJsonArray -> element.asJsonArray.mapNotNull { part ->
                    if (part.isJsonPrimitive) part.asString else null
                }
                element.isJsonPrimitive -> listOf(element.asString)
                else -> null
            }
        }
        return MessageDetail(
            id = obj.string("id").orEmpty(),
            subject = obj.string("subject"),
            intro = obj.string("intro"),
            from = sender,
            text = obj.string("text"),
            html = html,
            createdAt = obj.string("createdAt")
        )
    }

    fun account(payload: JsonElement): AccountResponse {
        val obj = payload.asObject()
        return AccountResponse(
            id = obj.string("id"),
            address = obj.string("address"),
            createdAt = obj.string("createdAt")
        )
    }

    fun token(payload: JsonElement): String {
        val obj = payload.asObject()
        return obj.string("token")?.takeIf { it.isNotBlank() }
            ?: throw IOException("Сервер не вернул токен авторизации")
    }

    private fun collection(payload: JsonElement, keys: List<String>): JsonArray {
        if (payload.isJsonArray) return payload.asJsonArray
        if (payload.isJsonObject) {
            val obj = payload.asJsonObject
            for (key in keys) {
                val value = obj.get(key)
                if (value != null && value.isJsonArray) return value.asJsonArray
            }
            val serverMessage = obj.string("message")
                ?: obj.string("detail")
                ?: obj.string("hydra:description")
            if (!serverMessage.isNullOrBlank()) throw IOException(serverMessage)
        }
        throw IOException("Сервис вернул неожиданный ответ. Попробуйте позже.")
    }

    private fun JsonElement.asObject(): JsonObject {
        if (isJsonObject) return asJsonObject
        if (isJsonArray) {
            for (element in asJsonArray) {
                if (element.isJsonObject) return element.asJsonObject
            }
        }
        throw IOException("Сервис вернул неожиданный ответ. Попробуйте позже.")
    }

    private fun JsonObject.string(key: String): String? {
        val value = get(key) ?: return null
        return if (value.isJsonPrimitive) value.asString else null
    }
}