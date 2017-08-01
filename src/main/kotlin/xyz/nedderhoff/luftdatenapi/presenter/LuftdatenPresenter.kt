package xyz.nedderhoff.luftdatenapi.presenter

import org.springframework.stereotype.Component
import xyz.nedderhoff.luftdatenapi.domain.HumidityDTO
import xyz.nedderhoff.luftdatenapi.domain.Pm1DTO
import xyz.nedderhoff.luftdatenapi.domain.Pm2DTO
import xyz.nedderhoff.luftdatenapi.domain.TemperatureDTO
import xyz.nedderhoff.luftdatenapi.helper.DateHelper

@Component
open class LuftdatenPresenter(val dateHelper: DateHelper) {

    fun toTemperatureDTO(v: MutableList<Any>) = TemperatureDTO(dateHelper.formatDate(v[0] as String), "Temperature (°C)", v[1])
    fun toHumidityDto(v: MutableList<Any>) = HumidityDTO(dateHelper.formatDate(v[0] as String), "Humidity (%)", v[1])
    fun toPm1DTO(v: MutableList<Any>) = Pm1DTO(dateHelper.formatDate(v[0] as String), "PM10 (µm)", v[1] as Double)
    fun toPm2DTO(v: MutableList<Any>) = Pm2DTO(dateHelper.formatDate(v[0] as String), "PM2.5 (µm)", v[1] as Double)
}