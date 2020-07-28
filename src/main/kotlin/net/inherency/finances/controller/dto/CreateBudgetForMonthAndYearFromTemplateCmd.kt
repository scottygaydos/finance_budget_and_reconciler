package net.inherency.finances.controller.dto

data class CreateBudgetForMonthAndYearFromTemplateCmd(
        val year: Int,
        val month: Int //This is zero indexed from client. 0 = January
)