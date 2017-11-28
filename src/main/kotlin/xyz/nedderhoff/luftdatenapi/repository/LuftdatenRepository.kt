package xyz.nedderhoff.luftdatenapi.repository

import org.influxdb.dto.Point
import org.influxdb.dto.Pong
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.stereotype.Component

@Component
open class LuftdatenRepository(val influxDBTemplate: InfluxDBTemplate<Point>?) {

    private val database = "feinstaub"

    fun ping(): Pong = influxDBTemplate!!.ping()

    fun query(queryString: String): QueryResult = influxDBTemplate!!
            .connection
            .query(Query(queryString, database))
}