package com.example.geolocal_2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPhoneNumber;
    private Button buttonSave;
    private TextView textViewPhoneNumber;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener referencias a los elementos de la interfaz
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonSave = findViewById(R.id.buttonSave);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);

        // Obtener la instancia de SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Obtener el número guardado, si existe
        String savedPhoneNumber = sharedPreferences.getString("phoneNumber", "");
        if (!savedPhoneNumber.isEmpty()) {
            textViewPhoneNumber.setText("Número de teléfono guardado: " + savedPhoneNumber);
        }

        // Configurar el clic del botón Guardar
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el número ingresado
                String phoneNumber = editTextPhoneNumber.getText().toString();

                // Guardar el número en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("phoneNumber", phoneNumber);
                editor.apply();

                // Mostrar un Toast
                Toast.makeText(MainActivity.this, "Número guardado", Toast.LENGTH_SHORT).show();

                // Actualizar el TextView con el número guardado
                textViewPhoneNumber.setText("Número de teléfono guardado: " + phoneNumber);
            }
        });
    }
}
