package com.cor.frii;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cor.frii.pojo.Product;
import com.cor.frii.pojo.ProductGas;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GasProductAdapterNew extends RecyclerView.Adapter<GasProductAdapterNew.viewHolder> implements View.OnClickListener {


    private ArrayList<Product> products;
    private Context context;
    private String TipoGas;

    public GasProductAdapterNew(ArrayList<Product> products, String TipoGas) {
        this.products = products;
        this.TipoGas = TipoGas;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gas_product_new, parent, false);
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

    void AbrirFragment(final View v, final int MarcasId, final float Measurent,final String Name) {
        final RequestQueue queue = Volley.newRequestQueue(context);
        StringBuilder sb = new StringBuilder();
        sb.append("http://34.71.251.155/api/product/markes/" + MarcasId);
        String url = sb.toString();
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        int ProductId = 0;
                        Gson gson = new Gson();
                        ArrayList<ProductGas> productDetails = gson.fromJson(response,
                                new TypeToken<ArrayList<ProductGas>>() {
                                }.getType());

                        for (ProductGas item : productDetails) {
                            if (item.getDetail_measurement_id().getName().split(" ")[1].equals(TipoGas.split("-")[1]) &&
                            item.getMeasurement() == Measurent) {
                                ProductId =item.getId();
                            }
                        }
                        AppCompatActivity activity = (InicioMapsActivity) v.getContext();
                        FragmentManager manager = activity.getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        GasDetailFragment oGasDetailFragment = new GasDetailFragment();
                        oGasDetailFragment.product_id = ProductId;
                        oGasDetailFragment.Description=Name +" "+Measurent+" Kilos";

                        transaction.replace(R.id.mainContainer, oGasDetailFragment);
                        transaction.commit();
                        transaction.addToBackStack(null);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        Button button_5_kilos, button_10_kilos, button_15_kilos, button_45_kilos;
        ImageView ProductGasImage;
        TextView GasDetailName;

        viewHolder(@NonNull final View itemView) {
            super(itemView);
            button_5_kilos = itemView.findViewById(R.id.Button_5_Kilos);
            button_10_kilos = itemView.findViewById(R.id.Button_10_Kilos);
            button_15_kilos = itemView.findViewById(R.id.Button_15_Kilos);
            button_45_kilos = itemView.findViewById(R.id.Button_45_Kilos);
            GasDetailName = itemView.findViewById(R.id.GasDetailName);
            ProductGasImage = itemView.findViewById(R.id.ProductGasImage);
        }

        void bind(final Product product) {
            GasDetailName.setText(product.getName());
            Picasso.get().load(product.getUrl()).into(ProductGasImage);
            button_5_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AbrirFragment(v, product.getMarkeId(),  5,product.getName());

                }
            });

            button_10_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AbrirFragment(v, product.getMarkeId(), 10,product.getName());

                }
            });
            button_15_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AbrirFragment(v, product.getMarkeId(), 15,product.getName());

                }
            });
            button_45_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AbrirFragment(v, product.getMarkeId(), 45,product.getName());

                }
            });
        }
    }

}
