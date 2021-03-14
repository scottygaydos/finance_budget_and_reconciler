package net.inherency.finances

import net.inherency.finances.domain.reconcile.ReconcileService
import net.inherency.finances.domain.reconcile.RemainingMintTransactionsService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class ReconcileCommandLineRunner(
        private val reconcileService: ReconcileService,
        private val remainingMintTransactionsService: RemainingMintTransactionsService): CommandLineRunner {

    private val log = LoggerFactory.getLogger(ReconcileCommandLineRunner::class.java)

    override fun run(vararg args: String?) {
        if (!args.map { it?.toLowerCase() }.contains("reconcile")) {
            log.info("Not reconciling this run...")
            return
        }
        try {
            val doDownloadFile = args.map { it?.toLowerCase()?.trim() }.contains("download")
            val result = reconcileService.reconcile(doDownloadFile)
            log.info("There are ${result.unreconciledMintTransactions.size} unreconciled mint transactions remaining.")
            log.info("There are ${result.unreconciledCategorizedTransactions.size} " +
                    "unreconciled categorized transactions remaining.")

            remainingMintTransactionsService
                    .promptAndHandleRemainingMintTransactions(result.unreconciledMintTransactions)

        } catch (e: Exception) {
            log.error("Could not reconcile in command line", e)
        }
    }
}