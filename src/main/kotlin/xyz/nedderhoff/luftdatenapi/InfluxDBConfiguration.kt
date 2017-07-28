package xyz.nedderhoff.luftdatenapi

import org.influxdb.dto.Point
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.influxdb.DefaultInfluxDBTemplate
import org.springframework.data.influxdb.InfluxDBConnectionFactory
import org.springframework.data.influxdb.InfluxDBProperties
import org.springframework.data.influxdb.InfluxDBTemplate
import org.springframework.data.influxdb.converter.PointConverter

@Configuration
@EnableConfigurationProperties(InfluxDBProperties::class)
class InfluxDBConfiguration {
    @Bean
    fun connectionFactory(properties: InfluxDBProperties): InfluxDBConnectionFactory {
        return InfluxDBConnectionFactory(properties)
    }

    @Bean
    fun influxDBTemplate(connectionFactory: InfluxDBConnectionFactory): InfluxDBTemplate<Point> {

        //You can use your own 'PointCollectionConverter' implementation, e.g. in case
        //you want to use your own custom measurement object.

        return InfluxDBTemplate(connectionFactory, PointConverter())
    }

    @Bean
    fun defaultTemplate(connectionFactory: InfluxDBConnectionFactory): DefaultInfluxDBTemplate {

        // If you are just dealing with Point objects from 'influxdb-java' you could
        //also use an instance of class DefaultInfluxDBTemplate.

        return DefaultInfluxDBTemplate(connectionFactory)
    }
}