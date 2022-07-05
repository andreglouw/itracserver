package com.ikno.itracclient.mapping.widgets;

import itracclient.Activator;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.resource.ResourceManager;

public class Separator extends Canvas { 
	private static final int IMAGEHEIGHT = 12; 
	private static final int IMAGEWIDTH = 6; 
	public static final int BARWIDTH = 6;
	private IMappingView viewer = null;
	private Image left = ResourceManager.getPluginImage(Activator.getDefault(), "images/leftarrow.png");
	private Rectangle leftrect = null;
	private Image right = ResourceManager.getPluginImage(Activator.getDefault(), "images/rightarrow.png");
	private Rectangle rightrect = null;
	
	public Separator(final IMappingView viewer, Composite parent, int style) {
		super(parent, style);
		this.viewer = viewer;
		this.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {}

			public void mouseDown(MouseEvent e) {
				if (leftrect.contains(e.x,e.y)) {
					viewer.minimizeInfoBar();
				} else if (rightrect.contains(e.x,e.y)) {
					viewer.maximizeInfoBar();
				}
			}

			public void mouseUp(MouseEvent e) {}
		});
		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point size = Separator.this.getSize();
				if (size.x > 0 && size.y > 0) {
					leftrect = new Rectangle(0,(size.y/2)-IMAGEHEIGHT-1,IMAGEWIDTH,IMAGEHEIGHT);
					e.gc.drawImage(left, leftrect.x, leftrect.y);
					rightrect = new Rectangle(0,(size.y/2)+1,IMAGEWIDTH,IMAGEHEIGHT);
					e.gc.drawImage(right, rightrect.x, rightrect.y);
				}
			}
		});
	}

}
