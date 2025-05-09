// ============== File: com/librarysystem/Date.java ==============
package com.librarysystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
// import java.time.temporal.ChronoUnit; // Not used in current version

public class Date {
    private LocalDate localDate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    public Date(int year, int month, int day) {
        this.localDate = LocalDate.of(year, month, day);
    }

    private Date(LocalDate localDate) { // Make this public if needed for cloning/conversion elsewhere
        this.localDate = localDate;
    }

    public int getDay() {
        return localDate.getDayOfMonth();
    }

    public int getMonth() {
        return localDate.getMonthValue();
    }

    public int getYear() {
        return localDate.getYear();
    }

    // CHANGED: Method now returns a new Date object
    public Date addMonths(int months) {
        LocalDate newLocalDate = this.localDate.plusMonths(months);
        return new Date(newLocalDate); // Return a NEW Date instance
    }

    @Override
    public String toString() {
        return localDate.format(FORMATTER);
    }

    public static Date fromString(String str) {
        return new Date(LocalDate.parse(str, FORMATTER));
    }

    public static Date getCurrentDate() {
        return new Date(LocalDate.now());
    }

    public boolean isAfter(Date other) {
        if (other == null || other.localDate == null) return false; // Guard against null
        return this.localDate.isAfter(other.localDate);
    }

    public boolean isBefore(Date other) {
        if (other == null || other.localDate == null) return false; // Guard against null
        return this.localDate.isBefore(other.localDate);
    }

    public boolean isEqual(Date other) {
        if (other == null || other.localDate == null) return false; // Guard against null
        return this.localDate.isEqual(other.localDate);
    }

    public boolean isSameDayOrAfter(Date other) {
        if (other == null || other.localDate == null) return false; // Guard against null
        return !this.localDate.isBefore(other.localDate);
    }

    // These also should return new Date objects for consistency
    public Date plusDays(long days) {
        return new Date(this.localDate.plusDays(days));
    }

    public Date minusDays(long days) {
        return new Date(this.localDate.minusDays(days));
    }
}