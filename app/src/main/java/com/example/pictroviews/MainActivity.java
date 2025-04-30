/**
 * MainActivity - Actividad principal de la aplicación PicTroViews
 *
 * Esta clase implementa la pantalla de inicio de la aplicación que muestra:
 * - Un botón para iniciar la funcionalidad principal (captura de fotos geolocalizadas)
 * - Un botón para salir de la aplicación
 *
 * Actúa como punto de entrada para el usuario y proporciona navegación
 * hacia la pantalla de captura de fotos (MenuApp).
 *
 * @author mbrown
 * @version 1.0
 */
package com.example.pictroviews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // Elementos de la interfaz de usuario
    private Button startButton;  // Botón para iniciar la aplicación
    private Button exitButton;   // Botón para salir de la aplicación

    /**
     * Inicializa la actividad y configura la interfaz de usuario.
     * Este método se ejecuta cuando se crea la actividad al iniciar la aplicación.
     *
     * @param savedInstanceState Estado guardado de la instancia de la actividad
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Establecer el layout de la actividad
        setContentView(R.layout.activity_main);

        // Inicializar los botones de la interfaz conectándolos con sus IDs en el layout
        startButton = findViewById(R.id.startButton);
        exitButton = findViewById(R.id.exitButton);

        // No se configuran listeners aquí porque se usan los onClick en el XML
    }

    /**
     * Cierra completamente la aplicación.
     * Este método está vinculado al botón "Salir" en el layout XML mediante android:onClick.
     *
     * El método finishAffinity() cierra todas las actividades de la aplicación en la pila
     * de actividades, terminando completamente el proceso de la aplicación.
     *
     * @param view Vista que desencadenó el evento (botón Salir)
     */
    public void closeApplication(View view) {
        finishAffinity();  // Cierra todas las actividades de la aplicación
    }

    /**
     * Abre la actividad MenuApp para iniciar la funcionalidad principal.
     * Este método está vinculado al botón "Iniciar" en el layout XML mediante android:onClick.
     *
     * Navega a la pantalla de captura de fotos con geolocalización (MenuApp).
     *
     * @param view Vista que desencadenó el evento (botón Iniciar)
     */
    public void openSecondActivity(View view) {
        // Crear un intent para abrir MenuApp
        Intent intent = new Intent(this, MenuApp.class);
        // Iniciar la actividad MenuApp
        startActivity(intent);
    }
}