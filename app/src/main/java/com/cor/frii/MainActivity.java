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
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.cor.frii.Login.LoginActivity;
import com.cor.frii.persistence.DatabaseClient;
import com.cor.frii.persistence.Session;
import com.cor.frii.persistence.entity.Acount;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;


//   AppCompatActivity

/* fragment implements

CategoriesFragment.OnFragmentInteractionListener,
    BrandsFragment.OnFragmentInteractionListener,ProductsFragment.OnFragmentInteractionListener,
    GasFragment.OnFragmentInteractionListener,CartdetailFragment.OnFragmentInteractionListener
 */


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        CategoriesFragment.OnFragmentInteractionListener,
        BrandsFragment.OnFragmentInteractionListener,
        ProductsFragment.OnFragmentInteractionListener,
        GasFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener,
        MisPedidosFragment.OnFragmentInteractionListener,
        GasNewFragment.OnFragmentInteractionListener,
        GasDetailFragment.OnFragmentInteractionListener,
        ProductNewFragment.OnFragmentInteractionListener,
        ProductDetailFragment.OnFragmentInteractionListener,
        NewMapsFragment.OnFragmentInteractionListener{


    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;


    FloatingActionButton flo_cart;
    FloatingActionButton flo_order_pedido;

    FloatingActionButton next_pedidos;

    //--
    TextView lblUsername, lblEmail, CerrarSecion, ProbandoID;
    int id = 3;

    private TextView badge_count;

    Boolean inicio=true;

    //e
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawer);
        navigationView = findViewById(R.id.navigationView);

        // estaclecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        cargarFragments();
        llenarInfoUsuario();

        next_pedidos=findViewById(R.id.siquiente_pedidos);

        next_pedidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getBaseContext(), InicioMapsActivity.class);
                startActivity(intent);
                System.out.println("saliendo de");
            }
        });



        /*
        //carito de comprar
        flo_cart = findViewById(R.id.fad_cart_order);
        flo_cart.setOnClickListener(new View.OnClickListener() {
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

         */


        CerrarSecion = findViewById(R.id.CerrarSesion);
        CerrarSecion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Session session = new Session(getApplicationContext());
                session.destroySession();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        badge_count=findViewById(R.id.badge_count);


    }

    public void badge_visible(){
        //TextView badge_count=findViewById(R.id.badge_count);
        badge_count.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        llenarInfoUsuario();

/*
        if (inicio){
            Intent intent=new Intent(getBaseContext(),InicioMapsActivity.class);
            startActivity(intent);
            System.out.println("inresnasp normal");
            inicio=false;

        }

 */



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

    public void cargarFragments() {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        //inicializando al fragment que contendra alos fragments categories y brands
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.navigationContainer, new NewMapsFragment());
        fragmentTransaction.commit();
    }


    private void llenarInfoUsuario() {
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();

        if (token != 0) {
            Acount acount = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getAcountDao()
                    .getUser(token);

            if (acount != null) {
                View view = navigationView.getHeaderView(0);
                lblUsername = view.findViewById(R.id.lblNombreUsuario);
                lblEmail = view.findViewById(R.id.lblEmailUsuario);

                lblUsername.setText(acount.getNombre());
                lblEmail.setText(acount.getEmail());
            } else {
                Toast.makeText(getApplicationContext(), "Error de  loggeado", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }


    }


}
