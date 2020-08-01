package net.inherency.finances.domain.transaction

import java.util.*

data class ReconciledTransaction (
        val mintId: String,
        val categorizedTransactionId: UUID
)
