package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ResultadosActivity extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = ResultadosActivity.class.getSimpleName();

    private View mView;

    private String[] mResultados;
    private Feature[] mFeatures;

    @SuppressLint("ValidFragment")
    public ResultadosActivity(String[] resultados, Feature[] features){
        mResultados = new String[resultados.length];
        mResultados = resultados;

        mFeatures = new Feature[features.length];
        mFeatures = features;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.form_resultados, container);

        ListView lvResultado = (ListView) mView.findViewById(R.id.listaResultados);
        ArrayList<Feature> listaFeatures  = new ArrayList<Feature>(Arrays.asList(mFeatures));

        ResultadosListAdapter adapter = new ResultadosListAdapter(mView.getContext(), listaFeatures);
        lvResultado.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvResultado.setAdapter(adapter);

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, mResultados);

        lvResultado.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @SuppressLint("RestrictedApi")
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
            {
                Feature feat = mFeatures[pos];
                ArcGISFeature arcGISFeature = (ArcGISFeature) feat;
                ArcGISFeatureTable selectedTable = (ArcGISFeatureTable) feat.getFeatureTable();
                //System.out.println(">>>>>>>>>> " + selectedTable.getTableName());

                QueryParameters queryParams = new QueryParameters();
                queryParams.setWhereClause("1=1");
                //
                //final FeatureLayer selectedLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(0);
                //queryRelatedFeatures(selectedLayer, queryParams);

                ((MainActivity) getActivity()).getmCallout().dismiss();
                ((MainActivity)getActivity()).getmMapView().setViewpointGeometryAsync(feat.getGeometry().getExtent(), 10);
                ((MainActivity)getActivity()).setProyecto(feat);
                dismiss();
            }
        });

        return mView;
    }

    /**
     * Uses the selected FeatureLayer to get FeatureTable RelationshipInfos used to
     * QueryRelatedFeaturesAsync which returns a list of related features.
     *
     * @param featureLayer    Layer selected from the Map
     * @param queryParameters Input parameters for query
     */
    private void queryRelatedFeatures(final FeatureLayer featureLayer, QueryParameters queryParameters) {
        final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = featureLayer
                .selectFeaturesAsync(queryParameters, FeatureLayer.SelectionMode.NEW);
        // clear previously selected layers
        featureLayer.clearSelection();
        featureQueryResultFuture.addDoneListener(() -> {
            // call get on the future to get the result
            try {
                if (featureQueryResultFuture.get().iterator().hasNext()) {
                    FeatureQueryResult result = featureQueryResultFuture.get();
                    System.out.println(">>>>>>>>>> result: " + result);
                    // iterate over features returned
                    for (Feature feature : result) {
                        System.out.println(">>>>>>>>>> feature: " + feature);
                        ArcGISFeature arcGISFeature = (ArcGISFeature) feature;
                        ArcGISFeatureTable selectedTable = (ArcGISFeatureTable) feature.getFeatureTable();
                        final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture = selectedTable
                                .queryRelatedFeaturesAsync(arcGISFeature);
                        relatedFeatureQueryResultFuture.addDoneListener(() -> {
                            try {
                                List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture
                                        .get();
                                // iterate over returned RelatedFeatureQueryResults
                                for (RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList) {
                                    // add Table Name to List
                                    String relatedTableName = relatedQueryResult.getRelatedTable().getTableName();
                                    System.out.println(">>>>>>>>>> " + relatedTableName);
                                    // iterate over Features returned
                                    for (Feature relatedFeature : relatedQueryResult) {
                                        // get the Display field to use as filter on related attributes
                                        ArcGISFeature agsFeature = (ArcGISFeature) relatedFeature;
                                        String displayFieldName = agsFeature.getFeatureTable().getLayerInfo().getDisplayFieldName();
                                        String displayFieldValue = agsFeature.getAttributes().get(displayFieldName).toString();
                                        System.out.println(">>>>>>>>>> " + displayFieldName);
                                        System.out.println(">>>>>>>>>> " + displayFieldValue);
                                    }
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                String error = "Error getting related feature query result: " + e.getMessage();
                                Toast.makeText(mView.getContext(), error, Toast.LENGTH_LONG).show();
                                Log.e(TAG, error);
                            }
                        });
                    }
                } else {
                    System.out.println(">>>>>>>>>> " + "No Resultados");
                }
            } catch (InterruptedException | ExecutionException e) {
                String error = "Error getting feature query result: " + e.getMessage();
                Toast.makeText(mView.getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
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
