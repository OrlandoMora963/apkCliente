package com.cor.frii.Login;

import android.accounts.Account;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.cor.frii.MainActivity;
import com.cor.frii.R;
import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.Session;
import com.cor.frii.persistence.entity.Acount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private TextView NewAccount, forgottedPassword;
    private TextInputEditText password;
    //--
    public String baseUrl = "http://34.71.251.155";
    private String key = "friibusiness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.EmailLogin);
        password = findViewById(R.id.txtPassword);
        NewAccount = findViewById(R.id.NewAccountLogin);
        forgottedPassword = findViewById(R.id.ForgottenPasswordLogin);

        final Session s = new Session(getApplicationContext());
        if (s.getToken() > 0) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        NewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewAccountActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        forgottedPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ForgottenPasswordActivity.class);
                startActivity(intent);
            }
        });

    }

    // Login
    public void iniciarSesion(View view) {
        final String user = email.getText().toString();
        final String pass = password.getText().toString();

        final Session session = new Session(getApplicationContext());
        int token_app = session.getToken();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            object.put("username", user);
            object.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Metodo de login si es que existe conexion a INTERNET
        String url = this.baseUrl + "/api/auth/obtain_token/";
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("token");
                            if (!token.equals("")) {

                                JWT parsedJWT = new JWT(token);
                                Claim subscriptionMetaData = parsedJWT.getClaim("user_id");

                                int id_user = subscriptionMetaData.asInt();

                                // Si es que existe el usuario en la DB - pasar al main activity
                                // si no insertar el usuario  -> si es que el TOKEN es valido
                                Acount cuenta = DatabaseClient.getInstance(getApplicationContext())
                                        .getAppDatabase()
                                        .getAcountDao()
                                        .login(user, pass);

                                if (cuenta != null) {

                                    cuenta.setToken(token);
                                    DatabaseClient.getInstance(getApplicationContext())
                                            .getAppDatabase()
                                            .getAcountDao()
                                            .updateUser(cuenta);

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    session.setToken(cuenta.getId());

                                    saveTokenBackend(id_user, token);
                                    finish();
                                } else {
                                    insertarUsuario(token, id_user, pass);

                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                                JSONObject obj = new JSONObject(res);
                                System.out.println(obj.toString());

                                Toast.makeText(getApplicationContext(), "Credenciales invalidas", Toast.LENGTH_LONG).show();
                            } catch (UnsupportedEncodingException | JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }) {
                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        return super.parseNetworkResponse(response);
                    }
                };

        requestQueue.add(jsonObjectRequest);


        /*DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .getAcountDao()
                .deleteById(3);*/
        // Todo - Implementacion  de LOGIN si es que no tiene coneccion a INTERNET
        // code...
        /*Acount cuenta = new Acount();
        cuenta.setId(1);
        cuenta.setNumDocumento("71847204");
        cuenta.setNombre("Elvis Aldair");
        cuenta.setPhoneOne("1412165");
        cuenta.setPhoneTwo(null);
        cuenta.setDireccion("JR LIBERTADORES 160");
        cuenta.setEmail("aldaelvis@hotmail.com");
        cuenta.setPassword("aldair123");

        DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .getAcountDao()
                .addUser(cuenta);
*/


    }

    // Implementacion de datos de usuario del servidor
    public void datosUsuario(final String token, int id, final String pass) {

    }


    public void insertarUsuario(final String token, final int id, final String pass) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        Acount a = DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(id);

        if (a == null) {
            JSONObject object = new JSONObject();
            String url = baseUrl + "/api/clients/" + id;
            JsonObjectRequest jsonObjectRequest =
                    new JsonObjectRequest(Request.Method.GET, url, object, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response);
                            Acount cuenta = new Acount();
                            try {
                                int i = response.getJSONObject("client").getInt("client_id");
                                cuenta.setId(response.getJSONObject("client").getInt("client_id"));
                                cuenta.setNumDocumento(response.getJSONObject("client").getString("num_document"));
                                cuenta.setNombre(response.getJSONObject("client").getString("name"));
                                cuenta.setPhoneOne(response.getJSONObject("client").getString("phone1"));
                                cuenta.setPhoneTwo(response.getJSONObject("client").getString("phone2"));
                                cuenta.setDireccion(response.getJSONObject("client").getString("address"));
                                cuenta.setEmail(response.getString("user"));
                                cuenta.setPassword(pass);
                                cuenta.setToken(token);

                                DatabaseClient.getInstance(getApplicationContext())
                                        .getAppDatabase()
                                        .getAcountDao()
                                        .addUser(cuenta);


                                Session session = new Session(getApplicationContext());
                                session.setToken(i);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);


                                saveTokenBackend(i, token);

                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
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
                                    JSONObject obj = new JSONObject(res);
                                    System.out.println(res);
                                } catch (UnsupportedEncodingException | JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            Log.d("Voley get", token);
                            headers.put("Authorization", "JWT " + token);
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                    };

            requestQueue.add(jsonObjectRequest);
        } else {
            a.setToken(token);
            a.setPassword(pass);
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getAcountDao()
                    .updateUser(a);

            Session session = new Session(getApplicationContext());
            session.setToken(a.getId());
            saveTokenBackend(a.getId(), token);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private void saveTokenBackend(final int id, final String tokenClient) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Friibusiness", "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();

                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        JSONObject object = new JSONObject();
                        try {
                            object.put("token", token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String url = "http://34.71.251.155/api/device/save/";
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            int status = response.getInt("status");
                                            if (status == 200) {
                                                System.out.println("Mensaje: " + response.getString("message"));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
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
                                        JSONObject obj = new JSONObject(res);
                                        System.out.println(res);
                                    } catch (UnsupportedEncodingException | JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Authorization", "JWT " + tokenClient);
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }
                });
    }
}
