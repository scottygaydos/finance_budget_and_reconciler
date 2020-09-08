package net.inherency.finances.controller

import net.inherency.finances.controller.dto.BudgetCategoryDTO
import net.inherency.finances.controller.dto.CreateTransactionCmd
import net.inherency.finances.controller.dto.TransactionDTO
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import net.inherency.finances.domain.transaction.TransactionService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("ws/transaction")
class TransactionController(private val transactionService: TransactionService,
                            private val budgetCategoryService: BudgetCategoryService) {

    @PostMapping(value = ["create_new"], consumes = ["application/json"])
    fun createNew(@RequestBody cmd: CreateTransactionCmd) {
        transactionService.create(cmd)
    }

    @GetMapping(value = ["types"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun reportAllTransactionTypes(): List<BudgetCategoryDTO> {
        return budgetCategoryService.reportAll()
    }

    @GetMapping(value = ["report"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun reportTransactions(): List<TransactionDTO> {
        return transactionService.reportAllCategorizedTransactionsAfter()
    }

}