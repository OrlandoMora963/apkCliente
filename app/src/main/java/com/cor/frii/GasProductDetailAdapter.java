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
import com.cor.frii.pojo.ProductStaff;
import com.cor.frii.utils.CartChangeColor;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GasProductDetailAdapter extends RecyclerView.Adapter<GasProductDetailAdapter.viewHolder> implements View.OnClickListener {


    private List<ProductStaff> products;
    private Context context;


    public GasProductDetailAdapter(ArrayList<ProductStaff> products) {
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

        TextView gas_name_detail;
        ImageView productImage;
        Button productButtonAdd;
        EditText ProductGasCantidad;
        TextView txtDistribuidor;
        TextView txtPrecio;
        viewHolder(@NonNull final View itemView) {
            super(itemView);


            gas_name_detail=itemView.findViewById(R.id.GasDetailName);
            productImage = itemView.findViewById(R.id.ProductGasImage);
            ProductGasCantidad = itemView.findViewById(R.id.ProductGasCantidad);
            txtDistribuidor = itemView.findViewById(R.id.txtDistribuidor);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            productButtonAdd=itemView.findViewById(R.id.productButtonAdd);


        }

        void bind(final ProductStaff product) {
            gas_name_detail.setText(product.getProductID().getDescription());
            Picasso.get().load(product.getProductID().getImage()).into(productImage);
            ProductGasCantidad.setText("1");
            txtPrecio.setText(String.valueOf(product.getPrice()));
            txtDistribuidor.setText(product.getCompanyID().getName());
            productButtonAdd.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {

                    ECart eCart = new ECart();
                    eCart.setName(product.getProductID().getDescription());
                    eCart.setPrice(product.getPrice());
                    //productDescription.setVisibility(View.INVISIBLE);
                    //badge_visible();
                    if (ProductGasCantidad.getText().length() > 0) {
                        eCart.setCantidad(Integer.parseInt(ProductGasCantidad.getText().toString()));
                        eCart.setTotal( (Float.parseFloat(ProductGasCantidad.getText().toString()) * product.getPrice()));
                        DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .addCart(eCart);
                        Toast.makeText(context, "Agregado al Carrito", Toast.LENGTH_LONG).show();
                        CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#d50000")));
                    } else {
                        Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
    }

}
