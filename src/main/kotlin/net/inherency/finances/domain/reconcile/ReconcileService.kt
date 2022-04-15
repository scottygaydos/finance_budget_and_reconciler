package net.inherency.finances.domain.reconcile

import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.account.AccountService
import net.inherency.finances.domain.transaction.CategorizedTransaction
import net.inherency.finances.domain.transaction.MintTransaction
import net.inherency.finances.domain.transaction.TransactionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReconcileService(
        private val transactionService: TransactionService,
        private val accountService: AccountService) {

    companion object {
        const val DAYS_TO_TEST_FORWARD_AND_BACKWARD = 3L

        data class ReconcileResult (
                val reconciledTransactions: Map<CategorizedTransaction, MintTransaction>,
                val unreconciledMintTransactions: List<MintTransaction>,
                val unreconciledCategorizedTransactions: List<CategorizedTransaction>
        )
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    fun reconcile(doDownloadFile: Boolean): ReconcileResult {
        log.info("Reading all reconciled transactions...")
        val reconciledTransactions = transactionService.listAllReconciledTransactions()

        log.info("Updating google sheet with mint transactions...")
        val allMintTransactions = transactionService.updateMintTransactions(doDownloadFile)
        val mintTxsToReconcile = allMintTransactions
                .filterNot { mintTx -> reconciledTransactions.map { it.mintId }.contains(mintTx.getIdAsString()) }
                .filterNot { mintTx -> mintTx.accountName == "PayPal Account" }
                .filterNot { mintTx -> mintTx.accountName == "Fixed Rate Mortgage" }
                .filterNot { mintTx -> mintTx.accountName == "HEALTH SAVINGS ACCOUNT" }

        log.info("Reading all existing categorized transactions that are reconcilable...")
        val allCategorizedTransactions = transactionService.listAllCategorizedTransactions()
        val catTxsToReconcile = allCategorizedTransactions
                .filterNot { categorizedTransaction ->
                    reconciledTransactions.map{it.categorizedTransactionId}.contains(categorizedTransaction.id) }
                .filter { it.reconcilable }

        log.info("There are ${mintTxsToReconcile.size} remaining mint transactions to reconcile")
        log.info("There are ${catTxsToReconcile.size} remaining categorized transactions to reconcile")

        val accumulator = matchRemaining(catTxsToReconcile, mintTxsToReconcile)
        val existingReconciledTransactions = reconciledTransactions.map {
            Pair(
                    allCategorizedTransactions.first { catTx -> catTx.id == it.categorizedTransactionId  },
                    allMintTransactions.first { mintTx -> mintTx.getIdAsString() == it.mintId })
        }
        accumulator.reconciledTransactions.putAll(existingReconciledTransactions)
        return ReconcileResult(
                accumulator.reconciledTransactions,
                accumulator.unreconciledMintTransactions,
                accumulator.unreconciledCategorizedTransactions
        )
    }

    private fun matchRemaining(
            categorizedTransactions: List<CategorizedTransaction>, mintTransactions: List<MintTransaction>)
            : ReconcileResultAccumulator {
        val mintTransactionsUnmatchedAtEnd = mintTransactions.toMutableList()
        val accounts = accountService.readAll()
        val accumulator = categorizedTransactions.map { catTx ->
            val matchingMintTx = popMatchingMintTxForCategorizedTx(accounts, catTx, mintTransactionsUnmatchedAtEnd)
            Pair(catTx, matchingMintTx)
        }.foldRight(ReconcileResultAccumulator()) { pair, acc ->
            val categorizedTx = pair.first
            val mintTx = pair.second
            if (mintTx != null) {
                acc.reconciledTransactions[categorizedTx] = mintTx
            } else {
                acc.unreconciledCategorizedTransactions.add(categorizedTx)
            }
            acc
        }
        accumulator.unreconciledMintTransactions = mintTransactionsUnmatchedAtEnd
        return accumulator
    }

    private fun popMatchingMintTxForCategorizedTx(
            accounts: List<Account>,
            categorizedTx: CategorizedTransaction,
            mintTransactions: MutableList<MintTransaction>): MintTransaction? {
        val matchingIndex =  mintTransactions.indexOfFirst { mintTx ->
            txDatesMatch(categorizedTx, mintTx)
                    && txAccountsMatch(accounts, categorizedTx, mintTx)
                    && txAmountsMatch(categorizedTx, mintTx)
        }
        return if (matchingIndex == -1) {
            null
        } else {
            return mintTransactions.removeAt(matchingIndex)
        }
    }

    private fun txAmountsMatch(categorizedTx: CategorizedTransaction, mintTx: MintTransaction): Boolean {
        return categorizedTx.authorizedAmount == mintTx.amount || categorizedTx.settledAmount == mintTx.amount
    }

    private fun txDatesMatch(categorizedTx: CategorizedTransaction, mintTx: MintTransaction): Boolean {
        val catDate = categorizedTx.date
        val mintDate = mintTx.date
        return LongRange(-DAYS_TO_TEST_FORWARD_AND_BACKWARD, DAYS_TO_TEST_FORWARD_AND_BACKWARD).any {
            catDate.plusDays(it) == mintDate
        }
    }

    private fun txAccountsMatch(
            accounts: List<Account>,
            categorizedTransaction: CategorizedTransaction,
            mintTx: MintTransaction): Boolean {
        val mintAcct = accounts.firstOrNull { acct -> acct.mintName == mintTx.accountName || acct.mintNameAlt == mintTx.accountName }
        return mintAcct.let {
            categorizedTransaction.creditAccountId == it?.id || categorizedTransaction.debitAccountId == it?.id
        }
    }

    private class ReconcileResultAccumulator {
        var reconciledTransactions: MutableMap<CategorizedTransaction, MintTransaction> = mutableMapOf()
        var unreconciledCategorizedTransactions: MutableList<CategorizedTransaction> = mutableListOf()
        var unreconciledMintTransactions: List<MintTransaction> = emptyList()
    }

}