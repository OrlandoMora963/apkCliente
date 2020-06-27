package com.cor.frii;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.entity.ECart;
import com.cor.frii.pojo.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductDetailAdapter extends RecyclerView.Adapter<ProductDetailAdapter.viewHolder> implements View.OnClickListener {


    List<Product> products;
    private Context context;
    private View.OnClickListener listener;

    private TextView badge_count;
    private View view_badge;

    public ProductDetailAdapter(List<Product> products) {
        this.products = products;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products_detail, parent, false);
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

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }




    class viewHolder extends RecyclerView.ViewHolder {
        TextView productTilte_detail;
        ImageView productImage;
        Button productButtonAdd;
        EditText productCantidad;
        View view;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            productTilte_detail = itemView.findViewById(R.id.Name_product_detail);


        }

        void bind(final Product product) {

            productTilte_detail.setText(product.getName());


        }


    }


}
