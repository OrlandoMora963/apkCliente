package com.cor.frii.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cor.frii.R;
import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.Session;
import com.cor.frii.persistence.entity.Acount;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AgendarPedido extends Dialog implements View.OnClickListener {

    public Context c;
    public Dialog d;
    public Button btnAgendar, no;
    RatingBar calificacion;
    EditText fecha_notify;
    private int id_orden;
    private float calificationNumber;

    public AgendarPedido(Context context, int id, float cal) {
        super(context);
        this.c = context;
        this.id_orden = id;
        this.calificationNumber = cal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_agendar_pedido);
        no = (Button) findViewById(R.id.btnCancelar);
        calificacion = findViewById(R.id.ratingBar);
        fecha_notify = findViewById(R.id.seleccionarfecha);
        btnAgendar = findViewById(R.id.btnAgenda);
        no.setOnClickListener(this);
        btnAgendar.setOnClickListener(this);

        if (calificationNumber > 0) {
            calificacion.setRating(calificationNumber);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancelar:
                dismiss();
                break;

            case R.id.btnAgenda:
                agendarPedido();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void agendarPedido() {
        int token = new Session(getContext()).getToken();

        final Acount acount = DatabaseClient.getInstance(c)
                .getAppDatabase()
                .getAcountDao()
                .getUser(token);

        RequestQueue queue = Volley.newRequestQueue(c);
        JSONObject object = new JSONObject();
        try {
            object.put("id_orden", id_orden);
            object.put("id_client", token);

            if (fecha_notify.getText().length() > 0) {
                object.put("fecha_notify", fecha_notify.getText().toString());
            } else {
                object.put("fecha_notify", "");
            }

            if (calificacion.getRating() > 0) {
                object.put("calificacion", calificacion.getRating());
            } else {
                object.put("calificacion", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://34.71.251.155/api/diary/";

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST,
                url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");
                    if (status == 200) {
                        System.out.println(response.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley get", "error voley" + error.toString());
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        System.out.println(res);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                Log.d("Voley get", acount.getToken());
                headers.put("Authorization", "JWT " + acount.getToken());
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(objectRequest);
    }
}
