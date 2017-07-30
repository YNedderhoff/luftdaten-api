package xyz.nedderhoff.luftdatenapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService
import java.util.*

@RestController
class LuftdatenController {

    @Autowired
    private val luftdatenService: LuftdatenService? = null

    @GetMapping("/pingInflux")
    fun ping() = luftdatenService!!.ping()

    @GetMapping("/temperature")
    fun getTemperatureInDateRange() = luftdatenService!!.queryTemperatureInDateRange(Date(), Date())

    @GetMapping("/humidity")
    fun getHumidityInDateRange() = luftdatenService!!.queryHumidityInDateRange(Date(), Date())

    @GetMapping("/pm")
    fun getPmInDateRange() = luftdatenService!!.queryPmInDateRange(Date(), Date())

    @GetMapping("/lastTemperature")
    fun getLastTemperature() = luftdatenService!!.queryLastTemperature()

    @GetMapping("/lastHumidity")
    fun getLastHumidity() = luftdatenService!!.queryLastHumidity()
}