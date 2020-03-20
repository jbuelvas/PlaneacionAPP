package com.example.planeacionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private Uri mHighQualityImageUri = null;
    private Bitmap mImgAttachment;
    private ArcGISFeature arcGISFeature;
    private ArcGISFeatureTable mObrasFeatureTable;
    private ArcGISFeature mCreatedFeature;
    private ServiceFeatureTable mServiceFeatureTableHitos;

    //Button
    private Button btnGuardar;

    //EditText
    EditText txtHito;

    //FloatingActionButton
    private FloatingActionButton addAttachmentFab;
    private boolean permissionsGranted = false;
    private static final int REQUEST_IMAGE_CAPTURE = 11;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;

    Context mContext;

    //Clases
    private Utilidades util;

    private String strNombre = "";
    private String strDescripcion = "";
    private String strEstado = "";
    private String idObra = "";
    private int oidHito = 0;
    private int consecutivoImg = 0;

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

            if(key.equalsIgnoreCase("ID_Obra_1")){
                idObra = value.toString();
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

        util = new Utilidades();

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
                        util.mostrarDialogoAdvertenciaMsg("Digitar hito del proyecto.", mView);
                    }
                    else
                    {
                        queryRelatedFeatures3();
                    }
                }
                catch(Exception ex){
                    String msg = ex.getMessage();
                }
            }
        });

        // inflate the floating action button
        addAttachmentFab = (FloatingActionButton) mView.findViewById(R.id.addAttachmentFAB);
        // select an image to upload as an attachment
        addAttachmentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    if (!permissionsGranted) {
                        boolean permissionCheck = ContextCompat.checkSelfPermission(((MainActivity)getActivity()).getmMapView().getContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                        System.out.println("***permissionCheck*** " + permissionCheck);
                        if (!permissionCheck) {
                            // If permissions are not already granted, request permission from the user.
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                        }
                        else{
                            permissionsGranted = true;
                            takePicture();
                        }
                    } else {
                        takePicture();
                        System.out.println("***permissionsGranted***");
                    }
                }
                catch(Exception exc){
                    System.out.println("***Exception " + exc.getMessage());
                }
            }
        });
    }

    /**
     *
     */
    public void takePicture() {
        boolean permissionCheck = ContextCompat.checkSelfPermission(((MainActivity)getActivity()).getmMapView().getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        System.out.println("***permissionCheck*** " + permissionCheck);
        if (!permissionCheck) {
            // If permissions are not already granted, request permission from the user.
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        }
        else{
            mHighQualityImageUri = generateTimeStampPhotoFileUri();
            System.out.println("*** mHighQualityImageUri: " + mHighQualityImageUri);
            // Fire off a ACTION_IMAGE_CAPTURE intent to launch a camera app.
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(((MainActivity)getActivity()).getPackageManager()) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mHighQualityImageUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     *
     */
    private Uri generateTimeStampPhotoFileUri()
    {
        Uri photoFileUri = null;
        File outputDir = getPhotoDirectory();
        if (outputDir != null)
        {
            String nombre_attachment = Constantes.NOMBRE_IMG_HITO + oidHito + "_" + consecutivoImg + ".png";
            File photoFile = new File(outputDir, nombre_attachment);
            photoFileUri = Uri.fromFile(photoFile);
        }

        return photoFileUri;
    }

    /**
     *
     */
    private File getPhotoDirectory()
    {
        File outputDir = null;
        String externalStorageStagte = Environment.getExternalStorageState();
        if (externalStorageStagte.equals(Environment.MEDIA_MOUNTED)) {
            File photoDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir = new File(photoDir, getString(R.string.app_name));
            System.out.println("*** outputDir: " + outputDir);
            if (!outputDir.exists()) {
                System.out.println("*** outputDir no existe: " + outputDir);
                if (!outputDir.mkdirs()) {
                    System.out.println("*** outputDir no se crea: " + outputDir);
                    outputDir = null;
                }
            }
        }

        return outputDir;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("*** onActivityResult ***");
        mImgAttachment = loadImageFromFile(mHighQualityImageUri.getPath(), 1000, 1000);
        // covert file to bytes to pass to ArcGISFeature
        byte[] imageByte = new byte[0];
        try {
            File imageFile = new File(mHighQualityImageUri.getPath());
            imageByte = FileUtils.readFileToByteArray(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String nombre_attachment = Constantes.NOMBRE_IMG_HITO + oidHito + "_" + consecutivoImg + ".png";
        System.out.println("*** nombre_attachment: " + nombre_attachment);
        System.out.println("*** arcGISFeature: " + arcGISFeature);

        ListenableFuture<Attachment> addResult = mCreatedFeature.addAttachmentAsync(imageByte, "image/png", nombre_attachment);
        addResult.addDoneListener(new Runnable() {
            @Override
            public void run() {
                final ListenableFuture<Void> tableResult = mServiceFeatureTableHitos.updateFeatureAsync(mCreatedFeature);
                tableResult.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("*** Attach Guardado ***");
                        applyServerEdits();
                    }
                });
            }
        });
    }

    /**
     * Applies changes from a Service Feature Table to the server.
     */
    private void applyServerEdits() {
        try {
            // check that the feature table was successfully updated
            ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable)mObrasFeatureTable;
            // apply edits to the server
            final ListenableFuture<List<FeatureEditResult>> updatedServerResult = mServiceFeatureTableHitos.applyEditsAsync();
            updatedServerResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<FeatureEditResult> edits = updatedServerResult.get();
                        System.out.println("*** edits.size() *** " + edits.size());
                        if (edits.size() > 0) {
                            if (!edits.get(0).hasCompletedWithErrors()) {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                //attachmentList.add(fileName);
                                String mAttributeID = mCreatedFeature.getAttributes().get("objectid").toString();
                                System.out.println("*** mAttributeID *** " + mAttributeID);
                                // update the attachment list view on the control panel
                                //Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.success_message), Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.failure_message), Toast.LENGTH_SHORT).show();
                                System.out.println("*** Error 1 *** ");
                            }
                        } else {
                            //Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.failure_edit_results), Toast.LENGTH_SHORT).show();
                            System.out.println("*** Error 2 *** ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("*** Exception *** " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap loadImageFromFile(String fileObject, int requiredWidth, int requiredHeight) {

        Bitmap imageBitmap = null;
        FileInputStream fileInputStream = null;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        /*
         * Setting 'inJustDecodeBounds' as true, return no bitmap on decode, but
         * allow to query the bitmap detail without allocating the memory.
         */
        bitmapOptions.inJustDecodeBounds = true;

        try {

            fileInputStream = new FileInputStream(fileObject);
            BitmapFactory.decodeStream(fileInputStream, null, bitmapOptions);
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Retrieve Bitmap Original Width and Height
        int bitmapOriginalWidth = bitmapOptions.outWidth;
        int bitmapOriginalHeight = bitmapOptions.outHeight;

        int scaleFactor = 1;

        // Find the scaling factor of the image
        while (bitmapOriginalWidth / 2 > requiredWidth
                && bitmapOriginalHeight / 2 > requiredHeight) {

            bitmapOriginalWidth /= 2;
            bitmapOriginalHeight /= 2;

            scaleFactor *= 2;
        }

        BitmapFactory.Options finalBitmapOptions = new BitmapFactory.Options();
        /*
         * Set scaleFactoer to 'inSampleSize' will requests the decoder to
         * subsample the original image, returning a smaller image to save
         * memory.
         */
        finalBitmapOptions.inSampleSize = scaleFactor;

        try {

            fileInputStream = new FileInputStream(fileObject);
            imageBitmap = BitmapFactory.decodeStream(fileInputStream, null,
                    finalBitmapOptions);
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageBitmap;
    }


    /**
     * Query related features from selected feature
     *
     */
    private void queryRelatedFeatures3() {
        mProgressDialog.setMessage("Registrando hito...");
        mProgressDialog.show();
        //ArcGISFeature feature = (ArcGISFeature) ((MainActivity)getActivity()).getmFeatureProyecto();
        final FeatureLayer selectedLayer = ((MainActivity)getActivity()).getmOperationalLayers().get(0);
        QueryParameters queryParams = new QueryParameters();
        queryParams.setWhereClause("ID_Obra_1 = " + Double.parseDouble(idObra));

        final ListenableFuture<FeatureQueryResult> relatedFeatureQueryResultFuture = selectedLayer
                .selectFeaturesAsync(queryParams, FeatureLayer.SelectionMode.NEW);

        relatedFeatureQueryResultFuture.addDoneListener(() -> {
            try {
                FeatureQueryResult result = relatedFeatureQueryResultFuture.get();

                // iterate over returned RelatedFeatureQueryResults
                for (Feature feature : result) {
                    arcGISFeature = (ArcGISFeature) feature;
                    ArcGISFeatureTable selectedTable = (ArcGISFeatureTable) feature.getFeatureTable();
                    System.out.println("* * *  selectedTable " + selectedTable.getTableName());

                    Map<String, Object> attr = arcGISFeature.getAttributes();
                    Set<String> keys = attr.keySet();
                    System.out.println("******************************************************************");

                    for (String key : keys) {
                        Object value = attr.get(key);
                        System.out.println(key + ": " + value + "\n");
                    }
                    System.out.println("******************************************************************");

                    final ListenableFuture<List<RelatedFeatureQueryResult>> relatedFeatureQueryResultFuture2 = selectedTable
                            .queryRelatedFeaturesAsync(arcGISFeature);
                    relatedFeatureQueryResultFuture.addDoneListener(() -> {
                        try {
                            List<RelatedFeatureQueryResult> relatedFeatureQueryResultList = relatedFeatureQueryResultFuture2
                                    .get();
                            // iterate over returned RelatedFeatureQueryResults
                            for (RelatedFeatureQueryResult relatedQueryResult : relatedFeatureQueryResultList) {
                                // add Table Name to List
                                String relatedTableName = relatedQueryResult.getRelatedTable().getTableName();
                                mObrasFeatureTable = relatedQueryResult.getRelatedTable();

                                if(relatedTableName.equalsIgnoreCase(getString(R.string.table_name_hitos))){
                                    System.out.println("* * *  relatedTableName " + relatedTableName);
                                    System.out.println("* * *  relatedQueryResult.getRelatedTable() " + relatedQueryResult.getRelatedTable().getTableName());

                                    mCreatedFeature = (ArcGISFeature) relatedQueryResult.getRelatedTable().createFeature();
                                    System.out.println("* * *  addFeature " + mCreatedFeature);

                                    Calendar fecha = Calendar.getInstance();
                                    System.out.println("* * *  fecha " + fecha);
                                    System.out.println("* * *  size " + mCreatedFeature.getAttributes().size());

                                    Map<String, Object> attr1 = mCreatedFeature.getAttributes();
                                    Set<String> keys1 = attr1.keySet();

                                    for (String key : keys1) {
                                        Object value = attr1.get(key);
                                        System.out.println(key + ": " + value + "\n");
                                    }

                                    //mCreatedFeature.getAttributes().put("OBJECTID", 0);
                                    //mCreatedFeature.getAttributes().put("ID_Obra", 26);
                                    mCreatedFeature.getAttributes().put("HITO", txtHito.getText().toString());
                                    //mCreatedFeature.getAttributes().put("Date", "16/03/2020");
                                    //mCreatedFeature.getAttributes().put("GlobalID", "");

                                    /*
                                    OBJECTID:	2
                                    ID_Obra:	2
                                    HITO:	Construcccion Puente
                                    Date:	1/29/2019 12:00:00 AM
                                    GlobalID:	e71a1b97-f7a9-4794-afe4-e764af65c650*/

                                    for (String key : keys1) {
                                        Object value = attr1.get(key);
                                        System.out.println(key + ": " + value + "\n");
                                    }

                                    arcGISFeature.relateFeature(mCreatedFeature);
                                    // Add the feature to the table.
                                    //relatedQueryResult.getRelatedTable().addFeatureAsync(mCreatedFeature);

                                    // check if feature can be added to feature table
                                    if (relatedQueryResult.getRelatedTable().canAdd()) {
                                        // add the new feature to the feature table and to server
                                        ListenableFuture<Void> addFeatureFuture = relatedQueryResult.getRelatedTable().addFeatureAsync(mCreatedFeature);

                                        addFeatureFuture.addDoneListener(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    // check the result of the future to find out if/when the addFeatureAsync call succeeded - exception will be
                                                    // thrown if the edit failed
                                                    System.out.println("*** Actualizando ***");
                                                    addFeatureFuture.get();

                                                    // if using an ArcGISFeatureTable, call getAddedFeaturesCountAsync to check the total number of features
                                                    // that have been added since last sync

                                                    // if dealing with ServiceFeatureTable, apply edits after making updates; if editing locally, then edits can
                                                    // be synchronized at some point using the SyncGeodatabaseTask.
                                                    //if (arcGisFeatureTable instanceof ServiceFeatureTable) {
                                                        mServiceFeatureTableHitos = (ServiceFeatureTable)relatedQueryResult.getRelatedTable();
                                                        // apply the edits
                                                        final ListenableFuture<List<FeatureEditResult>> applyEditsFuture = mServiceFeatureTableHitos.applyEditsAsync();
                                                        applyEditsFuture.addDoneListener(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    final List<FeatureEditResult> featureEditResults = applyEditsFuture.get();
                                                                    oidHito = (int)featureEditResults.get(0).getObjectId();
                                                                    // if required, can check the edits applied in this operation
                                                                    System.out.println(String.format("getObjectId: %d", featureEditResults.get(0).getObjectId()));
                                                                    mTabs.setCurrentTab(1);

                                                                    util.mostrarDialogoInfoMsg("Hito registrado.", mView);
                                                                    mProgressDialog.dismiss();
                                                                } catch (InterruptedException | ExecutionException e) {
                                                                    System.out.println(" e 1 ");
                                                                }
                                                            }
                                                        });
                                                    //}

                                                } catch (InterruptedException | ExecutionException e) {
                                                    // executionException may contain an ArcGISRuntimeException with edit error information.
                                                    if (e.getCause() instanceof ArcGISRuntimeException) {
                                                        ArcGISRuntimeException agsEx = (ArcGISRuntimeException)e.getCause();
                                                        System.out.println(String.format("Error al registra el hito %d\n=%s", agsEx.getErrorCode(), agsEx.getMessage()));
                                                        util.mostrarDialogoAdvertenciaMsg(String.format("Error al registra el hito %d\n=%s", agsEx.getErrorCode(), agsEx.getMessage()), mView);
                                                    } else {
                                                        System.out.println(" e 2 ");
                                                    }

                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });
                                    } else {
                                        System.out.println("Cannot add a feature to this feature table");
                                        util.mostrarDialogoAdvertenciaMsg("No es posible registrar el hito al proyecto, intentelo nuevamente.", mView);
                                    }
                                    // iterate over Features returned
                                        /*for (Feature relatedFeature : relatedQueryResult) {
                                            // get the Display field to use as filter on related attributes
                                            ArcGISFeature agsFeature = (ArcGISFeature) relatedFeature;
                                            String displayFieldName = agsFeature.getFeatureTable().getLayerInfo().getDisplayFieldName();
                                            String displayFieldValue = agsFeature.getAttributes().get(displayFieldName).toString();
                                            System.out.println("* * *  displayFieldName " + displayFieldName);
                                            System.out.println("* * *  displayFieldValue " + displayFieldValue);
                                        }*/
                                }

                            }
                        } catch (InterruptedException | ExecutionException e) {
                            String error = "Error getting related feature query result: " + e.getMessage();
                            Toast.makeText(this.mContext, error, Toast.LENGTH_LONG).show();
                            Log.e(TAG, error);
                        }
                    });
                }
            } catch (InterruptedException | ExecutionException e) {
                String error = "Error getting related feature query result: " + e.getMessage();
                Toast.makeText(this.mContext, error, Toast.LENGTH_LONG).show();
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
