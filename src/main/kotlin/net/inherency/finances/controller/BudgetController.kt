package net.inherency.finances.controller

import net.inherency.finances.controller.dto.CreateBudgetForMonthAndYearFromTemplateCmd
import net.inherency.finances.domain.budget.BudgetService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ws/budget")
class BudgetController(private val budgetService: BudgetService) {

    //TODO: Create integration test for this
    @PostMapping(value = ["create_for_month"], consumes = [APPLICATION_JSON_VALUE])
    fun createBudgetForMonthAndYearFromTemplate(
            @RequestBody cmd: CreateBudgetForMonthAndYearFromTemplateCmd) {
        budgetService.createBudgetForMonthAndYearFromTemplate(cmd)
    }

}