package xyz.nedderhoff.luftdatenapi.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService
import java.util.function.Supplier

@RestController
class LuftdatenController(val luftdatenService: LuftdatenService?) {

    val logger = LoggerFactory.getLogger(LuftdatenController::class.java)

    @GetMapping("/pingInflux")
    fun ping() = get("/pingInflux", Supplier { luftdatenService!!.ping() })

    @GetMapping("/temperature.json")
    fun getTemperatureSeries() = get("/temperature.json", Supplier { luftdatenService!!.queryTemperatureSeries() })

    @GetMapping("/temperature/last")
    fun getLastTemperature() = get("/temperature/last", Supplier { luftdatenService!!.queryLastTemperature() })

    @GetMapping("/humidity.json")
    fun getHumidityInDateRangeSeries() = get("/humidity.json", Supplier { luftdatenService!!.queryHumiditySeries() })

    @GetMapping("/humidity/last")
    fun getLastHumidity() = get("/humidity/last", Supplier { luftdatenService!!.queryLastHumidity() })

    @GetMapping("/pm.json")
    fun getPmSeries() = get("/pm.json", Supplier { luftdatenService!!.queryPmSeries() })

    @GetMapping("/pm/last")
    fun getLastPm() = get("/pm/last", Supplier { luftdatenService!!.queryLastPm() })

    private fun get(endpoint: String, supplier: Supplier<Any?>): Any? {
        logger.info("Received GET $endpoint call")
        return supplier.get()
    }

}