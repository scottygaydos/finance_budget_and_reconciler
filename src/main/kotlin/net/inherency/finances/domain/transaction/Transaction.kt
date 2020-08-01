package net.inherency.finances.domain.transaction

import net.inherency.finances.external.google.GoogleSheetWritable

interface Transaction : GoogleSheetWritable {

    fun getIdAsString(): String

}