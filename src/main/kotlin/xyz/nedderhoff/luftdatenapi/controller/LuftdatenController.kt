package xyz.nedderhoff.luftdatenapi.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService
import java.util.function.Supplier

@RestController
class LuftdatenController(val service: LuftdatenService?) {

    val logger = LoggerFactory.getLogger(LuftdatenController::class.java)!!

    @GetMapping("/ping")
    fun ping() = "Yep, I'm alive"

    @GetMapping("/pingInflux")
    fun pingInflux() = get("/pingInflux", Supplier { service!!.ping() })

    @GetMapping("/temperature.json")
    fun getTemperatureSeries() = get("/temperature.json", Supplier { service!!.queryTemperatureSeries() })

    @GetMapping("/temperature/last")
    fun getLastTemperature() = get("/temperature/last", Supplier { service!!.queryLastTemperature() })

    @GetMapping("/humidity.json")
    fun getHumidityInDateRangeSeries() = get("/humidity.json", Supplier { service!!.queryHumiditySeries() })

    @GetMapping("/humidity/last")
    fun getLastHumidity() = get("/humidity/last", Supplier { service!!.queryLastHumidity() })

    @GetMapping("/pm.json")
    fun getPmSeries() = get("/pm.json", Supplier { service!!.queryPmSeries() })

    @GetMapping("/pm1/last")
    fun getLastPm1() = get("/pm1/last", Supplier { service!!.queryLastPm1() })

    @GetMapping("/pm2/last")
    fun getLastPm2() = get("/pm2/last", Supplier { service!!.queryLastPm2() })

    @GetMapping("/lastMeasurements")
    fun getLastMeasurements() = get("/lastMeasurements", Supplier { service!!.queryLastMeasurements() })

    private fun get(endpoint: String, supplier: Supplier<Any?>): Any? {
        logger.info("Received GET $endpoint call")
        return supplier.get()
    }
}