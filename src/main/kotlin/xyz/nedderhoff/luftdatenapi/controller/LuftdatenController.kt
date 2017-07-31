package xyz.nedderhoff.luftdatenapi.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService

@RestController
class LuftdatenController(val luftdatenService: LuftdatenService?) {

    @GetMapping("/pingInflux")
    fun ping() {
        println("Got request to /pingInflux endpoint")
        luftdatenService!!.ping()
    }

    @GetMapping("/temperature")
    fun getTemperatureInDateRange() = luftdatenService!!.queryTemperatureInDateRange()

    @GetMapping("/temperature/last")
    fun getLastTemperature() = luftdatenService!!.queryLastTemperature()

    @GetMapping("/temperature.json")
    fun getTemperatureSeries() = luftdatenService!!.queryTemperatureInDateRangeAndReturnSeries()

    @GetMapping("/humidity")
    fun getHumidityInDateRange() = luftdatenService!!.queryHumidityInDateRange()

    @GetMapping("/humidity/last")
    fun getLastHumidity() = luftdatenService!!.queryLastHumidity()

    @GetMapping("/humidity.json")
    fun getHumidityInDateRangeSeries() = luftdatenService!!.queryHumidityInDateRangeAndReturnSeries()

    @GetMapping("/pm")
    fun getPmInDateRange() = luftdatenService!!.queryPmInDateRange()

    @GetMapping("/pm/last")
    fun getLastPm() = luftdatenService!!.queryLastPm()

    @GetMapping("/pm.json")
    fun getPmSeries() = luftdatenService!!.queryPmInDateRangeAndReturnSeries()

}