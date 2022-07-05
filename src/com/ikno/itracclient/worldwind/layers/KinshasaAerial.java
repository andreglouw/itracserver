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

public class KinshasaAerial extends BasicTiledImageLayer {
	
	public KinshasaAerial() {
        super(makeLevels());
        this.setUseTransparentTextures(true);
	}
    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Kinshasa Aerial");
        params.setValue(AVKey.SERVICE, "http://www.i-see.co.za/cgi-bin/gettile.php");
        params.setValue(AVKey.DATASET_NAME, "Kinshasa_Aerial");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 11);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(1.0), Angle.fromDegrees(1.0)));
        // Sector(minLat,maxLat,minLon,maxLon)
        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(-4.4646), Angle.fromDegrees(-4.1404),
                Angle.fromDegrees(15.0631), Angle.fromDegrees(15.4814)));
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
        return "Kinshasa Aerial";
    }
}
