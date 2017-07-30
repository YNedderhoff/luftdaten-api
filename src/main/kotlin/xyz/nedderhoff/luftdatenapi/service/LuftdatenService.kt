package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Point
import org.influxdb.dto.Pong
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.domain.HumidityDTO
import xyz.nedderhoff.luftdatenapi.domain.Pm1DTO
import xyz.nedderhoff.luftdatenapi.domain.Pm2DTO
import xyz.nedderhoff.luftdatenapi.domain.PmDTO
import xyz.nedderhoff.luftdatenapi.domain.TemperatureDTO
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream


@Service
class LuftdatenService {

    @Autowired
    private val influxDBTemplate: InfluxDBTemplate<Point>? = null

    private val database = "pm_temp_hum"
    private val luftdatenSeries = "feinstaub"
    private val selectLastValuePlaceholder = "SELECT %s FROM \"$luftdatenSeries\" "
    private val lastTemperatureQuery = String.format(selectLastValuePlaceholder, "last(\"temperature\")")
    private val lastHumidityQuery = String.format(selectLastValuePlaceholder, "last(\"humidity\")")


    fun ping(): Pong = influxDBTemplate!!.ping()

    fun queryTemperatureInDateRange(startDate: Date, endDate: Date): MutableList<TemperatureDTO>? {
        val queryString = "SELECT \"temperature\" FROM \"feinstaub\" WHERE time > now() - 1h"
        return queryAndReturnStream(queryString)
                .map { v -> TemperatureDTO(v[0] as String, v[1] as Double) }
                .collect(Collectors.toList())
    }

    fun queryHumidityInDateRange(startDate: Date, endDate: Date): MutableList<TemperatureDTO>? {
        val queryString = "SELECT \"humidity\" FROM \"feinstaub\" WHERE time > now() - 1h"
        return queryAndReturnStream(queryString)
                .map { v -> TemperatureDTO(v[0] as String, v[1] as Double) }
                .collect(Collectors.toList())
    }

    fun queryPmInDateRange(startDate: Date, endDate: Date): MutableList<PmDTO>? {
        val queryString = "SELECT \"SDS_P1\", \"SDS_P2\" FROM \"feinstaub\" WHERE time > now() - 1h"
        return queryAndReturnStream(queryString)
                .map { v -> PmDTO(Pm1DTO(v[0] as String, v[1] as Double), Pm2DTO(v[0] as String, v[2] as Double)) }
                .collect(Collectors.toList())
    }

    fun queryLastTemperature(): Optional<TemperatureDTO>? {
        return queryAndReturnStream(lastTemperatureQuery)
                .findFirst()
                .map { v -> TemperatureDTO(v[0] as String, v[1] as Double) }
    }

    fun queryLastHumidity(): Optional<HumidityDTO>? {
        return queryAndReturnStream(lastHumidityQuery)
                .findFirst()
                .map { v -> HumidityDTO(v[0] as String, v[1] as Double) }
    }

    private fun queryAndReturnStream(queryString: String): Stream<MutableList<Any>> {
        return query(queryString)
                .results[0]
                .series[0]
                .values
                .stream()
    }

    private fun query(queryString: String): QueryResult = influxDBTemplate!!
            .connection
            .query(Query(queryString, database))
}
