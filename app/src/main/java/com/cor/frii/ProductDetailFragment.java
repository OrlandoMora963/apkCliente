package com.cor.frii;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cor.frii.pojo.ProductStaff;
import com.cor.frii.utils.MapSelection;
import com.cor.frii.utils.MyJsonArrayRequest;
import com.cor.frii.utils.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProductDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProductDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    public int product_id=0;
    private ProductDetailAdapter productDetailAdapter;
    private RecyclerView recyclerView;
    ArrayList<ProductStaff> products;

    public ProductDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductDetailFragment newInstance(String param1, String param2) {
        ProductDetailFragment fragment = new ProductDetailFragment();
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
        final View view = inflater.inflate(R.layout.fragment_product_detail, container, false);
       recyclerView = view.findViewById(R.id.DetailProductsContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

     /*    products=new ArrayList<>();
        products.add(new Product(1,"cerveza pilse de 1 litro ","",2,2,2,"","",""));
        products.add(new Product(2,"cerveza pilse de 1 litro ","",2,2,2,"","",""));
        products.add(new Product(3,"cerveza pilse de 1 litro ","",2,2,2,"","",""));

        productDetailAdapter=new ProductDetailAdapter(products);
        recyclerView.setAdapter(productDetailAdapter);*/
        ListarDatos();
        return view;
    }
    public void onResume() {
        super.onResume();
        ListarDatos();

    }
    public  void ListarDatos(){
        JSONObject object = new JSONObject();
        try {
            object.put("product_id", product_id);
            object.put("latitude", MapSelection.latitud);
            object.put("longitude", MapSelection.longitud);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = "http://34.71.251.155/api/products/staff";
        MyJsonArrayRequest objectRequest = new MyJsonArrayRequest (Request.Method.POST,
                url,
                object,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            recyclerView.setAdapter(null);
                            Gson gson = new Gson();
                            products = gson.fromJson(response.toString(), new TypeToken<ArrayList<ProductStaff>>() {
                            }.getType());
                            productDetailAdapter= new ProductDetailAdapter(products);
                            recyclerView.setAdapter(productDetailAdapter);
                            for (ProductStaff item : products) {
                                item.getProductID().setImage("http://34.71.251.155/"+item.getProductID().getImage());
                            }
                            if(products.size()==0){
                                Toast.makeText(getContext(), "No hay proveedores con ese producto", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) ;

        VolleySingleton.getInstance(getContext()).addToRequestQueue(objectRequest);
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
