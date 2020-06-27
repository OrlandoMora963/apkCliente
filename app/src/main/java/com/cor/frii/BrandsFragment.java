package com.cor.frii;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.cor.frii.Login.LoginActivity;
import com.cor.frii.persistence.Session;
import com.cor.frii.pojo.Brands;
import com.cor.frii.utils.VolleySingleton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BrandsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //--
    private String urlBase = "http://34.71.251.155";

    private BrandsAdapter brandsAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Brands> brandsList;

    public BrandsFragment() {
        // Required empty public constructor
    }

    public static BrandsFragment newInstance(String param1, String param2) {
        BrandsFragment fragment = new BrandsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Validar informacion del usuario
        Session session = new Session(getContext());
        final int token = session.getToken();
        if (token == 0 || token < 0) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            Objects.requireNonNull(getActivity()).finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }
        //--

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_brands, container, false);
        brandsList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.BrandsContainer);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        llenarDatos();

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // Llenar datos
    private void llenarDatos() {
        final List<Brands> gas_categories = new ArrayList<>();
        Bundle bundle = this.getArguments();
        String url = "";

        if (bundle != null) {
            url = this.urlBase + "/api/markes/" + bundle.getInt("idCategory");
            if (bundle.getInt("idCategory") == 2) {
                gas_categories.add(new Brands(1, "Gas Normal", "", urlBase + "/media/images/gas-regular.png"));
                gas_categories.add(new Brands(2, "Gas Premium", "", urlBase + "/media/images/gas-premium.png"));
                gas_categories.add(new Brands(3, "Camion", "", urlBase + "/media/images/gas-cisterna.png"));

                brandsAdapter = new BrandsAdapter(gas_categories);
                recyclerView.setAdapter(brandsAdapter);

                brandsAdapter.setOnClickListener(new View.OnClickListener() {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();

                    @Override
                    public void onClick(View v) {
                        String brandsTitle = gas_categories.get(recyclerView.getChildAdapterPosition(v)).getName();
                        brandsTitle = brandsTitle.toLowerCase();
                        Toast.makeText(getContext(), brandsTitle, Toast.LENGTH_SHORT).show();
                        if (brandsTitle.equals("gas normal") || brandsTitle.equals("gas premium")) {
                            GasFragment gasFragment = new GasFragment();
                            Bundle b = new Bundle();
                            if (brandsTitle.equals("gas normal")) {
                                b.putString("type", "gas-normal");
                            } else {
                                b.putString("type", "gas-premium");

                            }
                            gasFragment.setArguments(b);


                            transaction.replace(R.id.mainContainer, new GasNewFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            GasCisternaFragment gasCisternaFragment = new GasCisternaFragment();
                            transaction.replace(R.id.mainContainer, gasCisternaFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                    }
                });

                return;
            }
        } else {
            url = this.urlBase + "/api/markes";
            brandsList.add(new Brands(1, "Gas Normal", "", urlBase + "/media/images/gas-regular.png"));
            brandsList.add(new Brands(2, "Gas Premium", "", urlBase + "/media/images/gas-premium.png"));
            brandsList.add(new Brands(3, "Camion", "", urlBase + "/media/images/gas-cisterna.png"));
        }

        JSONArray jsonArray = new JSONArray();
        JsonArrayRequest arrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, jsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                String imagen_url = urlBase + object.getString("image");
                                Brands brands = new Brands(
                                        Integer.parseInt(object.getString("id")),
                                        object.getString("name"),
                                        "",
                                        imagen_url);

                                brandsList.add(brands);
                            }

                            brandsAdapter = new BrandsAdapter(brandsList);
                            recyclerView.setAdapter(brandsAdapter);

                            brandsAdapter.setOnClickListener(new View.OnClickListener() {
                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();

                                @Override
                                public void onClick(View v) {
                                    String brandsTitle = brandsList.get(recyclerView.getChildAdapterPosition(v)).getName();
                                    brandsTitle = brandsTitle.toLowerCase();
                                    if (brandsTitle.equals("gas normal") || brandsTitle.equals("gas premium")) {
                                        GasFragment gasFragment = new GasFragment();
                                        Bundle b = new Bundle();
                                        if (brandsTitle.equals("gas normal")) {
                                            b.putString("type", "gas-normal");
                                        } else {
                                            b.putString("type", "gas-premium");
                                        }
                                        gasFragment.setArguments(b);


                                        transaction.replace(R.id.mainContainer, new GasNewFragment());
                                        transaction.addToBackStack(null);
                                        transaction.commit();

                                    } else if (brandsTitle.equals("camion")) {
                                        GasCisternaFragment gasCisternaFragment = new GasCisternaFragment();
                                        transaction.replace(R.id.mainContainer, gasCisternaFragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    } else {
                                        Bundle b = new Bundle();
                                        b.putInt("IdMarke", brandsList.get(recyclerView.getChildAdapterPosition(v)).getId());
                                        ProductsFragment productsFragment = new ProductsFragment();
                                        productsFragment.setArguments(b);


                                        transaction.replace(R.id.mainContainer, new ProductNewFragment());
                                        transaction.addToBackStack(null);
                                        transaction.commit();

                                    }

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley get", "error voley" + error.toString());
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                JSONObject obj = new JSONObject(res);
                                Log.d("Voley post", obj.toString());
                                String msj = obj.getString("message");
                                Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();

                            } catch (UnsupportedEncodingException | JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(arrayRequest);
    }

}
