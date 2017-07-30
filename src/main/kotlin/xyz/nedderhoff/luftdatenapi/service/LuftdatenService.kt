package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Point
import org.influxdb.dto.Pong
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.presenter.LuftdatenPresenter
import java.util.*
import java.util.stream.Collectors


@Service
class LuftdatenService(val influxDBTemplate: InfluxDBTemplate<Point>?, val luftdatenPresenter: LuftdatenPresenter) {

    private val database = "pm_temp_hum"
    private val luftdatenSeries = "feinstaub"
    private val selectClause = "SELECT %s FROM \"$luftdatenSeries\" "
    private val dateRangeQuery = " WHERE time > now() - 1h "
    private val lastTemperatureQuery = String.format(selectClause, "last(\"temperature\")")
    private val lastHumidityQuery = String.format(selectClause, "last(\"humidity\")")
    private val lastPmQuery = String.format(selectClause, "last(\"SDS_P1\"), last(\"SDS_P2\")")
    private val temperatureInDateRangeQuery = String.format(selectClause, "\"temperature\"") + dateRangeQuery
    private val humidityInDateRangeQuery = String.format(selectClause, "\"humidity\"") + dateRangeQuery
    private val pmInDateRangeQuery = String.format(selectClause, "\"SDS_P1\", \"SDS_P2\"") + dateRangeQuery


    fun ping(): Pong = influxDBTemplate!!.ping()

    fun queryTemperatureInDateRange(startDate: Date, endDate: Date): MutableList<Any>? =
            queryList(temperatureInDateRangeQuery, luftdatenPresenter::toTemperatureDTO)

    fun queryHumidityInDateRange(startDate: Date, endDate: Date): MutableList<Any>? =
            queryList(humidityInDateRangeQuery, luftdatenPresenter::toHumidityDto)

    fun queryPmInDateRange(startDate: Date, endDate: Date): MutableList<Any>? =
            queryList(pmInDateRangeQuery, luftdatenPresenter::toPmDTO)

    fun queryLastTemperature(): Optional<Any>? = querySingle(lastTemperatureQuery, luftdatenPresenter::toTemperatureDTO)

    fun queryLastHumidity(): Optional<Any>? = querySingle(lastHumidityQuery, luftdatenPresenter::toHumidityDto)

    fun queryLastPm(): Optional<Any>? = querySingle(lastPmQuery, luftdatenPresenter::toPmDTO)

    private fun querySingle(query: String, mappingFunction: (MutableList<Any>) -> Any): Optional<Any>? =
            queryAndReturnValues(query)
                    .stream()
                    .findFirst()
                    .map { mappingFunction(it) }

    private fun queryList(query: String, mappingFunction: (MutableList<Any>) -> Any): MutableList<Any>? =
            queryAndReturnValues(query)
                    .stream()
                    .map { mappingFunction(it) }
                    .collect(Collectors.toList())

    private fun queryAndReturnValues(queryString: String): MutableList<MutableList<Any>> =
            query(queryString)
                    .results[0]
                    .series[0]
                    .values

    private fun query(queryString: String): QueryResult = influxDBTemplate!!
            .connection
            .query(Query(queryString, database))
}
