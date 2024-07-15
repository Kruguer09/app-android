package com.example.trasstarea.adaptadores;

import static java.security.AccessController.getContext;
import static java.util.Comparator.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.trasstarea.R;
import com.example.trasstarea.actividades.EditarTareaActivity;
import com.example.trasstarea.modelo.Tarea;


public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TaskViewHolder> {

    private Context contexto = null;
    private ArrayList<Tarea> tasks;
    private Tarea tareaSeleccionada;
    private boolean boolPrior;

    public void setBoolPrior(boolean boolPrior) {
        this.boolPrior = boolPrior;
    }


    //Constructor
    public TareaAdapter(Context contexto, ArrayList<Tarea> tasks, boolean boolPrior) {
        this.contexto = contexto;
        this.tasks = tasks;
        this.boolPrior = boolPrior;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View elemento = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista, parent, false);
        return new TaskViewHolder(elemento);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder taskViewHolder, int position) {

        Tarea tarea = tasks.get(position);
        taskViewHolder.bind(tarea);
        taskViewHolder.itemView.setTag(tarea); //Adjuntamos la tarea en la vista del ViewHolder
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void mostrarOcultarFavoritos(ArrayList<Tarea> nuevaLista) {
        tasks = nuevaLista;
        notifyDataSetChanged();
    }

    @Nullable
    public Tarea getTareaSeleccionada() {
        return tareaSeleccionada;
    } //Pasar la tarea seleccionada a la actividad


    ////////////////////////////////////////////////////////////////////////////////////////////////
    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tarea;
        private final ProgressBar pgProgreso;
        private final TextView tvFechaCreacion;
        private final TextView tvDias;
        private ImageView favorito;

        //Método constructor
        private TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tarea = itemView.findViewById(R.id.elemento_tv_titulo);
            pgProgreso = itemView.findViewById(R.id.elemento_progressbar);
            tvFechaCreacion = itemView.findViewById(R.id.elemento_tv_fecha);
            tvDias = itemView.findViewById(R.id.elemento_tv_ndias);
            favorito = itemView.findViewById(R.id.iv_star);

            //Mostrar el menú contextual
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                //Hacemos la instantánea de cuál es la tarea seleccionada en el momento que se crea el viewHolder
                tareaSeleccionada = tasks.get(getAdapterPosition());
                //Mostramos el menú contextual
                MenuInflater inflater = new MenuInflater(contexto);
                inflater.inflate(R.menu.menu_contextual, menu);
            });

            //establezco un listener en la estrella de favorito para que cambie de valor
            favorito.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    escucharFavorito(position);
                }
            });

        }

        private void escucharFavorito(int position) {
            //obtengo la posición del view que pulso
            Tarea tarea = tasks.get(position);

            //invierto el valor de favorito en el momento en el que se encuentre
            tarea.setPrioritaria(!tarea.isPrioritaria());

            //Notifico al adaptador que el item en la posición dada ha cambiado para que se actualice visualmente
            notifyItemChanged(position);
        }

        //Método que nos permitirá configurar cada elemento del Recycler con las informaciones
        //de la tarea
        private void bind(Tarea tareaActual) {
            tarea.setText(tareaActual.getTitulo());
            pgProgreso.setProgress(tareaActual.getProgreso());
            tvFechaCreacion.setText(String.valueOf(tareaActual.getFechaCreacionString()));

            int dias = 0;
            tareaActual.quedanDias();

            //Obtenemos el paint de la tarea para poder modificarlo
            TextPaint paint = tarea.getPaint();

            //Comprobación de tarea completada
            if (tareaActual.getProgreso() < 100) { //Si la tarea no está completada
                dias = tareaActual.quedanDias();
                //Comprobación de días negativos para color rojo
                if (dias < 0) {
                    // Establece el color al original del texto del tema
                    tvDias.setTextColor(contexto.getColor(R.color.tareaPasada)); // Establece el color a rojo
                }
                //Si la tarea no está completa, aseguramos que el título no esté tachado
                tarea.setPaintFlags(tarea.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                //Si no está completada, establecemos los días según el método quedanDias()
                tvDias.setText(String.format(Integer.toString(dias)));
            } else { //Si la tarea está completada
                //Si la tarea está completada, agregamos el tachado al título
                tarea.setPaintFlags(tarea.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                //Establecemos los días a 0
                dias = 0;
                tvDias.setText(String.valueOf(dias));
            }

            //Establecemos el título según corresponda
            tarea.setText(tareaActual.getTitulo());

            //Configuramos la imagen de favorito según la prioridad
            if (tareaActual.isPrioritaria()) {
                favorito.setImageResource(R.drawable.prioritaria_star);
                tarea.setTypeface(null, Typeface.BOLD); // método de la clase Typeface para poner la letra en negrita
            } else {
                favorito.setImageResource(R.drawable.no_prioritaria_star);
                tarea.setTypeface(null, Typeface.NORMAL);
            }
        }

    }


}