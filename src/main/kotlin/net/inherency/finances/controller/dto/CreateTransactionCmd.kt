package net.inherency.finances.controller.dto

data class CreateTransactionCmd(
        val transactionDateString: String?,
        val transactionTypeId: Int, //named for legacy front end code; maps to budget category id
        val description: String,
        val creditAccountId: Int,
        val debitAccountId: Int,
        val authorizedAmount: String,
        val settledAmount: String?,
        val canReconcile: Boolean
)