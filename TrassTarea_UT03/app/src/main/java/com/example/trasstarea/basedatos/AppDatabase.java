package com.example.trasstarea.basedatos;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.trasstarea.adaptadores.DateConverter;
import com.example.trasstarea.dao.TareaDAO;
import com.example.trasstarea.modelo.Tarea;

@Database(entities = {Tarea.class}, version =1, exportSchema = false)

@TypeConverters(DateConverter.class) // Esta anotación sirve para convertir las fechas a un formato que Room pueda entender. Para ello he tenido que crear una clase DateConverter.


public abstract class AppDatabase extends RoomDatabase {


    //Usando el patrón SINGLETON, nos aseguramos que solo haya una instancia de la base de datos creada en nuestra aplicación.
    private static AppDatabase INSTANCIA;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCIA == null) {
            INSTANCIA = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,"TareasDatabase").build();
        }
        return INSTANCIA;
    }

    public static void destroyInstance() {
        INSTANCIA = null;
    }

    //Método para construir el objeto ProductoDAO con el que accederemos a la base de datos.
    public abstract TareaDAO tareaDAO();
}

