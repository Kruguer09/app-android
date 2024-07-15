package com.example.trasstarea.adaptadores;

import androidx.room.TypeConverter;
import java.util.Date;


public class DateConverter {

    // El método toDate() convierte un valor Long en una fecha.
    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    // El método toLong() convierte una fecha en un valor Long.
    @TypeConverter
    public static Long toLong(Date date) {
        return date == null ? null : date.getTime();
    }
}