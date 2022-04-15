package net.inherency.finances.domain.reconcile

import net.inherency.finances.domain.transaction.CategorizedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RemainingCategorizedTransactionsService {

    private val log: Logger = LoggerFactory.getLogger(RemainingCategorizedTransactionsService::class.java)

    fun handleRemainingCategorizedTransactions(transactions: List<CategorizedTransaction>) {
        log.info("The following rows of CATEGORIZED_TRANSACTIONS did not reconcile")
        transactions
            .filter { it.date.isAfter(LocalDate.of(2021, 1, 31)) } //TODO: Should I remove this and back populate?
            .filter { it.date.isAfter(LocalDate.now().withDayOfMonth(1).minusMonths(1))} //TODO: Should I remove this and back populate?
            .sortedBy { it.date }
            .forEach {
                log.info(it.toString())
            }

    }
}