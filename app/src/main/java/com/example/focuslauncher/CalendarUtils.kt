import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import java.util.Calendar

data class CalendarEvent(val title: String, val startTime: Long)

fun getTodayEvents(context: Context): List<CalendarEvent> {
    val events = mutableListOf<CalendarEvent>()

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    val startOfDay = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    val endOfDay = calendar.timeInMillis

    val projection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART
    )

    val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
    val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())

    val cursor: Cursor? = context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${CalendarContract.Events.DTSTART} ASC"
    )

    cursor?.use {
        val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
        val startIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)

        while (it.moveToNext()) {
            events.add(
                CalendarEvent(
                    title = it.getString(titleIndex),
                    startTime = it.getLong(startIndex)
                )
            )
        }
    }
    return events
}