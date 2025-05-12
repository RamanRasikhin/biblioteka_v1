package com.librarysystem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DateTest {

    @Test
    void testDateCreation() {
        Date date = new Date(2023, 10, 26);
        assertEquals(2023, date.getYear());
        assertEquals(10, date.getMonth());
        assertEquals(26, date.getDay());
    }

    @Test
    void testToString() {
        Date date = new Date(2023, 5, 7);
        assertEquals("2023-05-07", date.toString());
    }

    @Test
    void testFromString() {
        Date date = Date.fromString("2024-01-15");
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonth());
        assertEquals(15, date.getDay());
    }

    @Test
    void testAddMonths() {
        Date date = new Date(2023, 10, 26);
        Date newDate = date.addMonths(3);
        assertEquals(2024, newDate.getYear());
        assertEquals(1, newDate.getMonth());
        assertEquals(26, newDate.getDay());

        Date originalDateCheck = new Date(2023, 10, 26);
        assertEquals(originalDateCheck.getYear(), date.getYear());
        assertEquals(originalDateCheck.getMonth(), date.getMonth());
    }

    @Test
    void testAddMonthsCrossingYearBoundary() {
        Date date = new Date(2023, 11, 15);
        Date newDate = date.addMonths(2);
        assertEquals(2024, newDate.getYear());
        assertEquals(1, newDate.getMonth());
        assertEquals(15, newDate.getDay());
    }

    @Test
    void testIsAfter() {
        Date date1 = new Date(2023, 10, 26);
        Date date2 = new Date(2023, 10, 27);
        Date date3 = new Date(2023, 10, 26);
        assertTrue(date2.isAfter(date1));
        assertFalse(date1.isAfter(date2));
        assertFalse(date1.isAfter(date3));
    }

    @Test
    void testIsBefore() {
        Date date1 = new Date(2023, 10, 26);
        Date date2 = new Date(2023, 10, 27);
        Date date3 = new Date(2023, 10, 26);
        assertTrue(date1.isBefore(date2));
        assertFalse(date2.isBefore(date1));
        assertFalse(date1.isBefore(date3));
    }

    @Test
    void testIsEqual() {
        Date date1 = new Date(2023, 10, 26);
        Date date2 = new Date(2023, 10, 26);
        Date date3 = new Date(2023, 11, 26);
        assertTrue(date1.isEqual(date2));
        assertFalse(date1.isEqual(date3));
    }

    @Test
    void testGetCurrentDate() {
        Date currentDate = Date.getCurrentDate();
        assertNotNull(currentDate);
        assertTrue(currentDate.getYear() >= 2023);
    }
}