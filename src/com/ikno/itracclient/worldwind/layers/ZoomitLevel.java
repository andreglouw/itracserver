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

public class ZoomitLevel extends BasicTiledImageLayer {

	public ZoomitLevel() {
        super(makeLevels());
        this.setUseTransparentTextures(true);
	}
    private static LevelSet makeLevels() {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Zoomit");
        params.setValue(AVKey.SERVICE, "http://www.madmappers.com/getTile.php?T=Spot5&version=1&interface=worldwind&firstLevel=10");
        params.setValue(AVKey.DATASET_NAME, "Spot5");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 7);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 3);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(2.8125), Angle.fromDegrees(2.8125)));
        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(-34.835), Angle.fromDegrees(-16.125),
                Angle.fromDegrees(15.67), Angle.fromDegrees(32.994)));
        params.setValue(AVKey.TILE_URL_BUILDER, new TileUrlBuilder() {
        	public URL getURL(Tile tile, String imageFormat) throws MalformedURLException {
                StringBuffer sb = new StringBuffer(tile.getLevel().getService());
                sb.append("&T=");
                sb.append(tile.getLevel().getDataset());
                sb.append("&L=");
                sb.append(tile.getLevelNumber()-3);
                sb.append("&X=");
                sb.append(String.format("%d", tile.getColumn()));
                sb.append("&Y=");
                sb.append(String.format("%d", tile.getRow()));

//            	System.out.println("URL: "+sb.toString());
                return new URL(sb.toString());
            }
        });

        return new LevelSet(params);
    }

    @Override
    public String toString() {
        return "Zoomit!";
    }
}
