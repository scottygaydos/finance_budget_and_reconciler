package net.inherency.finances.controller

import net.inherency.finances.controller.dto.*
import net.inherency.finances.domain.budget.BudgetService
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("ws/budget")
class BudgetController(private val budgetService: BudgetService,
                       private val budgetCategoryService: BudgetCategoryService) {

    //TODO: Delete this when I am fully transitioned away from old PHP and FE
    @PostMapping(value = ["create_for_month_old"], consumes = ["application/x-www-form-urlencoded"])
    fun createBudgetForMonthAndYearFromTemplate(
            @RequestParam("budget_month") budgetMonth: Int,
            @RequestParam("budget_year") budgetYear: Int)  {
        val cmd = CreateBudgetForMonthAndYearFromTemplateCmd(budgetYear, budgetMonth)
        budgetService.createBudgetForMonthAndYearFromTemplate(cmd)
    }

    @PostMapping(value = ["create_for_month_new"], consumes = [APPLICATION_JSON_VALUE])
    fun createBudgetForMonthAndYearFromTemplateNew(
            @RequestBody cmd: CreateBudgetForMonthAndYearFromTemplateCmd) {
        createBudgetForMonthAndYearFromTemplate(cmd)
    }

    @PostMapping(value = ["create_for_month"], consumes = [APPLICATION_JSON_VALUE])
    fun createBudgetForMonthAndYearFromTemplate(
            @RequestBody cmd: CreateBudgetForMonthAndYearFromTemplateCmd) {
        budgetService.createBudgetForMonthAndYearFromTemplate(cmd)
    }

    @PostMapping(value = ["fix_month_to_match_deposits_new"], consumes = ["application/json"])
    fun fixMonthToMatchDeposits(@RequestBody cmd: FixBudgetToMatchDepositsCmd) {
        budgetService.fixMonthToMatchDeposits(
                FixBudgetToMatchDepositsCmd(cmd.budgetYear, cmd.budgetMonth + 1, cmd.transactionTypeId))
    }

    //TODO: Delete this once I transition fully to new front end.
    @Suppress("UNUSED_PARAMETER")
    @PostMapping(value = ["fix_month_to_match_deposits"], consumes = ["application/x-www-form-urlencoded"])
    fun fixMonthToMatchDeposits(
            @RequestParam("budget_month") budgetMonth: Int,
            @RequestParam("budget_year") budgetYear: Int,
            @RequestParam("transaction_type_id") transactionTypeId: Int,
            @RequestParam("diff_amount") unused: Int) {
        val cmd = FixBudgetToMatchDepositsCmd(budgetYear, budgetMonth, transactionTypeId)
        budgetService.fixMonthToMatchDeposits(cmd)
    }

    @GetMapping(value = ["report"])
    fun generateBudgetReport(
            @RequestParam("budget_month") budgetMonth: Int?,
            @RequestParam("budget_year") budgetYear: Int?): BudgetReportDTO {
        return budgetService.generateMonthlyBudgetReport(budgetMonth, budgetYear)
    }

    //TODO: Delete this once I transition fully to new front end.
    @Suppress("UNUSED_PARAMETER")
    @PostMapping(value = ["move_remainder_to_next_month"], consumes = ["application/x-www-form-urlencoded"])
    fun moveRemainderToNextMonth(
            @RequestParam("budget_month") budgetMonth: Int,
            @RequestParam("budget_year") budgetYear: Int,
            @RequestParam("amount") unused: Int) {
        val cmd = MoveRemainderToNextMonthCmd(budgetMonth, budgetYear)
        budgetService.moveRemainderToNextMonth(cmd)
    }

    @PostMapping(value = ["move_remainder_to_next_month_new"], consumes = ["application/json"])
    fun moveRemainderToNextMonth(@RequestBody cmd: MoveRemainderToNextMonthCmd) {
        val localCmd = cmd.copy(
                budgetMonth = cmd.budgetMonth + 1
        )
        budgetService.moveRemainderToNextMonth(localCmd)
    }

    @GetMapping(value = ["types"])
    fun getAllBudgetTypes(): List<BudgetCategoryDTO> {
        return budgetCategoryService.reportAll()
    }

}