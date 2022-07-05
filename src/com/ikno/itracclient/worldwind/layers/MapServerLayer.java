package com.ikno.itracclient.worldwind.layers;

import java.net.MalformedURLException;
import java.net.URL;

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

public class MapServerLayer extends BasicTiledImageLayer {
    private static final String defaultMapName = "tracking_sa";
    private String mapName;

    /**
     * Default OpenStreetMap hybrid layer - transparent, see-through.
     */
    public MapServerLayer()
    {
        super(makeLevels(defaultMapName));
        this.setUseTransparentTextures(true);
        this.mapName = defaultMapName;
    }

    /**
     * Access to a specific layer from OSM WMS server - eg 'osm-4326'.
     * @param datasetName the layer dataset name.
     */
    public MapServerLayer(String datasetName)
    {
        super(makeLevels(datasetName));
        this.setUseTransparentTextures(true);
        this.mapName = datasetName;
    }

    private static LevelSet makeLevels(String mapName)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Tracking/" + mapName);
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/cgi-bin/mapserv");
        params.setValue(AVKey.DATASET_NAME, mapName);
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 20);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(-34.834), Angle.fromDegrees(-22.125),
                Angle.fromDegrees(15.67), Angle.fromDegrees(32.994)));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

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
            sb.append("request=GetMap");
            sb.append("&map=");
            sb.append("/opt/mapserver/"+tile.getLevel().getDataset()+".map");
            sb.append("&layers=");
            sb.append(tile.getLevel().getDataset());
            sb.append("&srs=EPSG:4326");
            sb.append("&width=");
            sb.append(tile.getLevel().getTileWidth());
            sb.append("&height=");
            sb.append(tile.getLevel().getTileHeight());

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLatitude().getDegrees());

            sb.append("&format=image/png");
            sb.append("&service=WMS");
            sb.append("&version=1.1.1");

            return new java.net.URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return "Tracking: " + this.mapName;
    }

}
