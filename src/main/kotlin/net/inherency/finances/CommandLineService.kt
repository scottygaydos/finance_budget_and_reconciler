package net.inherency.finances

import org.springframework.stereotype.Service

@Service
class CommandLineService {

    fun readFromCommandLine(): String =
            readLine() ?: ""
}