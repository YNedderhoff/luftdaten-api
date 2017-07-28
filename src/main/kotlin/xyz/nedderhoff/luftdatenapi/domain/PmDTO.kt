package xyz.nedderhoff.luftdatenapi.domain

import java.time.LocalDate

data class PmDTO(val date: LocalDate?, val pm1: Double, val pm2: Double)