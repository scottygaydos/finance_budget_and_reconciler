package net.inherency.finances.external.google

/**
 * These should be data values that can generate a list of strings to write to a google sheet tab as an invariant.
 */
interface GoogleSheetWritable {

    fun toGoogleSheetRowList(): List<String>

}