package xyz.nedderhoff.luftdatenapi.domain

import java.time.LocalDate

data class TemperatureDTO(val date: LocalDate?, val temperature: Double)