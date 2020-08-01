package net.inherency.finances.domain.account

import org.springframework.stereotype.Service

@Service
class AccountService(private val accountRepository: AccountRepository) {

    fun readAll(): List<Account> {
        val accounts = accountRepository.readAll()
        validateAccounts(accounts)
        return accounts
    }

    private fun validateAccounts(accounts: List<Account>) {
        if (accounts.isEmpty()) {
            throw IllegalStateException("Application requires account records")
        }
        if (accounts.map { it.id }.toSet().size != accounts.size) {
            throw java.lang.IllegalStateException("Each account must have a unique id")
        }
        if (accounts.map { it.name }.toSet().size != accounts.size) {
            throw java.lang.IllegalStateException("Each account must have a unique name")
        }
    }

}