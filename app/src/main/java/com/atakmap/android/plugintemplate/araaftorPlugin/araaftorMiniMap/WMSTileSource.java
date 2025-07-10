package com.atakmap.android.plugintemplate.araaftorPlugin.araaftorMiniMap;

import android.util.Log;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;

/**
 * This class will allow you to overlay tiles from a WMS server.  Your WMS
 * server needs to support .  An example of how your base url should look:
 * <p>
 * https://xxx.xxx.xx.xx/geoserver/gwc/service/wms?LAYERS=base_map&FORMAT=image/jpeg
 * &SERVICE=WMS&VERSION=1.1.1REQUEST=GetMap&STYLES=&SRS=EPSG:900913&WIDTH=256&HEIGHT=256&BBOX=
 * <p>
 * Notice three things:
 * 1. I am pulling jpeg instead of png files.  For some reason our server
 * makes much smaller jpg files and this gives us a faster load time on
 * mobile networks.
 * 2. The bounding box is at the end of the base url. This is because the
 * getTileURLString method adds the bounding box values onto the end of
 * the base url.
 * 3. We are pulling the SRS=EPSG:900913 and not the SRS=EPSG:4326.
 * This all has to do drawing rounded maps onto flat displays.
 *
 * @author Steve Potell -- spotell@t-sciences.com
 */
public class WMSTileSource extends OnlineTileSourceBase {

    public WMSTileSource(final String aName, final int aZoomMinLevel,
                         final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
                         final String aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, new String[]{aBaseUrl});
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {

        StringBuilder tileURLString = new StringBuilder();
        tileURLString.append(getBaseUrl());
        tileURLString.append(wmsTileCoordinates(pMapTileIndex));
        return tileURLString.toString();
    }


    private final static double ORIGIN_SHIFT = Math.PI * 6378137;

    /**
     * WMS requires the bounding box to be defined as the point (west, south)
     * to the point (east, north).
     *
     * @param value
     * @return The WMS string defining the bounding box values.
     */
    public String wmsTileCoordinates(long value) {

        BoundingBox newTile = tile2boundingBox(MapTileIndex.getX(value), MapTileIndex.getY(value), MapTileIndex.getZoom(value));

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(newTile.west);
        stringBuffer.append(",");
        stringBuffer.append(newTile.south);
        stringBuffer.append(",");
        stringBuffer.append(newTile.east);
        stringBuffer.append(",");
        stringBuffer.append(newTile.north);

        return stringBuffer.toString();

    }

    /**
     * A simple class for holding the NSEW lat and lon values.
     */
    class BoundingBox {
        double north;
        double south;
        double east;
        double west;
    }

    /**
     * This method converts tile xyz values to a WMS bounding box.
     *
     * @param x    The x tile coordinate.
     * @param y    The y tile coordinate.
     * @param zoom The zoom level.
     * @return The completed bounding box.
     */
    BoundingBox tile2boundingBox(final int x, final int y, final int zoom) {

        Log.d("MapTile", "--------------- x = " + x);
        Log.d("MapTile", "--------------- y = " + y);
        Log.d("MapTile", "--------------- zoom = " + zoom);

        BoundingBox bb = new BoundingBox();
//        double my = yToWgs84toEPSGLat(y, zoom);
//        double mx = xToWgs84toEPSGLon(x, zoom);
//        double my1 = yToWgs84toEPSGLat(y+1, zoom);
//        double mx1 = xToWgs84toEPSGLon(x+1, zoom);
//        GeoPoint lu = toEPSG4326(mx, my);
//        GeoPoint rb = toEPSG4326(mx1, my1);
//        bb.north = lu.getLatitudeE6();
//        bb.west = lu.getLongitudeE6();
//        bb.south = rb.getLatitudeE6();
//        bb.east = rb.getLongitudeE6();
        bb.north = yToWgs84toEPSGLat(y, zoom);
        bb.south = yToWgs84toEPSGLat(y + 1, zoom);
        bb.west = xToWgs84toEPSGLon(x, zoom);
        bb.east = xToWgs84toEPSGLon(x + 1, zoom);

        return bb;
    }

    /**
     * Converts X tile number to EPSG value.
     *
     * @param tileX the x tile being requested.
     * @param zoom  The current zoom level.
     * @return EPSG longitude value.
     */
    static double xToWgs84toEPSGLon(int tileX, int zoom) {

        // convert x tile position and zoom to wgs84 longitude
        double value = tileX / Math.pow(2.0, zoom) * 360.0 - 180;

//zostajemy na wgs84
//        return value;
        // apply the shift to get the EPSG longitude
        return value * ORIGIN_SHIFT / 180.0;

    }

    /**
     * Converts Y tile number to EPSG value.
     *
     * @param tileY the y tile being requested.
     * @param zoom  The current zoom level.
     * @return EPSG latitude value.
     */
    static double yToWgs84toEPSGLat(int tileY, int zoom) {

        // convert x tile position and zoom to wgs84 latitude
        double value = Math.PI - (2.0 * Math.PI * tileY) / Math.pow(2.0, zoom);
        value = Math.toDegrees(Math.atan(Math.sinh(value)));

        value = Math.log(Math.tan((90 + value) * Math.PI / 360.0)) / (Math.PI / 180.0);
        //zostajemy na wgs84
//        return value;
        // apply the shift to get the EPSG latitude
        return value * ORIGIN_SHIFT / 180.0;

    }

    static GeoPoint toEPSG4326(double mx, double my) {

        double lon = (mx / ORIGIN_SHIFT) * 180.0;
        double lat = (my / ORIGIN_SHIFT) * 180.0;

        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
        GeoPoint gp = new GeoPoint(lat, lon);
        return gp;
    }
}
