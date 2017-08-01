package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Pong
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.domain.ResponseDTO
import xyz.nedderhoff.luftdatenapi.domain.SeriesDTO
import xyz.nedderhoff.luftdatenapi.presenter.LuftdatenPresenter
import xyz.nedderhoff.luftdatenapi.repository.LuftdatenRepository
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.format.FormatStyle
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.function.Supplier
import java.util.stream.Collectors


@Service
class LuftdatenService(val presenter: LuftdatenPresenter,
                       val repository: LuftdatenRepository,
                       val executorService: ExecutorService) {

    private val luftdatenSeries = "feinstaub"
    private val temperatureSeries = "temperature"
    private val humiditySeries = "humidity"
    private val pm1Series = "SDS_P1"
    private val pm2Series = "SDS_P2"

    private val selectClause = "SELECT %s FROM \"$luftdatenSeries\" "
    private val whereClause = " WHERE time > now() - 3d "
    private val groupClause = " GROUP BY time(1h) "

    private val lastTemperatureQuery = String.format(selectClause, "last(\"$temperatureSeries\"")
    private val lastHumidityQuery = String.format(selectClause, "last(\"$humiditySeries\")")
    private val lastPmQuery = String.format(selectClause, "last(\"$pm1Series\"), last(\"$pm2Series\")")

    private val temperatureSeriesQuery = String.format(selectClause, "mean(\"$temperatureSeries\")") + whereClause + groupClause
    private val humiditySeriesQuery = String.format(selectClause, "mean(\"$humiditySeries\")") + whereClause + groupClause
    private val pm1SeriesQuery = String.format(selectClause, "mean(\"$pm1Series\")") + whereClause + groupClause
    private val pm2SeriesQuery = String.format(selectClause, "mean(\"$pm2Series\")") + whereClause + groupClause

    private val temperatureColumns = mutableListOf("time", "temperature")
    private val humidityColumns = mutableListOf("time", "humidity")
    private val pm1Columns = mutableListOf("time", "PM10")
    private val pm2Columns = mutableListOf("time", "PM2.5")

    private val temperatureColour = "#FF4500"
    private val humidityColour = "#ADFF2F"
    private val pm1Colour = "#FFFF00"
    private val pm2Colour = "#00BFFF"


    fun ping(): Pong = repository.ping()

    fun queryLastTemperature(): Optional<Any>? = queryLastValue(lastTemperatureQuery, presenter::toTemperatureDTO)

    fun queryLastHumidity(): Optional<Any>? = queryLastValue(lastHumidityQuery, presenter::toHumidityDto)

    fun queryLastPm(): Optional<Any>? = queryLastValue(lastPmQuery, presenter::toPmDTO)

    fun queryTemperatureSeries(): ResponseDTO =
            ResponseDTO(querySeries(temperatureSeriesQuery, temperatureSeries, temperatureColour, temperatureColumns))

    fun queryHumiditySeries(): ResponseDTO =
            ResponseDTO(querySeries(humiditySeriesQuery, humiditySeries, humidityColour, humidityColumns))

    fun queryPmSeries(): ResponseDTO {
        val pm1Supplier = Supplier { querySeries(pm1SeriesQuery, pm1Series, pm1Colour, pm1Columns) }
        val pm2Supplier = Supplier { querySeries(pm2SeriesQuery, pm2Series, pm2Colour, pm2Columns) }

        val pm1Results = CompletableFuture.supplyAsync(pm1Supplier, executorService)
        val pm2Results = CompletableFuture.supplyAsync(pm2Supplier, executorService)

        return ResponseDTO(pm1Results.get()
                .toSet()
                .union(pm2Results.get())
                .toMutableList())
    }

    private fun querySeries(query: String, id: String, colour: String, columns: MutableList<String>): MutableList<SeriesDTO> =
            repository.query(query)
                    .results[0]
                    .series
                    .stream()
                    .map { SeriesDTO(id, it.name, colour, columns, formatValues(it.values)) }
                    .collect(Collectors.toList())

    private fun queryLastValue(query: String, mappingFunction: (MutableList<Any>) -> Any): Optional<Any>? =
            repository.query(query)
                    .results[0]
                    .series[0]
                    .values
                    .stream()
                    .findFirst()
                    .map { mappingFunction(it) }

    private fun formatValues(values: List<MutableList<Any>>): MutableList<MutableList<Any>> = values
            .stream()
            .map { mutableListOf(formatDate(it[0] as String), it[1]) }
            .collect(Collectors.toList())

    private fun formatDate(dateString: String): String = Instant
            .parse(dateString)
            .atZone(ZoneId.of(ZoneOffset.UTC.id))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmZ"))
}
