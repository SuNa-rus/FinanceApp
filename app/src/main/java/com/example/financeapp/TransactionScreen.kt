package com.example.financeapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
@Suppress("FunctionNaming")
internal fun TransactionScreen(viewModel: TransactionViewModel, showBalance: Boolean, onOpenSettings: () -> Unit) {
    val state = viewModel.uiState

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(stringResource(R.string.transaction_title))

            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.settings_title))
            }

            if (showBalance) {
                Text(
                    stringResource(
                        R.string.balance_summary,
                        state.balance,
                        state.transactionCount,
                    ),
                )
            } else {
                Text(
                    stringResource(
                        R.string.transaction_count,
                        state.transactionCount,
                    ),
                )
            }

            if (state.isLoading) {
                Text(stringResource(R.string.transaction_loading))
            }

            TransactionForm(viewModel, state)
            TransactionHistory(state.transactions)
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionForm(viewModel: TransactionViewModel, state: MainUiState) {
    TransactionFields(state, viewModel)
    TransactionTypeSelector(state, viewModel)
    TransactionErrorMessage(state.error)

    Button(
        onClick = viewModel::addTransaction,
        enabled = state.isDatabaseReady && !state.isSaving,
    ) {
        Text(stringResource(R.string.transaction_add))
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionFields(state: MainUiState, viewModel: TransactionViewModel) {
    OutlinedTextField(
        value = state.description,
        enabled = state.isDatabaseReady && !state.isSaving,
        onValueChange = {
            viewModel.onEvent(MainUiEvent.DescriptionChanged(it))
        },
        label = {
            Text(stringResource(R.string.transaction_description))
        },
    )

    OutlinedTextField(
        value = state.amount,
        enabled = state.isDatabaseReady && !state.isSaving,
        onValueChange = {
            viewModel.onEvent(MainUiEvent.AmountChanged(it))
        },
        label = {
            Text(stringResource(R.string.transaction_amount))
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
    )
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionTypeSelector(state: MainUiState, viewModel: TransactionViewModel) {
    Row {
        FilterChip(
            selected = state.type == TransactionType.EXPENSE,
            enabled = state.isDatabaseReady && !state.isSaving,
            onClick = {
                viewModel.onEvent(
                    MainUiEvent.TypeChanged(TransactionType.EXPENSE),
                )
            },
            label = {
                Text(stringResource(R.string.transaction_expense))
            },
        )

        FilterChip(
            selected = state.type == TransactionType.INCOME,
            enabled = state.isDatabaseReady && !state.isSaving,
            onClick = {
                viewModel.onEvent(
                    MainUiEvent.TypeChanged(TransactionType.INCOME),
                )
            },
            label = {
                Text(stringResource(R.string.transaction_income))
            },
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionErrorMessage(error: TransactionError?) {
    if (error != null) {
        val textId = when (error) {
            TransactionError.INVALID_INPUT ->
                R.string.transaction_invalid_input

            TransactionError.AMOUNT_TOO_LARGE ->
                R.string.transaction_amount_too_large

            TransactionError.DATABASE_ERROR ->
                R.string.transaction_database_error
        }

        Text(
            text = stringResource(textId),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionHistory(transactions: List<Transaction>) {
    if (transactions.isNotEmpty()) {
        Text(stringResource(R.string.transaction_history))

        transactions.asReversed().forEach { tx ->
            val row = if (tx.type == TransactionType.INCOME) {
                R.string.transaction_history_income
            } else {
                R.string.transaction_history_expense
            }

            Text(
                stringResource(
                    row,
                    tx.description,
                    tx.amount,
                ),
            )
        }
    }
}
