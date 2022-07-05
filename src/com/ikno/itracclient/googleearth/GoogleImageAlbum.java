package com.ikno.itracclient.googleearth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class GoogleImageAlbum {

	public static class AlbumEntry {
		public String imageURL;
		protected Image _image;
		public Rectangle bounds;
		public boolean selected = false;
		public boolean available = false;
		public int index;
		public AlbumEntry() {}
		public AlbumEntry(String imageURL) {
			this.imageURL = imageURL;
		}
		public synchronized void buildImage() {
			try {
				ImageDescriptor descriptor = ImageDescriptor.createFromURL(new URL(imageURL));
				if (descriptor != null)
					_image = descriptor.createImage(Display.getCurrent());
			} catch (MalformedURLException e) {}
			notifyAll();
		}
		public synchronized Image getImage() {
			while (_image == null) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			return _image;
		}
	}
	private List<AlbumEntry> albumEntries = new ArrayList<AlbumEntry>();
	private List<AlbumEntry> building = new ArrayList<AlbumEntry>();

	public class BuildAlbumImage extends Thread {
		private AlbumEntry entry;
		public BuildAlbumImage(AlbumEntry entry) {
			this.setDaemon(true);
			this.entry = entry;
		}
		public void run() {
			this.entry.buildImage();
			synchronized (building) {
				building.remove(this.entry);
			}
		}
	}

	public class BuildAlbum extends Thread {
		public BuildAlbum() {
			this.setDaemon(true);
			this.setName("Build Google Images");
		}
		public void run() {
			int current = 0;
			int busy;
			while (current < albumEntries.size()) {
				synchronized (building) {
					busy = building.size();
				}
				if (busy < 5) {
					AlbumEntry next = albumEntries.get(current++);
					synchronized (building) {
						building.add(next);
					}
					BuildAlbumImage task = new BuildAlbumImage(next);
					task.start();
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public GoogleImageAlbum() {
		String base = "http://maps.google.com/mapfiles/kml/pal%d/icon%d.png";
//		String base = "http://www.i-see.co.za/images/google/pal%d/icon%d.png";
		int idx = 0;
		for (int i=2;i<=5;i++) {
			for (int j=0;j<64;j++) {
				String url = String.format(base,i,j);
				AlbumEntry entry = new AlbumEntry(url);
				entry.index = idx++;
				albumEntries.add(entry);
			}
		}
		BuildAlbum albumTask = new BuildAlbum();
		albumTask.start();
	}
	public void dispose() {
		for (AlbumEntry entry : albumEntries) {
			entry._image.dispose();
			entry._image = null;
		}
	}
	public int size() {
		return albumEntries.size();
	}
	public AlbumEntry getAlbumEntryAt(int index) {
		return albumEntries.get(index);
	}
	public void localCopy(String path) {
		for (AlbumEntry entry : albumEntries) {
			int index = entry.imageURL.lastIndexOf("/",entry.imageURL.lastIndexOf("/")-1);
			String fileName = entry.imageURL.substring(index+1);
			String full = path;
			if (!full.endsWith(File.separator))
				full += File.separator;
			full += fileName;
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[]{entry.getImage().getImageData()};
			loader.save(full, SWT.IMAGE_PNG);
		}
	}
}
