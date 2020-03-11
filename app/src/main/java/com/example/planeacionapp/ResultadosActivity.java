package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;

import com.esri.arcgisruntime.data.Feature;

import java.util.ArrayList;
import java.util.Arrays;

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
            @SuppressLint("RestrictedApi")
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
            {
                Feature feat = mFeatures[pos];
                ((MainActivity) getActivity()).getmCallout().dismiss();
                ((MainActivity)getActivity()).getmMapView().setViewpointGeometryAsync(feat.getGeometry().getExtent(), 10);
                ((MainActivity)getActivity()).setProyecto(feat);
                dismiss();
            }
        });

        return mView;
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
