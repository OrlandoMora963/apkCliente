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
import com.cor.frii.pojo.ProductGas;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapterNew extends RecyclerView.Adapter<ProductAdapterNew.viewHolder> implements View.OnClickListener {


    List<Product> products;
    private Context context;
    private View.OnClickListener listener;

    private TextView badge_count;
    private View view_badge;

    public ProductAdapterNew(ArrayList<Product> products) {
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products_new, parent, false);
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
        TextView Name_product;
        ImageView productImage;
        Button Ver_Distribuidores;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            Name_product = itemView.findViewById(R.id.Name_product_detail);
            Ver_Distribuidores=itemView.findViewById(R.id.Ver_Distribuidores);
            productImage=itemView.findViewById(R.id.ProductImage);

        }

        void bind(final Product product) {

            Name_product.setText(product.getName());
            Picasso.get().load(product.getUrl()).into(productImage);
            Ver_Distribuidores.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppCompatActivity activity=(InicioMapsActivity) v.getContext();
                    FragmentManager manager=activity.getSupportFragmentManager();
                    FragmentTransaction transaction=manager.beginTransaction();
                    ProductDetailFragment oProductDetailFragment=new ProductDetailFragment();
                    oProductDetailFragment.product_id=product.getId();
                    transaction.replace(R.id.mainContainer,oProductDetailFragment );
                    transaction.commit();
                    transaction.addToBackStack(null);
                }
            });
        }


    }


}
