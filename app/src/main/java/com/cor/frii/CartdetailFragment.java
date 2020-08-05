package com.cor.frii;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
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
import com.cor.frii.Login.LoginActivity;
import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.Session;
import com.cor.frii.persistence.entity.Acount;
import com.cor.frii.persistence.entity.ECart;
import com.cor.frii.utils.MapSelection;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CartdetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CartdetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartdetailFragment extends Fragment implements CartDetailAdapter.EventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private String baseURL = "http://34.71.251.155/api";
    private String HOST_NODEJS = "http://34.71.251.155:9000";
    private static final String TAG = "GAS";
    private Socket socket;
    private CartDetailAdapter cartDetailAdapter;
    private RecyclerView recyclerView;
    List<ECart> cartDetails;
    Button procesarPedido;
    private TextView lblTotal;
    private RadioButton voucher;
    public CartdetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartdetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartdetailFragment newInstance(String param1, String param2) {
        CartdetailFragment fragment = new CartdetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Validar informacion del usuario
        Session session = new Session(getContext());
        final int token = session.getToken();
        if (token == 0 || token < 0) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            Objects.requireNonNull(getActivity()).finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }
        //--
        initSocket();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cartdetail, container, false);
        recyclerView = view.findViewById(R.id.CartDetailContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lblTotal = view.findViewById(R.id.lblTotal);
        voucher = view.findViewById(R.id.RadioButtonBoleta);
        llenarCarrito();

        procesarPedido = view.findViewById(R.id.ButtonCartProcesarPedido);

        List<ECart> eCarts = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getCartDao()
                .getCarts();

        assert eCarts != null;
        if (eCarts.size() == 0) {
            procesarPedido.setEnabled(false);
            procesarPedido.setBackgroundResource(R.drawable.custom_button_gray);
        } else {
            procesarPedido.setEnabled(true);
        }
        procesarPedido.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmarPedido();
            }
        });
      /*  procesarPedido.setOnClickListener(new View.OnClickListener() {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            @Override
            public void onClick(View v) {
                /*
                ProcesarpedidoFragment procesarpedidoFragment = new ProcesarpedidoFragment();
                transaction.replace(R.id.navigationContainer, procesarpedidoFragment);
                transaction.addToBackStack(null);
                transaction.commit();


            }
        });*/


        return view;
    }
    // Confirmar pedido
    private void confirmarPedido() {
        //Obtener el token del cliente
        final Acount acount = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(new Session(getContext()).getToken());

   //     if (lblDireccion.getText().toString().length() == 0 && lblDireccion.getText().toString().equals("")) {
     //       return;
       // }
        String comprobante;
        switch (voucher.getText().toString()) {
            case "Factura":
                comprobante = "factura";
                break;
            case "Pagar con tarjeta":
                comprobante = "tarjeta";
                break;
            default:
                comprobante = "boleta";
        }

        JSONObject jsonObject = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        try {
            jsonObject.put("voucher", comprobante);
            jsonObject.put("latitud", String.valueOf(MapSelection.latitud));
            jsonObject.put("longitud", String.valueOf(MapSelection.longitud));
            jsonObject.put("client_id", new Session(getContext()).getToken());

            JSONArray jsonArray = new JSONArray();
            List<ECart> eCarts = DatabaseClient.getInstance(getContext())
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts();

            if (eCarts != null) {
                for (ECart e : eCarts) {
                    JSONObject orders_detal = new JSONObject();
                    orders_detal.put("product_id", e.getProductRegister());
                    orders_detal.put("quantity", e.getCantidad());
                    orders_detal.put("unit_price", e.getPrice());
                    jsonArray.put(orders_detal);
                }
            }

            jsonObject.put("detalle_orden", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = this.baseURL + "/client/order/";
        System.out.println(jsonObject.toString());
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int status = response.getInt("status");
                            if (status == 201) {
                                String message = response.getString("message");
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();


                                JSONObject datas = new JSONObject();
                                datas.put("id", response.getJSONObject("data").getInt("order_id"));
                                datas.put("latitude", MapSelection.latitud    );
                                datas.put("longitude", MapSelection.longitud);
                                socket.emit("get orders", datas);

                                DatabaseClient.getInstance(getContext())
                                        .getAppDatabase()
                                        .getCartDao()
                                        .deleteAllCart();

                                Intent intent = new Intent(getContext(), PedidosActivity.class);
                                startActivity(intent);
                                getActivity().finish();
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

        queue.add(jsonObjectRequest);
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void calcularTotal(float total) {
        if (total == 0) {

            procesarPedido.setBackgroundResource(R.drawable.custom_button_gray);
            procesarPedido.setEnabled(false);
        }

        lblTotal.setText(String.valueOf(total));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void llenarCarrito() {
        cartDetails = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getCartDao()
                .getCarts();

        cartDetailAdapter = new CartDetailAdapter(cartDetails, this);
        recyclerView.setAdapter(cartDetailAdapter);


    }

    private void initSocket() {

        int id_user = new Session(getContext()).getToken();
        JSONObject data = new JSONObject();
        Acount cuenta = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(id_user);

        final JSONObject json_connect = new JSONObject();
        IO.Options opts = new IO.Options();
        // opts.forceNew = true;
        opts.reconnection = true;
        opts.query = "auth_token=thisgo77";
        try {
            json_connect.put("ID", "US01");
            json_connect.put("TOKEN", cuenta.getToken());
            json_connect.put("ID_CLIENT", id_user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            socket = IO.socket(HOST_NODEJS, opts);
            socket.connect();
            // SOCKET.io().reconnectionDelay(10000);
            Log.d(TAG, "Node connect ok");
            //conect();
        } catch (URISyntaxException e) {
            Log.d(TAG, "Node connect error");
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "emitiendo new conect");
                JSONObject data = new JSONObject();
                int id = new Session(getContext()).getToken();
                Acount cuenta = DatabaseClient.getInstance(getContext())
                        .getAppDatabase()
                        .getAcountDao()
                        .getUser(id);
                try {
                    data.put("ID", cuenta.getId());
                    data.put("type", "client");
                    Log.d(TAG, "conect " + data.toString());
                    socket.emit("new connect", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                Log.d(TAG, "SERVER connect " + date);


            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                Log.d(TAG, "SERVER disconnect " + date);
            }
        });

        socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                Log.d(TAG, "SERVER reconnect " + my_date);
            }
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                Log.d(TAG, "SERVER timeout " + my_date);
            }
        });

        socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                Log.d(TAG, "SERVER reconnecting " + my_date);
            }
        });


    }
}
