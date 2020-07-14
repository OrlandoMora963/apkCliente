package com.cor.frii;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.cor.frii.pojo.Order;
import com.cor.frii.utils.VolleySingleton;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;

import static androidx.core.content.ContextCompat.getSystemService;

public class MisPedidosFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Order> orders;
    private RecyclerView recyclerView;
    private MisPedidosAdapter misPedidosAdapter;
    //--
    private String baseURL = "http://34.71.251.155/api";
    static Socket SOCKET;
    public String HOST_NODEJS = "http://34.71.251.155:9000";
    //    public String HOST_NODEJS = "http://35.202.77.151";
    private final String TAG = "friibusiness";
    private Socket socket;
    RequestQueue queue;

    public MisPedidosFragment() {
        // Required empty public constructor
    }

    public static MisPedidosFragment newInstance(String param1, String param2) {
        MisPedidosFragment fragment = new MisPedidosFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initSocket();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mispedidos, container, false);

        //Iniciamos el socket para traer los pedidos
        orders = new ArrayList<>();
        llenarPedidos();

        recyclerView = view.findViewById(R.id.MisPedidosContainer);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //--


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MisPedidosFragment fragment = new MisPedidosFragment();
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.navigationContainer, fragment);
                fragmentTransaction.commit();
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //socket.on("status order", onStatusOrder);

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (ActivityMispedidosFragment.MyActivity == null)
            ActivityMispedidosFragment.MyActivity = (FragmentActivity) context;
        if (ActivityMispedidosFragment.context == null)
            ActivityMispedidosFragment.context = context;
        if (ActivityMispedidosFragment.Id == 0)
            ActivityMispedidosFragment.Id = MisPedidosFragment.this.getId();
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    // Llenar información de pedidos
    void llenarPedidos() {
        //--Usuario
        int token = new Session(getContext()).getToken();
        String url = this.baseURL + "/client/order/" + token;
        final Acount acount = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(token);
        JSONObject jsonObject = new JSONObject();

        queue = Volley.newRequestQueue(getContext());
        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            recyclerView.setAdapter(null);
                            orders.clear();
                            int status = response.getInt("status");
                            if (status == 200) {
                                JSONArray jsonArray = response.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    final Order order = new Order();
                                    order.setId(obj.getJSONObject("orden").getInt("id"));
                                    order.setDate(obj.getJSONObject("orden").getString("date"));
                                    order.setStatus(obj.getJSONObject("orden").getString("status"));
                                    order.setCalification((float) obj.getJSONObject("orden").getDouble("calification"));
                                    order.setClientDirection(new LatLng(
                                            obj.getJSONObject("orden").getDouble("latitude"),
                                            obj.getJSONObject("orden").getDouble("longitude")
                                    ));
                                    if (obj.getJSONObject("company").length() > 0) {
                                        order.setPhone(obj.getJSONObject("company").getString("phone"));
                                        order.setCompanyName(obj.getJSONObject("company").getString("name"));
                                        LatLng latLng = new LatLng(
                                                obj.getJSONObject("company").getDouble("latitude"),
                                                obj.getJSONObject("company").getDouble("longitude")
                                        );
                                        order.setCompanyDirection(latLng);
                                    }
                                    JSONArray details_data = obj.getJSONArray("order_detail");
                                    List<String> details = new ArrayList<>();
                                    for (int j = 0; j < details_data.length(); j++) {
                                        JSONObject jsonObject1 = details_data.getJSONObject(j);
                                        details.add(jsonObject1.getString("description"));
                                    }
                                    order.setDetalles(details);
                                    orders.add(order);
                                }
                                misPedidosAdapter = new MisPedidosAdapter(orders,MisPedidosFragment.this);
                                recyclerView.setAdapter(misPedidosAdapter);
                                misPedidosAdapter.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                                        FragmentTransaction transaction = manager.beginTransaction();
                                        String status = orders.get(recyclerView.getChildAdapterPosition(v)).getStatus();
                                        if (status.equals("confirm")) {
                                            MapsPerdidos misPedidosFragment = new MapsPerdidos();
                                            Bundle bundle = new Bundle();
                                            LatLng d_company = orders.get(recyclerView.getChildAdapterPosition(v)).getCompanyDirection();
                                            LatLng d_client = orders.get(recyclerView.getChildAdapterPosition(v)).getClientDirection();
                                            bundle.putParcelable("DCOMPANY", d_company);
                                            bundle.putParcelable("DCLIENT", d_client);
                                            misPedidosFragment.setArguments(bundle);
                                            transaction.add(R.id.navigationContainer, misPedidosFragment);
                                            transaction.addToBackStack(null);
                                            transaction.commit();
                                        }
                                    }
                                });
                                if (swipeRefreshLayout != null)
                                    swipeRefreshLayout.setRefreshing(false);
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
        queue.add(request);
    }


    private void initSocket() {
        int id_user = new Session(getContext()).getToken();
        final JSONObject data = new JSONObject();
        Acount cuenta = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(id_user);

        final JSONObject json_connect = new JSONObject();
        IO.Options opts = new IO.Options();
        //  opts.forceNew = true;
        opts.reconnection = true;
        opts.query = "auth_token=thisgo77";
        try {
            json_connect.put("ID", "US01");
            json_connect.put("TOKEN", cuenta.getToken());
            json_connect.put("ID_CLIENT", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            socket = IO.socket(HOST_NODEJS, opts);
            socket.connect();
            if (socket.connected())
                //   Toast.makeText(getContext(), "Socket Conectado", Toast.LENGTH_SHORT).show();
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

        final int token = new Session(getContext()).getToken();
        String url = this.baseURL + "/client/order/" + token;
        final Acount acount = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(token);
        final JSONObject datas = new JSONObject();
        try {
            datas.put("id", token);
            datas.put("token", acount.getToken());
            Log.d(TAG, "conect " + datas.toString());
            socket.emit("status order", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.on("confirm order client", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                try {
                    if (response.getJSONObject("data").getInt("order_id") > 0) {
                        ActivityMispedidosFragment.MyActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //    misPedidosAdapter.notifyDataSetChanged();
                                final JSONObject datas = new JSONObject();
                                try {
                                    datas.put("id", token);
                                    datas.put("token", acount.getToken());
                                    Log.d(TAG, "conect " + datas.toString());

                                    /*ActivityMispedidosFragment.MyActivity
                                    .getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(ActivityMispedidosFragment.Id, new MisPedidosFragment())
                                    .commit();*/
                                    String CHANNEL_ID = "channel1";
                                    orders.clear();
                                    llenarPedidos();

                                    NotificationManager notificationManager = (NotificationManager) ActivityMispedidosFragment.context.getSystemService(getContext().NOTIFICATION_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                                        NotificationChannel channel = new NotificationChannel(
                                                CHANNEL_ID,
                                                "Channel 1",
                                                NotificationManager.IMPORTANCE_HIGH
                                        );
                                        notificationManager.createNotificationChannel(channel);
                                    }

                                    android.app.Notification notification = new NotificationCompat.Builder(ActivityMispedidosFragment.context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.image)
                                            .setContentTitle("Pedido Confirmado")
                                            .setContentText("Un proveedor ya tiene su pedido")
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                            .setLights(Color.WHITE, 3000, 3000)
                                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                            .setColor(Color.BLUE)
                                            .setAutoCancel(true).build();
                                    // notificationId is a unique int for each notification that you must define
                                    //    NotificationManagerCompat notificationManager;
                                    //     notificationManager =   NotificationManagerCompat.from(getContext());
                                    notificationManager.notify(1, notification);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(response.toString());
            }
        });

        socket.on("delivered order client", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                try {
                    if (response.getJSONObject("data").getInt("order_id") > 0) {
                        ActivityMispedidosFragment.MyActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                final JSONObject datas = new JSONObject();
                                try {
                                    datas.put("id", token);
                                    datas.put("token", acount.getToken());
                                    Log.d(TAG, "conect " + datas.toString());
                                    /*ActivityMispedidosFragment.MyActivity.
                                            getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(ActivityMispedidosFragment.Id, new MisPedidosFragment())
                                            .commit();*/
                                    orders.clear();
                                    llenarPedidos();
                                    String CHANNEL_ID = "channel1";
                                    NotificationManager notificationManager = (NotificationManager) ActivityMispedidosFragment.context.getSystemService(getContext().NOTIFICATION_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                                        NotificationChannel channel = new NotificationChannel(
                                                CHANNEL_ID,
                                                "Channel 1",
                                                NotificationManager.IMPORTANCE_HIGH
                                        );
                                        notificationManager.createNotificationChannel(channel);
                                    }

                                    android.app.Notification notification = new NotificationCompat.Builder(ActivityMispedidosFragment.context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.image)
                                            .setContentTitle("Pedido Entregado")
                                            .setContentText("El proveedor llego a su casa")
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                            .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                            .setLights(Color.WHITE, 3000, 3000)
                                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                            .setColor(Color.BLUE)
                                            .setAutoCancel(true).build();
                                    // notificationId is a unique int for each notification that you must define
                                    //    NotificationManagerCompat notificationManager;
                                    //     notificationManager =   NotificationManagerCompat.from(getContext());
                                    notificationManager.notify(1, notification);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(response.toString());
            }
        });

        socket.on("order cancelar client", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                //REPONSE -> (RESPONSE.MESSAGE) TOAST

                ActivityMispedidosFragment.MyActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        orders.clear();
                        llenarPedidos();
                    }
                });
            }
        });

        socket.on("reorder client", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                ActivityMispedidosFragment.MyActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        orders.clear();
                        llenarPedidos();
                    }
                });
            }
        });
    }

    private Emitter.Listener onStatusOrder = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null)
                return;
            orders = new ArrayList<>();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject response = (JSONObject) args[0];
                    try {
                        recyclerView.setAdapter(null);
                        int status = response.getInt("status");
                        if (status == 200) {
                            JSONArray jsonArray = response.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                final Order order = new Order();

                                order.setId(obj.getJSONObject("orden").getInt("id"));
                                order.setDate(obj.getJSONObject("orden").getString("date"));
                                order.setStatus(obj.getJSONObject("orden").getString("status"));
                                order.setCalification((float) obj.getJSONObject("orden").getDouble("calification"));
                                order.setClientDirection(new LatLng(
                                        obj.getJSONObject("orden").getDouble("latitude"),
                                        obj.getJSONObject("orden").getDouble("longitude")
                                ));
                                if (obj.getJSONObject("company").length() > 0) {
                                    order.setPhone(obj.getJSONObject("company").getString("phone"));
                                    order.setCompanyName(obj.getJSONObject("company").getString("name"));
                                    LatLng latLng = new LatLng(
                                            obj.getJSONObject("company").getDouble("latitude"),
                                            obj.getJSONObject("company").getDouble("longitude")
                                    );
                                    order.setCompanyDirection(latLng);
                                }

                                JSONArray details_data = obj.getJSONArray("order_detail");
                                List<String> details = new ArrayList<>();
                                for (int j = 0; j < details_data.length(); j++) {
                                    JSONObject jsonObject1 = details_data.getJSONObject(j);
                                    details.add(jsonObject1.getString("description"));
                                }
                                order.setDetalles(details);

                                orders.add(order);
                            }
                            misPedidosAdapter = new MisPedidosAdapter(orders,MisPedidosFragment.this);
                            //    misPedidosAdapter.notifyDataSetChanged();

                            recyclerView.setAdapter(misPedidosAdapter);
                            misPedidosAdapter.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                                    FragmentTransaction transaction = manager.beginTransaction();
                                    String status = orders.get(recyclerView.getChildAdapterPosition(v)).getStatus();
                                    if (status.equals("confirm")) {
                                        MapsPerdidos misPedidosFragment = new MapsPerdidos();
                                        Bundle bundle = new Bundle();
                                        LatLng d_company = orders.get(recyclerView.getChildAdapterPosition(v)).getCompanyDirection();
                                        LatLng d_client = orders.get(recyclerView.getChildAdapterPosition(v)).getClientDirection();
                                        bundle.putParcelable("DCOMPANY", d_company);
                                        bundle.putParcelable("DCLIENT", d_client);
                                        misPedidosFragment.setArguments(bundle);
                                        transaction.add(R.id.navigationContainer, misPedidosFragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    }
                                }
                            });

                            if (swipeRefreshLayout != null)

                                swipeRefreshLayout.setRefreshing(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    public void reLoadFragment(Fragment fragment) {
        if (fragment instanceof MisPedidosFragment) {
            FragmentTransaction fragTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragTransaction.detach(fragment);
            fragTransaction.attach(fragment);
            fragTransaction.commit();
        }
    }

}