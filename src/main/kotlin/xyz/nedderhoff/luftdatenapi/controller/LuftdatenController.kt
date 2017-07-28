package xyz.nedderhoff.luftdatenapi.controller

import org.influxdb.dto.Point
import org.influxdb.dto.Pong
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xyz.nedderhoff.luftdatenapi.service.LuftdatenService
import java.util.concurrent.atomic.AtomicLong

@RestController
class LuftdatenController {

    @Autowired
    private val luftdatenService: LuftdatenService? = null

    @GetMapping("/pingInflux")
    fun ping() = luftdatenService!!.ping()

    @GetMapping("/query")
    fun query() = luftdatenService!!.query()

}