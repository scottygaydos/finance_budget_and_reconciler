package net.inherency.finances.domain.account

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class AccountRepositoryGoogleSheetImpl(private val googleSheetClient: GoogleSheetClient): AccountRepository {

    private lateinit var accounts: List<Account>
    private var cached = false

    @Synchronized
    override fun readAll(): List<Account> {
        return if (cached) {
            accounts
        } else {
            val rows = googleSheetClient.listValuesInTab(TabName.ACCOUNTS)
            val foundAccounts = rows
                    .subList(1, rows.size) //remove header
                    .map { row ->
                        Account(
                                row[0].toInt(),
                                row[1],
                                row[2],
                                row[3],
                                row[4],
                                row[5].toBoolean(),
                                row[6].toBoolean()
                        )
                    }
            this.accounts = foundAccounts
            this.cached = true
            accounts
        }

    }

}