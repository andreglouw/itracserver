package com.ikno.itracclient.views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.jface.action.IToolBarManager;

import itracclient.Activator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.ikno.itracclient.resource.ResourceManager;
import com.ikno.itracclient.resource.SWTResourceManager;

public class InfoBarView extends ViewPart {
	public static final String ID = "com.ikno.itracclient.views.InfoBarView";
	private Canvas canvas;
	private Composite parent = null;
	private boolean timeByDiff;
	private int interval;
	private SimpleDateFormat formatter;
	private Timer timer = null;

	public void createPartControl(final Composite parent) {
		this.parent = parent;
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		FormData data = new FormData();
		data.left = new FormAttachment(0,0);
		data.right = new FormAttachment(100,0);
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		canvas = new Canvas(parent,SWT.NONE);
		canvas.setLayoutData(data);
		canvas.addPaintListener(new InfoBarPainter());
		
		parent.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				doResize();
			}
		});
		this.setShowSeconds(true);
		initializeToolBar();
	}

	public void doResize() {
		System.out.println("Size: "+parent.getSize());
	}
	public void setFocus() {
		canvas.setFocus();
	}

	public class InfoBarPainter implements PaintListener {
		private Image background = null;
		private Font font = null;
		private Color foreground = null;
		
		public InfoBarPainter() {
			background = ResourceManager.getPluginImage(Activator.getDefault(), "images/infobar.jpg");
			font = new Font(Display.getCurrent(),"DS-Digital Bold Italic",18,SWT.BOLD|SWT.ITALIC);
			foreground = SWTResourceManager.getColor(new RGB(82,198,255));
		}
		public void paintControl(PaintEvent e) {
			e.gc.drawImage(background, 0, 0);
			String dateTime = formatter.format(Calendar.getInstance().getTime());
			e.gc.setFont(font);
			e.gc.setForeground(foreground);
			Rectangle area = canvas.getClientArea();
			Point size = e.gc.stringExtent(dateTime);
			int x = area.width-10-size.x;
			int y = (area.height/2)-(size.y/2);
			e.gc.drawString(dateTime, x, y, true);
		}
	}

	public class ShowClock extends TimerTask {
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						canvas.redraw();
						InfoBarView.this.schedule();
					} catch (Exception t) {}
				}
			});
		}
	}
	
	public void setShowSeconds(boolean showSeconds) {
		if (showSeconds == true) {
			timeByDiff = false;
			interval = 1;
			formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
		} else {
			timeByDiff = true;
			interval = 60;
			formatter = new SimpleDateFormat("dd MMM yyyy HH:mm");
		}
		this.schedule();
	}

	public void schedule() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timeByDiff) {
			Calendar now = Calendar.getInstance();
			int delay = this.interval-now.get(Calendar.SECOND);
			timer = new Timer(true);
			ShowClock task = new ShowClock();
			timer.schedule(task, delay*1000);
		} else {
			timer = new Timer(true);
			ShowClock task = new ShowClock();
			timer.schedule(task, this.interval*1000);
		}
	}
	
	@Override
	public void dispose() {
		timer.cancel();
		super.dispose();
	}
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}
}
