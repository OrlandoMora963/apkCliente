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
import com.cor.frii.pojo.ProductGas;
import com.cor.frii.utils.CartChangeColor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.viewHolder> implements View.OnClickListener {


    ArrayList<Product> products;
    private Context context;
    private View.OnClickListener listener;

    private TextView badge_count;
    private View view_badge;

    public ProductAdapter(ArrayList<Product> products) {
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();



         view_badge=LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_app_bar_main,parent,false);
        badge_count=view_badge.findViewById(R.id.badge_count);
        badge_count.setVisibility(View.INVISIBLE);
        badge_count.setBackgroundColor(Color.YELLOW);

        FloatingActionButton fla=view_badge.findViewById(R.id.fad_cart_order);
        fla.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));



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


    public void badge_visible(){
        badge_count=view_badge.findViewById(R.id.badge_count);
        badge_count.setVisibility(view_badge.VISIBLE);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView productTilte, productDescription,add_badge;
        ImageView productImage;
        Button productButtonAdd;
        EditText productCantidad;
        View view;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            productTilte = itemView.findViewById(R.id.Name_product_detail);
            productImage = itemView.findViewById(R.id.ProductImage);
            productButtonAdd = itemView.findViewById(R.id.Ver_Distribuidores);
            productCantidad = itemView.findViewById(R.id.ProductCantidad);
            productDescription = itemView.findViewById(R.id.ProductDescription);



        }

        void bind(final Product product) {
            productCantidad.setText("1");
            productTilte.setText(product.getName());
            productDescription.setText(product.getDescription());
            Picasso.get().load(product.getUrl()).into(productImage);
            boolean IsExistsButton = false;
            for (ECart item : DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts()) {
                if (item.getProductRegister() == product.getId())
                    IsExistsButton = true;
                break;
            }
            if (IsExistsButton) {
                productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
                productButtonAdd.setText("Agregado");
            }
            productButtonAdd.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {

                    ECart oECart = null;
                    for (ECart item : DatabaseClient.getInstance(context)
                            .getAppDatabase()
                            .getCartDao()
                            .getCarts()) {
                        if (item.getProductRegister() == product.getId())
                            oECart = item;
                        break;
                    }
                    if (oECart == null) {
                        ECart eCart = new ECart();
                        eCart.setName(product.getName());
                        eCart.setPrice(product.getPrice());
                        //productDescription.setVisibility(View.INVISIBLE);

                        //badge_visible();

                        if (productCantidad.getText().length() > 0) {
                            eCart.setCantidad(Integer.parseInt(productCantidad.getText().toString()));
                            eCart.setTotal(Float.parseFloat(productCantidad.getText().toString()) * product.getPrice());
                            eCart.setProductRegister(product.getId());
                            DatabaseClient.getInstance(context)
                                    .getAppDatabase()
                                    .getCartDao()
                                    .addCart(eCart);
                            productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
                            productButtonAdd.setText("Agregado");
                            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
                            Toast.makeText(context, "Agregado al Carrito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
                        }

                    }
                    else {
                        DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .deleteCart(oECart);
                        if (DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .getCarts().size()  == 0)
                            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#065FD3")));
                        productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#065FD3")));
                        productButtonAdd.setText("Agregar");
                        Toast.makeText(context, "Eliminado del Carrito", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


    }


}
