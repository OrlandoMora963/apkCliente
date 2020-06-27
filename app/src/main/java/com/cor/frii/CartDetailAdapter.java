package com.cor.frii;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.entity.ECart;

import java.util.List;

public class CartDetailAdapter extends RecyclerView.Adapter<CartDetailAdapter.viewHolder> implements View.OnClickListener {

    private List<ECart> cartDetails;
    private Context context;
    private EventListener eventListener;

    public CartDetailAdapter(List<ECart> cartDetails, EventListener eventListener) {
        this.eventListener = eventListener;
        this.cartDetails = cartDetails;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_cart_detail, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {
        holder.cartTitle.setText(cartDetails.get(position).getName());
        holder.cartCantidad.setText(String.valueOf(cartDetails.get(position).getCantidad()));
        holder.cartPrecioU.setText(String.valueOf(cartDetails.get(position).getPrice()));
        holder.cartSubtotal.setText(String.valueOf(cartDetails.get(position).getTotal()));

        final ECart eCart = cartDetails.get(position);
        calcularSubTotal(holder.cartCantidad, holder.cartPrecioU, holder.cartSubtotal, position);
        eventListener.calcularTotal(calcularTotal());

        holder.cartDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .getCartDao()
                        .deleteCart(eCart);

                notifyItemRemoved(position);
                cartDetails.remove(position);
                notifyDataSetChanged();
                eventListener.calcularTotal(calcularTotal());
            }
        });

        holder.cartCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (holder.cartCantidad.getText().length() > 0) {
                    calcularSubTotal(holder.cartCantidad, holder.cartPrecioU, holder.cartSubtotal, position);
                    actualizarCantidad(holder.cartCantidad, position);

                    eventListener.calcularTotal(calcularTotal());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return cartDetails.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {

        TextView cartTitle, cartPrecioU, cartSubtotal;
        EditText cartCantidad;
        ImageButton cartDeleteButton;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            cartTitle = itemView.findViewById(R.id.cartProductTitle);
            cartCantidad = itemView.findViewById(R.id.cartEditcantidad);
            cartPrecioU = itemView.findViewById(R.id.cartPrecioU);
            cartSubtotal = itemView.findViewById(R.id.cartSubTotal);
            cartDeleteButton = itemView.findViewById(R.id.cartDeleteButton);

        }
    }

    private void calcularSubTotal(EditText cantidad, TextView precio, TextView total, int posicion) {
        if (cantidad.length() > 0 && precio.length() > 0) {
            int c = Integer.parseInt(cantidad.getText().toString());
            float p = Float.parseFloat(precio.getText().toString());

            total.setText(String.valueOf(c * p));

            cartDetails.get(posicion).setCantidad(c);
            cartDetails.get(posicion).setPrice(p);
            cartDetails.get(posicion).setTotal(c * p);
        }
    }

    private void actualizarCantidad(TextView cantidad, int position) {
        ECart cart = DatabaseClient
                .getInstance(context)
                .getAppDatabase()
                .getCartDao()
                .getCart(cartDetails.get(position).getUid());

        int cant = Integer.parseInt(cantidad.getText().toString());
        cart.setCantidad(cant);
        cart.setTotal(cant * cart.getTotal());

        DatabaseClient.getInstance(context)
                .getAppDatabase()
                .getCartDao()
                .updateCart(cart);
    }

    private float calcularTotal() {
        float sum = 0;
        for (ECart e : cartDetails) {
            sum += e.getTotal();
        }
        return sum;
    }

    public interface EventListener {
        void calcularTotal(float total);
    }
}