package com.example.trasstarea.adaptadores;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TareaViewModel extends ViewModel {

    //Usamos MutableLiveData para evitar problemas de ciclo de vida de los fragmentos
    private final MutableLiveData<String> titulo = new MutableLiveData<>();
    private final MutableLiveData<String> fechaCreacion = new MutableLiveData<>();
    private final MutableLiveData<String> fechaObjetivo = new MutableLiveData<>();
    private final MutableLiveData<Integer> progreso = new MutableLiveData<>();
    private final MutableLiveData<Boolean> prioritaria = new MutableLiveData<>();
    private final MutableLiveData<String> descripcion = new MutableLiveData<>();
    private final MutableLiveData<String> documento = new MutableLiveData<>();
    private final MutableLiveData<String> imagen = new MutableLiveData<>();
    private final MutableLiveData<String> video = new MutableLiveData<>();
    private final MutableLiveData<String> audio = new MutableLiveData<>();


    //Métodos getter y setter
    public MutableLiveData<String> getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo.setValue(titulo);
    }

    public MutableLiveData<String> getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion.setValue(fechaCreacion);
    }

    public MutableLiveData<String> getFechaObjetivo() {
        return fechaObjetivo;
    }

    public void setFechaObjetivo(String fechaObjetivo) {
        this.fechaObjetivo.setValue(fechaObjetivo);
    }

    public MutableLiveData<Integer> getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso.setValue(progreso);
    }

    public MutableLiveData<Boolean> isPrioritaria() {
        return prioritaria;
    }

    public void setPrioritaria(boolean prioritaria) {
        this.prioritaria.setValue(prioritaria);
    }

    public MutableLiveData<String> getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.setValue(descripcion);
    }


    public MutableLiveData<String> getDocumento() {
        return documento;
    }

    public void setDocumento(String newDocumento) {
        documento.postValue(newDocumento);
    }


    public MutableLiveData<String> getImagen() {
        return imagen;
    }


    public void setImagen(String newImagen) {
        imagen.postValue(newImagen);
    }


    public MutableLiveData<String> getAudio() {
        return audio;
    }

    public void setAudio(String newAudio) {
        audio.postValue(newAudio);
    }


    public MutableLiveData<String> getVideo() {
        return video;
    }


    public void setVideo(String newVideo) {
        video.postValue(newVideo);
    }

    //Método para obtener una tarea
    public LiveData<Object> getTarea() { // Cambiar el tipo de dato
        LiveData<Object> liveData = new MutableLiveData<>();
        ((MutableLiveData<Object>) liveData).setValue(new Object()); // o liveData.postValue(new Object());
        return liveData;
    }

}