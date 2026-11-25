package com.onemail.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onemail.app.ui.screens.EmptyStateScreen
import com.onemail.app.ui.screens.InboxScreen
import com.onemail.app.ui.screens.MessageSheet
import com.onemail.app.ui.theme.OneMailTheme

@Composable
fun OneMailApp(viewModel: MailViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    var selectedMessageId by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    OneMailTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val account = state.account
                if (account == null) {
                    EmptyStateScreen(
                        creating = state.creating,
                        onCreate = viewModel::createMailbox
                    )
                } else {
                    InboxScreen(
                        account = account,
                        messages = state.messages,
                        refreshing = state.refreshing,
                        initialLoading = state.loadingMessages && state.messages.isEmpty(),
                        onRefresh = {
                            viewModel.refreshMessages(silent = true, userInitiated = true)
                        },
                        onCopy = {
                            clipboard.setText(AnnotatedString(account.address))
                            viewModel.notify("Адрес скопирован")
                        },
                        onDelete = { showDeleteDialog = true },
                        onOpenMessage = { selectedMessageId = it }
                    )
                }
            }
        }

        val account = state.account
        if (showDeleteDialog && account != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text(text = "Удалить почту?") },
                text = {
                    Text(
                        text = "Адрес ${account.address} и все письма будут удалены без возможности восстановления."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteMailbox()
                        }
                    ) {
                        Text(
                            text = "Удалить",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(
                            text = "Отмена",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        val messageId = selectedMessageId
        if (messageId != null) {
            MessageSheet(
                viewModel = viewModel,
                messageId = messageId,
                onDismiss = { selectedMessageId = null }
            )
        }
    }
}
