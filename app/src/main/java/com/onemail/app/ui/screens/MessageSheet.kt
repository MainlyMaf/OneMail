package com.onemail.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.onemail.app.data.MessageDetail
import com.onemail.app.ui.MailViewModel
import com.onemail.app.util.DateFormat
import com.onemail.app.util.userMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSheet(
    viewModel: MailViewModel,
    messageId: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var detail by remember(messageId) { mutableStateOf<MessageDetail?>(null) }
    var loading by remember(messageId) { mutableStateOf(true) }
    var errorText by remember(messageId) { mutableStateOf<String?>(null) }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.82f).dp

    LaunchedEffect(messageId) {
        loading = true
        errorText = null
        try {
            detail = viewModel.loadMessage(messageId)
        } catch (e: Exception) {
            val text = e.userMessage()
            errorText = text
            viewModel.notify(text)
        } finally {
            loading = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            when {
                loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                errorText != null -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorText ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> detail?.let { message -> MessageBody(message) }
            }
        }
    }
}

@Composable
private fun MessageBody(message: MessageDetail) {
    val senderName = message.from?.name?.takeIf { it.isNotBlank() }
        ?: message.from?.address?.takeIf { it.isNotBlank() }
        ?: "Неизвестный отправитель"
    val senderAddress = message.from?.address?.takeIf { it.isNotBlank() } ?: ""
    val subject = message.subject?.takeIf { it.isNotBlank() } ?: "(без темы)"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = subject,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = senderName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (senderAddress.isNotEmpty()) {
                    Text(
                        text = senderAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = DateFormat.fullTime(message.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(18.dp))

        SelectionContainer {
            Text(
                text = message.displayBody(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun MessageDetail.displayBody(): String {
    text?.takeIf { it.isNotBlank() }?.let { return it }
    val htmlParts = html?.filter { it.isNotBlank() }
    if (!htmlParts.isNullOrEmpty()) {
        val raw = htmlParts.joinToString(separator = "\n")
        val plain = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        if (plain.isNotBlank()) return plain.trim()
    }
    return intro?.takeIf { it.isNotBlank() } ?: "Это письмо не содержит текста."
}
