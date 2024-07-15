package com.example.trasstarea.fragmentos;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;

    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            if (getArguments() != null) {
                int[] dmaSeleccionada = getArguments().getIntArray("fechaSeleccionada");
                if(dmaSeleccionada != null) {
                    int diaSel = dmaSeleccionada[0];
                    int mesSel = dmaSeleccionada[1];
                    int annoSel = dmaSeleccionada[2];

                    return new DatePickerDialog(requireActivity(), listener, annoSel, mesSel, diaSel);
                }
            }
        } catch (Exception ex) {
            Log.e("Excepción en la fecha del DatePicker", Objects.requireNonNull(ex.getMessage()));
        } catch (Error err){
            Log.e("Error en la fecha del DatePicker", Objects.requireNonNull(err.getMessage()));
        }
        //Creamos una instancia del día de hoy si hay un valor nulo
        final Calendar hoy = Calendar.getInstance();
        return new DatePickerDialog(requireActivity(), listener, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));
    }

}
