package com.github.deityexe.util;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DateUtil {
    /**
     * The class logger.
     */
    private static final Logger logger = Logger.getLogger(GuildEvent.class.getName());

    private static final String[] days = new String[] { "So.", "Mo.", "Di.", "Mi.", "Do.", "Fr.", "Sa." };
    /**
     * Retrieve the day of week in short form.
     *
     * @param date The date to retrieve the day for.
     * @return The short string for the day of the week.
     */
    public String getDowShort(final Calendar date) {
        return days[date.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * Parses a date and time string into a combined Calendar instance.
     *
     * @param date The date to be parsed.
     * @param time The time to be parsed.
     * @return The Calendar instance.
     */
    public Calendar dateFromStrings(final String date, final String time) {
        Calendar rc = Calendar.getInstance();

        String[] dateParts = date.split("\\.");
        if (dateParts.length < 3) {
            logger.log(Level.SEVERE, String.format("invalid date: %s", date));
            throw new DeliverableError("Ungültiges Datum gefunden. Bitte TT.MM.JJJJ benutzen.");
        }

        String[] timeParts = time.split(":");
        if (timeParts.length < 2) {
            logger.log(Level.SEVERE, String.format("invalid time: %s", time));
            throw new DeliverableError("Ungültige Zeit gefunden. Bitte HH:MM oder HH:MM:SS benutzen.");
        }

        rc.set(Integer.valueOf(dateParts[2]),Integer.valueOf(dateParts[1]) - 1, Integer.valueOf(dateParts[0]),
                Integer.valueOf(timeParts[0]), Integer.valueOf(timeParts[1]));
        return rc;
    }

    /**
     * Parses the given date string into a calendar object.
     *
     * The resulting calendar will point to 00:00:00 at the given date.
     *
     * @param date The date to be parsed.
     * @return Calendar object representing the date.
     */
    public Calendar dateFromString(final String date) {
        return dateFromStrings(date, "00:00");
    }

    /**
     * Converts the given timestamp to a calendar object.
     *
     * @return Calendar object for the given timestamp.
     */
    public Calendar calendarFromTimestamp(final long timestamp) {
        Calendar rc = Calendar.getInstance();
        rc.setTimeInMillis(timestamp);
        return rc;
    }

    /**
     * Returns the time representation of a calendar object.
     *
     * @return Time of the event.
     */
    public String getTime(final Calendar date) {
        return String.format("%02d:%02d", date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
    }

    /**
     * Getter for the time for the given timestamp.
     *
     * @param timestamp The timestamp to get the time for.
     * @return String formatted time.
     */
    public String getTime(final long timestamp) {
        return this.getTime(calendarFromTimestamp(timestamp));
    }

    /**
     * Gets the formated date representation of a calendar object.
     *
     * @return Date as format string.
     */
    public String getDate(final Calendar date) {
        return String.format("%02d.%02d.%04d", date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH) + 1,
                date.get(Calendar.YEAR));
    }

    /**
     * Gets the formatted date as short version (without year).
     *
     * @param date TGhe date to be converted.
     * @return The short version of the date.
     */
    public String getDateShort(final Calendar date) {
        return String.format("%02d.%02d.", date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH) + 1);

    }

    /**
     * Gets a formatted date string for the given timestamp.
     *
     * @param timestamp The timestamp to retrieve the date string form
     * @return Date string for the given timestamp.
     */
    public String getDate(final long timestamp) {
        return this.getDate(calendarFromTimestamp(timestamp));
    }
}
