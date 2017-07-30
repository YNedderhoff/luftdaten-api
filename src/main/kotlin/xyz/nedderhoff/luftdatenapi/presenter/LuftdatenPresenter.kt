package xyz.nedderhoff.luftdatenapi.presenter

import org.springframework.stereotype.Component
import xyz.nedderhoff.luftdatenapi.domain.HumidityDTO
import xyz.nedderhoff.luftdatenapi.domain.Pm1DTO
import xyz.nedderhoff.luftdatenapi.domain.Pm2DTO
import xyz.nedderhoff.luftdatenapi.domain.PmDTO
import xyz.nedderhoff.luftdatenapi.domain.TemperatureDTO

@Component
class LuftdatenPresenter {

    fun toTemperatureDTO(v: MutableList<Any>) = TemperatureDTO(v[0] as String, v[1] as Double)
    fun toHumidityDto(v: MutableList<Any>) = HumidityDTO(v[0] as String, v[1] as Double)
    fun toPmDTO(v: MutableList<Any>) =
            PmDTO(Pm1DTO(v[0] as String, v[1] as Double), Pm2DTO(v[0] as String, v[2] as Double))
}