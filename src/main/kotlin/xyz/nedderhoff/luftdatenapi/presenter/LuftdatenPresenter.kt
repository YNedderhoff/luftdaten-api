package xyz.nedderhoff.luftdatenapi.presenter

import org.springframework.stereotype.Component
import xyz.nedderhoff.luftdatenapi.domain.LastMeasurementsResponseDTO
import xyz.nedderhoff.luftdatenapi.helper.DateHelper

@Component
open class LuftdatenPresenter(val dateHelper: DateHelper) {

    fun toTemperatureDTO(v: MutableList<Any>) =
            LastMeasurementsResponseDTO(formatDate(v[0]), "Temperature (°C)", v[1])

    fun toHumidityDto(v: MutableList<Any>) =
            LastMeasurementsResponseDTO(formatDate(v[0]), "Humidity (%)", v[1])

    fun toPm1DTO(v: MutableList<Any>) =
            LastMeasurementsResponseDTO(formatDate(v[0]), "PM10 (µm)", v[1])

    fun toPm2DTO(v: MutableList<Any>) =
            LastMeasurementsResponseDTO(formatDate(v[0]), "PM2.5 (µm)", v[1])

    private fun formatDate(date: Any) = dateHelper.formatDate(date as String)

}
