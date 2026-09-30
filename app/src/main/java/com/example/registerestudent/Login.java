package com.example.registerestudent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private EditText usuario;
    private EditText clave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuario = findViewById(R.id.edtLoginUsuario);
        clave = findViewById(R.id.edtLoginClave);
    }

    public void autenticar(View v) {
        String usuarioIngresado = usuario.getText().toString().trim();
        String claveIngresada = clave.getText().toString();

        if (usuarioIngresado.isEmpty() || claveIngresada.isEmpty()) {
            mostrarError("Debe ingresar usuario y contraseña");
            return;
        }

        SharedPreferences shp = getSharedPreferences("usuariosAcademico", Context.MODE_PRIVATE);
        String catalogoActual = shp.getString("Catalogo_Usuarios", "-1");

        // Si no hay ningún usuario almacenado, la autenticación falla
        if (catalogoActual.equals("-1")) {
            mostrarError("USUARIO/CLAVE INCORRECTOS");
            return;
        }

        try {
            JSONArray jsaArreglo = new JSONArray(catalogoActual);

            // Se cifra la contraseña de la pantalla para compararla con la almacenada
            String claveCifrada = Seguridad.sha256(claveIngresada);

            for (int i = 0; i < jsaArreglo.length(); i++) {
                JSONObject jsoElemento = jsaArreglo.getJSONObject(i);

                if (jsoElemento.optString("usuario").equals(usuarioIngresado)
                        && jsoElemento.optString("clave").equals(claveCifrada)) {
                    // Usuario y contraseña coinciden: se invoca la Activity principal
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return;
                }
            }

            mostrarError("USUARIO/CLAVE INCORRECTOS");
        }
        catch (Exception e) {
            mostrarError("Error ocurrido->" + e.getMessage());
        }
    }

    public void invocaRegistroEstudiante(View v) {
        startActivity(new Intent(this, RegistroEstudiante.class));
    }

    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("ERROR DE AUTENTICACIÓN")
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("ACEPTAR", (dialogo, i) -> dialogo.cancel())
                .show();
    }
}
