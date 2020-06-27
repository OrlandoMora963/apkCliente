package com.cor.frii;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.pojo.Product;

import java.util.List;

public class GasProductDetailAdapter extends RecyclerView.Adapter<GasProductDetailAdapter.viewHolder> implements View.OnClickListener {


    private List<Product> products;
    private Context context;


    public GasProductDetailAdapter(List<Product> products) {
        this.products = products;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gas_product_detail, parent, false);
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



        ImageView imageView;
        TextView gas_name_detail;

        viewHolder(@NonNull final View itemView) {
            super(itemView);


            gas_name_detail=itemView.findViewById(R.id.GasDetailName);



        }

        void bind(final Product product) {
            gas_name_detail.setText(product.getName());

        }
    }

}
