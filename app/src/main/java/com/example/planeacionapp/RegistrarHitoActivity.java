package com.example.planeacionapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Clase RegistrarHitoActivity
 *
 * Clase que registra un hito de una obra
 * @author Jaime Buelvas
 * @version: 1.0
 */
public class RegistrarHitoActivity extends DialogFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    public static final String TAG = RegistrarHitoActivity.class.getSimpleName();

    private View mView;
    private ProgressDialog mProgressDialog;
    private TabHost mTabs;

    //Button
    private Button btnGuardar;

    //EditText
    EditText txtHito;

    Context mContext;

    //Clases
    private Utilidades util;

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
        mContext = this.getContext();

        TextView txtNombre = mView.findViewById(R.id.txt_nombre);
        txtNombre.setText(strNombre);

        TextView txtDescripcion = mView.findViewById(R.id.txt_descripcion);
        txtDescripcion.setText(strDescripcion);

        TextView txtEstado = mView.findViewById(R.id.txt_estado);
        txtEstado.setText(strEstado);

        txtHito = (EditText) mView.findViewById(R.id.txt_hito);

        this.cargarTabs();
        this.cargarBotones();

        return mView;
    }

    /**
     * Método que inicializa los tabs del formulario
     * @param
     * @return
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

    /**
     * Método que inicializa los botones del formulario
     * @param
     * @return
     */
    private void cargarBotones() {
        btnGuardar = (Button) mView.findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                try
                {

                    System.out.println(txtHito.getText().toString());
                    if(txtHito.getText().toString().equalsIgnoreCase("")){
                        System.out.println("Hito1");
                        new AlertDialog.Builder(mContext)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Advertencia")
                                .setMessage("Digitar el hito de la obra.")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //finish();
                                    }
                                })
                                /*.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Toast.makeText(getApplicationContext(),"Nothing Happens",Toast.LENGTH_LONG).show();
                                    }
                                })*/
                                .show();
                    }
                    else
                    {
                        System.out.println("Hito2");
                    }
                }
                catch(Exception ex){
                    String msg = ex.getMessage();
                }
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
