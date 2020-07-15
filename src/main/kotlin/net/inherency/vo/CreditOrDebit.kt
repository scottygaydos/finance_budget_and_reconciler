package net.inherency.vo

enum class CreditOrDebit {
    CREDIT,
    DEBIT,
    UNKNOWN;

    companion object {
        fun parse(s: String): CreditOrDebit {
            return when {
                s.equals(CREDIT.name, ignoreCase = true) -> { CREDIT }
                s.equals(DEBIT.name, ignoreCase = true) -> { DEBIT }
                else -> { UNKNOWN }
            }
        }
    }
}