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

public class DSHighRes extends BasicTiledImageLayer {

	public DSHighRes() {
        super(makeLevels());
        this.setUseTransparentTextures(true);
	}
    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/DS High Res");
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/cgi-bin/gettile.php");
        params.setValue(AVKey.DATASET_NAME, "DSHighRes");
        params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
        params.setValue(AVKey.NUM_LEVELS, 8);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 3);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(0.70312500), Angle.fromDegrees(0.70312500)));
        // Sector(minLat,maxLat,minLon,maxLon)
        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(-26.6818), Angle.fromDegrees(-25.3532),
                Angle.fromDegrees(27.1718), Angle.fromDegrees(28.8263)));
        params.setValue(AVKey.TILE_URL_BUILDER, new TileUrlBuilder()
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
                sb.append("&L=");
                sb.append(tile.getLevelNumber());
                sb.append("&X=");
                sb.append(String.format("%04d", tile.getColumn()));
                sb.append("&Y=");
                sb.append(String.format("%04d", tile.getRow()));

                return new URL(sb.toString());
            }
        });

        return new LevelSet(params);
    }

    @Override
    public String toString()
    {
        return "DS High Res";
    }
}
