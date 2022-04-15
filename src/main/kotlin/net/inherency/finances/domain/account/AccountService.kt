package net.inherency.finances.domain.account

import net.inherency.finances.controller.dto.AccountDTO
import org.springframework.stereotype.Service

@Service
class AccountService(private val accountRepository: AccountRepository) {

    companion object {
        const val GLOBAL_EXTERNAL_DEBIT_ACCOUNT_NAME = "Global External"
    }

    private val allAccounts = mutableListOf<Account>()

    fun readAllCreditableAccounts(): List<AccountDTO> {
        return readAllAccountsMeetingCondition { it.canManuallyCredit }
    }

    fun readAllDebitableAccounts(): List<AccountDTO> {
        return readAllAccountsMeetingCondition { it.canManuallyDebit }
    }

    private fun readAllAccountsMeetingCondition(filterFunction: (account: Account) -> Boolean): List<AccountDTO> {
        return readAll()
                .filter { filterFunction.invoke(it) }
                .map { AccountDTO(it.id, it.name, "") }
    }

    fun readAll(): List<Account> {
        if (allAccounts.isEmpty()) {
            val accounts = accountRepository.readAll()
            validateAccounts(accounts)
            allAccounts.addAll(accounts)
        }

        return allAccounts
    }

    fun findByName(name: String): Account {
        return readAll().first { it.name == name }
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