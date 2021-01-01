package net.inherency.finances.domain.account

interface AccountRepository {
    fun readAll(): List<Account>
}