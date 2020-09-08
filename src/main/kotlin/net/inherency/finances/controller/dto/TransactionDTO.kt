package net.inherency.finances.controller.dto

import java.time.LocalDate

data class TransactionDTO (
        val transaction_date: LocalDate,
        val category: String,
        val description: String,
        val authorized_amount: Int,
        val settled_amount: Int,
        val can_reconcile: Boolean,
        val transaction_id: String,
        val credit_account_name: String,
        val debit_account_name: String,
        val credit_or_debit: String,
        val percentage: Int
)