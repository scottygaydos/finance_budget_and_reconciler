package net.inherency.finances.domain.account

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class AccountRepository(private val googleSheetClient: GoogleSheetClient) {

    fun readAll(): List<Account> {
        val rows = googleSheetClient.listValuesInTab(TabName.ACCOUNTS)
        return rows
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
    }

}