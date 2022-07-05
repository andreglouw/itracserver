package com.ikno.itracclient.worldwind.layers;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.*;

import java.net.*;
import java.util.GregorianCalendar;

/**
 * @author tag
 * @version $Id: CountryBoundariesLayer.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class StreetDetailMSLayer extends BasicTiledImageLayer
{
    public StreetDetailMSLayer()
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
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/StreetDetail");
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/tracking/servlet/mapservice?service=wwtile");
        params.setValue(AVKey.DATASET_NAME, "StreetDetail");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 15);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(-34.84), Angle.fromDegrees(37.50),
                Angle.fromDegrees(-17.8), Angle.fromDegrees(51.50)));
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
        return "Street Detail MS";
    }
}
