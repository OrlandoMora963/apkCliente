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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.entity.ECart;
import com.cor.frii.pojo.ProductStaff;
import com.cor.frii.utils.CartChangeColor;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailAdapter extends RecyclerView.Adapter<ProductDetailAdapter.viewHolder> implements View.OnClickListener {


    List<ProductStaff> products;
    private Context context;
    private View.OnClickListener listener;

    private TextView badge_count;
    private View view_badge;

    public ProductDetailAdapter(ArrayList<ProductStaff> products) {
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
        TextView txtDistribuidor;
        TextView txtPrecio;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            productTilte_detail = itemView.findViewById(R.id.Name_product_detail);
            productImage = itemView.findViewById(R.id.ProductImage);
            productCantidad = itemView.findViewById(R.id.ProductCantidad);
            txtDistribuidor = itemView.findViewById(R.id.txtDistribuidor);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            productButtonAdd = itemView.findViewById(R.id.productButtonAdd);
        }

        void bind(final ProductStaff product) {

            productTilte_detail.setText(product.getProductID().getDescription());
            Picasso.get().load(product.getProductID().getImage()).into(productImage);
            productCantidad.setText("1");
            txtPrecio.setText(String.valueOf(product.getPrice()));
            txtDistribuidor.setText(product.getCompanyID().getName());
            boolean IsExistsButton = false;
            for (ECart item : DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts()) {
                if (item.getProductRegister() == product.getID())
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
                        if (item.getProductRegister() == product.getID())
                            oECart = item;
                        break;
                    }
                    String frii_Background=String.format("#%06x", ContextCompat.getColor(context, R.color.frii_Background) & 0xffffff);
                    if (oECart == null) {
                        ECart eCart = new ECart();
                        eCart.setName(product.getProductID().getDescription());
                        eCart.setPrice(product.getPrice());
                        //productDescription.setVisibility(View.INVISIBLE);
                        //badge_visible();
                        if (productCantidad.getText().length() > 0) {
                            eCart.setCantidad(Integer.parseInt(productCantidad.getText().toString()));
                            eCart.setTotal((Float.parseFloat(productCantidad.getText().toString()) * product.getPrice()));
                            eCart.setProductRegister(product.getID());
                            DatabaseClient.getInstance(context)
                                    .getAppDatabase()
                                    .getCartDao()
                                    .addCart(eCart);
                            productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
                            productButtonAdd.setText("Agregado");
                            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
                            Toast.makeText(context, "Agregado al Carrito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .deleteCart(oECart);
                        if (DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .getCarts().size()  == 0)
                            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(frii_Background)));
                        productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(frii_Background)));

                        productButtonAdd.setText("Agregar");
                        Toast.makeText(context, "Eliminado del Carrito", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }


}
