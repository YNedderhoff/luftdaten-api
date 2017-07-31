package xyz.nedderhoff.luftdatenapi.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService

@RestController
class LuftdatenController(val luftdatenService: LuftdatenService?) {

    @GetMapping("/pingInflux")
    fun ping() = luftdatenService!!.ping()

    @GetMapping("/temperature")
    fun getTemperatureInDateRange() = luftdatenService!!.queryTemperatureInDateRange()

    @GetMapping("/temperature/last")
    fun getLastTemperature() = luftdatenService!!.queryLastTemperature()

    @GetMapping("/humidity")
    fun getHumidityInDateRange() = luftdatenService!!.queryHumidityInDateRange()

    @GetMapping("/humidity/last")
    fun getLastHumidity() = luftdatenService!!.queryLastHumidity()

    @GetMapping("/pm")
    fun getPmInDateRange() = luftdatenService!!.queryPmInDateRange()

    @GetMapping("/pm/last")
    fun getLastPm() = luftdatenService!!.queryLastPm()
}