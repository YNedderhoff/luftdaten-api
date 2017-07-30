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


@Service
class LuftdatenService {

    @Autowired
    private val influxDBTemplate: InfluxDBTemplate<Point>? = null

    private val database = "pm_temp_hum"
    private val luftdatenSeries = "feinstaub"
    private val selectLastValuePlaceholder = "SELECT %s FROM \"$luftdatenSeries\" "
    private val dateRangeQuery = " WHERE time > now() - 1h "
    private val lastTemperatureQuery = String.format(selectLastValuePlaceholder, "last(\"temperature\")")
    private val lastHumidityQuery = String.format(selectLastValuePlaceholder, "last(\"humidity\")")
    private val temperatureInDateRangeQuery = String.format(selectLastValuePlaceholder, "\"temperature\"") + dateRangeQuery
    private val humidityInDateRangeQuery = String.format(selectLastValuePlaceholder, "\"humidity\"") + dateRangeQuery
    private val pmInDateRangeQuery = String.format(selectLastValuePlaceholder, "\"SDS_P1\", \"SDS_P2\"") + dateRangeQuery


    fun ping(): Pong = influxDBTemplate!!.ping()

    fun queryTemperatureInDateRange(startDate: Date, endDate: Date): MutableList<TemperatureDTO>? {
        return queryAndReturnValues(temperatureInDateRangeQuery)
                .stream()
                .map { toTemperatureDTO(it) }
                .collect(Collectors.toList())
    }

    fun queryHumidityInDateRange(startDate: Date, endDate: Date): MutableList<HumidityDTO>? {
        return queryAndReturnValues(humidityInDateRangeQuery)
                .stream()
                .map { toHumidityDto(it) }
                .collect(Collectors.toList())
    }


    fun queryPmInDateRange(startDate: Date, endDate: Date): MutableList<PmDTO>? {
        return queryAndReturnValues(pmInDateRangeQuery)
                .stream()
                .map { toPmDTO(it) }
                .collect(Collectors.toList())
    }


    fun queryLastTemperature(): Optional<TemperatureDTO>? {
        return queryAndReturnValues(lastTemperatureQuery)
                .stream()
                .findFirst()
                .map { toTemperatureDTO(it) }
    }

    fun queryLastHumidity(): Optional<HumidityDTO>? {
        return queryAndReturnValues(lastHumidityQuery)
                .stream()
                .findFirst()
                .map { toHumidityDto(it) }
    }

    private fun queryAndReturnValues(queryString: String): MutableList<MutableList<Any>> {
        return query(queryString)
                .results[0]
                .series[0]
                .values
    }

    private fun query(queryString: String): QueryResult = influxDBTemplate!!
            .connection
            .query(Query(queryString, database))

    private fun toTemperatureDTO(v: MutableList<Any>) = TemperatureDTO(v[0] as String, v[1] as Double)
    private fun toHumidityDto(v: MutableList<Any>) = HumidityDTO(v[0] as String, v[1] as Double)
    private fun toPmDTO(v: MutableList<Any>) =
            PmDTO(Pm1DTO(v[0] as String, v[1] as Double), Pm2DTO(v[0] as String, v[2] as Double))
}
