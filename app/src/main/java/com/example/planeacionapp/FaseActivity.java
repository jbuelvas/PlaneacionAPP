package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.esri.arcgisruntime.arcgisservices.RelationshipInfo;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.EnvelopeBuilder;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FaseActivity extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = FaseActivity.class.getSimpleName();

    private View mView;
    private Spinner mSpinnerFase;
    private ArrayAdapter<String> mFaseAdapter;
    private LinearLayout mLayoutZonas;

    private Button mBtnBuscar;
    private ProgressDialog mProgressDialog;

    private ServiceFeatureTable mServiceFeatureTable;

    String valorFase;
    String valorZona;
    Context mContext;

    String[] mArrayFaseItems = {Constantes.VALOR_VACIO, "METROPOLITANA", "NORTE - CENTRO HISTORICO","RIOMAR","SUROCCIDENTE", "SURORIENTE"};
    final String[] arrayFaseValues={Constantes.VALOR_VACIO, "METROPOLITANA", "NORTE - CENTRO HISTORICO","RIOMAR","SUROCCIDENTE", "SURORIENTE"};

    private Spinner mSpinnerZonas;
    private ArrayAdapter<String> mZonasAdapter;

    ArrayList<Feature> mlistaFeaturesSectores;
    Feature[] arrayFeaturesSectores;
    Feature mSelectedFeature;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.form_fase, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        TextView mTitle = (TextView) mView.findViewById(R.id.alertTitleManzana1);
        mTitle.setText("Seleccione Localidad y Zona");
        mTitle.setTypeface(null, Typeface.BOLD);
        mTitle.setTextColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        View mDivider = mView.findViewById(R.id.titleDividerManzana1);
        mDivider.setBackgroundColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        mProgressDialog = new ProgressDialog(this.getContext());
        mContext = this.getContext();

        mlistaFeaturesSectores = new ArrayList<Feature>();


        // create a service feature table and a feature layer from it
        mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.zonas_service_url));

        mSpinnerFase = (Spinner) mView.findViewById(R.id.list_fases);
        cargarFases();

        mLayoutZonas = (LinearLayout) mView.findViewById(R.id.cont_zonas);

        ((MainActivity)getActivity()).getGeodesicGraphicsOverlay().getGraphics().clear();

        mBtnBuscar = (Button) mView.findViewById(R.id.btn_buscar_fase);
        mBtnBuscar.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                if(!valorFase.equalsIgnoreCase(Constantes.VALOR_VACIO)){
                    if(!valorZona.equalsIgnoreCase(Constantes.VALOR_VACIO)){
                        mProgressDialog.setMessage("Consultando...");
                        mProgressDialog.show();

                        String consulta = "";

                        final FeatureLayer zonasLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(0);
                        final FeatureLayer manzanasLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(5);
                        final FeatureLayer ruteoPriLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(7);
                        final FeatureLayer ruteoSecLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(8);
                        final FeatureLayer paradasLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(9);

                        if(mSelectedFeature != null){
                            consulta = "Sector = '" + valorZona + "'";

                            ((MainActivity)getActivity()).acercarSector(mSelectedFeature, consulta);
                        }
                        else{
                            consulta = "OBJECTID < 0";
                        }

                        zonasLayer.setDefinitionExpression(consulta);
                        manzanasLayer.setDefinitionExpression(consulta);
                        ruteoPriLayer.setDefinitionExpression(consulta);
                        ruteoSecLayer.setDefinitionExpression(consulta);
                        paradasLayer.setDefinitionExpression(consulta);

                        mProgressDialog.dismiss();
                        dismiss();


                    }
                    else
                    {
                        Toast.makeText(mContext, "Seleccione una zona", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    mLayoutZonas.setVisibility(LinearLayout.GONE);
                    Toast.makeText(mContext, "Seleccione una localidad", Toast.LENGTH_LONG).show();
                }
            }
        });

        return mView;
    }

    /*
     *
     */
    private void cargarFases(){
        mFaseAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, mArrayFaseItems);
        mSpinnerFase.setAdapter(mFaseAdapter);

        mSpinnerFase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                valorFase = arrayFaseValues[arg2];

                if(!valorFase.equalsIgnoreCase(Constantes.VALOR_VACIO)){
                    mProgressDialog.setMessage("Consultando...");
                    mProgressDialog.show();

                    String consulta = "Localidad = '" + valorFase + "'";

                    //System.out.println(consulta);

                    // create objects required to do a selection with a query
                    QueryParameters query = new QueryParameters();
                    // make search case insensitive
                    query.setWhereClause(consulta);
                    QueryParameters.OrderBy orderBy = new QueryParameters.OrderBy("Sector", QueryParameters.SortOrder.ASCENDING);
                    query.getOrderByFields().add(orderBy);

                    final FeatureLayer zonasLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(0);

                    // call select features
                    final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {

                            ArrayList<String> listaSectores = new ArrayList<String>();
                            listaSectores.add(Constantes.VALOR_VACIO);
                            // call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // check there are some results
                            Iterator<Feature> resultIterator = result.iterator();
                            Feature feature;
                            Envelope envelope = null;
                            EnvelopeBuilder envBuilder = new EnvelopeBuilder(zonasLayer.getSpatialReference());
                            //System.out.println("Paso 1");
                            if (resultIterator.hasNext()) {
                                while(resultIterator.hasNext()) {
                                    //System.out.println("Paso 2");
                                    feature = resultIterator.next();
                                    listaSectores.add(feature.getAttributes().get("Sector").toString());
                                    mlistaFeaturesSectores.add(feature);
                                }
                                String[] arrayZonas = (String[]) listaSectores.toArray(new String[listaSectores.size()]);
                                arrayFeaturesSectores = (Feature[]) mlistaFeaturesSectores.toArray(new Feature[mlistaFeaturesSectores.size()]);

                                mSpinnerZonas = (Spinner) mView.findViewById(R.id.list_zonas);
                                mZonasAdapter = new ArrayAdapter<String>(getContext(),
                                        android.R.layout.simple_spinner_dropdown_item, arrayZonas);
                                mSpinnerZonas.setAdapter(mZonasAdapter);

                                mSpinnerZonas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                               int arg2, long arg3) {
                                        valorZona = arrayZonas[arg2];
                                        //System.out.println("valorZona: " + valorZona);

                                        if(arg2 > 0){
                                            mSelectedFeature = arrayFeaturesSectores[arg2-1];
                                        }
                                        else{
                                            mSelectedFeature = null;
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> arg0) {
                                        // TODO Auto-generated method stub
                                    }
                                });

                                mLayoutZonas.setVisibility(LinearLayout.VISIBLE);
                            }
                            else
                            {
                                mLayoutZonas.setVisibility(LinearLayout.GONE);
                                Toast.makeText(mContext, "No existen zonas asignadas a la localidad consultada", Toast.LENGTH_LONG).show();
                            }

                            mProgressDialog.dismiss();

                        } catch (Exception e) {
                            String error = "Error: " + valorFase + ". Error: " + e.getMessage();
                            //Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                            Log.e(TAG, error);
                        }
                    });
                }
                else
                {
                    mLayoutZonas.setVisibility(LinearLayout.GONE);
                    Toast.makeText(mContext, "Seleccione una localidad", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
    }

    /**
     * M�todo encargado de manejar el evento de selecci�n de un combo
     */
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}
