package net.inherency.finances.controller

import net.inherency.finances.domain.transaction.TransactionService
import net.inherency.finances.domain.transaction.MintTransaction
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ws/updateData")
class UpdateDataFromMintController(private val transactionService: TransactionService) {

    @PostMapping(value = ["update"])
    fun update(): List<MintTransaction> {
        return transactionService.updateMintTransactions(true)
    }

}