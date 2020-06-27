package com.cor.frii.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cor.frii.R;
import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.entity.Acount;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class NewAccountActivity extends AppCompatActivity {

    private TextInputEditText txtName, txtNumDocumento, txtCorreo, txtTelefono, txtDireccion,
            txtPass, txtPassRepeat;
    private Button crearCuenta;
    private TextView terminos_condiciones;


    //--
    public String baseUrl = "http://34.71.251.155";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        txtName = findViewById(R.id.txtNombre);
        txtNumDocumento = findViewById(R.id.txtDni);
        txtCorreo = findViewById(R.id.txtEmail);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtPass = findViewById(R.id.txtPassword);
        txtPassRepeat = findViewById(R.id.txtPasswordRepeat);

        crearCuenta = findViewById(R.id.CrearCuenta);

        terminos_condiciones=findViewById(R.id.terminos_condiciones);
        terminos_condiciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),TerminosCondicionesActivity.class);
                startActivity(intent);
            }
        });



        List<Acount> acounts = DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .getAcountDao()
                .getUsers();

        for (Acount a : acounts) {
            System.out.println(a.getNombre());
            System.out.println(a.getEmail());
            System.out.println(a.getId());
            System.out.println("---------");
        }


    }

    public void crearCuenta(View view) {
        String nombre = txtName.getText().toString();
        String numDocumento = txtNumDocumento.getText().toString();
        String email = txtCorreo.getText().toString();
        String telefono = txtTelefono.getText().toString();
        String direccion = txtDireccion.getText().toString();
        String pass = txtPass.getText().toString();
        String passrepeat = txtPassRepeat.getText().toString();

        if (!pass.equals(passrepeat)) {
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden, Intentelo nuevamente", Toast.LENGTH_LONG).show();
        } else {

            postRegister(nombre, numDocumento, email, telefono, direccion, pass);

        }

    }


    public void postRegister(final String nombre, final String num_documento, final String email, final String telefono,
                             final String direccion, final String pass) {
        String url = this.baseUrl + "/api/clients/create/";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();

        try {
            object.put("num_documento", num_documento);
            object.put("nombre", nombre);
            object.put("phone1", telefono);
            object.put("phone2", "");
            object.put("direccion", direccion);

            object.put("email", email);
            object.put("username", email);
            object.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            System.out.println(response);
                            int id = response.getJSONObject("data").getInt("company_id");

                            if (id != 0) {
                                Acount cuenta = new Acount();

                                cuenta.setId(id);
                                cuenta.setNombre(nombre);
                                cuenta.setNumDocumento(num_documento);
                                cuenta.setEmail(email);
                                cuenta.setPhoneOne(telefono);
                                cuenta.setPhoneTwo(null);
                                cuenta.setDireccion(direccion);
                                cuenta.setPassword(pass);

                                DatabaseClient.getInstance(getApplicationContext())
                                        .getAppDatabase()
                                        .getAcountDao()
                                        .addUser(cuenta);

                                Toast.makeText(getApplicationContext(), "Gracias por registrate, Ya puede Iniciar sesión", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("ENTRO AQUI...........");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley post", "error voley" + error.toString());
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                // Now you can use any deserializer to make sense of data
                                JSONObject jsonObject = new JSONObject(res);
                                String message = jsonObject.getString("message");
                                System.out.println(res);

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            } catch (UnsupportedEncodingException | JSONException e1) {
                                // Couldn't properly decode data to string
                                Toast.makeText(getApplicationContext(), "Error en operacion", Toast.LENGTH_SHORT).show();
                                e1.printStackTrace();
                            }
                        }
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
