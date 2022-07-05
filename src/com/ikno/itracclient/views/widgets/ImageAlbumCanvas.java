package com.ikno.itracclient.views.widgets;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import com.ikno.itracclient.TracController;
import com.ikno.itracclient.dialogs.IconSelectionDialog;
import com.ikno.itracclient.googleearth.GoogleImageAlbum.AlbumEntry;

public class ImageAlbumCanvas extends Canvas {
	private int rows = 0;
	private int cols = 0;
	private int fitRows = 0;
	private int fitCols = 0;
	private int startRow = 0;
	private int startCol = 0;
	private int width = 32;
	private int height = 32;
	private int border = 2;
	private Color normal = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	private Color selected = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);

	public ImageAlbumCanvas(final Composite parent, int style, final IconSelectionDialog dialog) {
		super(parent,style|SWT.BORDER|SWT.V_SCROLL);
		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paint(event.gc);
			}
		});
		this.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				Rectangle rect = getClientArea();
				cols = ((rect.width-(2*border))/(width+(2*border)))-1;
				fitCols = (rect.width-(2*border))/(width+(2*border));
				rows = (TracController.getGoogleImageAlbum().size()/cols)+1;
				fitRows = (rect.height-(2*border))/(height+(2*border));
				ScrollBar vertical = getVerticalBar();
				if (rows > fitRows) {
					vertical.setEnabled(true);
					vertical.setIncrement(1);
					vertical.setPageIncrement(fitRows);
					vertical.setMaximum(rows);
					vertical.addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {
						}
						public void widgetSelected(SelectionEvent e) {
							ScrollBar scrollBar = (ScrollBar)e.widget;
							startRow = scrollBar.getSelection();
							redraw();
						}
					});
				} else {
					vertical.setEnabled(false);
				}
				redraw();
			}
		});
		this.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseUp(MouseEvent e) {
				boolean found = false;
				int idx = startRow*fitCols;
				int maxIdx = TracController.getGoogleImageAlbum().size();
				for (int row = startRow; row<=(startRow+fitRows)-1; row++) {
					for (int col = startCol; idx<maxIdx && col<=(startCol+fitCols)-1; col++) {
						AlbumEntry entry = TracController.getGoogleImageAlbum().getAlbumEntryAt(idx++);
						entry.selected = false;
						if (entry.bounds != null && entry.bounds.contains(e.x, e.y)) {
							System.out.println("Selected entry at "+entry.index);
							dialog.albumEntrySelected(entry);
							entry.selected = true;
							found = true;
						}
					}
				}
				if (!found)
					dialog.albumEntrySelected(null);
				redraw();
			}
		});
	}
	public void dispose() {
		super.dispose();
		normal.dispose();
		selected.dispose();
	}
	private void paint(GC gc) {
		int idx = startRow*fitCols;
		int maxIdx = TracController.getGoogleImageAlbum().size();
		if (rows > 0 && cols > 0) {
			int y = 0;
			for (int row = startRow; row<=(startRow+fitRows)-1; row++) {
				int x = 0;
				for (int col = startCol; idx<maxIdx && col<=(startCol+fitCols)-1; col++) {
					AlbumEntry entry = TracController.getGoogleImageAlbum().getAlbumEntryAt(idx++);
					Image image = entry.getImage();
					entry.bounds = new Rectangle(x,y,width+(border*2),height+(border*2));
					try {
						gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, x+border, y+border, width, height);
					} catch (Exception e) {
						gc.setForeground(selected);
						gc.drawLine(0, 0, width, height);
						gc.drawLine(0, height, width, 0);
					}
					if (entry.selected) {
						gc.setForeground(selected);
						Rectangle inner = new Rectangle(entry.bounds.x+1,entry.bounds.y+1,entry.bounds.width-2,entry.bounds.height-2);
						gc.drawRectangle(inner);
					}
					gc.setForeground(normal);
					gc.drawRectangle(entry.bounds);
					x += (width+(border*2));
				}
				y += (height+(border*2));
			}
		}
	}
}
