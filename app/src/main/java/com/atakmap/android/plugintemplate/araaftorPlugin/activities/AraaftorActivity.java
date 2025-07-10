package com.atakmap.android.plugintemplate.araaftorPlugin.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.Size;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import com.atakmap.android.plugintemplate.detection.BoundingBox;
import com.atakmap.android.plugintemplate.detection.DetectorObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.AraaftorOverlayManager;
import com.atakmap.android.plugintemplate.araaftorPlugin.CalculationCords;
import com.atakmap.android.plugintemplate.araaftorPlugin.DetectObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.ElementsOnMap;
import com.atakmap.android.plugintemplate.araaftorPlugin.GeoObject;
import com.atakmap.android.plugintemplate.araaftorPlugin.MediatorApplication;
import com.atakmap.android.plugintemplate.araaftorPlugin.Util;
import com.atakmap.android.plugintemplate.araaftorPlugin.adapters.DetectObjectAdapter;
import com.atakmap.android.plugintemplate.araaftorPlugin.api.GetWeatherDataTask;
import com.atakmap.android.plugintemplate.araaftorPlugin.araaftorMiniMap.AraaftorMinimap;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.DialogResultListener;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.UnitResultListener;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.AraaftorServiceListener;
import com.atakmap.android.plugintemplate.araaftorPlugin.interfaces.WeatherDataListener;
import com.atakmap.android.plugintemplate.araaftorPlugin.services.DialogService;
import com.atakmap.android.plugintemplate.araaftorPlugin.services.AraaftorService;
import com.atakmap.android.plugintemplate.araaftorPlugin.services.UnitService;
import com.atakmap.android.plugintemplate.araaftorPlugin.views.LineCompassView;
import com.atakmap.android.plugintemplate.plugin.R;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AraaftorActivity extends AppCompatActivity implements AraaftorServiceListener, WeatherDataListener, DialogResultListener, UnitResultListener {
    private final static int INTERVAL_IN_SECONDS = 5000;
    private static final int MAX_METERS_GOOD = 20, MAX_METERS_OK = 40, MAX_METERS_WEAK = 60, MAX_METERS_BAD = 100;

    Handler m_handler = new Handler();
    private AraaftorService araaftorService;
    boolean activityRunning = true;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Calendar c;
    String outputEnd;
    SimpleDateFormat formatter;
    long timeStart, timeEnd;
    private static final float ROTATION_SHAKE_THRESHOLD = 20.f;
    private float[] lastOrientation = new float[3];
    private long lastUpdateTime = 0;
    private TextureView textureView;
    private String cameraId;
    private double azimuthPhoto;
    private double azimuthNow;
    private DetectorObject detector;
    private ArrayList<String> imageFileName = new ArrayList<>();
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    public Size imageDimension;
    public float focalLenght;
    private float shockIndicator;
    public float objectRealHeight = 1.f;
    private float alfa = 0.4f;
    private float mapSize = 140.0f;
    private float scaleGrid = 1.f;
    public SizeF sensorDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private DialogService dialogService;
    private UnitService unitService;
    private static final int REQUEST_LOCALIZATION = 300;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    int[] viewIds = {R.id.arLatFloat, R.id.arLonFloat, R.id.arAltitude, R.id.arNumberOfSattelites, R.id.arDateTime};
    TextView arDateTime;
    ImageView celcius;
    ImageView windIcon;
    ImageView brightnessIcon;
    TextView brightness;
    AraaftorOverlayManager overlayManager;
    private AraaftorMinimap minimap;
    SeekBar seekBarLeft, seekBarRight, seekBarAlfa, seekBarSize;
    ImageView imageViewGrid;
    ImageView kierunki;
    ImageButton mapButton;
    ImageButton cameraButton;
    TextView alt_val;
    ImageButton cameraButtonRed;
    DisplayMetrics displayMetrics;
    ImageView imageUnit;
    TextView clsName;
    TextView cnfClass;
    TextView cnfT;
    TextView Lat;
    TextView LonFloat;
    TextView Lon;
    TextView LatFloat;
    TextView calcHeight;
    TextView calcDistance;
    TextView temperature;
    TextView wind;
    ImageView dayNight;
    ImageButton mapSettButton;
    private String selectedValue = "";
    LineCompassView compassView;
    private ExecutorService executor;
    private View flashView;
    NavigationView navigationView;
    private FrameLayout detailsContainer;
    private FrameLayout chageContainer;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private DetectObjectAdapter detectObjectAdapter;
    private List<DetectObject> detectObjectList = new ArrayList<>();
    private float dX, dY;
    RelativeLayout relativeMinimapLayout;
    ImageButton mapArrowButton;
    TextView alfaValue;
    TextView sizeValue;
    RelativeLayout bottomBar;
    RelativeLayout bottomBar2;
    RelativeLayout crosshair_view;
    ImageButton compassButton;
    ImageView compass;
    ImageView aroow_c;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        IntentFilter filter = new IntentFilter("com.atakmap.android.intent.ACTION_SEND_DATA");
        registerReceiver(receiverData, filter);
        setContentView(R.layout.araaftor_activity_layout);
        formatter = new SimpleDateFormat("H:mm:ss:SSS", Locale.US);
        c = Calendar.getInstance();
        timeEnd = c.getTime().getTime();
        outputEnd = formatter.format(timeEnd);
        temperature = findViewById(R.id.temperature);
        wind = findViewById(R.id.wind);
        mapSettButton = findViewById(R.id.mapSettButton);
        mapSettButton.setVisibility(View.GONE);
        dayNight = findViewById(R.id.dayNight);
        flashView = findViewById(R.id.flash_view);
        imageUnit = findViewById(R.id.imageUnit);
        clsName = findViewById(R.id.clsName);
        cnfClass = findViewById(R.id.cnfClass);
        cnfT= findViewById(R.id.cnfT);
        Lat = findViewById(R.id.Lat);
        sizeValue = findViewById(R.id.sizeValue);
        celcius = findViewById(R.id.celcius);
        windIcon = findViewById(R.id.windIcon);
        brightnessIcon = findViewById(R.id.brightnessIcon);
        brightness = findViewById(R.id.brightness);
        LonFloat = findViewById(R.id.LonFloat);
        Lon = findViewById(R.id.Lon);
        LatFloat = findViewById(R.id.LatFloat);
        imageUnit.setVisibility(View.GONE);
        clsName.setVisibility(View.GONE);
        cnfClass.setVisibility(View.GONE);
        cnfT.setVisibility(View.GONE);
        Lat.setVisibility(View.GONE);
        LonFloat.setVisibility(View.GONE);
        crosshair_view = findViewById(R.id.crosshair_view);
        Lon.setVisibility(View.GONE);
        LatFloat.setVisibility(View.GONE);
        bottomBar = findViewById(R.id.bottomBar);
        bottomBar2 = findViewById(R.id.bottomBar2);
        Intent intent = getIntent();
        araaftorService = MediatorApplication.getService();
        try {
            detector = new DetectorObject(araaftorService.getContext() , "yolo11n_float32.tflite");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayList<ElementsOnMap> latlons
                = (ArrayList<ElementsOnMap>) intent.getExtras().get("markerList");
        ElementsOnMap self = (ElementsOnMap) intent.getExtras().get("self");
        Location locationC = new Location("custom");
        locationC.setLatitude(self.first);
        locationC.setLongitude(self.second);
        setGpsData(locationC);
        List<String> app6as = new ArrayList<String>();
        LinkedList<GeoPoint> gObjs = new LinkedList<>();
        for (ElementsOnMap latlon : latlons) {
            GeoPoint geoObject = new GeoPoint(latlon.first, latlon.second);
            app6as.add(latlon.third);
            gObjs.add(geoObject);
        }
        overlayManager = new AraaftorOverlayManager(this, (AbsoluteLayout) findViewById(R.id.itemOverlay), new LinkedList<GeoObject>(), gObjs, app6as);
        cameraButton = findViewById(R.id.cameraButton);
        cameraButtonRed = findViewById(R.id.cameraButtonRed);
        cameraButtonRed.setVisibility(View.GONE);
        compassButton = findViewById(R.id.compassButton);
        compassButton.setVisibility(View.GONE);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectAndProcess(new DetectionCallback() {
                    @Override
                    public void onDetectionComplete(List<BoundingBox> detectedObjects, Bitmap bitmap) {
                        handleDetectionResults(detectedObjects, bitmap);
                    }
                });
            }
        });
        compass = findViewById(R.id.compass);
        aroow_c = findViewById(R.id.aroow_c);
        compassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(minimap.getVisibility()==View.VISIBLE){
                    minimap.setVisibility(View.GONE);
                    kierunki.setVisibility(View.GONE);
                    compass.setVisibility(View.VISIBLE);
                    aroow_c.setVisibility(View.VISIBLE);
                }
                else{
                    minimap.setVisibility(View.VISIBLE);
                    kierunki.setVisibility(View.VISIBLE);
                    compass.setVisibility(View.GONE);
                    aroow_c.setVisibility(View.GONE);
                }
            }
        });
        cameraButtonRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectAndProcessThree(new DetectionCallback() {
                    @Override
                    public void onDetectionComplete(List<BoundingBox> detectedObjects, Bitmap bitmap) {
                        handleDetectionResults(detectedObjects, bitmap);
                    }
                });
            }
        });
        compassView = (LineCompassView) findViewById(R.id.arCompassView);
        drawerLayout = findViewById(R.id.drawer);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                c = Calendar.getInstance();
                timeStart = c.getTime().getTime();
            }

            @Override
            public void onDrawerStateChanged(int newstate) {
                super.onDrawerStateChanged(newstate);
                c = Calendar.getInstance();
                timeEnd = c.getTime().getTime();
                outputEnd = formatter.format(timeEnd);
            }

        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        imageViewGrid = findViewById(R.id.imageView1);
        seekBarLeft = findViewById(R.id.seekBarLeft);
        seekBarRight = findViewById(R.id.seekBarRight);
        seekBarSize = findViewById(R.id.seekBarSize);
        seekBarAlfa = findViewById(R.id.seekBarAlfa);
        calcHeight = findViewById(R.id.calcHeight);
        alfaValue = findViewById(R.id.alfaValue);
        calcDistance = findViewById(R.id.calcDistance);
        arDateTime = findViewById(R.id.arDateTime);
        sizeValue.setText(mapSize + " px");
        alfaValue.setText(String.valueOf(alfa));
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat dec = new DecimalFormat("0.0");
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                window.getDefaultDisplay().getMetrics(displayMetrics);
                float density = getResources().getDisplayMetrics().density;
                float minSizePx = 140 * density;
                float maxSizePx = crosshair_view.getHeight();
                float progressPercentage = progress / 100f;
                float newSizePx = minSizePx + (progressPercentage * (maxSizePx - minSizePx));

                float scale = newSizePx / relativeMinimapLayout.getHeight();
                relativeMinimapLayout.setScaleX(scale);
                relativeMinimapLayout.setScaleY(scale);
                String height = dec.format(newSizePx) + " px";
                sizeValue.setText(height);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarAlfa.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat dec = new DecimalFormat("0.00");
                alfa = 0.f + (progress* 0.01f);
                String height = dec.format(alfa);
                alfaValue.setText(height);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                relativeMinimapLayout.setAlpha(alfa);
            }
        });
        seekBarLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scaleGrid = 0.1f + (progress / 100f) * 0.9f;
                imageViewGrid.setScaleX(scaleGrid);
                imageViewGrid.setScaleY(scaleGrid);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                window.getDefaultDisplay().getMetrics(displayMetrics);
                double scalePhoto = sensorDimension.getHeight()/imageDimension.getHeight();
                double distance = (objectRealHeight*focalLenght)/(scalePhoto*imageViewGrid.getHeight()*scaleGrid*((double) displayMetrics.heightPixels /imageDimension.getHeight()));
                DecimalFormat dec = new DecimalFormat("0.00");
                String distaceText = dec.format(distance) + " m";
                calcDistance.setText(distaceText);
            }
        });
        seekBarRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat dec = new DecimalFormat("0.00");
                objectRealHeight = 0.f + (progress* 0.01f);
                String height = dec.format(objectRealHeight) + " m";
                calcHeight.setText(height);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                window.getDefaultDisplay().getMetrics(displayMetrics);
                double scalePhoto = sensorDimension.getHeight()/imageDimension.getHeight();
                double distance = (objectRealHeight*focalLenght)/(scalePhoto*imageViewGrid.getHeight()*scaleGrid*((double) displayMetrics.heightPixels /imageDimension.getHeight()));
                DecimalFormat dec = new DecimalFormat("0.00");
                String distaceText = dec.format(distance) + " m";
                calcDistance.setText(distaceText);
            }
        });
        mapButton = findViewById(R.id.mapButton);
        kierunki = findViewById(R.id.kierunki);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minimap.getVisibility() == View.VISIBLE||compass.getVisibility() == View.VISIBLE) {
                    compassView.setVisibility(View.VISIBLE);
                    compass.setVisibility(View.GONE);
                    aroow_c.setVisibility(View.GONE);
                    mapArrowButton.setVisibility(View.GONE);
                    minimap.setVisibility(View.GONE);
                    kierunki.setVisibility(View.GONE);
                    mapSettButton.setVisibility(View.GONE);
                    compassButton.setVisibility(View.GONE);
                } else {
                    minimap.setVisibility(View.VISIBLE);
                    kierunki.setVisibility(View.VISIBLE);
                    mapArrowButton.setVisibility(View.VISIBLE);
                    compassView.setVisibility(View.GONE);
                    mapSettButton.setVisibility(View.VISIBLE);
                    compassButton.setVisibility(View.VISIBLE);
                }
            }
        });
        mapSettButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomBar.getVisibility() == View.VISIBLE) {
                    bottomBar.setVisibility(View.GONE);
                    bottomBar2.setVisibility(View.VISIBLE);
                    mapSettButton.setImageResource(R.drawable.mapend);
                } else {
                    bottomBar.setVisibility(View.VISIBLE);
                    bottomBar2.setVisibility(View.GONE);
                    mapSettButton.setImageResource(R.drawable.mapsett);
                }

            }
        });
        textureView = findViewById(R.id.ar_camera);
        navigationView = findViewById(R.id.navigation_view);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailsContainer = findViewById(R.id.details_container);
        chageContainer = findViewById(R.id.chageContainer);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        detectObjectAdapter = new DetectObjectAdapter(detectObjectList, this);
        recyclerView.setAdapter(detectObjectAdapter);
        detectObjectAdapter.setOnItemClickListener(new DetectObjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DetectObject item) {
                showDetailsPanel(item);
                recyclerView.setVisibility(View.GONE);
            }
        });

        textureView.setSurfaceTextureListener(textureListener);
        minimap = findViewById(R.id.minimap);
        relativeMinimapLayout = findViewById(R.id.relativeMinimapLayout);
        mapArrowButton = findViewById(R.id.mapArrowButton);
        mapArrowButton.setVisibility(View.GONE);
        mapArrowButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX();
                        dY = v.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() - dX;
                        float newY = event.getRawY() + dY;
                        float minX = 200f;
                        float maxX = crosshair_view.getWidth() - relativeMinimapLayout.getWidth();
                        float maxY = (float) crosshair_view.getHeight()-(relativeMinimapLayout.getHeight()/2);
                        float minY = 0f;
                        if (newX >= minX && newX <= maxX
                                && newY >= minY && newY <= maxY) {
                            relativeMinimapLayout.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
        minimap.setVisibility(View.GONE);
        kierunki.setVisibility(View.GONE);
        minimap.initMiniMap(locationC, gObjs, app6as);
        executor = Executors.newSingleThreadExecutor();

    }

    @Override
    public void onWeatherDataReceived(double[] result) {
        if (result != null) {
            String temp = String.valueOf(result[0]);
            temperature.setText(temp);
            String windText = String.valueOf(result[1]);
            wind.setText(windText + " km/h");
            if(result[2]==0){
                dayNight.setImageResource(R.drawable.night);
            }else{
                dayNight.setImageResource(R.drawable.day);
            }
        }
    }

    @Override
    public void onDialogResult(boolean isDone, DetectObject item) {
        if (isDone) {
            for (int i = 0; i < detectObjectList.size(); i++) {
                if(detectObjectList.get(0).id == item.id)
                {
                    detectObjectList.set(i,item);
                }
            }
            Toast.makeText(this, "Edition completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Edition cancelled", Toast.LENGTH_SHORT).show();
        }
        detectObjectAdapter.notifyDataSetChanged();
        ObjectAnimator animatorShow = ObjectAnimator.ofFloat(navigationView, "translationX", -navigationView.getWidth(), 0f);
        animatorShow.setDuration(3);
        animatorShow.start();
        animatorShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                navigationView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onUnitResult(boolean isDone, int id, String unit, String rank) {
        if (isDone) {
            detectObjectList.get(id-1).forceImageName = rank;
            detectObjectList.get(id-1).imageName = unit;

            Toast.makeText(this, "Force edition completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Force edition cancelled", Toast.LENGTH_SHORT).show();
        }
        detectObjectAdapter.notifyDataSetChanged();
        ObjectAnimator animatorShow = ObjectAnimator.ofFloat(navigationView, "translationX", -navigationView.getWidth(), 0f);
        animatorShow.setDuration(3);
        animatorShow.start();
        animatorShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                navigationView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        onDeleteResult(-1, detectObjectList.get(id-1).UID);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        detectObjectList.get(id-1).UID = detectObjectList.get(id-1).result.get(0).clsName+timeStamp;
        Handler handler = new Handler();
        Runnable showImageUnit = new Runnable() {
            @Override
            public void run() {
                onActivityResult(1, -1, detectObjectList.get(id-1).logtitude, detectObjectList.get(id-1).latitude, detectObjectList.get(id-1).altitude, detectObjectList.get(id-1).UID, detectObjectList.get(id-1).imageName);
            }
        };
        handler.postDelayed(showImageUnit,3000);
    }

    public interface DetectionCallback {
        void onDetectionComplete(List<BoundingBox> detectedObjects, Bitmap bitmap);
    }
    public void detectAndProcess(DetectionCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    showFlashEffect();
                    takePicture();
                    Thread.sleep(2000);
                    File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    File file = new File(picturesDirectory, imageFileName.get(0));
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    List<BoundingBox> detected = detectObjectOnPhoto(bitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDetectionComplete(detected, bitmap);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void detectAndProcessThree(DetectionCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        showFlashEffect();
                        takePicture();
                        Thread.sleep(2000);
                    }
                    Bitmap bestImage = null;
                    double highestSharpness = 0;
                    String name ="";

                    if (imageFileName.size() > 1) {
                        for (String imagePath : imageFileName) {
                            File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            File file = new File(picturesDirectory, imagePath);
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            if (bitmap != null) {
                                double sharpness = calculateSharpness(bitmap);
                                if (sharpness > highestSharpness) {
                                    highestSharpness = sharpness;
                                    bestImage = bitmap;
                                    name = imagePath;
                                }
                            }
                        }
                        if (bestImage != null) {
                            List<BoundingBox> detected = detectObjectOnPhoto(bestImage);
                            Bitmap finalBestImage = bestImage;
                            imageFileName.clear();
                            imageFileName.add(name);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onDetectionComplete(detected, finalBestImage);
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void showDetailsPanel(DetectObject item) {
        DecimalFormat dec = new DecimalFormat("0.00000");
        if (detailsContainer.getChildCount() == 0) {
            View detailsView = inflater.inflate(R.layout.ar_marker_details, detailsContainer, false);
            detailsContainer.addView(detailsView);
        }
        detailsContainer.setVisibility(View.VISIBLE);
        TextView unitNameText = detailsContainer.findViewById(R.id.arLatDeg2);
        unitNameText.setText(item.name);
        TextView unitDistText = detailsContainer.findViewById(R.id.unit_dist);
        unitDistText.setText(String.valueOf(dec.format(item.mean_dist)));
        TextView unitTimeText = detailsContainer.findViewById(R.id.ARM_val);
        unitTimeText.setText(DateFormat.format("dd/MM/yy kk:mm:ss", item.timestamp));
        LinearLayout detectedLay = detailsContainer.findViewById(R.id.detectedLay);
        LinearLayout confLay = detailsContainer.findViewById(R.id.confLay);
        detectedLay.removeAllViews();
        confLay.removeAllViews();
        for (int i = 0; i < item.result.size(); i++) {
            TextView newTextView = new TextView(this);
            newTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            newTextView.setText(item.result.get(i).clsName);
            newTextView.setTextColor(Color.WHITE);
            newTextView.setTextSize(12);

            TextView newTextView2 = new TextView(this);
            newTextView2.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            newTextView2.setText(String.valueOf(item.result.get(i).cnf) + "%");
            newTextView2.setTextColor(Color.WHITE);
            newTextView2.setTextSize(12);

            confLay.addView(newTextView2);
            detectedLay.addView(newTextView);
        }
        TextView unitLatText = detailsContainer.findViewById(R.id.INF_val);
        unitLatText.setText(String.valueOf(dec.format(item.logtitude)));
        TextView unitLonText = detailsContainer.findViewById(R.id.AAI_val);
        unitLonText.setText(String.valueOf(dec.format(item.latitude)));
        TextView alt_val = detailsContainer.findViewById(R.id.alt_val);
        alt_val.setText(String.valueOf(dec.format(item.altitude)));
        Button change = detailsContainer.findViewById(R.id.task);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChageCordPanel(item);
            }
        });
        Button back = detailsContainer.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        ImageButton image = detailsContainer.findViewById(R.id.image_but);
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(picturesDirectory, item.photoPath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        image.setImageBitmap(bitmap);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", 0f, -navigationView.getWidth());
                animator.setDuration(10);
                animator.start();

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        navigationView.setVisibility(View.GONE);
                    }
                });
                detailsContainer.setVisibility(View.GONE);
                dialogService = new DialogService(AraaftorActivity.this, AraaftorActivity.this);
                dialogService.showDialog(item, inflater);
            }
        });

        ImageButton imageUnit = detailsContainer.findViewById(R.id.item_image);
        int leftImageRes = this.getResources().getIdentifier(item.imageName, "drawable", this.getPackageName());
        int ImageRes = this.getResources().getIdentifier(item.forceImageName, "drawable", this.getPackageName());
        ImageView item_rank = detailsContainer.findViewById(R.id.item_rank);
        item_rank.setImageResource(ImageRes);
        imageUnit.setImageResource(leftImageRes);
        imageUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(navigationView, "translationX", 0f, -navigationView.getWidth());
                animator.setDuration(10);
                animator.start();

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        navigationView.setVisibility(View.GONE);
                    }
                });
                detailsContainer.setVisibility(View.GONE);
                unitService = new UnitService(AraaftorActivity.this, AraaftorActivity.this);
                unitService.showDialog(item, inflater, item.forceImageName);
            }
        });
    }
    private void showChageCordPanel(DetectObject item) {
        DecimalFormat dec = new DecimalFormat("0.00000");
        CalculationCords x = new CalculationCords();
        Location userLocation = araaftorService.getLocation();
        if (chageContainer.getChildCount() == 0) {
            View changeView = inflater.inflate(R.layout.change_marker_details, chageContainer, false);
            chageContainer.addView(changeView);
        }
        detailsContainer.setVisibility(View.GONE);
        chageContainer.setVisibility(View.VISIBLE);
        TextView unitNameText = chageContainer.findViewById(R.id.dist_val);
        unitNameText.setText(String.valueOf(dec.format(item.mean_dist)));
        TextView unitDistText = chageContainer.findViewById(R.id.lat_val);
        unitDistText.setText(String.valueOf(dec.format(item.latitude)));
        TextView unitTimeText = chageContainer.findViewById(R.id.lon_val);
        unitTimeText.setText(String.valueOf(dec.format(item.logtitude)));
        TextView unitConfidenceText = chageContainer.findViewById(R.id.high_val);
        unitConfidenceText.setText(String.valueOf(item.altitude));
        Spinner spinner = chageContainer.findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cord_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.WHITE);
                }
                String selectedItem = parent.getItemAtPosition(position).toString();
                handleMetodSelect(selectedItem);
                String lat = x.calculateCordinats(item.latitude,selectedItem);
                String log = x.calculateCordinats(item.logtitude,selectedItem);
                unitDistText.setText(lat);
                unitTimeText.setText(log);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(0);
        Button change = chageContainer.findViewById(R.id.task);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectObjectList.get(item.id-1).setAltitude(item.altitude);
                detectObjectList.get(item.id-1).setDist(item.mean_dist);
                detectObjectList.get(item.id-1).setLatitude(item.latitude);
                detectObjectList.get(item.id-1).setLogtitude(item.logtitude);
                detectObjectAdapter.notifyDataSetChanged();
                onDeleteResult(-1, item.UID);
                recyclerView.setVisibility(View.VISIBLE);
                chageContainer.setVisibility(View.GONE);
                navigationView.setVisibility(View.GONE);
                Handler handler = new Handler();
                Runnable showImageUnit = new Runnable() {
                    @Override
                    public void run() {
                        onActivityResult(1, -1, item.latitude, item.logtitude, item.altitude, item.UID, item.imageName);
                    }
                };
                handler.postDelayed(showImageUnit,3000);


            }
        });
        Button back = chageContainer.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsContainer.setVisibility(View.VISIBLE);
                chageContainer.setVisibility(View.GONE);
            }
        });

        Button plus_lon = chageContainer.findViewById(R.id.plus_lon);
        plus_lon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setLogtitude(item.logtitude + 0.00005);
                String cordsLon = x.calculateCordinats(item.logtitude,selectedValue);
                unitTimeText.setText(cordsLon);
                unitNameText.setText(String.valueOf(dec.format(item.mean_dist)));
            }
        });
        Button minus_lat = chageContainer.findViewById(R.id.minus_lat);
        minus_lat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setLatitude(item.latitude - 0.00005);
                String cordsLon = x.calculateCordinats(item.latitude,selectedValue);
                unitDistText.setText(cordsLon);
                item.setMean_dist(x.calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(),item.logtitude,item.latitude));
                unitNameText.setText(String.valueOf(dec.format(item.mean_dist)));
            }
        });
        Button plus_lat = chageContainer.findViewById(R.id.plus_lat);
        plus_lat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setLatitude(item.latitude + 0.00005);
                String cordsLon = x.calculateCordinats(item.latitude,selectedValue);
                unitDistText.setText(cordsLon);
                item.setMean_dist(x.calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(),item.logtitude,item.latitude));
                unitNameText.setText(String.valueOf(dec.format(item.mean_dist)));
            }
        });
        Button minus_lon = chageContainer.findViewById(R.id.minus_lon);
        minus_lon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setLogtitude(item.logtitude - 0.00005);
                String cordsLon = x.calculateCordinats(item.logtitude,selectedValue);
                unitTimeText.setText(cordsLon);
                item.setMean_dist(x.calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(),item.logtitude,item.latitude));
                unitNameText.setText(String.valueOf(dec.format(item.mean_dist)));
            }
        });
        Button minus_height = chageContainer.findViewById(R.id.minus_height);
        minus_height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setAltitude(item.altitude - 1.0);
                unitConfidenceText.setText(String.valueOf(item.altitude));
            }
        });
        Button plus_height = chageContainer.findViewById(R.id.plus_height);
        plus_height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setAltitude(item.altitude + 1.0);
                unitConfidenceText.setText(String.valueOf(item.altitude));
            }
        });
    }
    public void handleMetodSelect(String x){
        selectedValue = x;
    }
    private void showFlashEffect() {

        flashView.setVisibility(View.VISIBLE);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);
        fadeIn.setFillAfter(true);

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setFillAfter(true);

        flashView.startAnimation(fadeIn);

        flashView.postDelayed(new Runnable() {
            @Override
            public void run() {
                flashView.startAnimation(fadeOut);
            }
        }, 300);
    }
    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
        araaftorService.removemrAIServiceListener(this);
        unregisterReceiver(receiverData);
        m_handler.removeCallbacks(m_handlerTask);
        m_handler.removeCallbacks(m_handlerTask2);
        unregisterReceiver(batteryReceiver);
        drawerLayout.removeDrawerListener(actionBarDrawerToggle);
        activityRunning = false;
        minimap.activityPaused();
        imageFileName.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        IntentFilter filter = new IntentFilter("com.atakmap.android.intent.ACTION_SEND_DATA");
        registerReceiver(receiverData, filter);
        initBatteryLevel();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCALIZATION);
            return;
        }
        arDateTime.setText(DateFormat.format("dd-MM-yy kk:mm:ss", new Date()));
        m_handlerTask.run();
        m_handlerTask2.run();
        overlayManager.updateBorders();
        araaftorService.addmrAIServiceListener(this);
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        minimap.activityResumed();
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(AraaftorActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            setTextureTransform(characteristics);
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            this.focalLenght = focalLengths != null && focalLengths.length > 0 ? focalLengths[0] : -1;
            sensorDimension = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    public List<BoundingBox> detectObjectOnPhoto(Bitmap bitmapClear) {
        if(bitmapClear!=null) {
            Pair<Long, List<BoundingBox>> resultDetect = detector.Detect(bitmapClear);
            if(!resultDetect.second.isEmpty()){
                return resultDetect.second;
            }
            else {
                return null;
            }
        }
        else{
            return null;
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void handleDetectionResults(List<BoundingBox> results, Bitmap bitmapClear){
        DecimalFormat dec = new DecimalFormat("0.00");
        double mean_dist = 0.0;
        Double dist_sum = 0.0;
        if(results!=null){
            List<Double> distances = calculateDistanceAngleMethod( results);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            for (int i = 0; i < results.size(); i++) {
                dist_sum += distances.get(i);
            }
            Location userLocation = araaftorService.getLocation();
            Pair<Double, Double> cord = new Pair<>(userLocation.getLatitude(), userLocation.getLongitude());
            mean_dist = dist_sum/distances.size();
            Pair<Double, Double> cordinats = calculateCordinats(mean_dist, cord.first, cord.second);
            clsName.setText(results.get(0).clsName);
            cnfClass.setText(String.valueOf(results.get(0).cnf) + "%");
            LatFloat.setText(dec.format(cordinats.first));
            LonFloat.setText(dec.format(cordinats.second));
            DetectObject object = new DetectObject((detectObjectList.size() + 1), "Wykrycie " + (detectObjectList.size() + 1), new Date(), "shgpuci________",results,
                    imageFileName.get(0),"rank_b",distances,cordinats.first,cordinats.second,100,(results.get(0).clsName+timeStamp),mean_dist,azimuthPhoto);
            detectObjectList.add(object);
            detectObjectAdapter.notifyDataSetChanged();
            onActivityResult(1, -1, cordinats.first,cordinats.second, 100.0, (results.get(0).clsName+timeStamp), object.imageName);
            Handler handler = new Handler();
            Runnable hideImageUnit = new Runnable() {
                @Override
                public void run() {
                    temperature.setVisibility(View.VISIBLE);
                    celcius.setVisibility(View.VISIBLE);
                    windIcon.setVisibility(View.VISIBLE);
                    wind.setVisibility(View.VISIBLE);
                    brightnessIcon.setVisibility(View.VISIBLE);
                    brightness.setVisibility(View.VISIBLE);
                    imageUnit.setVisibility(View.GONE);
                    clsName.setVisibility(View.GONE);
                    cnfClass.setVisibility(View.GONE);
                    cnfT.setVisibility(View.GONE);
                    Lat.setVisibility(View.GONE);
                    LonFloat.setVisibility(View.GONE);
                    Lon.setVisibility(View.GONE);
                    LatFloat.setVisibility(View.GONE);
                }
            };

            Runnable showImageUnit = new Runnable() {
                @Override
                public void run() {
                    temperature.setVisibility(View.GONE);
                    wind.setVisibility(View.GONE);
                    celcius.setVisibility(View.GONE);
                    windIcon.setVisibility(View.GONE);
                    brightnessIcon.setVisibility(View.GONE);
                    brightness.setVisibility(View.GONE);
                    imageUnit.setVisibility(View.VISIBLE);
                    clsName.setVisibility(View.VISIBLE);
                    cnfClass.setVisibility(View.VISIBLE);
                    cnfT.setVisibility(View.VISIBLE);
                    Lat.setVisibility(View.VISIBLE);
                    LonFloat.setVisibility(View.VISIBLE);
                    Lon.setVisibility(View.VISIBLE);
                    LatFloat.setVisibility(View.VISIBLE);
                }
            };
            handler.postDelayed(showImageUnit, 3000);
            handler.postDelayed(hideImageUnit, 9000);

            File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            Bitmap bitmapWithBox = drawBoundingBoxes(bitmapClear, results, distances);
            Bitmap scaledBitmapWithBox = Bitmap.createScaledBitmap(bitmapWithBox, imageDimension.getWidth(), imageDimension.getHeight(), true);
            File outputFile = new File(picturesDirectory, "detected_result" + timeStamp + ".png");
            try {
                FileOutputStream out = new FileOutputStream(outputFile);
                scaledBitmapWithBox.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageFileName.clear();
        }
        else{
            imageFileName.clear();
        }
    }

    public Bitmap drawBoundingBoxes(Bitmap bitmap, List<BoundingBox> boxes, List<Double> distance) {
        Bitmap mutableBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);
        DecimalFormat df = new DecimalFormat("0.00");
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(220, 244, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.rgb(220, 244, 0));
        textPaint.setTextSize(20f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        int i = 1;
        for (BoundingBox box : boxes) {
            double x = distance.get(i-1);
            String formattedValue = df.format(x);
            float x1 = (float) (box.x1 * 2.5);
            float y1 = (float) (box.y1 * 2.5);
            float x2 = (float) (box.x2 * 2.5);
            float y2 = (float) (box.y2 * 2.5);
            canvas.drawRect(x1, y1, x2, y2, paint);
            canvas.drawText(box.clsName + " " + i + " " + box.cnf +"%", x1, y2, textPaint);
            canvas.drawText("Distance: " + formattedValue, x1, y1, textPaint);
            i++;
        }
        return mutableBitmap;
    }

    public List<Double> calculateDistanceAngleMethod(List<BoundingBox> boundingBoxes) {
        List<Double> distance = new ArrayList<>();
        double scalePhoto = sensorDimension.getHeight()/imageDimension.getHeight();

        for(BoundingBox box : boundingBoxes){
            float y1 =  (box.y1);
            float y2 =  (box.y2);
            double distanceMetric = (objectRealHeight*focalLenght)/(scalePhoto*(y2-y1)*(imageDimension.getHeight()/256));
            distance.add(distanceMetric);
        }
        return distance;
    }
    public List<Double> calculateDistanceAngleMethodTest(List<BoundingBox> boundingBoxes, float height, float heightImage) {
        List<Double> distance = new ArrayList<>();
        double scalePhoto = height/heightImage;

        for(BoundingBox box : boundingBoxes){
            float y1 =  (box.y1);
            float y2 =  (box.y2);
            double distanceMetric = (objectRealHeight*focalLenght)/(scalePhoto*(y2-y1)*(heightImage/256));
            distance.add(distanceMetric);
        }
        return distance;
    }
    public Pair<Double, Double> calculateCordinats(double distance, double lat1, double lon1) {
        double brng = azimuthPhoto;
        double d = distance/1000;
        int Eradius = 6371;
        double DestLat = Math.asin(Math.sin(Math.toRadians(lat1))*Math.cos(d/Eradius)+Math.cos(Math.toRadians(lat1))*Math.sin(d/Eradius)*Math.cos(Math.toRadians(brng)));

        double DestLon = Math.toRadians(lon1) + Math.atan2(Math.sin(Math.toRadians(brng))*Math.sin(d/Eradius)*Math.cos(Math.toRadians(lat1)),Math.cos(d/Eradius)-Math.sin(Math.toRadians(lat1))*Math.sin(DestLat));

        DestLon = (DestLon+3*Math.PI)%(2*Math.PI) - Math.PI;
        double finalDestLat = Math.toDegrees(DestLat);
        double finalDestLon = Math.toDegrees(DestLon);
        return new Pair<>(Math.toDegrees(DestLat),Math.toDegrees(DestLon));
    }

    protected void takePicture() {
        if (cameraDevice == null) {
            return;
        }

        if (isStoragePermissionGranted()) {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes = null;
                if (characteristics != null) {
                    jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                }
                int width = 1024;
                int height = 1024;
                if (jpegSizes != null && jpegSizes.length > 0) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }
                ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                List<Surface> outputSurfaces = new ArrayList<Surface>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                int jpegOrientation = (sensorOrientation + 270) % 360;

                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName1 = "captured_image_" + timeStamp + ".png";
                File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File file = new File(picturesDirectory, imageFileName1);
                imageFileName.add(imageFileName1);
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        try {
                            image = reader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            save(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }

                    private void save(byte[] bytes) throws IOException {
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(file);
                            output.write(bytes);
                        } finally {
                            if (null != output) {
                                output.close();
                            }
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        azimuthPhoto=azimuthNow;
                        createCameraPreview();
                    }
                };
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {
                            session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                    }
                }, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == REQUEST_LOCALIZATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    final Runnable m_handlerTask = new Runnable() {
        @Override
        public void run() {
            Date date = new Date();
            arDateTime.setText(DateFormat.format("dd/MM/yy kk:mm:ss", date));
            if (activityRunning) m_handler.postDelayed(m_handlerTask, INTERVAL_IN_SECONDS);
        }
    };

    final Runnable m_handlerTask2 = new Runnable() {
        @Override
        public void run() {
            new GetWeatherDataTask(AraaftorActivity.this).execute(araaftorService.getLatitude(), araaftorService.getLongitude());
            if (activityRunning) m_handler.postDelayed(m_handlerTask2, 300000);
        }
    };

    private double calculateSharpness(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        long totalBrightness = 0;
        long totalBrightnessSquared = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int brightness = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                totalBrightness += brightness;
                totalBrightnessSquared += brightness * brightness;
            }
        }

        long numPixels = width * height;
        double mean = totalBrightness / (double) numPixels;
        double variance = (totalBrightnessSquared / (double) numPixels) - (mean * mean);
        return variance;
    }


    private BroadcastReceiver receiverData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null ) {
                ElementsOnMap self = (ElementsOnMap) intent.getExtras().get("self");
                ArrayList<ElementsOnMap> latlons
                        = (ArrayList<ElementsOnMap>) intent.getExtras().get("markerList");
                Location locationC = new Location("custom");
                locationC.setLatitude(self.first);
                locationC.setLongitude(self.second);
                setGpsData(locationC);
                List<String> app6as = new ArrayList<String>();
                LinkedList<GeoPoint> gObjs = new LinkedList<>();
                for (ElementsOnMap latlon : latlons) {
                    GeoPoint geoObject = new GeoPoint(latlon.first, latlon.second);
                    app6as.add(latlon.third);
                    gObjs.add(geoObject);
                }
                overlayManager = new AraaftorOverlayManager(AraaftorActivity.this, (AbsoluteLayout) findViewById(R.id.itemOverlay), new LinkedList<GeoObject>(), gObjs, app6as);
                overlayManager.updateBorders();
                minimap.activityPaused();
                minimap.initMiniMap(locationC, gObjs, app6as);
                minimap.activityResumed();
            }
        }
    };

    public void setGpsData(Location location) {
        araaftorService.setLocation(location);
        listAssetFiles("");
        DecimalFormat dec = new DecimalFormat("#.00");

        double[] latlon = {location.getLatitude(), location.getLongitude()};
        String[] latlonStrings = Util.getLatLonString(latlon, 1);

        for (int i = 0; i < latlonStrings.length; i++) {
            ((TextView) findViewById(viewIds[i])).setText(latlonStrings[i]);
        }

        if (location.hasAltitude())
            ((TextView) findViewById(viewIds[2])).setText("Alt:" + dec.format(location.getAltitude()) + "m");
        if (location.hasAccuracy()) {
            setGpsLevel(location.getAccuracy());
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            findViewById(R.id.gps_signal_strength).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.gps_signal_strength).setVisibility(View.GONE);
        }
        if (araaftorService.getGpsSatsAvailable() != 0)
            ((TextView) findViewById(viewIds[3])).setText(Integer.toString(araaftorService.getGpsSatsAvailable()) + "sat");
    }

    public void initBatteryLevel() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
    public void setBatteryLevel(int scale, int level) {
        ImageView batteryView = (ImageView) findViewById(R.id.battery_level);
        int percentage = Math.round(100.0f * (float) level / (float) scale);
        if (percentage >= 97) {
            batteryView.setImageResource(R.drawable.batery1);
        } else if (percentage >= 90) {
            batteryView.setImageResource(R.drawable.batery1);
        } else if (percentage >= 60) {
            batteryView.setImageResource(R.drawable.batery4);
        } else if (percentage >= 40) {
            batteryView.setImageResource(R.drawable.batery3);
        } else if (percentage >= 20) {
            batteryView.setImageResource(R.drawable.batery5);
        } else if (percentage >= 10) {
            batteryView.setImageResource(R.drawable.batery2);
        } else {
            batteryView.setImageResource(R.drawable.batery2);
        }
    }
    public void setGpsLevel(float f) {
        ImageView gpsView = (ImageView) findViewById(R.id.gps_signal_strength);
        if (f <= MAX_METERS_GOOD) {
            gpsView.setImageResource(R.drawable.signal3);
        } else if (f <= MAX_METERS_OK) {
            gpsView.setImageResource(R.drawable.signal2);
        } else if (f <= MAX_METERS_WEAK) {
            gpsView.setImageResource(R.drawable.signal1);
        } else if (f <= MAX_METERS_BAD) {
            gpsView.setImageResource(R.drawable.signal0);
        }
    }

    BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        int scale = 0;
        int level = 100;

        @Override
        public void onReceive(Context context, Intent intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            m_handler.post(new Runnable() {

                @Override
                public void run() {
                    setBatteryLevel(scale, level);
                }
            });
        }
    };

    private boolean listAssetFiles(String path) {

        String[] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return false;
                    else {

                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }
    void setTextureTransform(CameraCharacteristics characteristics) {
        Size previewSize = getPreviewSize(characteristics);
        int width = previewSize.getWidth();
        int height = previewSize.getHeight();
        int sensorOrientation = getCameraSensorOrientation(characteristics);
        textureView.getSurfaceTexture().setDefaultBufferSize(width, height);
        RectF viewRect = new RectF(0, 0, textureView.getWidth(), textureView.getHeight());
        float rotationDegrees = 0;
        try {
            rotationDegrees = (float) getDisplayRotation();
        } catch (Exception ignored) {
        }
        float w, h;
        if ((sensorOrientation - rotationDegrees) % 180 == 0) {
            w = width;
            h = height;
        } else {
            w = height;
            h = width;
        }
        float viewAspectRatio = viewRect.width() / viewRect.height();
        float imageAspectRatio = w / h;
        final PointF scale;
        if (viewAspectRatio < imageAspectRatio) {
            scale = new PointF((viewRect.height() / viewRect.width()) * ((float) height / (float) width), 1f);
        } else {
            scale = new PointF(1f, (viewRect.width() / viewRect.height()) * ((float) width / (float) height));
        }
        if (rotationDegrees % 180 != 0) {
            float multiplier = viewAspectRatio < imageAspectRatio ? w / h : h / w;
            scale.x *= multiplier;
            scale.y *= multiplier;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scale.x, scale.y, viewRect.centerX(), viewRect.centerY());
        if (rotationDegrees != 0) {
            matrix.postRotate(0 - rotationDegrees, viewRect.centerX(), viewRect.centerY());
        }
        textureView.setTransform(matrix);
    }
    int getCameraSensorOrientation(CameraCharacteristics characteristics) {
        Integer cameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        return (360 - (cameraOrientation != null ? cameraOrientation : 0)) % 360;
    }
    Size getPreviewSize(CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
        // TODO: decide on which size fits your view size the best
        return previewSizes[0];
    }
    int getDisplayRotation() {
        switch (textureView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        setGpsData(location);
    }

    @Override
    public void onAzimuthChanged(float azimuth, float pitch, float roll, float gyro, float lightLevel) {
        TextView rollTv = (TextView) findViewById(R.id.horizon);
        TextView pitchTv = (TextView) findViewById(R.id.elevation);
        TextView azimuthTv = (TextView) findViewById(R.id.azimuthData);
        if (roll > 180) roll -= 360;
        rollTv.setText(Math.round(roll) + ".0");
        pitchTv.setText(Math.round(pitch) + ".0");
        azimuthTv.setText(Util.degToThousandth(azimuth));
        brightness.setText(String.valueOf(lightLevel));
        azimuthNow = azimuth;
        aroow_c.setRotation(azimuth);
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > 500) {
            float deltaAzimuth = Math.abs(azimuth - lastOrientation[0]);
            float deltaPitch = Math.abs(pitch - lastOrientation[1]);
            float deltaRoll = Math.abs(roll - lastOrientation[2]);

            float totalChange = deltaAzimuth + deltaPitch + deltaRoll;
            this.shockIndicator = totalChange;
            if (totalChange > ROTATION_SHAKE_THRESHOLD) {
                cameraButton.setVisibility(View.GONE);
                cameraButtonRed.setVisibility(View.VISIBLE);
            }else
            {
                cameraButton.setVisibility(View.VISIBLE);
                cameraButtonRed.setVisibility(View.GONE);
            }
            lastOrientation[0] = azimuth;
            lastOrientation[1] = pitch;
            lastOrientation[2] = roll;
            lastUpdateTime = now;
        }

        ImageView compas = (ImageView) findViewById(R.id.kierunki);
        compas.setRotation(azimuth);
        compassView.setAngle(azimuth);

        overlayManager.onSensorChanged((int) azimuth, (int) roll, (int) pitch);
    }

    @Override
    public void showSettingsAlert() {
        araaftorService.showSettingsAlert(this);
    }
    private static final String CAMERA_INFO = "com.atakmap.android.cameratemplate.PHOTO";
    private static final String DELETE_INFO = "com.atakmap.android.cameratemplate.DELETE";
    protected void onActivityResult(int requestCode, int resultCode, double lat, double lon, double altitude, String UID, String nameUnit) {
        Intent i = new Intent(CAMERA_INFO);
        if (resultCode == Activity.RESULT_OK) {
                i.putExtra("lat", lat);
                i.putExtra("lon", lon);
                i.putExtra("altitude", altitude);
                i.putExtra("UID", UID);
                i.putExtra("nameUnit", nameUnit);
        }
        sendBroadcast(i);
    }

    public interface CameraDataReceiver {
        void onCameraDataReceived(double lat, double lon, double altitude, String UID, String nameUnit);
    }

    public static class CameraDataListener extends BroadcastReceiver {
        private boolean registered = false;
        private CameraDataReceiver cdr = null;

        synchronized public void register(Context context, CameraDataReceiver cdr) {
            if (!registered)
                context.registerReceiver(this, new IntentFilter(CAMERA_INFO));
            this.cdr = cdr;
            registered = true;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (this) {
                try {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        double lat = (Double) extras.get("lat");
                        double lon = (Double) extras.get("lon");
                        double altitude = (Double) extras.get("altitude");
                        String UID = (String) extras.get("UID");
                        String nameUnit = (String) extras.get("nameUnit");
                            cdr.onCameraDataReceived(lat, lon, altitude, UID, nameUnit);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    /////////////
    protected void onDeleteResult(int resultCode, String UID) {
        Intent i = new Intent(DELETE_INFO);
        if (resultCode == Activity.RESULT_OK) {
            i.putExtra("UID", UID);
        }
        sendBroadcast(i);
    }

    public interface DeleteEventDataReceiver {
        void onDeleteEventDataReceived(String UID);
    }

    public static class DeleteEventDataListener extends BroadcastReceiver {
        private boolean registered = false;
        private DeleteEventDataReceiver dedr = null;

        synchronized public void register(Context context, DeleteEventDataReceiver dedr) {
            if (!registered)
                context.registerReceiver(this, new IntentFilter(DELETE_INFO));
            this.dedr = dedr;
            registered = true;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (this) {
                try {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        String UID = (String) extras.get("UID");
                        dedr.onDeleteEventDataReceived(UID);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
