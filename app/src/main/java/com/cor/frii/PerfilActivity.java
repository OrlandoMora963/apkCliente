package com.cor.frii;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
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
import com.google.android.material.navigation.NavigationView;

public class PerfilActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SettingFragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener,
        CategoriesFragment.OnFragmentInteractionListener,
        BrandsFragment.OnFragmentInteractionListener,
        ProductsFragment.OnFragmentInteractionListener,
        GasFragment.OnFragmentInteractionListener,
        MisPedidosFragment.OnFragmentInteractionListener,
        AccountFragment.OnFragmentInteractionListener {


    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    TextView CerrarSecion;
    TextView lblUsername, lblEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //Validar informacion del usuario
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();
        if (token == 0 || token < 0) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }
        //--

        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawerPerfil);
        navigationView = findViewById(R.id.navigationView);

        // estaclecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        Bundle datos = this.getIntent().getExtras();
        String name = datos.getString("name");
        int Id = datos.getInt("id");

        if (Id == R.id.Perfil) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.navigationContainer, new SettingFragment());
            //transaction.addToBackStack(null);
            transaction.commit();

        }
        if (Id == R.id.account) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.navigationContainer, new AccountFragment());
            //transaction.addToBackStack(null);
            transaction.commit();
        }

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


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Intent intent;
        if (menuItem.getItemId() == R.id.home) {
            intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }
        if (menuItem.getItemId() == R.id.account) {
            intent = new Intent(getBaseContext(), PerfilActivity.class);
            intent.putExtra("id", R.id.account);
            startActivity(intent);
            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
        }
        if (menuItem.getItemId() == R.id.Perfil) {

            intent = new Intent(getBaseContext(), PerfilActivity.class);
            String title = menuItem.getTitle().toString();
            intent.putExtra("name", title);
            intent.putExtra("id", R.id.Perfil);
            startActivity(intent);

            /*
            transaction.replace(R.id.navigationContainer, new SettingFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();

             */
        }
        if (menuItem.getItemId() == R.id.MisPedidos) {
            intent = new Intent(this, PedidosActivity.class);
            startActivity(intent);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        llenarInfoUsuario();
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
