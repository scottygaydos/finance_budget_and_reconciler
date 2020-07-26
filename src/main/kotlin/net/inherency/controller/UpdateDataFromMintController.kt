package net.inherency.controller

import net.inherency.domain.transaction.UpdateMintTransactionService
import net.inherency.external.mint.MintTransaction
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