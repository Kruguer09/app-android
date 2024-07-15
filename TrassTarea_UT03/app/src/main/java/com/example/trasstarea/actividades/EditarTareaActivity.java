package com.example.trasstarea.actividades;

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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.trasstarea.R;
import com.example.trasstarea.basedatos.AppDatabase;
import com.example.trasstarea.modelo.Tarea;
import com.example.trasstarea.fragmentos.FragmentoUno;
import com.example.trasstarea.fragmentos.FragmentoDos;
import com.example.trasstarea.adaptadores.TareaViewModel;

public class EditarTareaActivity extends AppCompatActivity implements
        //Interfaces de comunicación con los fragmentos
        FragmentoUno.ComunicacionPrimerFragmento,
        FragmentoDos.ComunicacionSegundoFragmento{
    private Tarea tareaEditable;
    private TareaViewModel tareaViewModel;
    private String titulo, descripcion;
    private String fechaCreacion, fechaObjetivo;
    private Integer progreso;
    private Boolean prioritaria;
    private String urlDocumento, urlImagen, urlVideo, urlAudio;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SharedPreferences sharedPreferences;
    private AppDatabase appDatabase = AppDatabase.getInstance(this);

    private TextView tvDocumento, tvImagen, tvVideo, tvAudio;
    private FragmentManager fragmentManager;
    private final Fragment fragmento1 = new FragmentoUno();
    private final Fragment fragmento2 = new FragmentoDos();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarea);

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


        // Recibimos el bundle con la tarea que vamos a editar
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Extraemos el id de la tarea del bundle
            tareaEditable = bundle.getParcelable("EditarTarea");
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

            //Escribimos valores en el ViewModel
            tareaViewModel.setTitulo(tareaEditable.getTitulo());
            tareaViewModel.setFechaCreacion(parsearFecha(tareaEditable.getFechaCreacion()));
            tareaViewModel.setFechaObjetivo(parsearFecha(tareaEditable.getFechaObjetivo()));
            tareaViewModel.setProgreso(tareaEditable.getProgreso());
            tareaViewModel.setPrioritaria(tareaEditable.isPrioritaria());
            tareaViewModel.setDescripcion(tareaEditable.getDescripcion());
            tareaViewModel.setDocumento(tareaEditable.getUrlDocumento());
            tareaViewModel.setImagen(tareaEditable.getUrlImagen());
            tareaViewModel.setAudio(tareaEditable.getUrlAudio());
            tareaViewModel.setVideo(tareaEditable.getUrlVideo());
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

        tareaViewModel.setFechaCreacion(parsearFecha(tareaEditable.getFechaCreacion()));
        tareaViewModel.setFechaObjetivo(parsearFecha(tareaEditable.getFechaObjetivo()));

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
        tvDocumento = findViewById(R.id.tv_documento);
        tvImagen = findViewById(R.id.tv_imagen);
        tvAudio = findViewById(R.id.tv_audio);
        tvVideo = findViewById(R.id.tv_video);

        urlDocumento = tvDocumento.getText().toString();
        urlImagen = tvImagen.getText().toString();
        urlAudio = tvAudio.getText().toString();
        urlVideo = tvVideo.getText().toString();

        //Creamos un nuevo objeto tarea con los campos editados
        Tarea tareaEditada = new Tarea(titulo, fechaCreacion,fechaObjetivo, progreso, prioritaria, descripcion, urlDocumento, urlImagen, urlVideo, urlAudio);

        tareaEditada.setId(tareaEditable.getId());

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new ActualizarTarea(tareaEditada));

        //Creamos un intent de vuelta para la actividad Listado
        Intent aListado = new Intent();
        //Creamos un Bundle para introducir la tarea editada
        Bundle bundle = new Bundle();
        bundle.putParcelable("TareaEditada", tareaEditada);
        aListado.putExtras(bundle);
        //Indicamos que el resultado ha sido OK
        setResult(RESULT_OK, aListado);

        //Volvemos a la actividad Listado
        finish();
    }

    @Override
    public void onBotonVolverClicked() {
        // Leemos los valores del formulario del fragmento 2
        descripcion = tareaViewModel.getDescripcion().getValue();
        fechaCreacion = tareaViewModel.getFechaCreacion().getValue();
        fechaObjetivo = tareaViewModel.getFechaObjetivo().getValue();
        progreso = tareaViewModel.getProgreso().getValue();
        prioritaria = tareaViewModel.isPrioritaria().getValue();

        // Obtenemos las rutas con timeStamp
        tvDocumento = findViewById(R.id.tv_documento);
        tvImagen = findViewById(R.id.tv_imagen);
        tvAudio = findViewById(R.id.tv_audio);
        tvVideo = findViewById(R.id.tv_video);

        String rutaDocumento = generarRutaConTimeStamp(tvDocumento.getText().toString(),  "doc");
        String rutaImagen = generarRutaConTimeStamp(tvImagen.getText().toString(),  "img");
        String rutaAudio = generarRutaConTimeStamp(tvAudio.getText().toString(),  "aud");
        String rutaVideo = generarRutaConTimeStamp(tvVideo.getText().toString(),  "vid");

        // Creamos un nuevo objeto tarea con los campos editados
        Tarea tareaEditada = new Tarea(titulo, fechaCreacion, fechaObjetivo, progreso, prioritaria, descripcion);
        // Asignamos las rutas generadas
        tareaEditada.setUrlDocumento(rutaDocumento);
        tareaEditada.setUrlImagen(rutaImagen);
        tareaEditada.setUrlAudio(rutaAudio);
        tareaEditada.setUrlVideo(rutaVideo);

        tareaEditada.setId(tareaEditable.getId());

        // Cambiamos el fragmento2 por el 1
        cambiarFragmento(fragmento1);
    }


    public void cambiarFragmento(Fragment fragment){
        if (!fragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.contenedor_frag, fragment)
                    .commit();
        }
    }

    //Método para validar la fecha
    private Date validarFecha(String fecha) {
        Date fechaValida = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            fechaValida = sdf.parse(fecha);
        } catch (ParseException e) {
            Log.e("Error fecha", "Formato de fecha no válido");
        }
        return fechaValida;
    }

        //Método para parsear la fecha a un formato legible
    public String parsearFecha(Date fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fecha);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();

            }

        }

    }

    public class ActualizarTarea implements Runnable {
        private final Tarea tareaEditada;

        public ActualizarTarea(Tarea tareaEditada) {
            this.tareaEditada = tareaEditada;
        }

        @Override
        public void run() {
            appDatabase.tareaDAO().update(tareaEditada);
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

    private String generarRutaConTimeStamp(String rutaOriginal, String tipoRecurso) {
        if (!rutaOriginal.isEmpty()) {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String descripcionAbreviada = tipoRecurso.substring(0, 3); // Tomamos los primeros 3 caracteres del tipo de recurso
            String rutaCorta = descripcionAbreviada + "_" + timeStamp; // Concatenamos el tipo de recurso abreviado y el timestamp
            // Aseguramos que la longitud de la ruta no supere los 15 caracteres
            if (rutaCorta.length() > 20) {
                rutaCorta = rutaCorta.substring(0, 20);
            }
            return rutaCorta;
        } else {
            return "";
        }
    }
}