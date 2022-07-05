package com.ikno.itracclient.worldwind.layers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Random;

import com.ikno.dao.utils.GoogleTileUtils;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

public class GoogleMaps extends BasicTiledImageLayer {
    public GoogleMaps()
    {
        super(makeLevels(new URLBuilder()));
        this.setUseTransparentTextures(true);
    }

    private static LevelSet makeLevels(URLBuilder urlBuilder)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/GoogleMaps");
        params.setValue(AVKey.SERVICE, "http://kh%d.google.com/kh?n=404&v=28&t=%s");
        params.setValue(AVKey.DATASET_NAME, "GoogleMaps");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 15);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(90d), Angle.fromDegrees(90d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        params.setValue(AVKey.TILE_URL_BUILDER, urlBuilder);
        params.setValue(AVKey.EXPIRY_TIME, new GregorianCalendar(2007, 7, 6).getTimeInMillis());

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder {
    	public URL getURL(Tile tile, String imageFormat) throws MalformedURLException {
    		int[] indexes = new int[]{0,1,2,3};
    		String service =  tile.getLevel().getService();
    		Random gener = new Random();
    		Sector s = tile.getSector();
    		double lat = s.getMinLatitude().getDegrees();
    		double lon = s.getMinLongitude().getDegrees();
    		int level = 15-tile.getLevel().getLevelNumber();
    		String satref = GoogleTileUtils.getSatelliteRef(lat,lon,level);
    		service = String.format(service, indexes[gener.nextInt(indexes.length)],satref);
        	System.out.println("URL: "+service);
            return new java.net.URL(service);
        }
    }

    @Override
    public String toString() {
        return "Google Maps";
    }
}
