package com.atakmap.android.plugintemplate.araaftorPlugin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import com.atakmap.android.plugintemplate.plugin.R;
import com.atakmap.android.plugintemplate.araaftorPlugin.services.AraaftorService;


public class AraaftorOverlayManager {
    AraaftorService service;
    AbsoluteLayout parent;
    private LinkedList<AraaftorMarker> units;
    Context context;
    private int horizontalFOV = 20;
    private int verticalFOV = 40;
    private int maxDistance = 40;
    private int width;
    private int height;
    private int symbol_max_size = 0;
    private double offsetUnitWidth;
    private double offsetUnitHeight;
    private int unitOffsetPerLevel;
    private static boolean isExtended = false;
    private LayoutInflater inflater;
    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MediatorApplication.getAppContext());
    private DecimalFormat df = new DecimalFormat("#.##");
    private DecimalFormat pf = new DecimalFormat("#.##%");
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private int[] X = new int[5];
    private int[] Y = new int[5];
    private Unit me;

    /**
     * <summary>
     * constructor
     * initialization of unit's list
     * </summary>
     *
     * @param context
     * @param parent
     * @param gObjs
     * @param gPoints
     */
    public AraaftorOverlayManager(Activity context, AbsoluteLayout parent, LinkedList<GeoObject> gObjs, LinkedList<GeoPoint> gPoints, List<String> app6as) {
        this.parent = parent;
        units = new LinkedList<>();
        df.setRoundingMode(RoundingMode.HALF_UP);
        if (gObjs != null) {
            for (int i = 1; i < gObjs.size(); i++) {
                units.add(new AraaftorMarker(gObjs.get(i)));
            }
        }
        if (gPoints != null) {
            for (int i = 0; i < gPoints.size(); i++) {
                units.add(new AraaftorMarker(gPoints.get(i), i, app6as.get(i)));
            }
        }
        service = MediatorApplication.getService();
        this.context = context;

        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        height = outMetrics.heightPixels;
        width = outMetrics.widthPixels;
        Log.d("SAME-metrics", "H=" + height + "Y" + width);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        navigationView = (NavigationView) context.findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) context.findViewById(R.id.drawer);
    }

    public void onSensorChanged(int azimuth, int roll, int pitch) {
        if (service.getLocation() != null) {
            int lat = (int) (service.getLocation().getLatitude() * 1E6);
            int lng = (int) (service.getLocation().getLongitude() * 1E6);
            GeoPoint point = new GeoPoint(lat, lng);
            for (AraaftorMarker marker : units) {
                paint(azimuth, pitch, point, marker);
            }
        }
    }

    public void updateBorders() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        horizontalFOV = getIntPref(preferences, "pref_ar_horizontal_fov", 20);
        verticalFOV = getIntPref(preferences, "pref_ar_vertical_fov", 40);
        maxDistance = getIntPref(preferences, "pref_ar_distance", 40) * 1000; ///to KM
        unitOffsetPerLevel = getIntPref(preferences, "pref_ar_unit_offset", 50);
        offsetUnitWidth = (double) width / (double) verticalFOV;
        offsetUnitHeight = (double) height / (double) horizontalFOV;
        symbol_max_size = getIntPref(preferences, "pref_ar_symbol_size", 100);
    }

    public static int getIntPref(SharedPreferences preferences, String name, int defaultValue) {
        String value = preferences.getString(name, defaultValue + "");
        return Integer.valueOf(value);
    }

    public static int getLowerDiff(int bearing, int azimuth) {
        ///359 i 1
        int remappedBearing = bearing;
        remappedBearing -= 360;

        return Math.min(bearing - azimuth, -remappedBearing + azimuth);
    }


    public void paint(int azimuth, int pitch, GeoPoint point, final AraaftorMarker marker) {
        final Unit unit = (Unit) marker.getObject();
        GeoPoint gPoint = marker.getPoint();
        if (unit != null) {
            final double bearing = point.bearingTo(unit.location);
            final double distance = point.distanceToAsDouble(unit.location);
            if (getLowerDiff((int) Math.round(bearing), azimuth) <= verticalFOV / 2 && distance <= maxDistance) {
                if (unit.isReleased()) {
                    unit.setReleased(false);
                } else {

                    if (marker.getObject().getApp6aSymbol().getIcon() != null) {
                        int yOffset = (int) Math.round(height / 2.0 + (double) pitch * offsetUnitHeight);
                        int xOffset = (int) Math.round(width / 2.0 + (bearing - azimuth) * offsetUnitWidth);

                        if (marker.getView() == null) {
                            View view = inflater.inflate(R.layout.ar_marker, null);
                            ImageView imageView = (ImageView) view.findViewById(R.id.unit_symbol);
                            ImageView underlayView = (ImageView) view.findViewById(R.id.underlay);
                            imageView.setImageBitmap(unit.getApp6aSymbol().getIcon());
                            RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                            int imageEdge = (int) Math.round(symbol_max_size * Math.max(0.1, (1.0 - (distance / (double) maxDistance))));
                            imageParams.width = imageEdge;
                            imageParams.height = imageEdge;
                            imageView.setLayoutParams(imageParams);
                            underlayView.setLayoutParams(imageParams);

                            TextView nameView = (TextView) view.findViewById(R.id.unit_code);
                            nameView.setText(unit.getApp6aCode());
                            TextView distView = (TextView) view.findViewById(R.id.unit_dist2);
                            distView.setText(df.format(distance));


                            TextView latView = (TextView) view.findViewById(R.id.unit_lat);
                            latView.setText(unit.getLatlonString(1));
                            TextView lonView = (TextView) view.findViewById(R.id.unit_lon);
                            lonView.setText(unit.getLatlonString(3));
//                            TextView thView = (TextView) view.findViewById(R.id.unit_threat);
//                            thView.setText(pf.format(unit.getThl() / 100));
                            ProgressBar Bar = (ProgressBar) view.findViewById(R.id.progressBar);
                            Bar.setMax(100);
                            Bar.setProgress((int) unit.getThl());

                            View line = view.findViewById(R.id.ar_line);
                            RelativeLayout.LayoutParams lineParams = (RelativeLayout.LayoutParams) line.getLayoutParams();
                            lineParams.height = Util.getRankLevelFromApp6aCode(unit.getApp6aCode()) * unitOffsetPerLevel + imageView.getMeasuredHeight() / 2;
                            lineParams.topMargin = imageView.getMeasuredHeight() / 2;
                            line.setLayoutParams(lineParams);

                            marker.setView(view);
                            view.setVisibility(View.INVISIBLE);
                            parent.addView(marker.getView());
                        }
                        View view = marker.getView();
                        ImageView imageView = (ImageView) view.findViewById(R.id.unit_symbol);
//                        imageView.setOnClickListener(new OnClickListener() {
//                            @Override
//                            public void onClick(View arg0) {
//                                parent.bringChildToFront(marker.getView());
//
//                                TextView nameV = (TextView) navigationView.findViewById(R.id.unit_name);
//                                nameV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Integer.parseInt(prefs.getString("pref_text_length", "19")))});
//                                nameV.setText(unit.getName());
//                                TextView Thl = (TextView) navigationView.findViewById(R.id.unit_ThL);
//                                Thl.setText(pf.format(unit.getThl() / 100));
//
//
//                                View v = navigationView.findViewById(R.id.status_icon_color_view);
//                                if (unit.getThl() <= 30) {
//                                    v.setBackgroundColor(Color.parseColor("#00c936"));
//                                } else if (unit.getThl() > 30 && unit.getThl() < 75) {
//                                    v.setBackgroundColor(Color.parseColor("#f2a10b"));
//                                } else {
//                                    v.setBackgroundColor(Color.RED);
//                                }
//                                TextView codeView = (TextView) navigationView.findViewById(R.id.unit_code2);
//                                codeView.setText(unit.getApp6aCode());
//
//                                TextView latView2 = (TextView) navigationView.findViewById(R.id.arLatFloat2);
//                                latView2.setText(unit.getLatlonString(0));
//                                TextView latView3 = (TextView) navigationView.findViewById(R.id.arLatDeg2);
//                                latView3.setText(unit.getLatlonString(1));
//                                TextView lonView2 = (TextView) navigationView.findViewById(R.id.arLonFloat2);
//                                lonView2.setText(unit.getLatlonString(2));
//                                TextView lonView3 = (TextView) navigationView.findViewById(R.id.arLonDeg2);
//                                lonView3.setText(unit.getLatlonString(3));
//                                TextView eta = (TextView) navigationView.findViewById(R.id.unit_ETA);
//                                if (service.getLocation().getSpeed() == 0) {
//                                    eta.setText("---");
//                                } else {
//                                    eta.setText(df.format(distance / service.getLocation().getSpeed() * 3.6));
//                                }
//                                TextView dist = (TextView) navigationView.findViewById(R.id.unit_dist);
//                                dist.setText(df.format(distance));
//                                TextView bear = (TextView) navigationView.findViewById(R.id.unit_bear);
//                                bear.setText(df.format(bearing));
//
//                                TableRow row = (TableRow) navigationView.findViewById(R.id.last_row);
//
//                                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
//                                } else {
//                                    drawerLayout.openDrawer(Gravity.LEFT);
//                                }
//
//                            }
//                        });
                        LayoutParams params = new LayoutParams(
                                (LayoutParams.WRAP_CONTENT), (LayoutParams.WRAP_CONTENT), xOffset - view.getWidth() / 2, yOffset - view.getHeight());
                        view.setLayoutParams(params);
                        Rect r = Util.locateView(imageView);
                        float touchX = r.left + ((r.right - r.left) / 2);
                        float touchY = r.top + ((r.bottom - r.top) / 2);
                        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                        int imageEdge = (int) Math.round(symbol_max_size * Math.max(0.1, (1.0 - (distance / (double) maxDistance))));
                        ImageView underlayView = (ImageView) view.findViewById(R.id.underlay);
                        if (touchX >= (width / 2 - 40) && touchX <= width / 2 + 40) {
                            if (touchY >= (height / 2 - 40) && touchY <= (height / 2 + 40)) {
                                if (!isExtended) {
                                    imageParams.width = imageEdge * 2;
                                    imageParams.height = imageEdge * 2;
                                    isExtended = true;
                                }
                            }
                        } else {
                            imageParams.width = imageEdge;
                            imageParams.height = imageEdge;
                            isExtended = false;
                        }
                        imageView.setLayoutParams(imageParams);
                        underlayView.setLayoutParams(imageParams);
                        View line = view.findViewById(R.id.ar_line);
                        RelativeLayout.LayoutParams lineParams = (RelativeLayout.LayoutParams) line.getLayoutParams();
                        lineParams.height = Util.getRankLevelFromApp6aCode(unit.getApp6aCode()) * unitOffsetPerLevel + imageView.getMeasuredHeight() / 2;
                        lineParams.topMargin = imageView.getMeasuredHeight() / 2;
                        line.setLayoutParams(lineParams);

                        if (view.getVisibility() == View.INVISIBLE)
                            view.setVisibility(View.VISIBLE);
                    }
                }
            } else if (!unit.isReleased()) {
                if (marker.getView() != null) {
                    ((ViewManager) marker.getView().getParent()).removeView(marker.getView());
                }

                marker.setView(null);
                unit.setReleased(true);

            }
        } else if (gPoint != null) {
            double bearing = point.bearingTo(gPoint);
            double distance = point.distanceToAsDouble(gPoint);
            if (getLowerDiff((int) Math.round(bearing), azimuth) <= verticalFOV / 2 && distance <= maxDistance) {
                int yOffset = (int) Math.round(height / 2.0 + (double) pitch * offsetUnitHeight);
                int xOffset = (int) Math.round(width / 2.0 + (bearing - azimuth) * offsetUnitWidth);
                if (marker.getView() == null) {
                    try {
                        View view = inflater.inflate(R.layout.ar_marker, null);
                        ImageView imageView = (ImageView) view.findViewById(R.id.unit_symbol);
                        ImageView underlayView = (ImageView) view.findViewById(R.id.underlay);
                        String temp = marker.getApp6a().replace("-", "_").replace("asset://mil_std_2525c/", "").replace(".png", "");
                        Log.d("bambus", "gown2 " + temp);
                        int id = this.context.getResources().getIdentifier(temp, "drawable", this.context.getPackageName());
                        Drawable drawable = this.context.getResources().getDrawable(id);
                        imageView.setImageDrawable(drawable);

                        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                        int imageEdge = (int) Math.round(symbol_max_size * Math.max(0.1, (1.0 - (distance / (double) maxDistance))));
                        imageParams.width = imageEdge;
                        imageParams.height = imageEdge;
                        imageView.setLayoutParams(imageParams);
                        underlayView.setLayoutParams(imageParams);
                        TextView nameView = (TextView) view.findViewById(R.id.unit_code);
                        nameView.setText((marker.getApp6a().replace("asset://mil-std-2525c/", "").replace(".png", "")));
                        TextView dist = (TextView) view.findViewById(R.id.unit_dist2);
                        dist.setText(Double.toString(distance));
                        TextView latView = (TextView) view.findViewById(R.id.unit_lat);
                        latView.setText("LAT:" + ((double) (marker.getPoint().getLatitudeE6() / 10E5)));
//                        latView.setVisibility(View.GONE);
                        TextView lonView = (TextView) view.findViewById(R.id.unit_lon);
                        lonView.setText("LON:" + ((double) (marker.getPoint().getLongitudeE6() / 10E5)));
//                        lonView.setVisibility(View.GONE);
                        TextView potView = (TextView) view.findViewById(R.id.potText);
                        //TextView threatView = (TextView) view.findViewById(R.id.threatText);

                        String codeApp = (marker.getApp6a().replace("asset://mil-std-2525c/", "").replace(".png", ""));
                        String affiliation = "Unknown";
                        if (codeApp.charAt(1) == 'f') {
                            affiliation = "Friendly";
                        } else if (codeApp.charAt(1) == 'h') {
                            affiliation = "Hostile";
                        } else if (codeApp.charAt(1) == 'n') {
                            affiliation = "Neutral";
                        }

                        String threat = "minimal";
                        if (marker.getPoint().getLatitudeE6() % 3 == 0) {
                            threat = "intermediate";
                        } else if (marker.getPoint().getLatitudeE6() % 3 == 1) {
                            threat = "high";
                        }
                        //threatView.setText("Threat: " + threat);

                        potView.setText(affiliation);
//                        potView.setVisibility(View.GONE);
                        ProgressBar Bar = (ProgressBar) view.findViewById(R.id.progressBar);
                        Bar.setVisibility(View.GONE);
//                    TextView utv = (TextView) view.findViewById(R.id.unit_threat);
//                    utv.setText("");
//                    utv.setVisibility(View.GONE);
                        View line = view.findViewById(R.id.ar_line);
                        marker.setView(view);
                        view.setVisibility(View.INVISIBLE);
                        parent.addView(marker.getView());
                    } catch (Exception e) {
                    }
                }
                View view = marker.getView();
                if (view != null) {
                    ImageView imageView = (ImageView) view.findViewById(R.id.unit_symbol);
                    LayoutParams params = new LayoutParams(
                            (LayoutParams.WRAP_CONTENT), (LayoutParams.WRAP_CONTENT), xOffset - view.getWidth() / 2, yOffset - view.getMeasuredHeight());
                    view.setLayoutParams(params);
                    View line = view.findViewById(R.id.ar_line);
                    RelativeLayout.LayoutParams lineParams = (RelativeLayout.LayoutParams) line.getLayoutParams();
                    lineParams.height = 0;
                    lineParams.topMargin = 0;
                    line.setLayoutParams(lineParams);
                    if (view.getVisibility() == View.INVISIBLE) view.setVisibility(View.VISIBLE);
                }
            } else {
                if (marker.getView() != null) {
                    ((ViewManager) marker.getView().getParent()).removeView(marker.getView());
                }
                marker.setView(null);
            }
        }
    }

    public void setShowCompass(boolean b) {

    }
}
