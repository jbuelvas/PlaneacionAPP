package com.example.planeacionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.example.planeacionapp.ui.spinner.ItemData;
import com.example.planeacionapp.ui.spinner.SpinnerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AppBarConfiguration mAppBarConfiguration;

    private MapView mMapView;
    private ArcGISMap mMap;
    private Portal mPortal;
    private PortalItem mPortalItem;
    private Spinner mSpinner;
    private Spinner mSpinnerRutas;
    private ProgressDialog mProgressDialog;

    private android.graphics.Point mScreenPoint;
    private com.esri.arcgisruntime.geometry.Point mMapPoint;
    private android.graphics.Point mClickPoint;

    private FeatureLayer mFeatureLayerManzana;

    public ServiceFeatureTable getmServiceFeatureTableManzana() {
        return mServiceFeatureTableManzana;
    }

    private ServiceFeatureTable mServiceFeatureTableManzana;

    public ArcGISFeature getmSelectedArcGISFeature() {
        return mSelectedArcGISFeature;
    }

    private ArcGISFeature mSelectedArcGISFeature;

    private Callout mCallout;
    LayoutInflater mInflater;
    private LocationDisplay mLocationDisplay;

    private GraphicsOverlay geodesicGraphicsOverlay = null;

    private FloatingActionButton mFabResultados;
    private FloatingActionButton mFabRegistrarHito;
    private Feature mFeatureProyecto;

    public ArrayList<FeatureLayer> getmOperationalLayers() {
        return mOperationalLayers;
    }

    private final ArrayList<FeatureLayer> mOperationalLayers = new ArrayList<>();

    private int requestCode = 2;
    String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION};

    public FloatingActionButton getmFabRegistrarHito() {
        return mFabRegistrarHito;
    }

    public FloatingActionButton getmFabResultados() {
        return mFabResultados;
    }

    public GraphicsOverlay getGeodesicGraphicsOverlay() {
        return geodesicGraphicsOverlay;
    }

    public ArcGISMap getmMap() {
        return mMap;
    }

    public MapView getmMapView() {
        return mMapView;
    }

    public FeatureLayer getmFeatureLayerManzana() {
        return mFeatureLayerManzana;
    }

    public Callout getmCallout() {
        return mCallout;
    }

    public Feature getmFeatureProyecto() {
        return mFeatureProyecto;
    }

    private Utilidades util;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInflater = this.getLayoutInflater();

        util = new Utilidades();

        // license with a license key
        ArcGISRuntimeEnvironment.setLicense(getResources().getString(R.string.license_key));

        // *** ADD ***
        mMapView = (MapView) findViewById(R.id.mapView);
        // get the portal url for ArcGIS Online
        mPortal = new Portal(getResources().getString(R.string.portal_url));
        // get the pre-defined portal id and portal url
        mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.webmap_licencias_id));
        // create a map from a PortalItem
        mMap = new ArcGISMap(mPortalItem);

        mServiceFeatureTableManzana = new ServiceFeatureTable(getString(R.string.manzanas_service_url));
        mServiceFeatureTableManzana.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE);
        mFeatureLayerManzana = new FeatureLayer(mServiceFeatureTableManzana);

        mFeatureLayerManzana.setDefinitionExpression("OBJECTID < 0");

        mProgressDialog = new ProgressDialog(mMapView.getContext());

        //mMap.getOperationalLayers().add(mFeatureLayerManzana);

        mMapView.setMap(mMap);
        mMap.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                //System.out.println("*** mMap.getLoadStatus(): " + mMap.getLoadStatus());
                if (mMap.getLoadStatus() == LoadStatus.LOADED) {
                    // create Features to use for listing related features
                    createFeatures(mMap);
                }
            }
        });

        // create a graphics overlay to contain the buffered geometry graphics
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(graphicsOverlay);

        // create a fill symbol for geodesic buffer polygons
        SimpleLineSymbol geodesicOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2);
        SimpleFillSymbol geodesicBufferFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GREEN,
                geodesicOutlineSymbol);

        // create a fill symbol for planar buffer polygons
        SimpleLineSymbol planarOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2);
        SimpleFillSymbol planarBufferFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED, planarOutlineSymbol);

        // create a marker symbol for tap locations
        SimpleMarkerSymbol tapSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.WHITE, 14);

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.
        geodesicGraphicsOverlay = new GraphicsOverlay();
        geodesicGraphicsOverlay.setRenderer(new SimpleRenderer(geodesicBufferFillSymbol));
        geodesicGraphicsOverlay.setOpacity(0.5f);
        mMapView.getGraphicsOverlays().add(geodesicGraphicsOverlay);

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        final GraphicsOverlay planarGraphicsOverlay = new GraphicsOverlay();
        planarGraphicsOverlay.setRenderer(new SimpleRenderer(planarBufferFillSymbol));
        planarGraphicsOverlay.setOpacity(0.5f);
        //mMapView.getGraphicsOverlays().add(planarGraphicsOverlay);

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        final GraphicsOverlay tapLocationsOverlay = new GraphicsOverlay();
        tapLocationsOverlay.setRenderer(new SimpleRenderer(tapSymbol));
        mMapView.getGraphicsOverlays().add(tapLocationsOverlay);

        // get callout and set style
        mCallout = mMapView.getCallout();

        mLocationDisplay = mMapView.getLocationDisplay();
        // Listen to changes in the status of the location data source.
        /*mLocationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                // If LocationDisplay started OK, then continue.
                if (dataSourceStatusChangedEvent.isStarted())
                    return;

                // No error is reported, then continue.
                if (dataSourceStatusChangedEvent.getError() == null)
                    return;

                // If an error is found, handle the failure to start.
                // Check permissions to see if failure may be due to lack of permissions.
                boolean permissionCheck1 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[0]) ==
                        PackageManager.PERMISSION_GRANTED;
                boolean permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[1]) ==
                        PackageManager.PERMISSION_GRANTED;

                if (!(permissionCheck1 && permissionCheck2)) {
                    // If permissions are not already granted, request permission from the user.
                    ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);
                } else {
                    // Report other unknown failure types to the user - for example, location services may not
                    // be enabled on the device.
                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                            .getSource().getLocationDataSource().getError().getMessage());
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                }
            }
        });*/
        /*mLocationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {

                LocationDataSource.Location location = mLocationDisplay.getLocation();
                if (location != null) {
                    Point point = location.getPosition();
                    //System.out.println(point.getX());
                    //System.out.println(point.getY());

                    // get the buffer distance (miles) entered in the text box.
                    double bufferInMeters = Double.valueOf("300");

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
                    geodesicGraphicsOverlay.getGraphics().clear();
                    tapLocationsOverlay.getGraphics().clear();

                    // add the buffer polygons and tap location graphics to the appropriate graphic overlays.
                    //planarGraphicsOverlay.getGraphics().add(planarBufferGraphic);
                    geodesicGraphicsOverlay.getGraphics().add(geodesicBufferGraphic);
                    tapLocationsOverlay.getGraphics().add(locationGraphic);

                    QueryParameters query = new QueryParameters();
                    query.setGeometry(geodesicGraphicsOverlay.getExtent());
                    // call select features
                    final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = mFeatureLayerManzana
                            .selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW);
                    // add done loading listener to fire when the selection returns
                    featureQueryResultFuture.addDoneListener(() -> {
                        try {
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
                                Log.d(TAG, "Selection #: " + counter + " Table name: " + feature.getFeatureTable().getTableName());
                            }
                            Toast.makeText(getApplicationContext(), counter + " features selected", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Select feature failed: " + e.getMessage());
                        }
                    });
                }
            }
        });
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
        mLocationDisplay.startAsync();

        Toast.makeText(MainActivity.this, mLocationDisplay.getLocation().toString(), Toast.LENGTH_LONG).show();*/

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent me) {
                // create a screen point from the mouse event
                mScreenPoint = new android.graphics.Point((int)me.getX(), (int)me.getY());

                // convert this to a map point
                com.esri.arcgisruntime.geometry.Point mapPoint = mMapView.screenToLocation(mScreenPoint);
                mMapPoint = mapPoint;
                //System.out.println("-----------" + mMapView.getMapScale());

                seleccionarManzana(me);

                return super.onSingleTapConfirmed(me);
            }
        });

        // Listen to changes in the status of the location data source.
        mLocationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                // If LocationDisplay started OK, then continue.
                if (dataSourceStatusChangedEvent.isStarted())
                    return;

                // No error is reported, then continue.
                if (dataSourceStatusChangedEvent.getError() == null)
                    return;

                // If an error is found, handle the failure to start.
                // Check permissions to see if failure may be due to lack of permissions.
                boolean permissionCheck1 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[0]) ==
                        PackageManager.PERMISSION_GRANTED;
                boolean permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[1]) ==
                        PackageManager.PERMISSION_GRANTED;

                if (!(permissionCheck1 && permissionCheck2)) {
                    // If permissions are not already granted, request permission from the user.
                    ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);
                } else {
                    // Report other unknown failure types to the user - for example, location services may not
                    // be enabled on the device.
                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                            .getSource().getLocationDataSource().getError().getMessage());
                    Toast.makeText(MainActivity.this, "Habilitar el GPS", Toast.LENGTH_LONG).show();

                    //System.out.println("ERROR: " + message);

                    // Update UI to reflect that the location display did not actually start
                    mSpinner.setSelection(0, true);
                }
            }
        });

        // Get the Spinner from layout
        mSpinner = (Spinner) findViewById(R.id.spinner);

        // Populate the list for the Location display options for the spinner's Adapter
        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("GPS Apagado", R.drawable.locationdisplaydisabled));
        list.add(new ItemData("GPS Iniciar", R.drawable.locationdisplayon));
        //list.add(new ItemData("Re-Center", R.drawable.locationdisplayrecenter));
        list.add(new ItemData("Mi Ubicación", R.drawable.locationdisplayrecenter));
        //list.add(new ItemData("Compass", R.drawable.locationdisplayheading));

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_layout, R.id.txt, list);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        // Stop Location Display
                        if (mLocationDisplay.isStarted())
                            mLocationDisplay.stop();

                        break;
                    case 1:
                        // Start Location Display
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();

                        break;
                    case 2:
                        // Re-Center MapView on Location
                        // AutoPanMode - Default: In this mode, the MapView attempts to keep the location symbol on-screen by
                        // re-centering the location symbol when the symbol moves outside a "wander extent". The location symbol
                        // may move freely within the wander extent, but as soon as the symbol exits the wander extent, the MapView
                        // re-centers the map on the symbol.
                        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();

                        break;
                    case 3:
                        // Start Navigation Mode
                        // This mode is best suited for in-vehicle navigation.
                        /*mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();*/
                        break;
                    case 4:
                        // Start Compass Mode
                        // This mode is better suited for waypoint navigation when the user is walking.
                        /*mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
                        if (!mLocationDisplay.isStarted())
                            mLocationDisplay.startAsync();*/
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }

        });


        //
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_share) //R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_tools, R.id.nav_send, R.id.nav_tools
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    /*
     *
     */
    private void cargarRutas(String consultaSector) {
        // Get the Spinner from layout
        mSpinnerRutas = (Spinner) findViewById(R.id.spinner_rutas);

        // Populate the list for the Location display options for the spinner's Adapter
        ArrayList<ItemData> list = new ArrayList<>();
        list.add(new ItemData("Rutas 1 y 2", R.drawable.ic_rutas_todas_24));
        list.add(new ItemData("Ruta 1", R.drawable.ic_ruta_1_24));
        list.add(new ItemData("Ruta 2", R.drawable.ic_ruta_2_24));

        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.spinner_layout_rutas, R.id.txt, list);
        mSpinnerRutas.setAdapter(adapter);
        mSpinnerRutas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FeatureLayer ruteoPriLayer = getmOperationalLayers().get(7);
                FeatureLayer ruteoSecLayer = getmOperationalLayers().get(8);
                FeatureLayer paradasLayer = getmOperationalLayers().get(9);

                switch (position) {
                    case 0:
                        ruteoPriLayer.setDefinitionExpression(consultaSector + " AND CAMION > 0");
                        ruteoSecLayer.setDefinitionExpression(consultaSector + " AND CAMION > 0");
                        paradasLayer.setDefinitionExpression(consultaSector + " AND CAMION > 0");

                        break;
                    case 1:
                        ruteoPriLayer.setDefinitionExpression(consultaSector + "AND CAMION = 1");
                        ruteoSecLayer.setDefinitionExpression(consultaSector + "AND CAMION = 1");
                        paradasLayer.setDefinitionExpression(consultaSector + "AND CAMION = 1");

                        break;
                    case 2:
                        ruteoPriLayer.setDefinitionExpression(consultaSector + "AND CAMION = 2");
                        ruteoSecLayer.setDefinitionExpression(consultaSector + "AND CAMION = 2");
                        paradasLayer.setDefinitionExpression(consultaSector + "AND CAMION = 2");

                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }

        });

        mSpinnerRutas.setVisibility(View.VISIBLE);
    }

    /*
     *
     */
    @SuppressLint("RestrictedApi")
    private void seleccionarManzana(MotionEvent e) {
        // get the point that was clicked and convert it to a point in map coordinates
        mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

        final FeatureLayer manzanasLayer = getmOperationalLayers().get(5);

        // clear any previous selection
        manzanasLayer.clearSelection();
        mSelectedArcGISFeature = null;

        // identify the GeoElements in the given layer
        final ListenableFuture<IdentifyLayerResult> identifyFuture = mMapView
                .identifyLayerAsync(manzanasLayer, mClickPoint, 5, false, 1);

        // add done loading listener to fire when the selection returns
        identifyFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // call get on the future to get the result Map<String, Object>
                    IdentifyLayerResult layerResult = identifyFuture.get();
                    List<GeoElement> resultGeoElements = layerResult.getElements();
                    if (!resultGeoElements.isEmpty()) {
                        if (resultGeoElements.get(0) instanceof ArcGISFeature) {
                            mSelectedArcGISFeature = (ArcGISFeature) resultGeoElements.get(0);

                            // highlight the selected feature
                            manzanasLayer.selectFeature(mSelectedArcGISFeature);

                            int oidManzana = (int) mSelectedArcGISFeature.getAttributes()
                                    .get("OBJECTID");

                            //System.out.println("*** oidManzana: " + String.valueOf(oidManzana));

                            AlertDialog.Builder alerta = new AlertDialog.Builder(mMapView.getContext())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Advertencia")
                                    .setMessage("¿Desea cambiar el estado de la manzana a TERMINADA?")
                                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            editarManzana(mSelectedArcGISFeature);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //Toast.makeText(getApplicationContext(),"Nothing Happens",Toast.LENGTH_LONG).show();
                                        }
                                    });

                            mDialog = alerta.show();

                            mMapView.setViewpointCenterAsync(mMapPoint);
                        }
                    } else {
                        // none of the features on the map were selected
                        Toast.makeText(mMapView.getContext(), "No se encontraron manzanas en el punto seleccionado", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Select feature failed: " + e.getMessage());
                }
            }
        });
    }

    private void closeDialog()
    {
        if(mDialog != null) mDialog.dismiss();
        mProgressDialog.dismiss();
    }

    /**
     *
     * @param feature
     */
    public void editarManzana(ArcGISFeature feature){

        try
        {


            mProgressDialog.setMessage("Actualizando estado...");
            mProgressDialog.show();

            feature.getAttributes().put("Estado", 1);

            ServiceFeatureTable mServiceFeatureTableEncuesta = new ServiceFeatureTable(getString(R.string.manzanas_service_url));
            mServiceFeatureTableEncuesta.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE);
            // update the feature in the table new Feature
            mServiceFeatureTableEncuesta.updateFeatureAsync(feature).get();
            // if dealing with ServiceFeatureTable, apply edits after making updates; if editing locally, then edits can
            // be synchronized at some point using the SyncGeodatabaseTask.
            if (mServiceFeatureTableEncuesta instanceof ServiceFeatureTable) {
                ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) mServiceFeatureTableEncuesta;

                // can call getUpdatedFeaturesCountAsync to verify number of updates to be applied before calling applyEditsAsync
                final List<FeatureEditResult> featureEditResults = serviceFeatureTable.applyEditsAsync().get();
            }

            // if required, can check the edits applied in this operation by using returned FeatureEditResult

            Toast.makeText(mMapView.getContext(), "Manzana actualizada", Toast.LENGTH_LONG).show();
            getmMapView().setViewpointGeometryAsync(feature.getGeometry().getExtent(), 20);

            closeDialog();
        }
        catch(Exception ex){
            String msg = ex.getMessage();
            //System.out.println("********** " + msg);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            mLocationDisplay.startAsync();
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied), Toast
                    .LENGTH_SHORT).show();

            // Update UI to reflect that the location display did not actually start
            mSpinner.setSelection(0, true);
        }
    }

    /**
     * Create Features from Layers in the Map
     *
     * @param map ArcGISMap to get Layers and Tables
     */
    private void createFeatures(ArcGISMap map) {
        LayerList layers = map.getOperationalLayers();
        // add the National Parks Feature layer to LayerList
        for (Layer layer : layers) {
            FeatureLayer fLayer = (FeatureLayer) layer;
            fLayer.setDefinitionExpression("OBJECTID < 0");
            //System.out.println("*** fLayer " + fLayer.getName());
            mOperationalLayers.add(fLayer);
        }
    }

    /**
     *
     */
    public void acercarSector(Feature feature, String consultaSector) {
        util.mostrarDialogoInfoMsg(getString(R.string.msg_info_manzana), getmMapView());
        getmMapView().setViewpointGeometryAsync(feature.getGeometry().getExtent(), 10);

        cargarRutas(consultaSector);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        super.onDestroy();
    }
}
