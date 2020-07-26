package net.inherency.controller

import net.inherency.config.ConfigurationService
import net.inherency.external.google.GoogleSheetService
import net.inherency.external.google.TabName
import net.inherency.external.mint.MintClient
import net.inherency.external.mint.MintFileParser
import net.inherency.external.mint.MintTransactionFactory
import net.inherency.vo.MintTransaction
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ws/updateData")
class UpdateDataFromMintController(
        private val configurationService: ConfigurationService,
        private val googleSheetService: GoogleSheetService) {

    @PostMapping(value = ["update"])
    fun update(): List<MintTransaction> {
        val allMintTransactions = downloadAllMintTransactions()
        googleSheetService.clearTabAndWriteAllValuesToSheetAfterSorting(TabName.MINT_TRANSACTIONS, allMintTransactions)
        return googleSheetService.listAllMintTransactionsInTransactionsTab()
    }

    private fun downloadAllMintTransactions(): List<MintTransaction> {
        val mint = MintClient(configurationService, MintFileParser(MintTransactionFactory()))
        return mint.downloadAllTransactions()
    }

}