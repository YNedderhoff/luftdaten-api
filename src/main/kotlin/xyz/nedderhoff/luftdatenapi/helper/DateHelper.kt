package xyz.nedderhoff.luftdatenapi.helper

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@Component
open class DateHelper {
    fun formatDate(dateString: String): String = Instant
            .parse(dateString)
            .atZone(ZoneId.of(ZoneOffset.UTC.id))
            .toString()
}