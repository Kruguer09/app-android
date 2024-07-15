package com.example.trasstarea.actividades;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import android.annotation.SuppressLint;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.trasstarea.R;
import com.example.trasstarea.adaptadores.TareaAdapter;
import com.example.trasstarea.basedatos.AppDatabase;
import com.example.trasstarea.modelo.Tarea;

public class ListadoTareasActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView listadoVacio;
    private MenuItem menuItemPrior;
    private ArrayList<Tarea> tareas = new ArrayList<>();
    private TareaAdapter adaptador;
    private boolean boolPrior = false;
    private Tarea tareaSeleccionada;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private AppDatabase appDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_tareas);

        //Desde aquí le cambio el estilo del actionBar para que tenga un fondo diferente y aparezca el logotipo
        cambiarColorActionBar();

        // Instanciamos la escucha a las preferencias compartidas
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        aplicarTema();

        //Binding del TextView
        listadoVacio = findViewById(R.id.listado_vacio);

        //Binding del RecyclerView
        rv = findViewById(R.id.listado_recycler);

        //Instanciamos la base de datos
        try {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TareasDatabase").allowMainThreadQueries().build();
                appDatabase = AppDatabase.getInstance(getApplicationContext());
            } else {
                appDatabase = AppDatabase.getInstance(getApplicationContext());
            }

            //Obtenemos las tareas de la base de datos y las mostramos en el RecyclerView
            LiveData<List<Tarea>> listaBD = appDatabase.tareaDAO().getAll();
            listaBD.observe(this,
                new Observer<List<Tarea>>() {
                    @Override
                    public void onChanged(List<Tarea> nuevaLista) {
                        // Actualizar la lista de tareas
                        tareas.clear();

                        // Añadir las tareas de la base de datos a la lista
                        tareas.addAll(nuevaLista);

                        // Ordenar las tareas
                        tareas = ordenarPorCriterio(tareas);

                        // Notificar al adaptador
                        adaptador.notifyDataSetChanged();

                        // Comprobar si el listado está vacío
                        comprobarListadoVacio();
                    }
            });//fin observer

        } catch (Exception e) {
            Log.e("Error en la base de datos", Objects.requireNonNull(e.getLocalizedMessage()));
        }


        //RESTAURACIÓN DEL ESTADO GLOBAL DE LA ACTIVIDAD
        if (savedInstanceState != null) {
            //Recuperamos la lista de Tareas y el booleano prioritarias
            tareas = savedInstanceState.getParcelableArrayList("listaTareas");
            boolPrior = savedInstanceState.getBoolean("boolPrior");
        } else {
            //Inicializamos la lista de tareas y el booleano prioritarias
            //inicializarListaTareas();
            boolPrior = false;
        }

        //Creamos el adaptador y lo vinculamos con el RecycleView
        adaptador = new TareaAdapter(this, tareas, boolPrior);
        rv.setAdapter(adaptador);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //Registramos el rv para el menú contextual
        registerForContextMenu(rv);

        //Comprobamos si el listado está vacío
        comprobarListadoVacio();


    }//fin onCreate

    //Capturar escucha de cambio de tema en PreferenciasActivity
    @Override
    protected void onResume() {
        super.onResume();

        // Instanciamos el gestor de preferencias compartidas para detectar cambios en las preferencias y aplicarlos
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

        //Instanciamos la base de datos cuando se reanuda la actividad
        try {
            if (appDatabase == null)
                appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TareasDatabase").allowMainThreadQueries().build();
            else
                appDatabase = AppDatabase.getInstance(getApplicationContext());

        } catch (Exception e) {
            Log.e("Error en la base de datos", Objects.requireNonNull(e.getLocalizedMessage()));
        }

    }//fin onResume

    //SALVADO DEL ESTADO GLOBAL DE LA ACTIVIDAD
    //Salva la lista de tareas y el valor booleano de prioritarias para el caso en que la actividad
    // sea destruida por ejemplo al cambiar la orientación del dispositivo
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("listaTareas", tareas);
        outState.putBoolean("prioritarias", boolPrior);
    }//fin onSaveInstanceState


    ////////////////////////////////////// OPCIONES DEL MENÚ ///////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menuItemPrior = menu.findItem(R.id.item_priority);
        //Colocamos el icono adecuado
        iconoPrioritarias();
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //OPCION CREAR TAREA
        if (id == R.id.item_add) {
            //Llamada al launcher con contrato y respuesta definidos
            lanzadorCrearTarea.launch(null);
        }

        //OPCION MOSTRAR PRIORITARIAS / TODAS
        else if (id == R.id.item_priority) {

            //Conmutamos el valor booleando
            boolPrior = !boolPrior;
            if (item.getIcon().getConstantState().equals(getResources().getDrawable(R.drawable.no_prioritaria_star).getConstantState())) {
                iconoPrioritarias();
                adaptador.mostrarOcultarFavoritos(obtenerTareasPrioritarias()); // Mostrar solo tareas favoritas
            } else {
                iconoPrioritarias();
                adaptador.mostrarOcultarFavoritos(tareas); // Mostrar todas las tareas
            }
            //Colocamos el icono adecuado
            iconoPrioritarias();
            adaptador.setBoolPrior(boolPrior);
            adaptador.notifyDataSetChanged();

            //Comprobamos que hay algún elemento que mostrar
            comprobarListadoVacio();
        }

        //OPCIÓN PREFERENCIAS

        else if (id == R.id.item_preferencias) {
            //Lanzamos la actividad de preferencias
            Intent intent = new Intent(this, PreferenciasActivity.class);
            lanzadorPreferencias.launch(intent);
        }


        //OPCIÓN ACERCA DE...
        else if (id == R.id.item_about) {

            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.logo_pencil_about);
            imageView.requestLayout();

            //Creamos un AlertDialog como cuadro de diálogo
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.about_title);
            builder.setView(imageView);
            builder.setMessage(R.string.about_msg);
            // Botón "Aceptar"
            builder.setPositiveButton(R.string.about_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss(); // Cerrar el cuadro de diálogo.
                }
            });
            // Mostrar el cuadro de diálogo
            builder.create().show();
        }

        else if (id==R.id.item_estadisticas){
            Intent intent = new Intent(this, EstadisticasActivity.class);
            startActivity(intent);
        }



        //OPCIÓN SALIR
        else if (id == R.id.item_exit) {
            Toast.makeText(this, R.string.msg_salida, Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }//fin onOptionsItemSelected


    private ArrayList<Tarea> obtenerTareasPrioritarias() {
        ArrayList<Tarea> tareasPrioritarias = new ArrayList<>();

        //para cada tarea, si es favorita, que se añada a la lista nueva de favoritos
        for (Tarea tarea : tareas) {
            if (tarea.isPrioritaria()) {
                tareasPrioritarias.add(tarea);
            }
        }
        return tareasPrioritarias; //devolvemos el arrayList con las tareas favoritas para meterlas en el adaptador cuando pulsa la estrella en el menú
    }

    //////////////////////////////// OPCIONES DEL MENÚ CONTEXTUAL  /////////////////////////////////
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        //Leemos la tarea seleccionada en el evento de mostrar el menú contextual
        tareaSeleccionada = adaptador.getTareaSeleccionada();

        int itemId = item.getItemId();

        //OPCION DESCRIPCIÓN
        if (itemId == R.id.item_descripcion) {
            // Mostrar un cuadro de diálogo con la descripción de la tarea
            AlertDialog.Builder builder = new AlertDialog.Builder(ListadoTareasActivity.this);
            builder.setTitle(R.string.dialog_description);
            builder.setMessage(tareaSeleccionada.getDescripcion());
            builder.setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        }

        //OPCION EDITAR
        else if (itemId == R.id.item_editar) {

            //capturo la tarea seleccionada
            Tarea editarTarea = tareaSeleccionada.getTarea();

            //lanzamos la actividad EditarTareaActivity con la tarea seleccionada
            Intent intent = new Intent(this, EditarTareaActivity.class);
            intent.putExtra("EditarTarea", editarTarea);
            startActivity(intent);

            return true;
        }

        //OPCION BORRAR
        else if (itemId == R.id.item_borrar) {

            if (tareaSeleccionada != null) {
                // Mostrar un cuadro de diálogo para confirmar el borrado
                AlertDialog.Builder builder = new AlertDialog.Builder(ListadoTareasActivity.this);
                builder.setTitle(R.string.dialog_confirmacion_titulo);
                builder.setMessage(getString(R.string.dialog_msg) + " \"" + tareaSeleccionada.getTitulo() + "\"?");
                builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //obtenemos la tarea seleccionada
                        Tarea tarea = adaptador.getTareaSeleccionada();

                        //Creamos un objeto de la clase Executor que usamos para crear un hilo secundario y así poder borrar el objeto en la base de datos.
                        Executor executor = Executors.newSingleThreadExecutor();

                        //Ejecutamos el hilo secundario y le mandamos la tarea a borrar como parámetro
                        executor.execute(new BorrarTarea(tarea));

                        //Comprobamos si el listado ha quedado vacío
                        comprobarListadoVacio();

                        //Notificamos que la tarea ha sido borrada al usuario
                        Toast.makeText(ListadoTareasActivity.this, R.string.dialog_erased, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.dialog_no, null);
                builder.show();
            } else {
                // Si no se encuentra el elemento, mostrar un mensaje de error
                Toast.makeText(ListadoTareasActivity.this, R.string.dialog_not_found, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    ////////////////////////// COMUNICACIONES CON ACTIVIDADES SECUNDARIAS //////////////////////////

    //Contrato personalizado para el lanzador hacia la actividad CrearTareaActivity
    ActivityResultContract<Intent, Tarea> contratoCrear = new ActivityResultContract<Intent, Tarea>() {
        //En primer lugar se define el Intent de ida
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Intent intent) {
            return new Intent(context, CrearTareaActivity.class);
        }

        //Ahora se define el método de parseo de la respuesta. En este caso se recibe un objeto Tarea
        @Override
        public Tarea parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                try {
                    return (Tarea) Objects.requireNonNull(intent.getExtras()).get("NuevaTarea");
                } catch (NullPointerException e) {
                    Log.e("Error en intent devuelto", Objects.requireNonNull(e.getLocalizedMessage()));
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), R.string.action_canceled, Toast.LENGTH_SHORT).show();
            }
            return null; // Devuelve null si no se pudo obtener una Tarea válida.
        }
    };

    //Registramos el lanzador hacia la actividad CrearTareaActivity con el contrato personalizado y respuesta con implementación anónima
    private final ActivityResultLauncher<Intent> lanzadorCrearTarea = registerForActivityResult(contratoCrear, new ActivityResultCallback<Tarea>() {
        @Override
        public void onActivityResult(Tarea nuevaTarea) {
            if (nuevaTarea != null) {

                //Añadimos la nueva tarea a la colección tareas desde la base de datos
                tareas.add(nuevaTarea);

                adaptador.notifyItemInserted(tareas.size() - 1); // Agregar el elemento nuevo al adaptador.
                Toast.makeText(ListadoTareasActivity.this.getApplicationContext(), R.string.tarea_add, Toast.LENGTH_SHORT).show();
                ListadoTareasActivity.this.comprobarListadoVacio();
            }
        }
    });

    //Contrato para el lanzador hacia la actividad EditarTareaActivity
    ActivityResultContract<Tarea, Tarea> contratoEditar = new ActivityResultContract<Tarea, Tarea>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Tarea tarea) {
            Intent intent = new Intent(context, EditarTareaActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("TareaEditable", tarea);
            intent.putExtras(bundle);
            return intent;
        }

        @Override
        public Tarea parseResult(int i, @Nullable Intent intent) {
            if (i == Activity.RESULT_OK && intent != null) {
                try {
                    return (Tarea) Objects.requireNonNull(intent.getExtras()).get("TareaEditada");
                } catch (NullPointerException e) {
                    Log.e("Error en intent devuelto", Objects.requireNonNull(e.getLocalizedMessage()));
                }
            } else if (i == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), R.string.action_canceled, Toast.LENGTH_SHORT).show();
            }
            return null; // Devuelve null si no se pudo obtener una Tarea válida.
        }
    };

    //Respuesta para el lanzador hacia la actividad EditarTareaActivity
    ActivityResultCallback<Tarea> resultadoEditar = new ActivityResultCallback<Tarea>() {
        @Override
        public void onActivityResult(Tarea tareaEditada) {
            if (tareaEditada != null) {
                //Seteamos el id de la tarea recibida para que coincida con el de la tarea editada
                tareaEditada.setId(tareaSeleccionada.getId());

                //Procedemos a la sustitución de la tarea editada por la seleccionada.
                int posicionEnColeccion = tareas.indexOf(tareaSeleccionada);

                //Actualizamos la tarea en la base de datos
                appDatabase.tareaDAO().update(tareaEditada);

                //Sustituimos la tarea seleccionada por la editada en la colección
                tareas.remove(tareaSeleccionada);
                tareas.add(posicionEnColeccion, tareaEditada);

                //Notificamos al adaptador y comprobamos si el listado ha quedado vacío
                adaptador.notifyItemChanged(posicionEnColeccion);
                comprobarListadoVacio();

                //Comunicamos que la tarea ha sido editada al usuario
                Toast.makeText(getApplicationContext(), R.string.tarea_edit, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final ActivityResultLauncher<Intent> lanzadorPreferencias = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            activityResult -> {
                if (activityResult.getResultCode() == RESULT_OK) {
                    recreate();
                }
            }
    );

    //Registramos el lanzador hacia la actividad EditarTareaActivity con el contrato y respuesta personalizados
    ActivityResultLauncher<Tarea> lanzadorActividadEditar = registerForActivityResult(contratoEditar, resultadoEditar);


    //////////////////////////////////////////////////////////////////////////////
    //                              OTROS MÉTODOS                               //
    //////////////////////////////////////////////////////////////////////////////

    //Método para cambiar el icono de acción para mostrar todas las tareas o solo prioritarias
    private void iconoPrioritarias() {
        if (boolPrior)
            //Ponemos en la barra de herramientas el icono PRIORITARIAS
            menuItemPrior.setIcon(R.drawable.prioritaria_star);
        else
            //Ponemos en la barra de herramientas el icono NO PRIORITARIAS
            menuItemPrior.setIcon(R.drawable.no_prioritaria_star);
    }

    //Método que comprueba si el listado de tareas está vacío.
    //Cuando está vacío oculta el RecyclerView y muestra el TextView con el texto correspondiente.
    private void comprobarListadoVacio() {

        ViewTreeObserver vto = rv.getViewTreeObserver();

        //Observamos cuando el RecyclerView esté completamente terminado
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Contamos la altura total del RecyclerView
                int alturaRV = 0;
                for (int i = 0; i < adaptador.getItemCount(); i++) {
                    View itemView = rv.getChildAt(i);
                    if (itemView != null)
                        alturaRV += itemView.getHeight();
                }

                if (alturaRV == 0) {
                    listadoVacio.setText(boolPrior ? R.string.listado_tv_no_prioritarias : R.string.listado_tv_vacio);
                    listadoVacio.setVisibility(View.VISIBLE);
                } else {
                    listadoVacio.setVisibility(View.GONE);
                }

                // Remueve el oyente para evitar llamadas innecesarias
                rv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////
    //                         MÉTODOS DE ORDENACIÓN                            //
    //////////////////////////////////////////////////////////////////////////////

    //Método para ordenar las tareas por el criterio seleccionado en las preferencias de la aplicación y devolverlas ordenadas en un ArrayList
    private ArrayList<Tarea> ordenarPorCriterio(ArrayList<Tarea> tareas) {
        //obtenemos el criterio de ordenación de las preferencias
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orden = sharedPreferences.getString("key_pref_criterio", "2");

        //ordena de forma ascendente o descendiente dependiendo del criterio seleccionado
        if(sharedPreferences.getBoolean("key_pref_orden", true)){

            //orden ascendente
            if(orden.equals("1")){//ordenar por título
                tareas.sort((t1, t2) -> t1.getTitulo().compareTo(t2.getTitulo()));
            }else if(orden.equals("2")){//ordenar por fecha de creación
                tareas.sort((t1, t2) -> t1.getFechaCreacion().compareTo(t2.getFechaCreacion()));
            }else if(orden.equals("3")){//ordenar por días restantes
                tareas.sort((t1, t2) -> t1.quedanDias() - t2.quedanDias());
            }else if(orden.equals("4")){//ordenar por progreso
                tareas.sort((t1, t2) -> t1.getProgreso() - t2.getProgreso());
            }
        }else{
            //orden descendente
            if(orden.equals("1")){//ordenar por título
                tareas.sort((t1, t2) -> t2.getTitulo().compareTo(t1.getTitulo()));
            }else if(orden.equals("2")){//ordenar por fecha de creación
                tareas.sort((t1, t2) -> t2.getFechaCreacion().compareTo(t1.getFechaCreacion()));
            }else if(orden.equals("3")){//ordenar por días restantes
                tareas.sort((t1, t2) -> t2.quedanDias() - t1.quedanDias());
            }else if(orden.equals("4")){//ordenar por progreso
                tareas.sort((t1, t2) -> t2.getProgreso() - t1.getProgreso());
            }
        }
        return tareas;
    }

    //////////////////////////////////////////////////////////////////////////////
    //                      APLICAR COLOR AL ACTION BAR                         //
    //////////////////////////////////////////////////////////////////////////////

    private void cambiarColorActionBar() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Listener para detectar cambios en las preferencias
        boolean theme = sharedPreferences.getBoolean("key_pref_theme", false);

        if (theme) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            //establecer el background de activity_listado_tareas.xml con el recurso trama_background_pencil_dark, backgroundTint #1C1C1C y BackgroundTintMode SRC_ATOP
            getWindow().setBackgroundDrawableResource(R.drawable.trama_background_pencil_dark);

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


    //////////////////////////////////////////////////////////////////////////////
    //              MÉTODOS PARA APLICAR TEMA SEGÚN PREFERENCIAS                //
    //////////////////////////////////////////////////////////////////////////////
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

    //////////////////////////////////////////////////////////////////////////////
    //                         MÉTODO DE BORRAR TAREA                           //
    //////////////////////////////////////////////////////////////////////////////

    class BorrarTarea implements Runnable {
        private final Tarea tarea;

        public BorrarTarea(Tarea tarea) {
            this.tarea = tarea;
        }

        @Override
        public void run() {
            appDatabase.tareaDAO().delete(tarea);
        }
    }

}