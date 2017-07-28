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
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset


@Service
class LuftdatenService {

    @Autowired
    private val influxDBTemplate: InfluxDBTemplate<Point>? = null

    private val database: String = "pm_temp_hum"

    fun ping(): Pong? {
        return influxDBTemplate!!.ping()
    }

    //TODO make all this async

    fun queryLastTemperature(): TemperatureDTO {
        val queryString = "SELECT last(\"temperature\") FROM \"feinstaub\" "
        val (date, value) = getValues(queryString)
        return TemperatureDTO(date, value)
    }

    fun queryLastHumidity(): HumidityDTO {
        val queryString = "SELECT last(\"humidity\") FROM \"feinstaub\" "
        val (date, value) = getValues(queryString)
        return HumidityDTO(date, value)
    }

    fun queryLastPm(): PmDTO {
        val queryString1 = "SELECT last(\"SDS_P1\") FROM \"feinstaub\" "
        val queryString2 = "SELECT last(\"SDS_P2\") FROM \"feinstaub\" "
        val (date1, value1) = getValues(queryString1)
        val (_, value2) = getValues(queryString2)
        return PmDTO(date1, value1, value2)
    }

    private fun getValues(queryString: String): Pair<LocalDate?, Double> {
        val queryResult = query(queryString)
        val values = getValidValues(queryResult)
        val date = getDate(values[0] as String)
        val value = values[1] as Double
        return Pair(date, value)
    }

    fun query(queryString: String): QueryResult = influxDBTemplate!!.connection.query(Query(queryString, database))

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

    private fun getDate(dateString: String): LocalDate? {
        val instant = Instant.parse(dateString)
        val zonedDateTime = instant.atZone(ZoneId.of(ZoneOffset.UTC.id))
        return zonedDateTime.toLocalDate()
    }
}
