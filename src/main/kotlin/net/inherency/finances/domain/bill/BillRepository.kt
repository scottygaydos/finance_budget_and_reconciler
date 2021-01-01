package net.inherency.finances.domain.bill

interface BillRepository {
    fun readAll(): List<BillData>
}