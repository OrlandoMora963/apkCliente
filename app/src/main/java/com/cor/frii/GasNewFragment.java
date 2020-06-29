package com.cor.frii;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.cor.frii.pojo.Product;
import com.cor.frii.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GasNewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GasNewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GasNewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String urlBase = "http://34.71.251.155";
    private OnFragmentInteractionListener mListener;


    private GasProductAdapterNew gasProductAdapter;
    private RecyclerView recyclerView;
    ArrayList<Product> products;


    public GasNewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GasNewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GasNewFragment newInstance(String param1, String param2) {
        GasNewFragment fragment = new GasNewFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gas_new, container, false);
        recyclerView = view.findViewById(R.id.ProductGasContainer_new);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    /*    products=new ArrayList<>();

        products.add(new Product(1,"solgas ","",2,2,2,"","",""));
        products.add(new Product(2,"intigas","",2,2,2,"","",""));
        products.add(new Product(3,"llamagas","",2,2,2,"","",""));

        gasProductAdapter=new GasProductAdapterNew(products);
        recyclerView.setAdapter(gasProductAdapter);*/
        llenarDatos();

        return view;
    }
    private void llenarDatos() {
        products = new ArrayList<>();
        Bundle b = this.getArguments();
        String type = "";
        if (b != null) {
            type = b.getString("type");
        }
        String url = this.urlBase + "/api/product/gas/" + type;
        JSONObject jsonArray = new JSONObject();
        final String finalType = type;
        JsonObjectRequest arrayRequest =
                new JsonObjectRequest(Request.Method.GET, url, jsonArray, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray array = response.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                String imagen_url = urlBase + object.getString("image");
                                Product product = new Product(
                                        Integer.parseInt(object.getString("id")),
                                        object.getJSONObject("marke_id").getString("name") + " " +
                                                object.getJSONObject("detail_measurement_id").getString("name"),
                                        "",
                                        Float.parseFloat(object.getString("unit_price")),

                                        object.getInt("measurement"),
                                        1,
                                        imagen_url,
                                        finalType,
                                        object.getJSONObject("marke_id").getString("name"));

                                products.add(product);
                            }

                            gasProductAdapter = new GasProductAdapterNew(products);
                            recyclerView.setAdapter(gasProductAdapter);
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
                                Toast.makeText(getContext(), "No hay productos :(", Toast.LENGTH_SHORT).show();

                            } catch (UnsupportedEncodingException | JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(arrayRequest);
    }
    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
