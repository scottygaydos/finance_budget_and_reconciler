package net.inherency.finances.domain.budget.template

import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service

@Service
class BudgetTemplateService(
        private val budgetTemplateRepository: BudgetTemplateRepository) {

    fun readAllBudgetTemplateValues(): List<BudgetTemplateData> {
        val templateValues= budgetTemplateRepository.readAll()
        validateTemplateValues(templateValues)
        return templateValues
    }

    private fun validateTemplateValues(templateValues: List<BudgetTemplateData>) {

        if (templateValues.isEmpty()) {
            throw IllegalStateException("Please initialize budget template values in spreadsheet")
        }
        if (!objectIsUnique(templateValues) {templateValue -> templateValue.budgetCategoryId}) {
            throw IllegalStateException("Each budget template record must reference a unique budget category")
        }
        if (containsNegativeAmount(templateValues)) {
            throw java.lang.IllegalStateException("Each budget template record must be a positive value (in cents)")
        }
    }

    private fun containsNegativeAmount(templateValues: List<BudgetTemplateData>): Boolean {
        return templateValues.any { it.amount < 0 }
    }

}