package net.inherency.finances

import net.inherency.finances.domain.bill.BillService
import net.inherency.finances.domain.budget.BudgetService
import net.inherency.finances.domain.reconcile.ReconcileService
import net.inherency.finances.domain.reconcile.RemainingMintTransactionsService
import net.inherency.finances.domain.report.ReportService
import net.inherency.finances.domain.transaction.TransactionService
import net.inherency.finances.external.mint.MintClient
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.lang.Exception
import java.time.LocalDate

@Service
class ReconcileCommandLineRunner(
        private val reconcileService: ReconcileService,
        private val remainingMintTransactionsService: RemainingMintTransactionsService,
        private val commandLineService: CommandLineService,
        private val mintClient: MintClient,
        private val transactionService: TransactionService,
        private val reportService: ReportService,
        private val budgetService: BudgetService,
        private val billService: BillService): CommandLineRunner {

    private val log = LoggerFactory.getLogger(ReconcileCommandLineRunner::class.java)

    override fun run(vararg args: String?) {
        if (!args.map { it?.toLowerCase() }.contains("reconcile")) {
            log.info("Not reconciling this run...")
            return
        }
        try {
            val result = reconcileService.reconcile(false)
            log.info("There are ${result.unreconciledMintTransactions.size} unreconciled mint transactions remaining to handle.")
            log.info("There are ${result.unreconciledCategorizedTransactions.size} unreconciled categorized transactions remaining to handle.")
            log.info("Here are recent unreconciled categorized transactions:")
            result.unreconciledCategorizedTransactions
                .filter { it.date.isAfter(LocalDate.now().minusDays(30)) }

            remainingMintTransactionsService
                    .promptAndHandleRemainingMintTransactions(result.unreconciledMintTransactions)

            reportService.updateTransactionReport(
                transactionService.reportAllCategorizedTransactionsAfter()
            )

            reportService.updateBudgetReport(
                budgetService.generateMonthlyBudgetReport()
            )

            reportService.updateBillReport(
                billService.findAllBillsReportableViaUI()
            )

            log.info("Reconciliation complete.")
            log.info("Do you want to delete the download file?")
            val doDelete = commandLineService.readConfirmation()
            if (doDelete) {
                mintClient.deleteDownloadFile()
            }
        } catch (ex: FileNotFoundException) {
            log.info("Will not reconcile in command line because there is no transactions.csv file downloaded.")
        } catch (e: Exception) {
            log.error("Could not reconcile in command line", e)
        }
    }
}