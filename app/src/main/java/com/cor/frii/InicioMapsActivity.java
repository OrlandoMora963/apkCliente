package com.cor.frii;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.entity.ECart;
import com.cor.frii.utils.CartChangeColor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class InicioMapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
    MainFragment.OnFragmentInteractionListener,CategoriesFragment.OnFragmentInteractionListener,
    BrandsFragment.OnFragmentInteractionListener,NewMapsFragment.OnFragmentInteractionListener,
    ProductNewFragment.OnFragmentInteractionListener,GasNewFragment.OnFragmentInteractionListener,
    GasDetailFragment.OnFragmentInteractionListener,ProductDetailFragment.OnFragmentInteractionListener{

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;


    FloatingActionButton flo_order_pedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciomaps);

        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawermaps);
        navigationView = findViewById(R.id.navigationView);

        // estaclecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        //iniciando el frgament de los mapas y los condejos o sugerencias
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.add(R.id.navigationContainer,new MainFragment());
        transaction.commit();


        CartChangeColor.flo_cart = findViewById(R.id.fad_cart_order);


        CartChangeColor.flo_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Cart.class);
                startActivity(intent);
                //finish();
            }
        });

        flo_order_pedido = findViewById(R.id.siquiente_pedidos);
        flo_order_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        List<ECart> cartDetails = DatabaseClient.getInstance(this)
                .getAppDatabase()
                .getCartDao()
                .getCarts();
        if(cartDetails.size()>0)
            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64dd17")));
        else
            CartChangeColor.flo_cart.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#065FD3")));


    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (menuItem.getItemId() == R.id.home) {
            transaction.replace(R.id.navigationContainer, new MainFragment());
            transaction.commit();
            Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
        }
        if (menuItem.getItemId() == R.id.account) {
            //transaction.replace(R.id.navigationContainer,new AccountFragment());

            Intent intent = new Intent(getBaseContext(), PerfilActivity.class);
            intent.putExtra("id", R.id.account);
            startActivity(intent);

            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
    }
        if (menuItem.getItemId() == R.id.Perfil) {

            Intent intent = new Intent(getBaseContext(), PerfilActivity.class);
            String title = menuItem.getTitle().toString();
            intent.putExtra("name", title);
            intent.putExtra("id", R.id.Perfil);
            startActivity(intent);
        }
        if (menuItem.getItemId() == R.id.MisPedidos) {

            Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
            startActivity(intent);
        }

        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
