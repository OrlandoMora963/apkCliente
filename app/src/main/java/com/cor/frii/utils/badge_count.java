package com.cor.frii.utils;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cor.frii.R;

public class badge_count {

     private TextView badge_visible;

     public void visible(View v){
         badge_visible=v.findViewById(R.id.badge_count);
         badge_visible.setVisibility(View.VISIBLE);
     }
}
