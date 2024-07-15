package com.example.trasstarea.modelo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.trasstarea.adaptadores.DateConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
public class Tarea implements Parcelable {
    private static long contador_id = 0;

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    private long id;

    @NonNull
    @ColumnInfo(name = "titulo")
    private String titulo;

    @NonNull
    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "fechaCreacion")
    private Date fechaCreacion;

    @NonNull
    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "fechaObjetivo")
    private Date fechaObjetivo;

    @ColumnInfo(name = "progreso")
    private Integer progreso;

    @ColumnInfo(name = "prioritaria")
    private Boolean prioritaria;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    @ColumnInfo(name = "URL_doc")
    private String urlDocumento;

    @ColumnInfo(name = "URL_img")
    private String urlImagen;

    @ColumnInfo(name = "URL_aud")
    private String urlAudio;

    @ColumnInfo(name = "URL_vid")
    private String urlVideo;


    public Tarea() {
        this.id = ++contador_id;
    }

    // Constructor
    public Tarea(String titulo, String fechaCreacion, String fechaObjetivo, int progreso, boolean prioritaria, String descripcion) {
        this.id = ++contador_id;
        this.titulo = titulo;
        this.fechaCreacion = validarFecha(fechaCreacion);
        this.fechaObjetivo = validarFecha(fechaObjetivo);
        this.progreso = progreso;
        this.prioritaria = prioritaria;
        this.descripcion = descripcion;
    }

    public Tarea(String titulo, String fechaCreacion, String fechaObjetivo, Integer progreso, Boolean prioritaria, String descripcion, String urlDocumento, String urlimagen, String urlAudio, String urlVideo) {
        this.id = ++contador_id;
        this.titulo = titulo;
        this.fechaCreacion = validarFecha(fechaCreacion);
        this.fechaObjetivo = validarFecha(fechaObjetivo);
        this.progreso = progreso;
        this.prioritaria = prioritaria;
        this.descripcion = descripcion;
        this.urlDocumento = urlDocumento;
        this.urlImagen = urlimagen;
        this.urlAudio = urlAudio;
        this.urlVideo = urlVideo;
    }



    //////////////////////////////////////////////////////////////////////////////
    //                           GUETTERS & SETTERS                             //
    //////////////////////////////////////////////////////////////////////////////
    public long getId() {
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    //getTarea
    public Tarea getTarea(){
        return this;
    }


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }


    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public String getFechaCreacionString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fechaCreacion);
    }
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    public Date getFechaObjetivo() {
        return fechaObjetivo;
    }


    public void setFechaObjetivo(Date fechaObjetivo) {
        this.fechaObjetivo = fechaObjetivo;
    }


    public Integer getProgreso() {
        return progreso;
    }

    public void setProgreso(Integer progreso) {
        this.progreso = progreso;
    }


    public Boolean isPrioritaria() {
        return prioritaria;
    }

    public void setPrioritaria(Boolean prioritaria) {
        this.prioritaria = prioritaria;
    }



    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }



    public String getUrlDocumento() {
        return urlDocumento;
    }
    public void setUrlDocumento(String urlDocumento) {
        this.urlDocumento = urlDocumento;
    }

    public String getUrlImagen() {
        return urlImagen;
    }
    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getUrlVideo() {
        return urlVideo;
    }
    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getUrlAudio() {
        return urlAudio;
    }
    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }


    //////////////////////////////////////////////////////////////////////////////
    //                          OTROS MÉTODOS DE CLASE                          //
    //////////////////////////////////////////////////////////////////////////////

    @NonNull
    @Override
    public String toString() {
        return "Tarea: " + titulo + "\n" +"Descripcion='" + descripcion;
    }


    //Métodos equals y hashCode para que funcione bien buscar en la colección usando indexOf()
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Tarea tarea = (Tarea) other;
        return id == tarea.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //Métodos para la interfaz Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(titulo);
        dest.writeLong(fechaCreacion.getTime());
        dest.writeLong(fechaObjetivo.getTime());
        dest.writeInt(progreso);
        dest.writeByte((byte) (prioritaria ? 1 : 0));
        dest.writeString(descripcion);
        dest.writeString(urlDocumento);
        dest.writeString(urlImagen);
        dest.writeString(urlAudio);
        dest.writeString(urlVideo);
    }

    //Método para crear un objeto Tarea a partir de un Parcel
    protected Tarea(Parcel in) {
        id = in.readLong();
        titulo = in.readString();
        fechaCreacion = new Date(in.readLong());
        fechaObjetivo = new Date(in.readLong());
        progreso = in.readInt();
        prioritaria = in.readByte() != 0;
        descripcion = in.readString();
        urlDocumento = in.readString();
        urlImagen = in.readString();
        urlAudio = in.readString();
        urlVideo = in.readString();
    }



    //Método para crear un array de objetos Tarea de un tamaño determinado
    public static final Creator<Tarea> CREATOR = new Creator<Tarea>() {
        @Override
        public Tarea createFromParcel(Parcel source) {
            return new Tarea(source);
        }

        @Override
        public Tarea[] newArray(int size) {
            return new Tarea[size];
        }
    };


    //////////////////////////////////////////////////////////////////////////////
    //                          MÉTODOS PARA LAS FECHAS                         //
    //////////////////////////////////////////////////////////////////////////////

    public void setFechaObjetivoValida(String fechaObjetivo) {
        this.fechaObjetivo = validarFecha(fechaObjetivo);
    }


    public Date validarFecha(@NonNull String fecha){
        Date fechaDate = new Date(); //Para evitar devolver null
        if (validarFormatoFecha(fecha)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                fechaDate = sdf.parse(fecha);
            } catch (Exception e) {
                Log.e("Error fecha","Parseo de fecha no válido");
            }
        } else {
            Log.e("Error fecha","Formato de fecha no válido");
        }
        return fechaDate;
    }


    //Otros métodos
    private boolean validarFormatoFecha(@NonNull String fecha) {
        String regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(19|20)\\d\\d$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fecha);
        return matcher.matches();
    }

    //Método para calcular los días que quedan para la fecha objetivo
    public int quedanDias() {
        Date hoy = new Date(); // Obtener la fecha actual
        long diferenciaMillis = fechaObjetivo.getTime() - hoy.getTime();
        long diasDiferencia = TimeUnit.DAYS.convert(diferenciaMillis, TimeUnit.MILLISECONDS);
        return (int) diasDiferencia;
    }




}


