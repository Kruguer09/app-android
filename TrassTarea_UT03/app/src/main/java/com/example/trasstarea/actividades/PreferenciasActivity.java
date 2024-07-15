package com.example.trasstarea.actividades;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.trasstarea.R;

public class PreferenciasActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // Objeto para acceder a las preferencias compartidas
    private SharedPreferences.OnSharedPreferenceChangeListener listener; // Listener para detectar cambios en las preferencias
    public SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        listener = (sharedPreferences, key) -> {
            if (key.equals("key_pref_theme") || key.equals("key_pref_font") || key.equals("key_reset") || key.equals("key_pref_criterio") || key.equals("key_pref_orden")){
                recreate(); // Vuelve a crear la actividad para aplicar el nuevo tema
            }
        };

        aplicarTema();
        // Registramos el listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        // Mostramos el botón de atrás en la barra de acción y establecemos las letras en color blanco para que pegue con el tema
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.title_activity_preferencias) + "</font>"));
        }

    }// Fin del método onCreate

    @Override
    protected void onResume() {
        super.onResume();
        // Cambiamos el tema de la actividad según la preferencia
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Eliminamos el listener cuando la actividad está en pausa
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    // Fragmento de preferencias que se muestra en la actividad
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference resetPreferences = findPreference("key_reset");
        }

    }

    // Método para gestionar el botón de atrás en la barra de acción
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Cerramos la actividad Preferencias cuando pulsamos el botón home (flecha superior izquierda del menú)
        if (item.getItemId() == android.R.id.home) {
            // Devolvemos el resultado OK a la actividad principal para que se recree y aplique el nuevo tema
            setResult(RESULT_OK);
            finish();
            // Devolvemos true para indicar que hemos gestionado el evento
            return true;
        }
        // Si no hemos gestionado el evento, lo delegamos al método de la superclase
        return super.onOptionsItemSelected(item);
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

    public void resetPreferences(View view) {
        // Mostrar un cuadro de diálogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.reset_preferences)
                .setMessage(R.string.reset_preferences_dialog_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    // Restablecer los valores de preferencias
                    resetPreferenceValues();

                })
                .setNegativeButton(R.string.cancel, null) // No hacer nada si el usuario cancela
                .show();
    }

    public void resetPreferenceValues(){
        editor = sharedPreferences.edit();
        editor.putBoolean("key_pref_theme", true);
        editor.putString("key_pref_font", "2");
        editor.putString("key_pref_criterio", "2");
        editor.putBoolean("key_pref_orden", true);
        editor.putBoolean("key_pref_sd", false);
        editor.putString("key_pref_limpieza", "0");
        editor.putBoolean("key_pref_database", false);
        editor.putBoolean("key_pref_backup", false);
        editor.putString("key_servidorexterno", "");
        editor.putString("key_ip_servidor", "10.0.2.2");
        editor.putString("key_ip_puerto", "1001");
        editor.putString("key_usuario", "");
        editor.putString("key_password", "");

        editor.apply();

        Toast.makeText(this, "Preferencias restablecidas", Toast.LENGTH_SHORT).show();
    }

}

