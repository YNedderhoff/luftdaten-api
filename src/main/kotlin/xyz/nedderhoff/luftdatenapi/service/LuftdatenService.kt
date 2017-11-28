package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Pong
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import xyz.nedderhoff.luftdatenapi.domain.LastMeasurementsResponseDTO
import xyz.nedderhoff.luftdatenapi.domain.SeriesDTO
import xyz.nedderhoff.luftdatenapi.domain.SeriesResponseDTO
import xyz.nedderhoff.luftdatenapi.helper.DateHelper
import xyz.nedderhoff.luftdatenapi.repository.LuftdatenRepository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.ws.rs.NotFoundException


@Service
class LuftdatenService(val repository: LuftdatenRepository,
                       val dateHelper: DateHelper,
                       val executorService: ExecutorService) {

    private val logger = LoggerFactory.getLogger(LuftdatenService::class.java)!!

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

    private val temperatureLabel = "Temperature (°C)"
    private val humidityLabel = "Humidity (%)"
    private val pm1Label = "PM10 (µm)"
    private val pm2Label = "PM2.5 (µm)"

    private val temperatureColumns = mutableListOf("time", temperatureLabel)
    private val humidityColumns = mutableListOf("time", humidityLabel)
    private val pm1Columns = mutableListOf("time", pm1Label)
    private val pm2Columns = mutableListOf("time", pm2Label)

    private val temperatureColour = "#FF4500"
    private val humidityColour = "#ADFF2F"
    private val pm1Colour = "#FFFF00"
    private val pm2Colour = "#00BFFF"


    fun ping(): Pong = repository.ping()

    fun queryLastMeasurements(): MutableList<LastMeasurementsResponseDTO> {
        val temperatureResultsFuture = supplyLastMeasurementFuture(Supplier { queryLastTemperature() })
        val humidityResultsFuture = supplyLastMeasurementFuture(Supplier { queryLastHumidity() })
        val pm1ResultsFuture = supplyLastMeasurementFuture(Supplier { queryLastPm1() })
        val pm2ResultsFuture = supplyLastMeasurementFuture(Supplier { queryLastPm2() })

        return mutableListOf(
                temperatureResultsFuture.get(),
                humidityResultsFuture.get(),
                pm1ResultsFuture.get(),
                pm2ResultsFuture.get())
    }

    fun queryLastTemperature() = queryLastValue(lastTemperatureQuery, temperatureLabel)

    fun queryLastHumidity() = queryLastValue(lastHumidityQuery, humidityLabel)

    fun queryLastPm1() = queryLastValue(lastPm1Query, pm1Label)

    fun queryLastPm2() = queryLastValue(lastPm2Query, pm2Label)

    fun queryTemperatureSeries(): SeriesResponseDTO {
        val supplier = Supplier { querySeries(temperatureSeriesQuery, temperatureSeries, temperatureColour, temperatureColumns) }
        return SeriesResponseDTO(supplySeriesFuture(supplier).get())
    }

    fun queryHumiditySeries(): SeriesResponseDTO {
        val supplier = Supplier { querySeries(humiditySeriesQuery, humiditySeries, humidityColour, humidityColumns) }
        return SeriesResponseDTO(supplySeriesFuture(supplier).get())
    }

    fun queryPmSeries(): SeriesResponseDTO {
        val pm1Supplier = Supplier { querySeries(pm1SeriesQuery, pm1Series, pm1Colour, pm1Columns) }
        val pm2Supplier = Supplier { querySeries(pm2SeriesQuery, pm2Series, pm2Colour, pm2Columns) }

        val pm1ResultsFuture = supplySeriesFuture(pm1Supplier)
        val pm2ResultsFuture = supplySeriesFuture(pm2Supplier)

        return SeriesResponseDTO(pm1ResultsFuture.get()
                .toSet()
                .union(pm2ResultsFuture.get())
                .toMutableList())
    }

    private fun querySeries(query: String, id: String, colour: String, columns: MutableList<String>): MutableList<SeriesDTO> {
        logger.info("Query: {}", query)
        return repository.query(query)
                .results[0]
                .series
                .stream()
                .map { SeriesDTO(id, it.name, colour, columns, formatValues(it.values)) }
                .collect(Collectors.toList())
    }

    private fun queryLastValue(query: String, label: String): LastMeasurementsResponseDTO {
        logger.info("Query: {}", query)
        return repository.query(query)
                .results[0]
                .series[0]
                .values
                .stream()
                .findFirst()
                .map { LastMeasurementsResponseDTO(formatDate(it[0]), label, it[1]) }
                .orElseThrow { NotFoundException("Error retrieving last value of $label") }
    }

    private fun formatValues(values: List<MutableList<Any>>): MutableList<MutableList<Any>> = values
            .stream()
            .map { mutableListOf(formatDate(it[0]), it[1]) }
            .collect(Collectors.toList())

    private fun formatDate(date: Any) = dateHelper.formatDate(date as String)

    private fun supplyLastMeasurementFuture(pm2Supplier: Supplier<LastMeasurementsResponseDTO>) = CompletableFuture
            .supplyAsync(pm2Supplier, executorService)
            .exceptionally {
                logger.error("An unexpected error occurred", it)
                throw it
            }

    private fun supplySeriesFuture(pm2Supplier: Supplier<MutableList<SeriesDTO>>) = CompletableFuture
            .supplyAsync(pm2Supplier, executorService)
            .exceptionally {
                logger.error("An unexpected error occurred", it)
                throw it
            }
}
