package com.cor.frii;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.pojo.Brands;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BrandsAdapter extends RecyclerView.Adapter<BrandsAdapter.viewHolder> implements View.OnClickListener {

    List<Brands> brands;

    private View.OnClickListener listener;

    public BrandsAdapter(List<Brands> brands) {
        this.brands = brands;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    /*
     * modificaciones
     * */

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_brands, parent, false);
        view.setOnClickListener(this);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.bind(brands.get(position));

    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {

        this.listener = onClickListener;

    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView brandsTitle;
        ImageView brandsImage;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            brandsTitle = itemView.findViewById(R.id.BrandsTitle);
            brandsImage = itemView.findViewById(R.id.BrandsImage);
        }

        void bind(final Brands brands) {
            brandsTitle.setText(brands.getName());

            Picasso.get().load(brands.getUrl()).into(brandsImage);
        }
    }
}
