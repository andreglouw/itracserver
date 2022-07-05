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
public class CountryBoundariesMSLayer extends BasicTiledImageLayer
{
    public CountryBoundariesMSLayer()
    {
        super(makeLevels(new URLBuilder()));
        this.setUseTransparentTextures(true);
    }

    private static LevelSet makeLevels(URLBuilder urlBuilder)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/TrackingBoundaries");
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/cgi-bin/mapserv?map=/opt/mapserver/tracking_boundaries.map");
        params.setValue(AVKey.DATASET_NAME, "Country Boundaries");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 13);
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
            sb.append("&layer=");
            sb.append(tile.getLevel().getDataset());

            sb.append("&mapsize=");
            sb.append(tile.getLevel().getTileWidth());
            sb.append(" ");
            sb.append(tile.getLevel().getTileHeight());

            Sector s = tile.getSector();
            sb.append("&minx=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append("&miny=");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append("&maxx=");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append("&maxy=");
            sb.append(s.getMaxLatitude().getDegrees());

            sb.append("&mode=map");
            
            return new java.net.URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.PoliticalBoundaries.Name");
    }
}
