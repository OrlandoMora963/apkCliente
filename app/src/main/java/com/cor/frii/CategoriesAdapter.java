package com.cor.frii;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cor.frii.pojo.Categories;
import com.cor.frii.utils.LoadImage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.viewHolder>
        implements View.OnClickListener {

    List<Categories> categories;

    private View.OnClickListener listener;

    public CategoriesAdapter(List<Categories> categories) {
        this.categories = categories;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_categories, parent, false);
        view.setOnClickListener(this);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        //holder.categoriestitle.setText(categories.get(position).getName());
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView categoriestitle;
        ImageView categoriesImage;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            categoriestitle = itemView.findViewById(R.id.CategoriesName);
            categoriesImage = itemView.findViewById(R.id.CategoriesImage);
        }

        void bind(final Categories categories) {
            categoriestitle.setText(categories.getName());
            new LoadImage(categoriesImage).execute(categories.getUrl());

            Picasso.get().load(categories.getUrl()).into(categoriesImage);
        }
    }
}
