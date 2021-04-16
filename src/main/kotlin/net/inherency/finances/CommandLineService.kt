package net.inherency.finances

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandLineService {

    companion object {
        val AFFIRMATIVE_ANSWERS = listOf("y", "yes", "true")
    }

    private val log = LoggerFactory.getLogger(CommandLineService::class.java)

    fun readFromCommandLine(): String =
            readLine() ?: ""

    fun readConfirmation(): Boolean {
        log.info("To confirm enter one of the following: $AFFIRMATIVE_ANSWERS")
        return AFFIRMATIVE_ANSWERS.map { it.toLowerCase() }.contains(readFromCommandLine().toLowerCase())
    }


}