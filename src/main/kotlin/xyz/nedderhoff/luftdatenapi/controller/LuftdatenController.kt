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

    @GetMapping("/temperature.json")
    fun getTemperatureSeries() = luftdatenService!!.queryTemperatureSeries()

    @GetMapping("/temperature/last")
    fun getLastTemperature() = luftdatenService!!.queryLastTemperature()

    @GetMapping("/humidity.json")
    fun getHumidityInDateRangeSeries() = luftdatenService!!.queryHumiditySeries()

    @GetMapping("/humidity/last")
    fun getLastHumidity() = luftdatenService!!.queryLastHumidity()

    @GetMapping("/pm.json")
    fun getPmSeries() = luftdatenService!!.queryPmSeries()

    @GetMapping("/pm/last")
    fun getLastPm() = luftdatenService!!.queryLastPm()

}