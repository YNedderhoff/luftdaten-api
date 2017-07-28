package xyz.nedderhoff.luftdatenapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService

@RestController
class LuftdatenController {

    @Autowired
    private val luftdatenService: LuftdatenService? = null

    @GetMapping("/pingInflux")
    fun ping() = luftdatenService!!.ping()

    @GetMapping("/lastTemperature")
    fun getLastTemperature() = luftdatenService!!.queryLastTemperature()

    @GetMapping("/lastHumidity")
    fun getLastHumidity() = luftdatenService!!.queryLastHumidity()

    @GetMapping("/lastPm")
    fun getLasPm() = luftdatenService!!.queryLastPm()

}