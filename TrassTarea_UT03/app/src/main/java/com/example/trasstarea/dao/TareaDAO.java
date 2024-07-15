package com.example.trasstarea.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import java.util.List;

import com.example.trasstarea.adaptadores.DateConverter;
import com.example.trasstarea.modelo.Tarea;

// La anotación @Dao indica que esta interfaz es un Data Access Object (DAO).
@Dao
public interface TareaDAO {


    @Query("SELECT * FROM tarea")
    LiveData<List<Tarea>> getAll();

    // El método insert() inserta una tarea nueva en la base de datos.
    @TypeConverters(DateConverter.class) // Esta anotación sirve para convertir las fechas a un formato que Room pueda entender. Para ello he tenido que crear una clase DateConverter.
    @Insert(onConflict = OnConflictStrategy.REPLACE) // La anotación @Insert indica que este método inserta una tarea en la base de datos. El parámetro onConflict = OnConflictStrategy.REPLACE indica que si la tarea ya existe en la base de datos, se reemplazará por la nueva tarea.
    void insert(Tarea tarea);

    // El método update() actualiza una tarea en la base de datos.
    @TypeConverters(DateConverter.class)
    @Update
    void update(Tarea tarea);

    // El método delete() elimina una tarea de la base de datos.
    @Delete
    void delete(Tarea tarea);

    @Query("SELECT * FROM tarea WHERE prioritaria = 1")
    LiveData<List<Tarea>> getTotalTareasPrioritarias();


}


