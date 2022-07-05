package com.ikno.itracclient.mapping.widgets;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ikno.itracclient.IMappingAssetTracker;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.resource.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class InfoBar extends Canvas {
	private static final int IMAGEWIDTH = 60;
	private static final int IMAGEHEIGHT = 22;
	public static final int CANVASWIDTH = 68;
	public static final int BORDERWIDTH = 4;
	
	private List<ActiveObject> activeObjects = new ArrayList<ActiveObject>();
	private IMappingView viewer = null;
	Image button = ResourceManager.getPluginImage(Activator.getDefault(), "images/button.png");
	Image highbutton = ResourceManager.getPluginImage(Activator.getDefault(), "images/highbutton.png");
	Font largeFont = new Font(Display.getCurrent(),"Arial",7,SWT.NONE);
	Font smallFont = new Font(Display.getCurrent(),"Arial",6,SWT.NONE);
	Color foreground = SWTResourceManager.getColor(new RGB(0,0,0));
	public boolean minimized = false;
	
	public class ActiveObject implements Comparable {
		public IMappingAssetTracker assetTrack = null;
		private Point point;
		private Rectangle area;
		public boolean highlight = false;
		public ActiveObject(IMappingAssetTracker assetTrack) {
			this.assetTrack = assetTrack;
		}
		public void redraw(GC gc, Point point) {
			area = new Rectangle(point.x,point.y,IMAGEWIDTH,IMAGEHEIGHT);
			if (highlight)
				gc.drawImage(highbutton, point.x, point.y);
			else
				gc.drawImage(button, point.x, point.y);
			gc.setForeground(foreground);
			String text = assetTrack.getAsset().getAssetName();
			gc.setFont(largeFont);
			Point size = gc.stringExtent(text);
			if (size.x < (IMAGEWIDTH-2)) {
				int x = (area.width/2)-(size.x/2);
				if (x < 1)
					x = 1;
				int y = point.y+(area.height/2)-(size.y/2);
				gc.drawString(text, x, y, true);
			} else {
				int icut = (int)((float)text.length()*((float)(IMAGEWIDTH-2))/((float)size.x));
				String first = text.substring(0,icut);
				String rest = text.substring(icut);
				gc.setFont(smallFont);
				Point firstSize = gc.stringExtent(first);
				int x = (area.width/2)-(firstSize.x/2);
				int y = point.y+(area.height/2)-firstSize.y;
				gc.drawString(first, (x < 1) ? 1 : x, y, true);
				Point restSize = gc.stringExtent(rest);
				x = (area.width/2)-(restSize.x/2);
				y = point.y+(area.height/2)+firstSize.y-restSize.y;
				gc.drawString(rest, (x < 1) ? 1 : x, y, true);
			}
		}
		public boolean contains(int x, int y) {
			return area.contains(new Point(x,y));
		}
		public int compareTo(Object arg0) {
			return assetTrack.getAsset().getAssetName().compareTo(((ActiveObject)arg0).assetTrack.getAsset().getAssetName());
		}
	}
	public InfoBar(final IMappingView viewer, final Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED | SWT.BORDER);
		this.viewer = viewer;
		this.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				for (ActiveObject activeObject : activeObjects) {
					if (activeObject.contains(e.x,e.y)) {
						activeObject.assetTrack.zoomToAsset();
						viewer.setSelectedAsset(activeObject.assetTrack.getAsset());
					}
				}
			}

			public void mouseDown(final MouseEvent e) {
				if (e.button == 3) {
					boolean handled = false;
					for (final ActiveObject activeObject : activeObjects) {
						if (activeObject.contains(e.x,e.y)) {
							handled = true;
            				Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
            				try {
            					MenuItem item = new MenuItem(menu, SWT.PUSH);
            					item.setText("Clear History");
            					item.addListener(SWT.Selection, new Listener() {
            						public void handleEvent(Event e) {
            							activeObject.assetTrack.clearHistory();
            						}
            					});
            					item = new MenuItem(menu, SWT.PUSH);
            					if (activeObject.assetTrack.isFollow())
            						item.setText("Follow Off");
            					else
            						item.setText("Follow Me");
            					item.addListener(SWT.Selection, new Listener() {
            						public void handleEvent(Event e) {
            							toggleFollowAssetTrack(activeObject.assetTrack);
            						}
            					});
            					item = new MenuItem(menu, SWT.PUSH);
            					if (activeObject.assetTrack.isShowHistory())
            						item.setText("Line Off");
            					else
            						item.setText("Line On");
            					item.addListener(SWT.Selection, new Listener() {
            						public void handleEvent(Event e) {
            							activeObject.assetTrack.setShowHistory(!activeObject.assetTrack.isShowHistory());
            						}
            					});
            					item = new MenuItem(menu, SWT.PUSH);
            					if (activeObject.assetTrack.isShowLimitedHistory())
            						item.setText("Full History");
            					else
            						item.setText("Limited History");
            					item.addListener(SWT.Selection, new Listener() {
            						public void handleEvent(Event e) {
            							activeObject.assetTrack.setShowLimitedHistory(!activeObject.assetTrack.isShowLimitedHistory());
            						}
            					});
            					item = new MenuItem(menu, SWT.PUSH);
            					item.setText("Zoom To");
            					item.addListener(SWT.Selection, new Listener() {
            						public void handleEvent(Event e) {
            							activeObject.assetTrack.zoomToAsset();
            						}
            					});
            					item = new MenuItem(menu, SWT.PUSH);
            					item.setText("Remove");
            					item.addListener(SWT.Selection, new Listener() {
            						public void handleEvent(Event e) {
            							viewer.removeAssetTrack(activeObject.assetTrack);
            						}
            					});
            					Point clicked = e.display.getCursorLocation();
            					menu.setLocation(clicked.x,clicked.y);
            					menu.setVisible(true);
            					while (!menu.isDisposed() && menu.isVisible()) {
            						if (!e.display.readAndDispatch())
            							e.display.sleep();
            					}
            				} catch (Exception exc) {
            					System.out.println("Caught: "+exc);
            				} finally {
            					menu.dispose();
            				}
						}
						
					}
					if (!handled && activeObjects.size() > 0) {
        				Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        				try {
        					MenuItem item = new MenuItem(menu, SWT.PUSH);
        					item.setText("Remove All");
        					item.addListener(SWT.Selection, new Listener() {
        						public void handleEvent(Event e) {
        							ActiveObject[] objects = activeObjects.toArray(new ActiveObject[]{});
        							for (ActiveObject activeObject : objects)
        								viewer.removeAssetTrack(activeObject.assetTrack);
        						}
        					});
        					Point clicked = e.display.getCursorLocation();
        					menu.setLocation(clicked.x,clicked.y);
        					menu.setVisible(true);
        					while (!menu.isDisposed() && menu.isVisible()) {
        						if (!e.display.readAndDispatch())
        							e.display.sleep();
        					}
        				} catch (Exception exc) {
        					System.out.println("Caught: "+exc);
        				} finally {
        					menu.dispose();
        				}
					}
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});
		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point start = new Point((CANVASWIDTH/2)-(IMAGEWIDTH/2)-(BORDERWIDTH/2),1);
				for (ActiveObject activeObject : activeObjects) {
					activeObject.redraw(e.gc,start);
					start.y += (IMAGEHEIGHT+1);
				}
			}
		});
	}
	public void addAssetTrack(IMappingAssetTracker assetTrack) {
		activeObjects.add(new ActiveObject(assetTrack));
		Collections.sort(activeObjects);
		if (minimized == true) {
			viewer.maximizeInfoBar();
		} else
			this.redraw();
	}
	public void removeAssetTrack(IMappingAssetTracker assetTrack) {
		ActiveObject found = null;
		for (ActiveObject activeObject : activeObjects) {
			if (activeObject.assetTrack.equals(assetTrack)) {
				found = activeObject;
				break;
			}
		}
		if (found != null) {
			activeObjects.remove(found);
			this.redraw();
		}
	}
	public void toggleFollowAssetTrack(IMappingAssetTracker assetTrack) {
		for (ActiveObject activeObject : activeObjects) {
			if (activeObject.assetTrack.equals(assetTrack)) {
				if (assetTrack.isFollow() == true) {
					assetTrack.setFollow(false);
					activeObject.highlight = false;
				} else {
					assetTrack.setFollow(true);
					activeObject.highlight = true;
				}
			} else {
				activeObject.assetTrack.setFollow(false);
				activeObject.highlight = false;
			}
		}
		this.redraw();
	}
	public void switchOnFollowAssetTrack(IMappingAssetTracker assetTrack) {
		assetTrack.setFollow(true);
		for (ActiveObject activeObject : activeObjects) {
			if (activeObject.assetTrack.equals(assetTrack)) {
				activeObject.highlight = true;
			} else {
				activeObject.assetTrack.setFollow(false);
				activeObject.highlight = false;
			}
		}
		this.redraw();
	}
}

