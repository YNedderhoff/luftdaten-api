package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Pong
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.domain.ResponseDTO
import xyz.nedderhoff.luftdatenapi.domain.SeriesDTO
import xyz.nedderhoff.luftdatenapi.helper.DateHelper
import xyz.nedderhoff.luftdatenapi.presenter.LuftdatenPresenter
import xyz.nedderhoff.luftdatenapi.repository.LuftdatenRepository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.function.Supplier
import java.util.stream.Collectors


@Service
class LuftdatenService(val presenter: LuftdatenPresenter,
                       val repository: LuftdatenRepository,
                       val dateHelper: DateHelper,
                       val executorService: ExecutorService) {

    private val luftdatenSeries = "feinstaub"
    private val temperatureSeries = "temperature"
    private val humiditySeries = "humidity"
    private val pm1Series = "SDS_P1"
    private val pm2Series = "SDS_P2"

    private val selectClause = "SELECT %s FROM \"$luftdatenSeries\" "
    private val whereClause = " WHERE time > now() - 3d "
    private val groupClause = " GROUP BY time(1h) "

    private val lastTemperatureQuery = String.format(selectClause, "last(\"$temperatureSeries\")")
    private val lastHumidityQuery = String.format(selectClause, "last(\"$humiditySeries\")")
    private val lastPm1Query = String.format(selectClause, "last(\"$pm1Series\")")
    private val lastPm2Query = String.format(selectClause, "last(\"$pm2Series\")")

    private val temperatureSeriesQuery = String.format(selectClause, "mean(\"$temperatureSeries\")") + whereClause + groupClause
    private val humiditySeriesQuery = String.format(selectClause, "mean(\"$humiditySeries\")") + whereClause + groupClause
    private val pm1SeriesQuery = String.format(selectClause, "mean(\"$pm1Series\")") + whereClause + groupClause
    private val pm2SeriesQuery = String.format(selectClause, "mean(\"$pm2Series\")") + whereClause + groupClause

    private val temperatureColumns = mutableListOf("time", "temperature")
    private val humidityColumns = mutableListOf("time", "humidity")
    private val pm1Columns = mutableListOf("time", "PM10")
    private val pm2Columns = mutableListOf("time", "PM2.5 (µm)")

    private val temperatureColour = "#FF4500"
    private val humidityColour = "#ADFF2F"
    private val pm1Colour = "#FFFF00"
    private val pm2Colour = "#00BFFF"


    fun ping(): Pong = repository.ping()

    fun queryLastMeasurements(): MutableList<Any> {
        val temperatureSupplier = Supplier { queryLastValue(lastTemperatureQuery, presenter::toTemperatureDTO) }
        val humiditySupplier = Supplier { queryLastValue(lastHumidityQuery, presenter::toHumidityDto) }
        val pm1Supplier = Supplier { queryLastValue(lastPm1Query, presenter::toPm1DTO) }
        val pm2Supplier = Supplier { queryLastValue(lastPm2Query, presenter::toPm2DTO) }

        val temperatureResultsFuture = CompletableFuture.supplyAsync(temperatureSupplier, executorService)
        val humidityResultsFuture = CompletableFuture.supplyAsync(humiditySupplier, executorService)
        val pm1ResultsFuture = CompletableFuture.supplyAsync(pm1Supplier, executorService)
        val pm2ResultsFuture = CompletableFuture.supplyAsync(pm2Supplier, executorService)

        return mutableListOf(temperatureResultsFuture.get(), humidityResultsFuture.get(), pm1ResultsFuture.get(), pm2ResultsFuture.get())
    }

    fun queryLastTemperature(): Any = queryLastValue(lastTemperatureQuery, presenter::toTemperatureDTO)

    fun queryLastHumidity(): Any = queryLastValue(lastHumidityQuery, presenter::toHumidityDto)

    fun queryLastPm1(): Any = queryLastValue(lastPm1Query, presenter::toPm1DTO)

    fun queryLastPm2(): Any = queryLastValue(lastPm2Query, presenter::toPm2DTO)

    fun queryTemperatureSeries(): ResponseDTO {
        val supplier = Supplier { querySeries(temperatureSeriesQuery, temperatureSeries, temperatureColour, temperatureColumns) }
        val resultsFuture = CompletableFuture.supplyAsync(supplier, executorService)
        return ResponseDTO(resultsFuture.get())
    }

    fun queryHumiditySeries(): ResponseDTO {
        val supplier = Supplier { querySeries(humiditySeriesQuery, humiditySeries, humidityColour, humidityColumns) }
        val resultsFuture = CompletableFuture.supplyAsync(supplier, executorService)
        return ResponseDTO(resultsFuture.get())
    }

    fun queryPmSeries(): ResponseDTO {
        val pm1Supplier = Supplier { querySeries(pm1SeriesQuery, pm1Series, pm1Colour, pm1Columns) }
        val pm2Supplier = Supplier { querySeries(pm2SeriesQuery, pm2Series, pm2Colour, pm2Columns) }

        val pm1ResultsFuture = CompletableFuture.supplyAsync(pm1Supplier, executorService)
        val pm2ResultsFuture = CompletableFuture.supplyAsync(pm2Supplier, executorService)

        return ResponseDTO(pm1ResultsFuture.get()
                .toSet()
                .union(pm2ResultsFuture.get())
                .toMutableList())
    }

    private fun querySeries(query: String, id: String, colour: String, columns: MutableList<String>): MutableList<SeriesDTO> =
            repository.query(query)
                    .results[0]
                    .series
                    .stream()
                    .map { SeriesDTO(id, it.name, colour, columns, formatValues(it.values)) }
                    .collect(Collectors.toList())

    private fun queryLastValue(query: String, mappingFunction: (MutableList<Any>) -> Any): Any =
            repository.query(query)
                    .results[0]
                    .series[0]
                    .values
                    .stream()
                    .findFirst()
                    .map { mappingFunction(it) }

    private fun formatValues(values: List<MutableList<Any>>): MutableList<MutableList<Any>> = values
            .stream()
            .map { mutableListOf(dateHelper.formatDate(it[0] as String), it[1]) }
            .collect(Collectors.toList())
}
