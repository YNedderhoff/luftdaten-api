package xyz.nedderhoff.luftdatenapi.service

import com.google.common.base.Preconditions
import org.influxdb.dto.Point
import org.influxdb.dto.Pong
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.domain.HumidityDTO
import xyz.nedderhoff.luftdatenapi.domain.PmDTO
import xyz.nedderhoff.luftdatenapi.domain.TemperatureDTO
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture


@Service
class LuftdatenService {

    @Autowired
    private val influxDBTemplate: InfluxDBTemplate<Point>? = null

    private val database = "pm_temp_hum"
    private val luftdatenSeries = "feinstaub"
    private val selectPlaceholder = "SELECT last(\"%s\") FROM \"$luftdatenSeries\" "
    private val lastTemperatureQuery = String.format(selectPlaceholder, "temperature")
    private val lastHumidityQuery = String.format(selectPlaceholder, "humidity")
    private val lastPm1Query = String.format(selectPlaceholder, "SDS_P1")
    private val lastPm2Query = String.format(selectPlaceholder, "SDS_P2")


    fun ping(): Pong = influxDBTemplate!!.ping()

    fun queryLastTemperature(): TemperatureDTO {
        val (date, value) = getValues(lastTemperatureQuery)
        return TemperatureDTO(date, value)
    }

    fun queryLastHumidity(): HumidityDTO {
        val (date, value) = getValues(lastHumidityQuery)
        return HumidityDTO(date, value)
    }

    fun queryLastPm(): PmDTO {

        val async1 = CompletableFuture.supplyAsync({ getValues(lastPm1Query) })
        val async2 = CompletableFuture.supplyAsync({ getValues(lastPm2Query) })
        CompletableFuture.allOf(async1, async2)

        val (date1, value1) = async1.get()
        val (_, value2) = async2.get()
        return PmDTO(date1, value1, value2)
    }

    private fun query(queryString: String): QueryResult = influxDBTemplate!!
            .connection
            .query(Query(queryString, database))

    private fun getValues(queryString: String): Pair<String, Double> {
        val queryResult = query(queryString)
        val values = getValidValues(queryResult)
        val date = getDate(values[0] as String)
        val value = values[1] as Double
        return Pair(date, value)
    }

    private fun getValidValues(queryResult: QueryResult): MutableList<Any> {
        Preconditions.checkState(!queryResult.hasError())
        val results = queryResult.results
        Preconditions.checkState(results.size == 1)
        val series = results[0].series
        Preconditions.checkState(series.size == 1)
        Preconditions.checkState(series[0].values.size == 1)
        val values = series[0].values
        Preconditions.checkState(values[0].size == 2)
        return values[0]
    }

    private fun getDate(dateString: String): String {
        val instant = Instant.parse(dateString)
        val zonedDateTime = instant.atZone(ZoneId.of(ZoneOffset.UTC.id))
        return zonedDateTime.format(DateTimeFormatter.ISO_INSTANT)
    }
}
