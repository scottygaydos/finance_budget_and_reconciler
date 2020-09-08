package net.inherency.finances.controller

import net.inherency.finances.controller.dto.BudgetCategoryDTO
import net.inherency.finances.controller.dto.BudgetReportDTO
import net.inherency.finances.controller.dto.CreateBudgetForMonthAndYearFromTemplateCmd
import net.inherency.finances.domain.budget.BudgetService
import net.inherency.finances.domain.budget.category.BudgetCategoryService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("ws/budget")
class BudgetController(private val budgetService: BudgetService,
                       private val budgetCategoryService: BudgetCategoryService) {

    //TODO: Create integration test for this
    @PostMapping(value = ["create_for_month"], consumes = [APPLICATION_JSON_VALUE])
    fun createBudgetForMonthAndYearFromTemplate(
            @RequestBody cmd: CreateBudgetForMonthAndYearFromTemplateCmd) {
        budgetService.createBudgetForMonthAndYearFromTemplate(cmd)
    }

    @GetMapping(value = ["report"])
    fun generateBudgetReport(
            @RequestParam("budget_month") budgetMonth: Int?,
            @RequestParam("budget_year") budgetYear: Int?): BudgetReportDTO {
        return budgetService.generateMonthlyBudgetReport(budgetMonth, budgetYear)
    }

    @GetMapping(value = ["/types"])
    fun getAllBudgetTypes(): List<BudgetCategoryDTO> {
        return budgetCategoryService.reportAll()
    }

}