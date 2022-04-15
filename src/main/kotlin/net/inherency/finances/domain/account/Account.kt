package net.inherency.finances.domain.account

import java.math.BigDecimal

data class Account (
        val id: Int,
        val name: String,
        val description: String,
        val mintName: String,
        val mintNameAlt: String,
        val canManuallyCredit: Boolean,
        val canManuallyDebit: Boolean,
        val budgetMultiplier: BigDecimal
)