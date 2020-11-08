package net.inherency.finances.controller.dto

data class MoveRemainderToNextMonthCmd (
        val budgetYear: Int,
        val budgetMonth: Int //TODO: Is this 0 or 1 indexed?  Standardize and document.  Seems to be 0 = January.
)