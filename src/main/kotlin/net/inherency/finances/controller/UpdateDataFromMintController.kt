package net.inherency.finances.controller

import net.inherency.finances.domain.transaction.UpdateMintTransactionService
import net.inherency.finances.domain.transaction.MintTransaction
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ws/updateData")
class UpdateDataFromMintController(private val updateMintTransactionService: UpdateMintTransactionService) {

    @PostMapping(value = ["update"])
    fun update(): List<MintTransaction> {
        return updateMintTransactionService.update()
    }

}