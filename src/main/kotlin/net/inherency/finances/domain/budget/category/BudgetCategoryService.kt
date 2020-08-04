package net.inherency.finances.domain.budget.category

import net.inherency.finances.objectIsUnique
import org.springframework.stereotype.Service

@Service
class BudgetCategoryService(private val budgetCategoryRepository: BudgetCategoryRepository) {

    @Suppress("unused")
    fun readAll(): List<BudgetCategoryData> {
        val categories = budgetCategoryRepository.readAll()
        validateCategories(categories)
        return categories
    }

    private fun validateCategories(categories: List<BudgetCategoryData>) {
        if (categories.isEmpty()) {
            throw IllegalStateException("Please initialize categories data in spreadsheet")
        }
        if (!objectIsUnique(categories) {category -> category.id}) {
            throw IllegalStateException("Each budget category id must be unique")
        }
        if (!objectIsUnique(categories) {category -> category.name}) {
            throw IllegalStateException("Each budget category name must be unique")
        }
    }

}