package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Pong
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.presenter.LuftdatenPresenter
import xyz.nedderhoff.luftdatenapi.repository.LuftdatenRepository
import java.util.*
import java.util.stream.Collectors


@Service
class LuftdatenService(val presenter: LuftdatenPresenter, val repository: LuftdatenRepository) {

    private val luftdatenSeries = "feinstaub"
    private val selectClause = "SELECT %s FROM \"$luftdatenSeries\" "
    private val dateRangeQuery = " WHERE time > now() - 1h "
    private val lastTemperatureQuery = String.format(selectClause, "last(\"temperature\")")
    private val lastHumidityQuery = String.format(selectClause, "last(\"humidity\")")
    private val lastPmQuery = String.format(selectClause, "last(\"SDS_P1\"), last(\"SDS_P2\")")
    private val temperatureInDateRangeQuery = String.format(selectClause, "\"temperature\"") + dateRangeQuery
    private val humidityInDateRangeQuery = String.format(selectClause, "\"humidity\"") + dateRangeQuery
    private val pmInDateRangeQuery = String.format(selectClause, "\"SDS_P1\", \"SDS_P2\"") + dateRangeQuery


    fun ping(): Pong = repository.ping()

    fun queryTemperatureInDateRange(startDate: Date, endDate: Date): MutableList<Any>? =
            queryList(temperatureInDateRangeQuery, presenter::toTemperatureDTO)

    fun queryHumidityInDateRange(startDate: Date, endDate: Date): MutableList<Any>? =
            queryList(humidityInDateRangeQuery, presenter::toHumidityDto)

    fun queryPmInDateRange(startDate: Date, endDate: Date): MutableList<Any>? =
            queryList(pmInDateRangeQuery, presenter::toPmDTO)

    fun queryLastTemperature(): Optional<Any>? = querySingle(lastTemperatureQuery, presenter::toTemperatureDTO)

    fun queryLastHumidity(): Optional<Any>? = querySingle(lastHumidityQuery, presenter::toHumidityDto)

    fun queryLastPm(): Optional<Any>? = querySingle(lastPmQuery, presenter::toPmDTO)

    fun querySingle(query: String, mappingFunction: (MutableList<Any>) -> Any): Optional<Any>? =
            queryAndReturnValues(query)
                    .stream()
                    .findFirst()
                    .map { mappingFunction(it) }

    fun queryList(query: String, mappingFunction: (MutableList<Any>) -> Any): MutableList<Any>? =
            queryAndReturnValues(query)
                    .stream()
                    .map { mappingFunction(it) }
                    .collect(Collectors.toList())

    private fun queryAndReturnValues(queryString: String): MutableList<MutableList<Any>> =
            repository.query(queryString)
                    .results[0]
                    .series[0]
                    .values
}
