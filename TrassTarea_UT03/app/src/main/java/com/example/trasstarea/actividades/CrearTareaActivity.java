package com.example.trasstarea.actividades;

import static java.lang.System.in;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.trasstarea.R;
import com.example.trasstarea.basedatos.AppDatabase;
import com.example.trasstarea.modelo.Tarea;
import com.example.trasstarea.fragmentos.FragmentoUno;
import com.example.trasstarea.fragmentos.FragmentoDos;
import com.example.trasstarea.adaptadores.TareaViewModel;


public class CrearTareaActivity extends AppCompatActivity implements
        //Interfaces de comunicación con los fragmentos
        FragmentoUno.ComunicacionPrimerFragmento,
        FragmentoDos.ComunicacionSegundoFragmento {
    private TareaViewModel tareaViewModel;
    private String titulo, descripcion;
    private String fechaCreacion, fechaObjetivo;
    private Integer progreso;
    private Boolean prioritaria = false;
    private String urlDocumento, urlImagen, urlVideo, urlAudio;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private AppDatabase appDatabase = AppDatabase.getInstance(this);

    private FragmentManager fragmentManager;

    private final Fragment fragmento1 = new FragmentoUno();
    private final Fragment fragmento2 = new FragmentoDos();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        aplicarTema();

        // Listener para detectar cambios en las preferencias
        boolean theme = sharedPreferences.getBoolean("key_pref_theme", false);

        // Cambiamos el tema de la actividad según la preferencia. Debe tener en cuenta el setTintMode para ambos modos en SRC_ATOP y el backgroundTint con el color #F0FFFFFF
        if (theme) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getWindow().setBackgroundDrawableResource(R.drawable.background_mosaic);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getWindow().setBackgroundDrawableResource(R.drawable.background_mosaic_dark);
        }


        //Desde aquí le cambio el estilo del actionBar para que tenga un fondo diferente y aparezca el logotipo
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
            actionBar.setLogo(R.drawable.trasstarea_logo_letras_header_white);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        //Instanciamos el ViewModel
        tareaViewModel = new ViewModelProvider(this).get(TareaViewModel.class);

        //Instanciamos el gestor de fragmentos
        fragmentManager = getSupportFragmentManager();

        //Si hay estado guardado
        if (savedInstanceState != null) {
            // Recuperar el ID o información del fragmento
            int fragmentId = savedInstanceState.getInt("fragmentoId", -1);

            if (fragmentId != -1) {
                // Usar el ID o información para encontrar y restaurar el fragmento
                cambiarFragmento(Objects.requireNonNull(fragmentManager.findFragmentById(fragmentId)));
            }else{
                //Si no tenemos ID de fragmento cargado, cargamos el primer fragmento
                cambiarFragmento(fragmento1);
            }
        }else{
            //Si no hay estado guardado, cargamos el primer fragmento
            cambiarFragmento(fragmento1);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int fragmentID = Objects.requireNonNull(getSupportFragmentManager().
                findFragmentById(R.id.contenedor_frag)).getId();
        outState.putInt("fragmentoId", fragmentID);
    }
    
    
    //Implementamos los métodos de la interfaz de comunicación con el primer fragmento
    @Override
    public void onBotonSiguienteClicked() {
        //Leemos los valores del formulario del fragmento 1
        titulo = tareaViewModel.getTitulo().getValue();
        fechaCreacion = tareaViewModel.getFechaCreacion().getValue();
        fechaObjetivo = tareaViewModel.getFechaObjetivo().getValue();
        progreso = tareaViewModel.getProgreso().getValue();
        prioritaria = tareaViewModel.isPrioritaria().getValue();

        //Cambiamos el fragmento
        cambiarFragmento(fragmento2);
    }

    @Override
    public void onBotonCancelarClicked() {
        Intent aListado = new Intent();
        //Indicamos en el resultado que ha sido cancelada la actividad
        setResult(RESULT_CANCELED, aListado);
        //Volvemos a la actividad Listado
        finish();
    }

    //Implementamos los métodos de la interfaz de comunicación con el segundo fragmento
    @Override
    public void onBotonGuardarClicked() {
        //Leemos los valores del formulario del fragmento 2
        descripcion = tareaViewModel.getDescripcion().getValue();
        urlDocumento = tareaViewModel.getDocumento().getValue();
        urlImagen = tareaViewModel.getImagen().getValue();
        urlAudio = tareaViewModel.getAudio().getValue();
        urlVideo = tareaViewModel.getVideo().getValue();


        //Creamos la nueva tarea
        Tarea nuevaTarea = new Tarea(titulo, fechaCreacion,fechaObjetivo, progreso, prioritaria, descripcion, urlDocumento, urlImagen, urlVideo, urlAudio);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new InsertarTarea(nuevaTarea));

        //Creamos un intent de vuelta para la actividad Listado
        Intent aListado = new Intent();
        //Creamos un Bundle para introducir la nueva tarea
        Bundle bundle = new Bundle();
        bundle.putParcelable("NuevaTarea", nuevaTarea);
        aListado.putExtras(bundle);

        //Indicamos que el resultado ha sido OK
        setResult(RESULT_OK, aListado);

        //Volvemos a la actividad Listado
        finish();
    }


    @Override
    public void onBotonVolverClicked() {
        //Leemos los valores del formulario del fragmento 2
        descripcion = tareaViewModel.getDescripcion().getValue();
        urlDocumento = tareaViewModel.getDocumento().getValue();
        urlImagen = tareaViewModel.getImagen().getValue();
        urlAudio = tareaViewModel.getAudio().getValue();
        urlVideo = tareaViewModel.getVideo().getValue();


        //Cambiamos el fragmento2 por el 1
        cambiarFragmento(fragmento1);
    }

    public void cambiarFragmento(Fragment fragment){
        if (!fragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contenedor_frag, fragment)
                    .commit();
        }
    }

    //Clase que inserta un objeto producto en la base de datos usando un hilo diferente al principal.
    class InsertarTarea implements Runnable {
        private final Tarea nuevaTarea;

        public InsertarTarea(Tarea nuevaTarea) {

            this.nuevaTarea = nuevaTarea;
        }

        @Override
        public void run() {
            appDatabase.tareaDAO().insert(nuevaTarea);
        }
    }

    public void aplicarTema() {
        boolean tema=sharedPreferences.getBoolean("key_pref_theme", true);
        if(tema){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            getResources().getConfiguration().uiMode &= ~Configuration.UI_MODE_NIGHT_MASK; //actualizar la configuración de la actividad
        }
        else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        String fuente=(sharedPreferences.getString("key_pref_font","2"));

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

//    private void copiarArchivo(Uri Origen, String Destino){
//        try {
//            //Verificar si la Uri es valida
//            if (origen == null)
//                return;
//            try (InputStream inputStream = getContentResolver().openInputStream(origen)) {
//                if (inputStream != null) {
//                    int fileSize = inputStream.available();
//                }
//
//                //Calcular el tamaño del buffer dinámicamente
//                int bufferSize = Math.max((int) (fileSize + 0.1 * fileSize));
//
//                try (OutputStream outputStream = new FileOutputStream(Destino)) {
//                    byte[] buffer = new byte[bufferSize];
//                    int bytesRead;
//                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                        outputStream.write(buffer, 0, bytesRead);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            //Cerrar los flujos de entrada y salida
//            try {
//                inputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}