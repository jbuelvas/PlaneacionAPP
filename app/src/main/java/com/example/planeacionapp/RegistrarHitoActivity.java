package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TabHost;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;

import java.util.Map;
import java.util.Set;

import androidx.fragment.app.DialogFragment;

public class RegistrarHitoActivity extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = RegistrarHitoActivity.class.getSimpleName();

    private View mView;
    private ProgressDialog mProgressDialog;
    private TabHost mTabs;

    private String strNombre = "";
    private String strDescripcion = "";
    private String strEstado = "";

    @SuppressLint("ValidFragment")
    public RegistrarHitoActivity(Feature feature){
        Map<String, Object> attr = feature.getAttributes();
        Set<String> keys = attr.keySet();
        for (String key : keys) {
            Object value = attr.get(key);

            if(key.equalsIgnoreCase("Nom_Proyecto")){
                strNombre = value.toString();
            }

            if(key.equalsIgnoreCase("DescrProyecto")){
                strDescripcion = value.toString();
            }

            if(key.equalsIgnoreCase("Estado")){
                strEstado = value.toString();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.form_registrar_hito, container);
        
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        TextView mTitle = (TextView) mView.findViewById(R.id.alertTitleHito);
        mTitle.setText("Guardar Hito");
        mTitle.setTypeface(null, Typeface.BOLD);
        mTitle.setTextColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        View mDivider = mView.findViewById(R.id.titleDividerHito);
        mDivider.setBackgroundColor(Color.parseColor(Constantes.COLOR_TITULO_MENSAJES));

        mProgressDialog = new ProgressDialog(this.getContext());

        TextView txtNombre = mView.findViewById(R.id.txt_nombre);
        txtNombre.setText(strNombre);

        TextView txtDescripcion = mView.findViewById(R.id.txt_descripcion);
        txtDescripcion.setText(strDescripcion);

        TextView txtEstado = mView.findViewById(R.id.txt_estado);
        txtEstado.setText(strEstado);

        this.cargarTabs();

        return mView;
    }

    /**
     *
     */
    private void cargarTabs() {
        Resources res = getResources();

        mTabs=(TabHost) mView.findViewById(android.R.id.tabhost);
        mTabs.setup();

        TabHost.TabSpec spec = mTabs.newTabSpec("Proyecto");
        spec.setContent(R.id.tabFormulario);
        spec.setIndicator("Proyecto" ,
                res.getDrawable(android.R.drawable.ic_btn_speak_now));
        mTabs.addTab(spec);

        spec=mTabs.newTabSpec("Fotos");
        spec.setContent(R.id.tabFotos);
        spec.setIndicator("Fotos",
                res.getDrawable(android.R.drawable.ic_dialog_map));
        mTabs.addTab(spec);

        mTabs.setCurrentTab(0);
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
