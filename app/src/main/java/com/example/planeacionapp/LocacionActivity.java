package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
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
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LocacionActivity extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = LocacionActivity.class.getSimpleName();

    private View mView;
    private ProgressDialog mProgressDialog;
    Context mContext;

    String[] mArrayDistanciaItems = {"300 mts","500 mts","1.000 mts"};
    final String[] arrayDistanciaValues={"300", "500", "1000"};

    private Spinner mSpinnerDistancia;
    private ArrayAdapter<String> mDistanciaAdapter;
    String valorDistancia;

    private Button mBtnBuscar;

    private ServiceFeatureTable mServiceFeatureTable;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.form_locacion, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        TextView mTitle = (TextView) mView.findViewById(R.id.alertTitleLocacion);
        mTitle.setText("Cerca de mí en");
        mTitle.setTypeface(null, Typeface.BOLD);
        mTitle.setTextColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        View mDivider = mView.findViewById(R.id.titleDividerLocacion);
        mDivider.setBackgroundColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        mProgressDialog = new ProgressDialog(this.getContext());
        mContext = this.getContext();

        // create a service feature table and a feature layer from it
        mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.obra_service_url));

        cargarDistancias();

        LocationDisplay locationDisplay = ((MainActivity)getActivity()).getmMapView().getLocationDisplay();
        locationDisplay.stop();

        ((MainActivity)getActivity()).getGeodesicGraphicsOverlay().getGraphics().clear();

        mBtnBuscar = (Button) mView.findViewById(R.id.btn_buscar_locacion);
        mBtnBuscar.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View arg0)
            {
                mProgressDialog.setMessage("Consultando...");
                mProgressDialog.show();

                MainActivity mainActivity = ((MainActivity)getActivity());

                LocationDisplay locationDisplay = ((MainActivity)getActivity()).getmMapView().getLocationDisplay();
                locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                locationDisplay.startAsync();

                locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
                    @Override
                    public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {

                        LocationDataSource.Location location = locationDisplay.getLocation();
                        if (location != null) {
                            Point point = location.getPosition();
                            System.out.println(point.getX());
                            System.out.println(point.getY());

                            // get the buffer distance (miles) entered in the text box.
                            double bufferInMeters = Double.valueOf(valorDistancia);

                            // create a planar buffer graphic around the input location at the specified distance
                            Geometry bufferGeometryPlanar = GeometryEngine.buffer(point, bufferInMeters);
                            Graphic planarBufferGraphic = new Graphic(bufferGeometryPlanar);

                            // create a geodesic buffer graphic using the same location and distance
                            Geometry bufferGeometryGeodesic = GeometryEngine.bufferGeodetic(point, bufferInMeters,
                                    new LinearUnit(LinearUnitId.METERS), Double.NaN, GeodeticCurveType.GEODESIC);
                            Graphic geodesicBufferGraphic = new Graphic(bufferGeometryGeodesic);

                            // create a graphic for the user tap location
                            Graphic locationGraphic = new Graphic(point);

                            //planarGraphicsOverlay.getGraphics().clear();
                            mainActivity.getGeodesicGraphicsOverlay().getGraphics().clear();
                            //tapLocationsOverlay.getGraphics().clear();

                            // add the buffer polygons and tap location graphics to the appropriate graphic overlays.
                            //planarGraphicsOverlay.getGraphics().add(planarBufferGraphic);
                            mainActivity.getGeodesicGraphicsOverlay().getGraphics().add(geodesicBufferGraphic);
                            //tapLocationsOverlay.getGraphics().add(locationGraphic);

                            QueryParameters query = new QueryParameters();
                            query.setGeometry(mainActivity.getGeodesicGraphicsOverlay().getExtent());
                            QueryParameters.OrderBy orderBy = new QueryParameters.OrderBy("Nom_Proyecto".trim(), QueryParameters.SortOrder.ASCENDING);
                            query.getOrderByFields().add(orderBy);
                            mainActivity.getmFeatureLayerObra().setDefinitionExpression("1=1");
                            // call select features
                            final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = mServiceFeatureTable.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                            // add done loading listener to fire when the selection returns
                            featureQueryResultFuture.addDoneListener(() -> {
                                try {
                                    ArrayList<Feature> listaFeatures = new ArrayList<Feature>();
                                    // call get on the future to get the result
                                    FeatureQueryResult featureQueryResult = featureQueryResultFuture.get();
                                    // create an Iterator
                                    Iterator<Feature> iterator = featureQueryResult.iterator();
                                    Feature feature;
                                    // cycle through selections
                                    int counter = 0;
                                    while (iterator.hasNext()) {
                                        feature = iterator.next();
                                        counter++;
                                        listaFeatures.add(feature);
                                        Log.d(TAG, "Selection #: " + counter + " Table name: " + feature.getFeatureTable().getTableName());
                                    }
                                    Feature[] features = (Feature[]) listaFeatures.toArray(new Feature[listaFeatures.size()]);

                                    mainActivity.setResultados(features);
                                    Toast.makeText(mContext, counter + " registros encontrados", Toast.LENGTH_SHORT).show();

                                    if(counter == 0){
                                        mainActivity.getmFabResultados().setVisibility(View.GONE);
                                    }
                                    else
                                    {
                                        mainActivity.getmFabResultados().setVisibility(View.VISIBLE);
                                    }

                                } catch (Exception e) {
                                    Log.e(TAG, "Select feature failed: " + e.getMessage());
                                }

                                dismiss();
                                locationDisplay.stop();
                                mainActivity.getGeodesicGraphicsOverlay().getGraphics().clear();
                            });
                        }
                    }
                });

                mProgressDialog.dismiss();

            }
        });

        return mView;
    }

    /*
     *
     */
    private void cargarDistancias(){
        mDistanciaAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, mArrayDistanciaItems);

        mSpinnerDistancia = (Spinner) mView.findViewById(R.id.list_distancias);
        mSpinnerDistancia.setAdapter(mDistanciaAdapter);

        mSpinnerDistancia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                valorDistancia = arrayDistanciaValues[arg2];
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
