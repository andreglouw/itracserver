package com.ikno.itracclient.worldwind.layers;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;

public class Iraq1mCIB_MS extends BasicTiledImageLayer {

    public Iraq1mCIB_MS()
    {
        super(makeLevels(new URLBuilder()));
        this.setValue(AVKey.URL_READ_TIMEOUT, 120000);
        this.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 180000);
        this.setUseTransparentTextures(true);
    }

    private static LevelSet makeLevels(URLBuilder urlBuilder)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Iraq1m_MS");
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/tracking/servlet/mapservice?service=wwtile");
        params.setValue(AVKey.DATASET_NAME, "Iraq_1m_MS");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 15);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 4);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        /*
        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(29.0772),Angle.fromDegrees(36.7802), 
                Angle.fromDegrees(39.2404), Angle.fromDegrees(48.3905)));
        */
        params.setValue(AVKey.TILE_URL_BUILDER, urlBuilder);
        params.setValue(AVKey.EXPIRY_TIME, new GregorianCalendar(2007, 7, 6).getTimeInMillis());

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder
    {
    	/* Version 0.4.0
    	public URL getURL(Tile tile) throws MalformedURLException
         */
    	/* Version 0.5.0
        */
    	public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
        {
            StringBuffer sb = new StringBuffer(tile.getLevel().getService());

            sb.append("&column="+tile.getColumn());
            sb.append("&row="+tile.getRow());
            sb.append("&level="+tile.getLevel().getLevelNumber());
            
            Sector s = tile.getSector();
            sb.append("&minx=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append("&miny=");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append("&maxx=");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append("&maxy=");
            sb.append(s.getMaxLatitude().getDegrees());

            sb.append("&format=image/png");

            sb.append("&dataset=");
            sb.append(tile.getLevel().getDataset());
            
//        	System.out.println("URL: "+sb.toString());
            return new java.net.URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return "Iraq 1m MS";
    }
}
