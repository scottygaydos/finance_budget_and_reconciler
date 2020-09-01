package net.inherency.finances.controller

import net.inherency.finances.controller.dto.AccountDTO
import net.inherency.finances.domain.account.AccountService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ws/accounts")
class AccountsController(private val accountService: AccountService) {

    @GetMapping(value = ["creditable_accounts"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAllCreditableAccounts(): List<AccountDTO> {
        return accountService.readAllCreditableAccounts()
    }

    @GetMapping(value = ["debitable_accounts"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAllDebitableAccounts(): List<AccountDTO> {
        return accountService.readAllDebitableAccounts()
    }

}