package zq.yaw.utils

import java.sql.Time
import java.util.*

class TimeUtils {
    companion object {
        fun isSameDayOfMillis(ms1: Long, ms2: Long): Boolean {
            val interval = ms1 - ms2
            return (interval < MILLIS_IN_DAY && interval > -1L * MILLIS_IN_DAY) && toDay(ms1) == toDay(ms2)
        }
        private fun toDay(millis: Long): Long {
            return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY
        }
        private const val SECONDS_IN_DAY = 60 * 60 * 24
        private const val MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY
    }
}