package com.cor.frii;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.pojo.Product;

import java.util.List;

public class GasProductAdapterNew extends RecyclerView.Adapter<GasProductAdapterNew.viewHolder> implements View.OnClickListener {


    private List<Product> products;
    private Context context;


    public GasProductAdapterNew(List<Product> products) {
        this.products = products;
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

    class viewHolder extends RecyclerView.ViewHolder {


        Button button_5_kilos,button_10_kilos,button_15_kilos,button_45_kilos;
        ImageView imageView;
        TextView gas_name;

        viewHolder(@NonNull final View itemView) {
            super(itemView);

            button_5_kilos=itemView.findViewById(R.id.Button_5_Kilos);
            button_10_kilos=itemView.findViewById(R.id.Button_10_Kilos);
            button_15_kilos=itemView.findViewById(R.id.Button_15_Kilos);
            button_45_kilos=itemView.findViewById(R.id.Button_45_Kilos);
            gas_name=itemView.findViewById(R.id.GasDetailName);

            button_5_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCompatActivity activity=(InicioMapsActivity) v.getContext();
                    FragmentManager manager=activity.getSupportFragmentManager();
                    FragmentTransaction transaction=manager.beginTransaction();
                    transaction.replace(R.id.mainContainer, new GasDetailFragment());
                    transaction.commit();
                    transaction.addToBackStack(null);

                }
            });

            button_10_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCompatActivity activity=(InicioMapsActivity) v.getContext();
                    FragmentManager manager=activity.getSupportFragmentManager();
                    FragmentTransaction transaction=manager.beginTransaction();
                    transaction.replace(R.id.mainContainer, new GasDetailFragment());
                    transaction.commit();
                    transaction.addToBackStack(null);

                }
            });
            button_15_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCompatActivity activity=(InicioMapsActivity) v.getContext();
                    FragmentManager manager=activity.getSupportFragmentManager();
                    FragmentTransaction transaction=manager.beginTransaction();
                    transaction.replace(R.id.mainContainer, new GasDetailFragment());
                    transaction.commit();
                    transaction.addToBackStack(null);

                }
            });
            button_45_kilos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCompatActivity activity=(InicioMapsActivity) v.getContext();
                    FragmentManager manager=activity.getSupportFragmentManager();
                    FragmentTransaction transaction=manager.beginTransaction();
                    transaction.replace(R.id.mainContainer, new GasDetailFragment());
                    transaction.commit();
                    transaction.addToBackStack(null);

                }
            });




        }

        void bind(final Product product) {
            gas_name.setText(product.getName());

        }
    }

}
