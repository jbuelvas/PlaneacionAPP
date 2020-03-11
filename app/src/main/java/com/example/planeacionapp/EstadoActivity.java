package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EstadoActivity extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = EstadoActivity.class.getSimpleName();

    private View mView;
    private Spinner mSpinnerEstado;
    private ArrayAdapter<String> mEstadoAdapter;

    private Button mBtnBuscar;
    private ProgressDialog mProgressDialog;

    private ServiceFeatureTable mServiceFeatureTable;

    String valorEstado;
    Context mContext;

    String[] mArrayEstadoItems = {Constantes.VALOR_SIN_SELECCION, "A TIEMPO","RETRASADO","SUSPENDIDO"};
    final String[] arrayEstadoValues={"0", "1", "2", "3"};

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.form_estado, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        TextView mTitle = (TextView) mView.findViewById(R.id.alertTitleManzana);
        mTitle.setText("Seleccione Estado");
        mTitle.setTypeface(null, Typeface.BOLD);
        mTitle.setTextColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        View mDivider = mView.findViewById(R.id.titleDividerManzana);
        mDivider.setBackgroundColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        mProgressDialog = new ProgressDialog(this.getContext());
        mContext = this.getContext();

        // create a service feature table and a feature layer from it
        mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.obra_service_url));

        mSpinnerEstado = (Spinner) mView.findViewById(R.id.list_estados);
        cargarEstados();

        LocationDisplay locationDisplay = ((MainActivity)getActivity()).getmMapView().getLocationDisplay();
        locationDisplay.stop();

        ((MainActivity)getActivity()).getGeodesicGraphicsOverlay().getGraphics().clear();

        mBtnBuscar = (Button) mView.findViewById(R.id.btn_buscar);
        mBtnBuscar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                mProgressDialog.setMessage("Consultando...");
                mProgressDialog.show();

                String consulta = "Est_Cumplimiento = " + Integer.valueOf(valorEstado);

                // create objects required to do a selection with a query
                QueryParameters query = new QueryParameters();
                // make search case insensitive
                query.setWhereClause(consulta);
                if(valorEstado == "0"){
                    query.setWhereClause("1=1");
                }
                // call select features
                final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                // add done loading listener to fire when the selection returns
                future.addDoneListener(() -> {
                    try {
                        int contador = 0;
                        ArrayList<String> listaResultados = new ArrayList<String>();
                        ArrayList<Feature> listaFeatures = new ArrayList<Feature>();
                        // call get on the future to get the result
                        FeatureQueryResult result = future.get();
                        // check there are some results
                        Iterator<Feature> resultIterator = result.iterator();
                        Feature feature;
                        Envelope envelope = null;
                        if (resultIterator.hasNext()) {
                            // get the extent of the first feature in the result to zoom to
                            //Feature feature = resultIterator.next();
                            //Envelope envelope = feature.getGeometry().getExtent();

                            while(resultIterator.hasNext()) {
                                contador++;
                                feature = resultIterator.next();
                                envelope = feature.getGeometry().getExtent();
                                listaFeatures.add(feature);
                            }
                            Feature[] features = (Feature[]) listaFeatures.toArray(new Feature[listaFeatures.size()]);

                            ((MainActivity)getActivity()).setResultados(features);
                            ((MainActivity)getActivity()).getmFabResultados().setVisibility(View.VISIBLE);

                            ((MainActivity)getActivity()).getmMapView().setViewpointGeometryAsync(envelope, 10);
                            //mMapView.setViewpointGeometryAsync(envelope, 10);
                            // select the feature
                            //((MainActivity)getActivity()).getmFeatureLayerObra().selectFeature(feature);
                            Toast.makeText(mContext, "Se encontraron " + contador + " resultados", Toast.LENGTH_LONG).show();

                            if(valorEstado == "0"){
                                ((MainActivity)getActivity()).getmFeatureLayerObra().setDefinitionExpression("1=1");
                            }
                            else{
                                ((MainActivity)getActivity()).getmFeatureLayerObra().setDefinitionExpression(consulta);
                            }
                            //((MainActivity)getActivity()).getmMap().getOperationalLayers().add(((MainActivity)getActivity()).getmFeatureLayerObra());

                            dismiss();
                        }
                        else
                        {
                            ((MainActivity)getActivity()).getmFeatureLayerObra().setDefinitionExpression("OBJECTID < 0");
                            ((MainActivity)getActivity()).getmFabResultados().setVisibility(View.GONE);
                            Toast.makeText(mContext, "No existen proyectos con el estado consultado", Toast.LENGTH_LONG).show();
                        }

                        mProgressDialog.dismiss();

                    } catch (Exception e) {
                        String error = "Error: " + valorEstado + ". Error: " + e.getMessage();
                        //Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, error);
                    }
                });
            }
        });

        return mView;
    }

    /*
     *
     */
    private void cargarEstados(){
        mEstadoAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, mArrayEstadoItems);
        mSpinnerEstado.setAdapter(mEstadoAdapter);

        mSpinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                valorEstado = arrayEstadoValues[arg2];
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
