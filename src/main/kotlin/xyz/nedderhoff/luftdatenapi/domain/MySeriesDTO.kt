package xyz.nedderhoff.luftdatenapi.domain

data class MySeriesDTO(val id: String, val name: String, val colour: String, val axis: MutableList<String>, val values: MutableList<MutableList<Any>>)