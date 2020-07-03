package com.cor.frii;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
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
import com.cor.frii.persistence.entity.ECart;
import com.cor.frii.pojo.Product;
import com.cor.frii.pojo.ProductGas;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GasProductAdapter extends RecyclerView.Adapter<GasProductAdapter.viewHolder> implements View.OnClickListener {


    private List<Product> products;
    private Context context;


    public GasProductAdapter(ArrayList<Product> products) {
        this.products = products;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gas_product, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView gasProductTitle;
        ImageView gasProductImage;
        EditText productGasCantidad;
        Button productGasAddCart;
        RadioGroup radioGroup;
        RadioButton peso;

        viewHolder(@NonNull final View itemView) {
            super(itemView);
            gasProductTitle = itemView.findViewById(R.id.GasDetailName);
            gasProductImage = itemView.findViewById(R.id.ProductGasImage);
            productGasCantidad = itemView.findViewById(R.id.ProductGasCantidad);
            productGasAddCart = itemView.findViewById(R.id.Button_45_Kilos);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            peso = itemView.findViewById(R.id.gas10kl);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.gas5kl:
                            peso = itemView.findViewById(R.id.gas5kl);
                            break;
                        case R.id.gas10kl:
                            peso = itemView.findViewById(R.id.gas10kl);
                            break;
                        case R.id.gas15kl:
                            peso = itemView.findViewById(R.id.gas15kl);
                            break;

                        case R.id.gas45kl:
                            peso = itemView.findViewById(R.id.gas45kl);
                            break;
                    }
                }
            });
        }

        void bind(final Product product) {
            double precio = 0.0;
            float price = 0;
            productGasCantidad.setText("1");
            gasProductTitle.setText(product.getName());
            Picasso.get().load(product.getUrl()).into(gasProductImage);
            productGasAddCart.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    String marke = product.getMarke();
                    final String pesoText = peso.getText().toString();
                    final int cantidad = Integer.parseInt(productGasCantidad.getText().toString());

                    JSONObject object = new JSONObject();
                    try {
                        object.put("name", marke);
                        object.put("measurement", Integer.parseInt(pesoText));
                        object.put("gas_type", product.getType());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RequestQueue queue = Volley.newRequestQueue(context);

                    String url = "http://34.71.251.155/api/product/price/";
                    JsonObjectRequest objectRequest =
                            new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        int status = response.getInt("status");
                                        if (status == 200) {
                                            double precio = response.getDouble("data");

                                            ECart eCart = new ECart();
                                            eCart.setName(product.getName() + " " + pesoText + " KG");
                                            eCart.setPrice((float) precio);
                                            eCart.setCantidad(cantidad);
                                            eCart.setTotal((float) (cantidad * precio));
                                            DatabaseClient.getInstance(context)
                                                    .getAppDatabase()
                                                    .getCartDao()
                                                    .addCart(eCart);
                                            Toast.makeText(context, "Agregado al Carrito", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
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
                                            JSONObject obj = new JSONObject(res);
                                            Log.d("Voley post", obj.toString());
                                            String msj = obj.getString("message");
                                            Toast.makeText(context, msj, Toast.LENGTH_SHORT).show();

                                        } catch (UnsupportedEncodingException | JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                            });
                    objectRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(objectRequest);

                }
            });
        }
    }

}
