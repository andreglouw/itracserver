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
public class CountryBoundariesLayer extends BasicTiledImageLayer
{
    public CountryBoundariesLayer()
    {
        super(makeLevels(new URLBuilder()));
        this.setUseTransparentTextures(true);
    }

    private static LevelSet makeLevels(URLBuilder urlBuilder)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/CountryBoundaries");
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/cgi-bin/gettile.php");
        params.setValue(AVKey.DATASET_NAME, "Countries");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 10);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
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
            if (sb.lastIndexOf("?") != sb.length() - 1)
                sb.append("?");
            sb.append("T=");
            sb.append(tile.getLevel().getDataset().replace(" ", "_"));
            sb.append("&F=image/png");
            sb.append("&L=");
            sb.append(tile.getLevelNumber());
            sb.append("&X=");
            sb.append(String.format("%d", tile.getColumn()));
            sb.append("&Y=");
            sb.append(String.format("%d", tile.getRow()));

            /*
            Sector s = tile.getSector();
            System.out.println(String.format("Tile %d_%d relates to sector %.5f, %.5f, %.5f, %.5f",
            		tile.getRow(),tile.getColumn(),s.getMinLongitude().getDegrees(),s.getMinLatitude().getDegrees(),
            		s.getMaxLongitude().getDegrees(),s.getMaxLatitude().getDegrees()));
            */

            return new URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return "Country Boundaries";
    }
}
