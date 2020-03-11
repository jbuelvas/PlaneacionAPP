package com.example.planeacionapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Clase ResultadosListAdapter
 *
 * Clase que visualiza los resultados de los listados
 * @author Jaime Buelvas
 * @version: 1.0
 */
public class ResultadosListAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater lInflater;
    private ArrayList<Feature> objects;

    private String strNombre;
    private String strDescripcion;
    private String strAnioFiscal;

    public ResultadosListAdapter(Context context, ArrayList<Feature> features) {
        ctx = context;
        objects = features;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    Feature getResultado(int position) {
        return ((Feature) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_resultados, parent, false);
        }

        Feature feature = getResultado(position);

        Map<String, Object> attr = feature.getAttributes();
        Set<String> keys = attr.keySet();
        for (String key : keys) {
            Object value = attr.get(key);
            System.out.println(key);

            if(key.equalsIgnoreCase("Nom_Proyecto")){
                strNombre = "" + value;
            }
            if(key.equalsIgnoreCase("DescrProyecto")){
                strDescripcion = "" + value;
            }
            if(key.equalsIgnoreCase("AñoFiscal")){
                strAnioFiscal = "" + value;
            }
        }

        System.out.println(strNombre);

        ((TextView) view.findViewById(R.id.tv_nombre)).setText(strNombre.toUpperCase());
        ((TextView) view.findViewById(R.id.tv_descripcion)).setText(strDescripcion.substring(0,1).toUpperCase() + strDescripcion.substring(1).toLowerCase());
        ((TextView) view.findViewById(R.id.tv_anio_fiscal)).setText(strAnioFiscal.substring(0,4));

        return view;
    }
}
