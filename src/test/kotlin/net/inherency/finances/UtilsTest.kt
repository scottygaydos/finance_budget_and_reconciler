package net.inherency.finances

import net.inherency.finances.domain.budget.template.BudgetTemplateData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun `Object in list should be considered unique`() {
        val templateValues = listOf(
                BudgetTemplateData(1, 1, "a"),
                BudgetTemplateData(2, 10, "b")
        )
        val result = objectIsUnique(templateValues) { entry -> entry.budgetCategoryId }
        Assertions.assertTrue(result)
    }

    @Test
    fun `Object in list should not be considered unique`() {
        val templateValues = listOf(
                BudgetTemplateData(1, 1, "a"),
                BudgetTemplateData(1, 10, "b")
        )
        val result = objectIsUnique(templateValues) { entry -> entry.budgetCategoryId }
        Assertions.assertFalse(result)
    }
}