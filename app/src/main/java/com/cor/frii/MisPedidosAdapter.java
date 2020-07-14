package com.cor.frii;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.Session;
import com.cor.frii.persistence.entity.Acount;
import com.cor.frii.pojo.Order;

import com.cor.frii.utils.AgendarPedido;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MisPedidosAdapter extends RecyclerView.Adapter<MisPedidosAdapter.viewHolder> implements View.OnClickListener {

    private List<Order> data;
    private Context context;
    private View view;
    private View.OnClickListener listener;
    ViewGroup viewGroup;
    private Socket socket;
    private MisPedidosFragment oMisPedidosFragment;

    public String HOST_NODEJS = "http://34.71.251.155:9000";

    public static final String TAG = "firebase";
    private final static int NOTIFICATION_ID = 0;
    private final static String CHANNEL_ID = "NOTIFICACION";

    public MisPedidosAdapter(List<Order> data,MisPedidosFragment oMisPedidosFragment) {
        this.data = data;
        this.oMisPedidosFragment=oMisPedidosFragment;
    }


    @Override
    public void onClick(View v) {
        if (listener != null) listener.onClick(v);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_mispedidos, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();
        viewGroup = parent;

        return new viewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {
        holder.titlePedido.setText(data.get(position).getDate());
        holder.btnEliminar.setEnabled(false);
        switch (data.get(position).getStatus()) {
            case "wait":
                holder.estadoPedido.setText("Buscando proveedor...");
                holder.cancelar.setText("Cancelar");
                break;
            case "refuse":
                holder.estadoPedido.setText("Rechazado");
                break;
            case "confirm":
                holder.estadoPedido.setText("Confirmado");
                holder.cancelar.setEnabled(true);
                holder.cancelar.setText("Cancelar");

                break;
            case "delivered":
                holder.estadoPedido.setText("Entregado");
                holder.cancelar.setText("Calificar");
                holder.cancelar.setBackgroundResource(R.drawable.custom_button_calificar);
                break;
            default:
                holder.estadoPedido.setText("Cancelado");
                holder.cancelar.setText("Repedir");
                holder.cancelar.setBackgroundResource(R.drawable.custom_button_repedir);
                holder.mensaje.setVisibility(View.GONE);
                holder.llamar.setVisibility(View.GONE);
                holder.btnEliminar.setEnabled(true);
                break;
        }
        StringBuilder details = new StringBuilder();
        for (int i = 0; i < data.get(position).getDetalles().size(); i++) {
            details.append(data.get(position).getDetalles().get(i)).append("\n");
        }
        holder.detallePedido.setText(details.toString());

        if (data.get(position).getPhone() != null && !data.get(position).getPhone().equals("")) {
            holder.llamar.setEnabled(true);
            holder.mensaje.setEnabled(true);
            holder.llamar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String dial = "tel: " + data.get(position).getPhone();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(dial));
                    context.startActivity(intent);
                }
            });

            holder.mensaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto: " + data.get(position).getPhone()));
                    context.startActivity(intent);
                }
            });

            holder.timerAuto.setText("Confirmado por: " + data.get(position).getCompanyName());
        } else {
            holder.llamar.setEnabled(false);
            holder.llamar.setBackgroundResource(R.drawable.custom_button_gray);
            holder.mensaje.setEnabled(false);
            holder.mensaje.setBackgroundResource(R.drawable.custom_button_gray);
        }
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Acount acount = DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .getAcountDao()
                        .getUser(new Session(context).getToken());
                JSONObject jsonObject = new JSONObject();
                RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(context));
                try {
                    jsonObject.put("order_id", data.get(position).getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String url = "http://34.71.251.155/api/order/delete";
                System.out.println(jsonObject.toString());
                JsonObjectRequest jsonObjectRequest =
                        new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                                oMisPedidosFragment.llenarPedidos();
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
                                headers.put("Authorization", "JWT " + acount.getToken());
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };

                queue.add(jsonObjectRequest);
            }
        });
        if (data.get(position).getStatus().equals("cancel")) {
            holder.cancelar.setText("Repedir");
            holder.cancelar.setBackgroundResource(R.drawable.custom_button_repedir);
            if (holder.timer != null) {
                holder.timer.cancel();
            }
        } else {
            if (data.get(position).getStatus().equals("wait")) {
                holder.timerflag = true;
                long timer = 120000;
                if (holder.timer != null) {
                    holder.timer.cancel();
                }

                holder.timer = new CountDownTimer(timer, 1000) {
                    public void onTick(long millisUntilFinished) {
                        NumberFormat f = new DecimalFormat("00");
                        long hour = (millisUntilFinished / 3600000) % 24;
                        long min = (millisUntilFinished / 60000) % 60;
                        long sec = (millisUntilFinished / 1000) % 60;

                        holder.timerAuto.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                    }

                    public void onFinish() {
                        tiempoCancelar(data.get(position).getId(), data.get(position).getStatus(), position);
                        notificacionCancelado("Pedido cancelado", "El tiempo expiro");
                        holder.timerAuto.setText("Expired!!");
                        notifyDataSetChanged();
                        notifyItemChanged(position);
                    }
                };
            }

            holder.cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("entro...." + holder.cancelar.getText());
                    if (holder.cancelar.getText().equals("Cancelar")) {

                        /*mensajeConfirmacion(data.get(position).getId(), data.get(position).getStatus(), position);
                        notifyDataSetChanged();
                        notifyItemChanged(position);*/

                        initSocket();
                        emitirCancelar(data.get(position).getId());

                        holder.timerflag = false;
                    } else if (holder.cancelar.getText().equals("Calificar")) {
                        AgendarPedido agendarPedido = new AgendarPedido(context, data.get(position).getId(),
                                data.get(position).getCalification());
                        agendarPedido.show();
                    }
                }
            });
        }


        holder.cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.cancelar.getText().equals("Cancelar")) {
                    mensajeConfirmacion(data.get(position).getId(), data.get(position).getStatus(), position);

                    notifyDataSetChanged();
                    notifyItemChanged(position);

                    holder.timerflag = false;
                    System.out.println("entro...." + holder.cancelar.getText());
                } else if (holder.cancelar.getText().equals("Calificar")) {
                    AgendarPedido agendarPedido = new AgendarPedido(context, data.get(position).getId(),
                            data.get(position).getCalification());
                    agendarPedido.show();
                } else if (holder.cancelar.getText().equals("Repedir")) {
                    /*repedirOrden(
                            data.get(position).getId(),
                            data.get(position).getClientDirection().latitude,
                            data.get(position).getClientDirection().longitude
                    );
                    //FUNCION( DATA.GET(POSITION).GETID )*/
                    initSocket();
                    emitirReorder(
                            data.get(position).getId(),
                            data.get(position).getClientDirection().latitude,
                            data.get(position).getClientDirection().longitude
                    );

                    notifyDataSetChanged();
                    notifyItemChanged(position);
                }
            }
        });

        if (holder.timerflag) {
            holder.timer.start();
        } else {
            if (holder.timer != null) {
                holder.timer.cancel();
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        TextView titlePedido, estadoPedido, detallePedido;
        Button llamar, mensaje, cancelar, btnRepedir;
        CountDownTimer timer;
        boolean timerflag = false;
        TextView timerAuto;
ImageButton btnEliminar;
        viewHolder(@NonNull View itemView) {
            super(itemView);
            titlePedido = itemView.findViewById(R.id.TitlePedidos);
            estadoPedido = itemView.findViewById(R.id.EstadoPedido);
            detallePedido = itemView.findViewById(R.id.DetallePedido);

            llamar = itemView.findViewById(R.id.ButtonLLamarPedido);
            mensaje = itemView.findViewById(R.id.ButtonMensajePedido);
            cancelar = itemView.findViewById(R.id.ButtonCancelarPedido);
            btnRepedir = itemView.findViewById(R.id.buttonRepedir);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            timerAuto = itemView.findViewById(R.id.timerAuto);
        }
    }

    private void mensajeConfirmacion(final int idOrden, final String status, final int position) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Desea realmente cancelar el pedido")
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*tiempoCancelar(idOrden, status, position);*/
                        initSocket();
                        emitirCancelar(idOrden);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.show();
    }

    private void tiempoCancelar(int idOrden, String status, final int position) {
        final String url = "http://34.71.251.155/api/order/client/cancel/";
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idOrden);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int status = response.getInt("status");
                            if (status == 200) {
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                                /*initSocket();*/
                                Order order = data.get(position);
                                order.setStatus("cancel");
                                data.set(position, order);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
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
                });
        queue.add(jsonObjectRequest);
    }

    private void repedirOrden(final int idOrden, final double latitude, final double longitude) {
        final String url = "http://34.71.251.155/api/client/order/repedir/";
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id_orden", idOrden);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int status = response.getInt("status");
                            if (status == 200) {
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                                initSocket();
                                JSONObject datas = new JSONObject();
                                datas.put("id", idOrden);
                                datas.put("latitude", latitude);
                                datas.put("longitude", longitude);
                                Log.d(TAG, "conect " + datas.toString());
                                socket.emit("get orders", datas);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
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
                });
        queue.add(jsonObjectRequest);
    }

    private void notificacionCancelado(String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cart)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, noBuilder.build());
    }


    private void initSocket() {

        int id_user = new Session(context).getToken();
        JSONObject data = new JSONObject();
        Acount cuenta = DatabaseClient.getInstance(context)
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
            json_connect.put("ID_CLIENT", 2);
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
                int id = new Session(context).getToken();
                Acount cuenta = DatabaseClient.getInstance(context)
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

        JSONObject datas = new JSONObject();
        try {
            datas.put("id", id_user);
            datas.put("token", cuenta.getToken());
            Log.d(TAG, "conect " + datas.toString());
            socket.emit("status order", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //FUNCION(IDORDEN)
    private void emitirCancelar(int idorden) {
        JSONObject datas = new JSONObject();
        int id_user = new Session(context).getToken();
        try {
            datas.put("id", idorden);
            datas.put("id_user", id_user);
            socket.emit("order cancel client", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitirReorder(int idorden, double latitude, double longitude) {
        JSONObject datas = new JSONObject();
        int id_user = new Session(context).getToken();
        try {
            datas.put("id", idorden);
            datas.put("id_user", id_user);
            socket.emit("reorder client", datas);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject orden = new JSONObject();
        try {
            orden.put("id", idorden);
            orden.put("latitude", latitude);
            orden.put("longitude", longitude);
            socket.emit("get orders", orden);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
