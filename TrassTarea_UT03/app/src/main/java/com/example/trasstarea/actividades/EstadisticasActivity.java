package com.example.trasstarea.actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.trasstarea.R;
import com.example.trasstarea.basedatos.AppDatabase;
import com.example.trasstarea.modelo.Tarea;

import android.content.SharedPreferences;
import android.widget.TextView;

import java.util.List;

public class EstadisticasActivity extends AppCompatActivity {

    private AppDatabase appDatabase;
    private SharedPreferences sharedPreferences;

    private TextView tv_totalTareas;
    private TextView tv_totalPrioritarias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Listener para detectar cambios en las preferencias
        boolean theme = sharedPreferences.getBoolean("key_pref_theme", true);

        // Cambiamos el tema de la actividad según la preferencia. Le asigno el fondo personalizado diseñado por mí
        if (theme) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getWindow().setBackgroundDrawableResource(R.drawable.background_mosaic);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getWindow().setBackgroundDrawableResource(R.drawable.background_mosaic_dark);
        }

        //Desde aquí le cambio el estilo del actionBar para que tenga un fondo diferente y aparezca el logotipo
        cambiarColorActionBar();
        //Aplicamos el tema y la fuente seleccionada por el usuario
        aplicarTema();

        // Mostramos el botón de atrás en la barra de acción
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Binding de los elementos de la vista
        tv_totalTareas = findViewById(R.id.tv_resultado_totalTareas);
        tv_totalPrioritarias = findViewById(R.id.tv_resultado_totalPrioritarias);


        //Instanciamos la base de datos para poder obtener los datos de las tareas
        try {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TareasDatabase").allowMainThreadQueries().build();
                appDatabase = AppDatabase.getInstance(getApplicationContext());
            } else {
                appDatabase = AppDatabase.getInstance(getApplicationContext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //Obtenemos el número total de tareas y el número total de tareas prioritarias
        LiveData<List<Tarea>> totalTareas = appDatabase.tareaDAO().getAll();
        LiveData<List<Tarea>> totalPrioritarias = appDatabase.tareaDAO().getTotalTareasPrioritarias();

        // Observar el LiveData para el total de tareas
        totalTareas.observe(this, new Observer<List<Tarea>>() {
            @Override
            public void onChanged(List<Tarea> tareas) {
                tv_totalTareas.setText(String.valueOf(tareas.size()));
            }
        });

    // Observar el LiveData para el total de tareas prioritarias
        totalPrioritarias.observe(this, new Observer<List<Tarea>>() {
            @Override
            public void onChanged(List<Tarea> tareas) {
                tv_totalPrioritarias.setText(String.valueOf(tareas.size()));
            }
        });


    }


    // Método para gestionar el botón de atrás en la barra de acción
    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void aplicarTema () {
        boolean tema = sharedPreferences.getBoolean("key_pref_theme", true);
        if (tema) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getResources().getConfiguration().uiMode &= ~Configuration.UI_MODE_NIGHT_MASK; //actualizar la configuración de la actividad
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        String fuente = (sharedPreferences.getString("key_pref_font", "2"));

        Configuration configuration = getResources().getConfiguration();

        switch (fuente) {
            case "1":
                configuration.fontScale = 0.8f;
                getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                break;
            case "2":
                configuration.fontScale = 1.0f;
                getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                break;
            case "3":
                configuration.fontScale = 1.2f;
                getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                break;
            default:
                configuration.fontScale = 1.0f;
                getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                break;
        }
    }
    private void cambiarColorActionBar () {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Listener para detectar cambios en las preferencias
        boolean theme = sharedPreferences.getBoolean("key_pref_theme", false);

        if (theme) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            //establecer el background de activity_listado_tareas.xml con el recurso trama_background_pencil_dark, backgroundTint #1C1C1C y BackgroundTintMode SRC_ATOP
            getWindow().setBackgroundDrawableResource(R.drawable.background_mosaic_dark);

        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (theme == true) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
                actionBar.setLogo(R.drawable.trasstarea_logo_letras_header);
            } else { //modo oscuro
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.oscuro)));

                actionBar.setLogo(R.drawable.trasstarea_logo_letras_header_white);
            }

            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }//fin cambiarColorActionBar
}