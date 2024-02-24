package com.cureius.pocket.feature_transaction.domain.repository

import com.cureius.pocket.feature_transaction.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>

    fun getTransactionsCreatedOnCurrentMonth(): Flow<List<Transaction>>

    fun getTransactionsOfAccountNumber(accountNumbers: Array<String>): Flow<List<Transaction>>

    fun getLatestTransactionWithBalanceForAccountNumber(accountNumber: String): Flow<Transaction>?

    fun getTransactionsForDateRange(start: Long, end: Long): Flow<List<Transaction>>

    suspend fun getTransactionById(id: Long): Transaction?

    suspend fun insertTransaction(transaction: Transaction)

    suspend fun updateTransaction(transaction: Transaction)

    suspend fun insertTransactions(transactions: List<Transaction>)

    suspend fun deleteTransaction(transaction: Transaction)
}