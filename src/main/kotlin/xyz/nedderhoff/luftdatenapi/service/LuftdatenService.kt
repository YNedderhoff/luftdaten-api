package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Pong
import org.influxdb.dto.QueryResult
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.domain.MyResponseDTO
import xyz.nedderhoff.luftdatenapi.domain.MySeriesDTO
import xyz.nedderhoff.luftdatenapi.presenter.LuftdatenPresenter
import xyz.nedderhoff.luftdatenapi.repository.LuftdatenRepository
import java.util.Optional
import java.util.stream.Collectors


@Service
class LuftdatenService(val presenter: LuftdatenPresenter, val repository: LuftdatenRepository) {

    private val luftdatenSeries = "feinstaub"
    private val selectClause = "SELECT %s FROM \"$luftdatenSeries\" "
    private val dateRangeQuery = " WHERE time > now() - 3d "
    private val groupQuery = " GROUP BY time(1h) "
    private val lastTemperatureQuery = String.format(selectClause, "last(\"temperature\")")
    private val lastHumidityQuery = String.format(selectClause, "last(\"humidity\")")
    private val lastPmQuery = String.format(selectClause, "last(\"SDS_P1\"), last(\"SDS_P2\")")
    private val temperatureInDateRangeQuery = String.format(selectClause, "mean(\"temperature\")") + dateRangeQuery + groupQuery
    private val humidityInDateRangeQuery = String.format(selectClause, "mean(\"humidity\")") + dateRangeQuery + groupQuery
    private val pmInDateRangeQuery = String.format(selectClause, "\"SDS_P1\", \"SDS_P2\"") + dateRangeQuery
    private val pm1InDateRangeQuery = String.format(selectClause, "mean(\"SDS_P1\")") + dateRangeQuery + groupQuery
    private val pm2InDateRangeQuery = String.format(selectClause, "mean(\"SDS_P2\")") + dateRangeQuery + groupQuery


    fun ping(): Pong = repository.ping()

    fun queryTemperatureInDateRange(): MutableList<Any>? =
            queryList(temperatureInDateRangeQuery, presenter::toTemperatureDTO)

    fun queryHumidityInDateRange(): MutableList<Any>? =
            queryList(humidityInDateRangeQuery, presenter::toHumidityDto)

    fun queryPmInDateRange(): MutableList<Any>? =
            queryList(pmInDateRangeQuery, presenter::toPmDTO)

    fun queryLastTemperature(): Optional<Any>? = querySingle(lastTemperatureQuery, presenter::toTemperatureDTO)

    fun queryLastHumidity(): Optional<Any>? = querySingle(lastHumidityQuery, presenter::toHumidityDto)

    fun queryLastPm(): Optional<Any>? = querySingle(lastPmQuery, presenter::toPmDTO)

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
            repository.query(queryString)
                    .results[0]
                    .series[0]
                    .values

    fun queryTemperatureInDateRangeAndReturnSeries(): MyResponseDTO =
            MyResponseDTO(queryAndReturnSeries(temperatureInDateRangeQuery, "temperature", "#FF4500", mutableListOf("time", "temperature")))

    fun queryHumidityInDateRangeAndReturnSeries(): MyResponseDTO =
            MyResponseDTO(queryAndReturnSeries(humidityInDateRangeQuery, "humidity", "#ADFF2F", mutableListOf("time", "humidity")))

    //TODO use futures
    fun queryPmInDateRangeAndReturnSeries(): MyResponseDTO =
            MyResponseDTO(queryAndReturnSeries(pm1InDateRangeQuery, "pm10", "#FFFF00", mutableListOf("time", "PM10"))
                    .toSet()
                    .union(queryAndReturnSeries(pm2InDateRangeQuery, "pm2.5", "#00BFFF", mutableListOf("time", "PM2.5")))
                    .toMutableList())

    private fun queryAndReturnSeries(queryString: String, id: String, colour: String, columns: MutableList<String>): MutableList<MySeriesDTO> {
        val query = repository.query(queryString)
        return query
                .results[0]
                .series
                .stream()
                .map { s -> MySeriesDTO(id, s.name, colour, columns, s.values) }
                .collect(Collectors.toList())
    }
}
