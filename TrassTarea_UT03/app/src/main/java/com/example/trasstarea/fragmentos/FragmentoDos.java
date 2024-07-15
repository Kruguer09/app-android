package com.example.trasstarea.fragmentos;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.trasstarea.R;
import com.example.trasstarea.adaptadores.TareaViewModel;
import com.example.trasstarea.basedatos.AppDatabase;
import com.example.trasstarea.modelo.Tarea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentoDos extends Fragment {

    //Creamos valores estáticos para los request, que será un valor entero que se utiliza para identificar una solicitud del fichero seleccionado.
    private static final int DOCUMENTO = 1;
    private static final int FOTO = 2;
    private static final int AUDIO = 3;
    private static final int VIDEO = 4;

    private Uri uriArchivo;
    private int requestCode;

    private AppDatabase appDatabase;
    private SharedPreferences sharedPreferences;

    private TareaViewModel tareaViewModel;
    private EditText etDescripcion;
    private ImageView bt_documento, bt_foto, bt_video, bt_audio;
    private TextView tvDocumento, tvImagen, tvVideo, tvAudio;

    //Interfaces de comunicación con la actividad para el botón Guardar y Volver
    public interface ComunicacionSegundoFragmento {
        void onBotonGuardarClicked();

        void onBotonVolverClicked();
    }

    private ComunicacionSegundoFragmento comunicadorSegundoFragmento;


    public FragmentoDos() {

    }

    //Sobrescribimos onAttach para establecer la comunicación entre el fragmento y la actividad
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ComunicacionSegundoFragmento) { //Si la actividad implementa la interfaz
            comunicadorSegundoFragmento = (ComunicacionSegundoFragmento) context; //La actividad se convierte en escuchadora
        } else {
            throw new ClassCastException(context + " debe implementar la interfaz de comunicación con el segundo fragmento");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflamos el fragmento
        View view = inflater.inflate(R.layout.fragment_segundo, container, false);



        //Binding y config botones
        etDescripcion = view.findViewById(R.id.et_descripcion);
        bt_documento = view.findViewById(R.id.btn_documento);
        bt_foto = view.findViewById(R.id.btn_imagen);
        bt_video = view.findViewById(R.id.btn_video);
        bt_audio = view.findViewById(R.id.btn_audio);

        tvDocumento = view.findViewById(R.id.tv_documento);
        tvImagen = view.findViewById(R.id.tv_imagen);
        tvVideo = view.findViewById(R.id.tv_video);
        tvAudio = view.findViewById(R.id.tv_audio);

        //Obtenemos la instancia de la base de datos
        appDatabase = AppDatabase.getInstance(getActivity().getApplicationContext());

        //Vinculamos el fragmento con el ViewModel
        tareaViewModel = new ViewModelProvider(requireActivity()).get(TareaViewModel.class);

        //tareaViewModel.getDescripcion().observe(getViewLifecycleOwner(), s -> etDescripcion.setText(s));


        return view;
    }

    @Override
    @NonNull
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Binding y set EditText Descripción
        tareaViewModel.getDescripcion().observe(getViewLifecycleOwner(), s -> etDescripcion.setText(s));
        tareaViewModel.getDocumento().observe(getViewLifecycleOwner(), s -> tvDocumento.setText(s));
        tareaViewModel.getImagen().observe(getViewLifecycleOwner(), s -> tvImagen.setText(s));
        tareaViewModel.getAudio().observe(getViewLifecycleOwner(), s -> tvAudio.setText(s));
        tareaViewModel.getVideo().observe(getViewLifecycleOwner(), s -> tvVideo.setText(s));


        //Binding y configuracion de botones
        bt_documento.setOnClickListener(v -> seleccionarArchivo(DOCUMENTO, "application/pdf"));
        bt_foto.setOnClickListener(v -> seleccionarArchivo(FOTO, "image/*"));
        bt_video.setOnClickListener(v -> seleccionarArchivo(VIDEO, "video/*"));
        bt_audio.setOnClickListener(v -> seleccionarArchivo(AUDIO, "audio/*"));

        //Boton Volver
        Button btVolver = view.findViewById(R.id.bt_volver);
        btVolver.setOnClickListener(v -> {
            //Escribimos en el ViewModel
            escribirViewModel();
            //Llamamos al método onBotonVolverClicked que está implementado en la actividad
            if(comunicadorSegundoFragmento != null)
                comunicadorSegundoFragmento.onBotonVolverClicked();
        });

        //Boton Guardar
        Button btGuardar = view.findViewById(R.id.bt_guardar);
        btGuardar.setOnClickListener(v -> {
//            Escribimos en el ViewModel
            tareaViewModel.setDescripcion(etDescripcion.getText().toString());

            String documento = generarRutaConTimeStamp(tvDocumento.getText().toString(), "doc");
            String imagen = generarRutaConTimeStamp(tvImagen.getText().toString(), "img");
            String audio = generarRutaConTimeStamp(tvAudio.getText().toString(), "aud");
            String video = generarRutaConTimeStamp(tvVideo.getText().toString(), "vid");

            tareaViewModel.setDocumento(documento);
            tareaViewModel.setImagen(imagen);
            tareaViewModel.setAudio(audio);
            tareaViewModel.setVideo(video);

            Tarea nuevaTarea = new Tarea(tareaViewModel.getTitulo().getValue(), tareaViewModel.getFechaCreacion().getValue(), tareaViewModel.getFechaObjetivo().getValue(), tareaViewModel.getProgreso().getValue(), tareaViewModel.isPrioritaria().getValue(), tareaViewModel.getDescripcion().getValue(), tareaViewModel.getDocumento().getValue(), tareaViewModel.getImagen().getValue(), tareaViewModel.getVideo().getValue(), tareaViewModel.getAudio().getValue());


            //mostramos un mensaje de confirmación con un toast.
            Toast.makeText(getContext(), "Nueva tarea creada", Toast.LENGTH_LONG).show();


            //Llamamos al método onBotonGuardarClicked que está implementado en la actividad.
            if(comunicadorSegundoFragmento != null)
                comunicadorSegundoFragmento.onBotonGuardarClicked();
        });

    }




    //Método que abre el dialogo para seleccionar un archivo
    private void seleccionarArchivo(int archivoSeleccionado, String tipoArchivo) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(tipoArchivo);
        startActivityForResult(intent, archivoSeleccionado);
    }

    //Método para recibir el resultado de la selección de archivos
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            uriArchivo = data.getData(); // Guarda la Uri del archivo
            onArchivoSeleccionado(requestCode, uriArchivo.toString());
            copiarArchivo(uriArchivo, requestCode);
        }
    }


    //Método para mostrar el archivo seleccionado en el TextView correspondiente
    public void onArchivoSeleccionado(int archivoSeleccionado, String uriArchivo) {
        switch (archivoSeleccionado) {
            case DOCUMENTO:
                tareaViewModel.setDocumento(generarRutaConTimeStamp(uriArchivo, "doc"));
                break;

            case FOTO:
                tareaViewModel.setImagen(generarRutaConTimeStamp(uriArchivo, "img"));
                break;

            case AUDIO:
                tareaViewModel.setAudio(generarRutaConTimeStamp(uriArchivo, "aud"));
                break;

            case VIDEO:
                tareaViewModel.setVideo(generarRutaConTimeStamp(uriArchivo, "vid"));
                break;
        }
    }

    //Método para guardar el estado del formulario en un Bundle
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("descripcion",  etDescripcion.getText().toString());
        outState.putString("documento",  generarRutaConTimeStamp(tvDocumento.getText().toString(),  "doc"));
        outState.putString("imagen",  generarRutaConTimeStamp(tvImagen.getText().toString(),  "img"));
        outState.putString("audio",  generarRutaConTimeStamp(tvAudio.getText().toString(),  "aud"));
        outState.putString("video",  generarRutaConTimeStamp(tvVideo.getText().toString(),  "vid"));

        //Sincronizamos la información salvada también en el ViewModel
        escribirViewModel();
    }

    //Método para restaurar el estado del formulario
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            etDescripcion.setText(savedInstanceState.getString("descripcion"));
            tvDocumento.setText(savedInstanceState.getString("documento"));
            tvImagen.setText(savedInstanceState.getString("imagen"));
            tvAudio.setText(savedInstanceState.getString("audio"));
            tvVideo.setText(savedInstanceState.getString("video"));
        }
    }

    private void escribirViewModel(){
        tareaViewModel.setDescripcion(etDescripcion.getText().toString());
        tareaViewModel.setDocumento(tvDocumento.getText().toString());
        tareaViewModel.setImagen(tvImagen.getText().toString());
        tareaViewModel.setAudio(tvAudio.getText().toString());
        tareaViewModel.setVideo(tvVideo.getText().toString());
    }

    //Método para convertir una uri en una ruta con timeStamp como nombre de archivo atendiendo a que el archivo puede ser un documento, una imagen, un audio o un video, dependiendo su extensión. Su nombre sólo estará compuesto por el tipo de archivo más un timeStamp.
    private String uriToRuta(String uri) {
        if(uri != null) {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            return timeStamp;
        }
        return null;
    }

//    private String generarRutaConTimeStamp(String rutaOriginal, String tipoRecurso) {
//        if (!rutaOriginal.isEmpty()) {
//            // Obtener la extensión del archivo original
//            String extension = rutaOriginal.substring(rutaOriginal.lastIndexOf("."));
//
//            // Generar el timestamp
//            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
//
//            // Crear la ruta corta con tipo de recurso, timestamp y extensión
//            String rutaCorta = tipoRecurso.substring(0, 3) + "_" + timeStamp + extension;
//
//            // Asegurar que la longitud de la ruta no supere los 24 caracteres
//            if (rutaCorta.length() > 24) {
//                rutaCorta = rutaCorta.substring(0, 24);
//            }
//
//            return rutaCorta;
//        } else {
//            return "";
//        }
//    }

    private String generarRutaConTimeStamp(String rutaOriginal, String tipoRecurso) {
        if (!rutaOriginal.isEmpty()) {
            // Obtener la extensión del archivo original
            String extension = rutaOriginal.substring(rutaOriginal.length()-4);

            // Extensiones según el tipo de recurso
            switch (tipoRecurso) {
                case "imagen":
                    extension = ".jpg";
                    break;
                case "documento":
                    extension = ".pdf";
                    break;
                case "audio":
                    extension = ".mp3";
                    break;
                case "video":
                    extension = ".mp4";
                    break;
                // Si el tipo de recurso no coincide con ninguno de los casos anteriores, no se cambia la extensión
                default:
                    break;
            }

            // Generar el timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

            // Crear la ruta corta con tipo de recurso, timestamp y extensión
            String rutaCorta = tipoRecurso.substring(0, 3) + "_" + timeStamp + extension;

            // Asegurar que la longitud de la ruta no supere los 24 caracteres
            if (rutaCorta.length() > 24) {
                rutaCorta = rutaCorta.substring(0, 24);
            }

            return rutaCorta;
        } else {
            return "";
        }
    }

    //Método para copiar el archivo que obtenemos de la selección en la carpeta files de la aplicación a la memoria interna del teléfono
    private void copiarArchivo(Uri uriArchivo, int requestCode) {
        try {
            // Obtener el tipo de recurso
            String tipoRecurso = getTipoRecurso(requestCode);

            // Obtener el directorio de destino basado en el estado del CheckBoxPreference
            File directorioDestino;
            if (sd_isEnabled()) {
                directorioDestino = getExternalFilesDirSDCard(tipoRecurso);
            } else {
                directorioDestino = getExternalFilesDir(tipoRecurso);
            }

            // Obtener el nombre del archivo original
            String nombreArchivoOriginal = getNombreArchivo(uriArchivo);

            // Crear el archivo de destino con el mismo nombre y extensión
            File archivoDestino = new File(directorioDestino, nombreArchivoOriginal);

            // Copiar el archivo
            InputStream inputStream = getContext().getContentResolver().openInputStream(uriArchivo);
            OutputStream outputStream = new FileOutputStream(archivoDestino);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getExternalFilesDirSDCard(String tipoRecurso) {
        File directorio = new File(getContext().getExternalFilesDir(null), tipoRecurso);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        return directorio;
    }

    private boolean sd_isEnabled() {
        return sharedPreferences.getBoolean("sd", false);
    }

    private String getNombreArchivo(Uri uri) {
        String nombreArchivo = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = getContext().getContentResolver();
            ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                String[] partes = uri.getPath().split("/");
                nombreArchivo = partes[partes.length - 1];
                parcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nombreArchivo;
    }


    //Método para obtener el directorio de destino. Si no existe, lo crea. Además le pasa el tipo de recurso para crear el directorio llamándolo con el nombre del tipo de recurso
    private File getExternalFilesDir(String tipoRecurso) {
        File directorio = new File(getContext().getExternalFilesDir(null), tipoRecurso);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        return directorio;
    }

    //Método para obtener el tipo de recurso según el requestCode
    private String getTipoRecurso(int requestCode) {
        switch (requestCode) {
            case DOCUMENTO:
                return "pdf";
            case FOTO:
                return "img";
            case AUDIO:
                return "aud";
            case VIDEO:
                return "vid";
            default:
                return "";
        }
    }


}
/*
    =================================================================================================================================================================
    METODOS QUE ESTABA TRABAJANDO PARA COPIAR EL ARCHIVO SELECCIONADO A LA MEMORIA INTERNA DEL TELÉFONO



    //Método para copiar el archivo que obtenemos de la selección en la carpeta files de la aplicación a la memoria interna del teléfono
    private void copiarArchivo(Uri uriArchivo, int requestCode) {
        try {
            // Obtemos el tipo de recurso
            String tipoRecurso = getTipoRecurso(requestCode);

            // Obtenemos el directorio de destino
            File directorioDestino = getExternalFilesDir(tipoRecurso);

            // Genera el nombre del fichero con timestamp
            String nombreFichero = generarRutaConTimeStamp(uriArchivo.toString(), tipoRecurso);
            File archivoDestino = new File(directorioDestino, nombreFichero);

            // Copia el archivo
            InputStream inputStream = getContext().getContentResolver().openInputStream(uriArchivo);
            OutputStream outputStream = new FileOutputStream(archivoDestino);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();


        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    //Método para obtener el directorio de destino. Si no existe, lo crea. Además le pasa el tipo de recurso para crear el directorio llamándolo con el nombre del tipo de recurso
    private File getExternalFilesDir(String tipoRecurso) {
        File directorio = new File(getContext().getExternalFilesDir(null), tipoRecurso);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        return directorio;
    }

    //Método para obtener el tipo de recurso según el requestCode
    private String getTipoRecurso(int requestCode) {
        switch (requestCode) {
            case DOCUMENTO:
                return "pdf";
            case FOTO:
                return "img";
            case AUDIO:
                return "aud";
            case VIDEO:
                return "vid";
            default:
                return "";
        }
    }


 */