package net.inherency.finances.domain.bill

import net.inherency.finances.external.google.GoogleSheetClient
import net.inherency.finances.external.google.TabName
import org.springframework.stereotype.Repository

@Repository
class BillRepository(private val googleSheetClient: GoogleSheetClient) {

    fun readAll(): List<BillData> {
        val rows = googleSheetClient.listValuesInTab(TabName.BILLS)
        return rows
                .subList(1, rows.size) //remove header
                .map { row ->
                    BillData(
                            row[0].toInt(),
                            row[1],
                            row[2],
                            row[3].toInt(),
                            row[4].toBoolean(),
                            row[5].toBoolean()
                    )
                }
    }

}