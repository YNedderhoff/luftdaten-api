package xyz.nedderhoff.luftdatenapi.service

import org.influxdb.dto.Point
import org.influxdb.dto.Pong
import org.influxdb.dto.Query
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.stereotype.Service

import java.util.concurrent.TimeUnit
import org.influxdb.dto.QueryResult



@Service
class LuftdatenService {

    @Autowired
    private val influxDBTemplate: InfluxDBTemplate<Point>? = null

    fun write() {
        influxDBTemplate!!.createDatabase()
        val p = Point.measurement("disk")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("tenant", "default")
                .addField("used", 80L)
                .addField("free", 1L)
                .build()
        influxDBTemplate.write(p)
    }

    fun ping(): Pong? {
        return influxDBTemplate!!.ping()
    }

    fun query(): String {
        val queryString = "SELECT mean(\"temperature\") FROM \"feinstaub\" WHERE  time > now() - 2h GROUP BY time(1h)"
        val queryString2 = "SELECT last(*) FROM \"feinstaub\""
        val query = Query.encode("select value from feinstaub where time > '2013-08-12 23:32:01.232';")
        val queryResult = influxDBTemplate!!.connection.query(
                Query(queryString, "pm_temp_hum"))
        return queryResult.toString()

    }
}
