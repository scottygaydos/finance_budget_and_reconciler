package net.inherency.finances.domain.reconcile

import net.inherency.finances.domain.account.Account
import net.inherency.finances.domain.transaction.CreditOrDebit
import net.inherency.finances.domain.transaction.MintTransaction
import org.springframework.stereotype.Service

@Service
class DebitAndCreditAccountFactory {

    fun determineCreditAndDebitAccounts(mintTx: MintTransaction, mintAccount: Account,
                                                globalExternalAccount: Account): Pair<Account, Account> {
        return when (mintTx.creditOrDebit) {
            CreditOrDebit.CREDIT -> {
                Pair(mintAccount, globalExternalAccount)
            }
            CreditOrDebit.DEBIT -> {
                Pair(globalExternalAccount, mintAccount)
            }
            CreditOrDebit.UNKNOWN -> {
                throw IllegalArgumentException("Please review mint tx for credit/debit status: $mintTx")
            }
        }
    }
}