package net.inherency.finances.controller

import net.inherency.finances.controller.dto.BillReportDTO
import net.inherency.finances.domain.bill.BillService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ws/bills")
class BillController(private val billService: BillService) {

    @GetMapping(value = ["report"], produces = [APPLICATION_JSON_VALUE])
    fun generateBillReport(): BillReportDTO {
        return billService.findAllBillsReportableViaUI()
    }

}