package com.example.planeacionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
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
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

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
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AppBarConfiguration mAppBarConfiguration;

    private MapView mMapView;
    private ArcGISMap mMap;
    private Portal mPortal;
    private PortalItem mPortalItem;

    private android.graphics.Point mScreenPoint;
    private com.esri.arcgisruntime.geometry.Point mMapPoint;
    private android.graphics.Point mClickPoint;

    private FeatureLayer mFeatureLayerObra;
    private ServiceFeatureTable mServiceFeatureTableObra;

    private ArcGISFeature mSelectedArcGISFeature;

    private Callout mCallout;
    LayoutInflater mInflater;
    private LocationDisplay mLocationDisplay;

    private GraphicsOverlay geodesicGraphicsOverlay = null;

    private FloatingActionButton mFabResultados;
    private FloatingActionButton mFabRegistrarHito;
    private Feature mFeatureProyecto;

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

    public FeatureLayer getmFeatureLayerObra() {
        return mFeatureLayerObra;
    }

    public Callout getmCallout() {
        return mCallout;
    }

    public Feature getmFeatureProyecto() {
        return mFeatureProyecto;
    }

    String[] mResultados = {"300 mts","500 mts","1.000 mts"};
    Feature[] mFeatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInflater = this.getLayoutInflater();

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

        mServiceFeatureTableObra = new ServiceFeatureTable(getString(R.string.obra_service_url));
        mServiceFeatureTableObra.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_CACHE);
        mFeatureLayerObra = new FeatureLayer(mServiceFeatureTableObra);

        mFeatureLayerObra.setDefinitionExpression("OBJECTID < 0");

        mMap.getOperationalLayers().add(mFeatureLayerObra);

        mMapView.setMap(mMap);

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
                    System.out.println(point.getX());
                    System.out.println(point.getY());

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
                    final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = mFeatureLayerObra
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
                System.out.println("-----------" + mMapView.getMapScale());

                seleccionarObra(me);

                return super.onSingleTapConfirmed(me);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFabResultados = findViewById(R.id.resultados);
        mFabResultados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultadosActivity activity = new ResultadosActivity(mResultados, mFeatures);
                activity.show(getSupportFragmentManager(), ResultadosActivity.TAG);
            }
        });

        mFabRegistrarHito = findViewById(R.id.registrar_hito);
        mFabRegistrarHito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrarHitoActivity activity = new RegistrarHitoActivity(mFeatureProyecto);
                activity.show(getSupportFragmentManager(), RegistrarHitoActivity.TAG);
            }
        });

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
                R.id.nav_home, R.id.nav_share, R.id.nav_send, R.id.nav_tools) //R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_tools
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    /*
     *
     */
    private void seleccionarObra(MotionEvent e){
        // get the point that was clicked and convert it to a point in map coordinates
        mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());

        // clear any previous selection
        mFeatureLayerObra.clearSelection();
        mSelectedArcGISFeature = null;
        mCallout.dismiss();

        // identify the GeoElements in the given layer
        final ListenableFuture<IdentifyLayerResult> identifyFuture = mMapView
                .identifyLayerAsync(mFeatureLayerObra, mClickPoint, 5, false, 1);

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
                            mFeatureLayerObra.selectFeature(mSelectedArcGISFeature);

                            String Nom_Proyecto = (String) mSelectedArcGISFeature.getAttributes()
                                    .get("Nom_Proyecto");

                            String estado = (String) mSelectedArcGISFeature.getAttributes()
                                    .get("DescrProyecto");

                            showCallout(Nom_Proyecto, estado);

                            mMapView.setViewpointCenterAsync(mMapPoint);
                        }
                    } else {
                        // none of the features on the map were selected
                        mCallout.dismiss();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Select feature failed: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Displays Callout
     *
     */
    private void showCallout(String nombre, String estado) {

        // create a text view for the callout
        /*RelativeLayout calloutLayout = new RelativeLayout(getApplicationContext());

        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setId(R.id.textview);
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setTextSize(18);
        calloutContent.setPadding(0, 10, 10, 10);

        calloutContent.setText(title);

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.RIGHT_OF, calloutContent.getId());

        // create image view for the callout
        ImageView imageView = new ImageView(getApplicationContext());
        imageView
                .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_info_outline_black_18dp));
        imageView.setLayoutParams(relativeParams);
        imageView.setOnClickListener(new ImageViewOnclickListener());

        calloutLayout.addView(calloutContent);
        calloutLayout.addView(imageView);

        mCallout.setGeoElement(mSelectedArcGISFeature, null);
        mCallout.setContent(calloutLayout);
        mCallout.show();*/

        View calloutView = mInflater.inflate(R.layout.callout, null);

        LinearLayout cont_nombre = (LinearLayout) calloutView.findViewById(R.id.cont_nombre);
        LinearLayout cont_estado = (LinearLayout) calloutView.findViewById(R.id.cont_estado);

        //Access the internal Textviews inside the calloutView.
        TextView callout_nombre = (TextView) calloutView.findViewById(R.id.callout_nombre);
        TextView callout_estado = (TextView) calloutView.findViewById(R.id.callout_estado);

        cont_nombre.setVisibility(LinearLayout.VISIBLE);
        cont_estado.setVisibility(LinearLayout.VISIBLE);

        //Set Up values
        callout_nombre.setText(nombre);
        callout_nombre.setEnabled(true);

        callout_estado.setText(estado);
        callout_estado.setEnabled(true);

        mCallout.setGeoElement(mSelectedArcGISFeature, null);
        //Set the content, show the view
        mCallout.setContent(calloutView);
        //mCallout.setStyle(R.layout.activity_encuesta);
        mCallout.refresh();
        mCallout.show();
    }

    public void setResultados(Feature[] features){
        mFeatures = new Feature[features.length];
        mFeatures = features;

        //if(bankAccNos.contains(bakAccNo))

        ArrayList<String> listaResultados = new ArrayList<String>();
        String reg = "";

        ArrayList<String> listaCamposNoVisibles = new ArrayList<String>();
        listaCamposNoVisibles.add("Shape__Area");
        listaCamposNoVisibles.add("Shape__Length");
        listaCamposNoVisibles.add("OBJECTID");
        listaCamposNoVisibles.add("Dependencia");
        listaCamposNoVisibles.add("DireccionProyecto");
        listaCamposNoVisibles.add("ID_Obra_1");
        listaCamposNoVisibles.add("ID_Sector");
        listaCamposNoVisibles.add("Programa");
        listaCamposNoVisibles.add("ObjProyecto");
        listaCamposNoVisibles.add("DurProyecto");
        listaCamposNoVisibles.add("Responsable");
        listaCamposNoVisibles.add("Dependencia");
        listaCamposNoVisibles.add("Nombre_Contratista");
        listaCamposNoVisibles.add("ID_Localidad");
        listaCamposNoVisibles.add("Intervencion");

        for (Feature feat : features) {
            // create a Map of all available attributes as name value pairs
            Map<String, Object> attr = feat.getAttributes();
            Set<String> keys = attr.keySet();
            reg = "";

            for (String key : keys) {
                Object value = attr.get(key);

                if(!listaCamposNoVisibles.contains(key)){
                    reg += key + ": " + value + "\n";
                }
            }
            listaResultados.add(reg);
        }

        mResultados = (String[]) listaResultados.toArray(new String[listaResultados.size()]);
    }

    @SuppressLint("RestrictedApi")
    public void setProyecto(Feature feature){
        mFeatureProyecto = feature;
        this.getmFabRegistrarHito().setVisibility(View.VISIBLE);
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
