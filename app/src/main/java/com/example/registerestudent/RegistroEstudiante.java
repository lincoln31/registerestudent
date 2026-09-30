package com.example.registerestudent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegistroEstudiante extends AppCompatActivity {

    // Se declaran variables para controlar los objetos en la pantalla
    private EditText numDoc;
    private EditText nombres;
    private EditText apellidos;
    private EditText correo;
    private EditText usuario;
    private EditText clave;
    private EditText confirmaClave;
    private Spinner listaTD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_estudiante);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Se capturan los objetos de la pantalla
        numDoc = findViewById(R.id.edtNumDoc);
        nombres = findViewById(R.id.edtNombres);
        apellidos = findViewById(R.id.edtApellidos);
        correo = findViewById(R.id.edtCorreo);
        usuario = findViewById(R.id.edtUsuario);
        clave = findViewById(R.id.edtClave);
        confirmaClave = findViewById(R.id.edtConfirmaClave);
        listaTD = findViewById(R.id.spnTD);

        // Arreglo de opciones para el tipo de documento
        String[] strOpcTD = {
                "Seleccione una opción"
                , "Tarjeta de Identidad"
                , "Cédula de Ciudadanía"
                , "Cédula de Extranjería"
                , "Pasaporte"
                , "Permiso Protección Temporal"
        };

        // Adaptador para conectarse con el Spinner
        ArrayAdapter<String> adaptadorListaTD = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, strOpcTD);
        adaptadorListaTD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Se asocia el Spinner con el adaptador
        listaTD.setAdapter(adaptadorListaTD);
    }

    public void registraEstudiante(View v) {
        String mensaje = "";

        String correoTexto = correo.getText().toString().trim();
        String usuarioTexto = usuario.getText().toString().trim();
        String claveTexto = clave.getText().toString();
        String confirmaTexto = confirmaClave.getText().toString();

        if (listaTD.getSelectedItem().toString().equals("Seleccione una opción")) {
            mensaje += "\nERROR falta ingresar: Tipo Documento";
        }
        if (numDoc.getText().toString().trim().isEmpty()) {
            mensaje += "\nERROR falta ingresar: Número Documento";
        }
        if (nombres.getText().toString().trim().isEmpty()) {
            mensaje += "\nERROR falta ingresar: Nombres";
        }
        if (apellidos.getText().toString().trim().isEmpty()) {
            mensaje += "\nERROR falta ingresar: Apellidos";
        }
        if (correoTexto.isEmpty()) {
            mensaje += "\nERROR falta ingresar: Correo Electrónico";
        }
        else if (!correoTexto.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            mensaje += "\nERROR estructura correo electrónico: ejemplo@gmail.com";
        }
        if (usuarioTexto.isEmpty()) {
            mensaje += "\nERROR falta ingresar: Usuario";
        }
        if (claveTexto.isEmpty()) {
            mensaje += "\nERROR falta ingresar: Contraseña";
        }
        if (confirmaTexto.isEmpty()) {
            mensaje += "\nERROR falta ingresar: Confirme Contraseña";
        }
        if (!claveTexto.isEmpty() && !confirmaTexto.isEmpty() && !claveTexto.equals(confirmaTexto)) {
            mensaje += "\nERROR la Contraseña y Confirme Contraseña no coinciden";
        }

        if (!mensaje.isEmpty()) {
            mostrarDialogo("ERROR OCURRIDO", mensaje.trim(), "CORREGIR");
            return;
        }

        // Se crea el archivo dentro del dispositivo
        SharedPreferences shp = getSharedPreferences("usuariosAcademico", Context.MODE_PRIVATE);

        // Se lee si actualmente ya existe un catálogo de usuarios almacenado
        String catalogoActual = shp.getString("Catalogo_Usuarios", "-1");

        try {
            JSONArray jsaArreglo;
            if (catalogoActual.equals("-1")) {
                // No existen usuarios en el archivo, se crea el arreglo JSON vacío
                jsaArreglo = new JSONArray();
            }
            else {
                jsaArreglo = new JSONArray(catalogoActual);
            }

            // No se permiten nombres de usuario repetidos
            for (int i = 0; i < jsaArreglo.length(); i++) {
                if (jsaArreglo.getJSONObject(i).optString("usuario").equals(usuarioTexto)) {
                    mostrarDialogo("ERROR OCURRIDO", "El usuario '" + usuarioTexto + "' ya existe", "CORREGIR");
                    return;
                }
            }

            // Se agrega el estudiante actual dentro del arreglo JSON
            JSONObject jsoElemento = new JSONObject();
            jsoElemento.put("tipo_documento", listaTD.getSelectedItem().toString());
            jsoElemento.put("num_documento", numDoc.getText().toString().trim());
            jsoElemento.put("nombres", nombres.getText().toString().trim());
            jsoElemento.put("apellidos", apellidos.getText().toString().trim());
            jsoElemento.put("correo_electronico", correoTexto);
            jsoElemento.put("usuario", usuarioTexto);
            // La contraseña nunca se guarda en claro: se almacena su hash SHA-256
            jsoElemento.put("clave", Seguridad.sha256(claveTexto));
            jsaArreglo.put(jsoElemento);

            // Se escribe y se guardan los cambios
            SharedPreferences.Editor edt = shp.edit();
            edt.putString("Catalogo_Usuarios", jsaArreglo.toString());
            edt.apply();
        }
        catch (Exception e) {
            mostrarDialogo("ERROR ALMACENANDO ESTUDIANTE", "Error ocurrido->" + e.getMessage(), "ACEPTAR");
            return;
        }

        mostrarDialogo("CONFIRMACIÓN EXITOSA", "Usuario Registrado Exitosamente", "ACEPTAR");
        limpiaFormulario(null);
    }

    public void limpiaFormulario(View v) {
        numDoc.setText("");
        nombres.setText("");
        apellidos.setText("");
        correo.setText("");
        usuario.setText("");
        clave.setText("");
        confirmaClave.setText("");
        listaTD.setSelection(0);
    }

    public void regresaMenuPpal(View v) {
        // Se regresa a la pantalla de Login
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void mostrarDialogo(String titulo, String mensaje, String textoBoton) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton(textoBoton, (dialogo, i) -> dialogo.cancel())
                .show();
    }
}
