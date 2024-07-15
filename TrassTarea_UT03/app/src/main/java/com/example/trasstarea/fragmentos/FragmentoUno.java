package com.example.trasstarea.fragmentos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import com.example.trasstarea.R;
import com.example.trasstarea.adaptadores.TareaViewModel;

public class FragmentoUno extends Fragment {

    private TareaViewModel tareaViewModel;
    private EditText etTitulo, etFechaCreacion, etFechaObjetivo;
    private Spinner spProgreso;
    private CheckBox cbPrioritaria;

    private SharedPreferences sharedPreferences;

    //Interfaz de comunicación con la actividad
    public interface ComunicacionPrimerFragmento {
        void onBotonSiguienteClicked();
        void onBotonCancelarClicked();
    }

    private ComunicacionPrimerFragmento comunicadorPrimerFragmento;

    public FragmentoUno(){}

    //Sobrescribimos onAttach para establecer la comunicación entre el fragmento y la actividad
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ComunicacionPrimerFragmento) {  //Si la clase implementa la interfaz
            comunicadorPrimerFragmento = (ComunicacionPrimerFragmento) context; //La clase se convierte en escuchadora
        } else {
            throw new ClassCastException(context + " debe implementar interfaz de comunicación con el primer fragmento");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //Vinculamos el fragmento con el ViewModel
        tareaViewModel = new ViewModelProvider(requireActivity()).get(TareaViewModel.class);

        //Inflamos el fragmento
        return inflater.inflate(R.layout.fragment_primero, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Binding y set EditText Título
        etTitulo = view.findViewById(R.id.et_titulo);
        tareaViewModel.getTitulo().observe(getViewLifecycleOwner(), s -> etTitulo.setText(s));

        //Binding, config y set EditText Fechas
        etFechaCreacion = view.findViewById(R.id.et_fecha_creacion);
        tareaViewModel.getFechaCreacion().observe(getViewLifecycleOwner(), s -> etFechaCreacion.setText(s));
        etFechaCreacion.setOnClickListener(this::escuchadorFecha);

        etFechaObjetivo = view.findViewById(R.id.et_fecha_objetivo);
        tareaViewModel.getFechaObjetivo().observe(getViewLifecycleOwner(), s -> etFechaObjetivo.setText(s));
        etFechaObjetivo.setOnClickListener(this::escuchadorFecha);

        //Binding, config y set Spinner Progreso
        spProgreso = view.findViewById(R.id.sp_progreso);
        String[] progresos = requireContext().getResources().getStringArray(R.array.progreso_texto);
        ArrayAdapter<String> adaptadorProgreso = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, progresos);
        adaptadorProgreso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProgreso.setAdapter(adaptadorProgreso);
        tareaViewModel.getProgreso().observe(getViewLifecycleOwner(), intProgreso -> spProgreso.setSelection(intProgreso / 25));
        //Situamos el spinner en la posición calculada como progreso/25

        //Binding y set CheckBox Prioritaria
        cbPrioritaria = view.findViewById(R.id.cb_prioritaria);
        tareaViewModel.isPrioritaria().observe(getViewLifecycleOwner(), aBoolean -> cbPrioritaria.setChecked(aBoolean));

        //Binding y config Button Siguiente
        Button siguiente = view.findViewById(R.id.bt_siguiente);
        siguiente.setOnClickListener(v -> {
            //Validación de campos
            if (etTitulo.getText().toString().isEmpty()
                    || etFechaCreacion.getText().toString().isEmpty()
                    || etFechaObjetivo.getText().toString().isEmpty()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.ad_campos_incompletos))
                        .setMessage(getString(R.string.ad_campos_contenido))
                        .setPositiveButton(R.string.bt_aceptar, (dialog, id) -> {
                            dialog.dismiss();
                        })
                        .show();

            } //Validación de fechas
            else if(!compararFechas(etFechaObjetivo.getText().toString(), etFechaCreacion.getText().toString())) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.ad_fechas))
                        .setMessage(getString(R.string.ad_fechas_contenido))
                        .setPositiveButton(R.string.bt_aceptar, (dialog, id) -> {
                            dialog.dismiss();
                        })
                        .show();
            }else {
                //Escribimos en el ViewModel
                escribirViewModel();
                //Llamamos al método onBotonSiguienteClicked que está implementado en la actividad.
                if (comunicadorPrimerFragmento != null) {
                    comunicadorPrimerFragmento.onBotonSiguienteClicked();
                }
            }
        });

        //Binding y config Button Cancelar
        Button cancelar = view.findViewById(R.id.bt_cancelar);
        cancelar.setOnClickListener(v -> {
            //Llamamos al método onBotonCancelarClicked que está implementado en la actividad.
            if (comunicadorPrimerFragmento != null) {
                comunicadorPrimerFragmento.onBotonCancelarClicked();
            }
        });
    }

    //Método para guardar el estado del formulario en un Bundle
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("titulo",  etTitulo.getText().toString());
        outState.putString("fechaCreacion", etFechaCreacion.getText().toString());
        outState.putString("fechaObjetivo", etFechaObjetivo.getText().toString());
        outState.putInt("progreso", spProgreso.getSelectedItemPosition());
        outState.putBoolean("prioritaria", cbPrioritaria.isChecked());
        //Sincronizamos la información salvada también en el ViewModel
        escribirViewModel();
    }

    //Método para restaurar el estado del formulario
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            etTitulo.setText(savedInstanceState.getString("titulo"));
            etFechaCreacion.setText(savedInstanceState.getString("fechaCreacion"));
            etFechaObjetivo.setText(savedInstanceState.getString("fechaObjetivo"));
            spProgreso.setSelection(savedInstanceState.getInt("progreso"));
            cbPrioritaria.setChecked(savedInstanceState.getBoolean("prioritaria"));
        }

    }

    private void escribirViewModel(){
        tareaViewModel.setTitulo(etTitulo.getText().toString());
        tareaViewModel.setFechaCreacion(etFechaCreacion.getText().toString());
        tareaViewModel.setFechaObjetivo(etFechaObjetivo.getText().toString());
        tareaViewModel.setProgreso(25 * spProgreso.getSelectedItemPosition());
        tareaViewModel.setPrioritaria(cbPrioritaria.isChecked());
    }

    private void escuchadorFecha(View view) {
        DatePickerFragment fragmentoFecha = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int anno, int mes, int dia) {
                final String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, (mes + 1), anno);
                // mes+1 porque Enero es 0
                ((EditText) view).setText(selectedDate);
            }
        });
        //Leemos qué EditText está siendo utilizado
        int id = view.getId();
        String fechaSeleccionada = null;
        if (id == R.id.et_fecha_creacion)
            fechaSeleccionada = tareaViewModel.getFechaCreacion().getValue();
        else if (id == R.id.et_fecha_objetivo)
            fechaSeleccionada = tareaViewModel.getFechaObjetivo().getValue();

        Bundle bundle = new Bundle();
        //Metemos en el bundle un array de enteros para dia, mes y año de la fecha seleccionada
        if (fechaSeleccionada != null)
            bundle.putIntArray("fechaSeleccionada", getDMA(fechaSeleccionada));
        fragmentoFecha.setArguments(bundle);
        fragmentoFecha.show(requireActivity().getSupportFragmentManager(), "datePicker");
    }


    private int[] getDMA(String fechaLeida){
        int[] dma = new int[3];
        try {
            //Intentamos leer la fecha con formato dd/mm/aaaa
            String[] cadena = fechaLeida.split("/");
            //Leemos el dia
            dma[0] = Integer.parseInt(cadena[0]);
            //Leemos el mes
            dma[1] = Integer.parseInt(cadena[1]) - 1; //Enero es el mes 0
            //Leemos el año
            dma[2] = Integer.parseInt(cadena[2]);
        } catch (Exception e) {
            //si algo falla, mostramos el error y ponemos la fecha de hoy
            Log.e ("Error formato fecha", Objects.requireNonNull(e.getMessage()));
            final Calendar c = Calendar.getInstance();
            //dia
            dma[0] = c.get(Calendar.DAY_OF_MONTH);
            //mes
            dma[1] = c.get(Calendar.MONTH);
            //año
            dma[2] = c.get(Calendar.YEAR);
        }
        return dma;
    }

    private boolean compararFechas(String fechaSel, String fechaCrea) {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            // Convertir las cadenas de fecha a objetos Date
            Date fechaSeleccionada = formatoFecha.parse(fechaSel);
            Date fechaCreacion = formatoFecha.parse(fechaCrea);

            // Comparar las fechas
            return !fechaSeleccionada.before(fechaCreacion);

        } catch (ParseException e) {
            e.printStackTrace(); // Manejar la excepción si ocurre un error de formato
            return false; // Retornar false en caso de error
        }
    }
}