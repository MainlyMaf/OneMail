package com.onemail.app.data

import com.google.gson.annotations.SerializedName

data class DomainItem(
    val id: String? = null,
    val domain: String? = null,
    @SerializedName("isActive") val isActive: Boolean? = null,
    @SerializedName("isPrivate") val isPrivate: Boolean? = null
)

data class DomainsResponse(
    @SerializedName("hydra:member") val domains: List<DomainItem> = emptyList()
)

data class CreateAccountRequest(
    val address: String,
    val password: String
)

data class AccountResponse(
    val id: String? = null,
    val address: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class TokenRequest(
    val address: String,
    val password: String
)

data class TokenResponse(
    val token: String? = null,
    val id: String? = null
)

data class Sender(
    val address: String? = null,
    val name: String? = null
)

data class MessageSummary(
    val id: String = "",
    val subject: String? = null,
    val intro: String? = null,
    val from: Sender? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    val seen: Boolean = false
)

data class MessagesResponse(
    @SerializedName("hydra:member") val messages: List<MessageSummary> = emptyList(),
    @SerializedName("hydra:totalItems") val totalItems: Int? = null
)

data class MessageDetail(
    val id: String = "",
    val subject: String? = null,
    val intro: String? = null,
    val from: Sender? = null,
    val text: String? = null,
    val html: List<String>? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class AccountData(
    val id: String,
    val address: String,
    val password: String,
    val token: String
)
