package xyz.nedderhoff.luftdatenapi.domain

import java.time.LocalDate

data class HumidityDTO(val date: LocalDate?, val humidity: Double)