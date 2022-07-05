package com.ikno.itracclient.views.widgets;

import itracclient.Activator;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.jpedal.PdfDecoder;

import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.resource.ResourceManager;

public class ReportViewerWidget extends Composite {
	private static final Logger logger = Logging.getLogger(ReportViewerWidget.class.getName());

	private java.awt.Frame awtFrame;
	private JApplet applet;
	private JScrollPane scrollPane;
	private PdfDecoder decodePDF = new PdfDecoder();
	private String fileName;
	private String description;
	private int currentPage = 1;
	private float scaling = 1.5f;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ReportViewerWidget(Composite parent, int style) {
		super(parent, style | SWT.EMBEDDED | SWT.NO_BACKGROUND);
		this.setLayout(new FillLayout());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			decodePDF.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));
			decodePDF.setInset(5,5); 
			awtFrame = SWT_AWT.new_Frame(this);
			applet = new JApplet();
			//add a border and center
			scrollPane = new JScrollPane(decodePDF);
			applet.add(scrollPane);
			awtFrame.add(applet);
			awtFrame.pack();
		} catch (Throwable e) {
			logger.log(Level.SEVERE,"Exception in createPartControl >> PDFViewer",e);
		}
		addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.PAGE_DOWN)
					changePage(+1);
				else if (e.keyCode == SWT.PAGE_UP)
					changePage(-1);
				else if (e.keyCode == SWT.END && e.stateMask == SWT.CONTROL)
					changePage(+999);
				else if (e.keyCode == SWT.HOME && e.stateMask == SWT.CONTROL)
					changePage(-999);
				else if (e.keyCode == SWT.KEYPAD_ADD)
					zoom(1.5f);
				else if (e.keyCode == SWT.KEYPAD_SUBTRACT)
					zoom(2f/3f);
			}
		});
	}

	public void contributeToToolBar(IToolBarManager toolBarManager) {
		Action print = new Action("Print") {
			public void run() {
				new Printer().printPDF(decodePDF,awtFrame);
			}
		};
		print.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Printer 16 n p.png"));
		toolBarManager.add(print);
		Action save = new Action("Save") {
			public void run() {
				if (fileName == null)
					return;
				try {
					FileDialog dlg = new FileDialog(Display.getCurrent().getActiveShell(),SWT.SAVE);
					dlg.setFileName(description);
					dlg.setFilterExtensions(new String[]{"pdf"});
					String saveAs = dlg.open();
					if (saveAs != null) {
						if (!saveAs.endsWith(".pdf"))
							saveAs = saveAs+".pdf";
						FileInputStream istream = new FileInputStream(fileName);
						FileOutputStream ostream = new FileOutputStream(saveAs);
						byte[] inpb = new byte[istream.available()];
						int len = istream.read(inpb);
						ostream.write(inpb);
						istream.close();
						ostream.close();
					}
				} catch (Exception e) {
					
				}
			}
		};
		save.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Save Blue 16 n p8.png"));
		toolBarManager.add(save);
		Action pageForward = new Action("Page Forward") {
			public void run() {
				ReportViewerWidget.this.changePage(1);
			}
		};
		pageForward.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Forward 16 n p8.png"));
		toolBarManager.add(pageForward);
		Action pageBack = new Action("Page Back") {
			public void run() {
				ReportViewerWidget.this.changePage(-1);
			}
		};
		pageBack.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Back 16 n p8.png"));
		toolBarManager.add(pageBack);
		Action zoomIn = new Action("Zoom In") {
			public void run() {
				ReportViewerWidget.this.zoom(1.5f);
			}
		};
		zoomIn.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Zoom In 16 n p8.png"));
		toolBarManager.add(zoomIn);
		Action zoomOut = new Action("Zoom Out") {
			public void run() {
				ReportViewerWidget.this.zoom(2f/3f);
			}
		};
		zoomOut.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(),"images/Stock/16x16/Zoom Out 16 n p8.png"));
		toolBarManager.add(zoomOut);
	}
	public void dispose() {
		decodePDF = null;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
            	awtFrame.dispose();
            }
        });
		super.dispose();
	}
	public boolean setFocus() {
		System.out.println("ImageViewer setFocus() called...");
        EventQueue.invokeLater(new Runnable() {
            public void run() {
            	ReportViewerWidget.this.decodePDF.repaint();
            	ReportViewerWidget.this.awtFrame.requestFocus();
            }
        });
        return super.setFocus();
	}
 	public void repaint() {
 		/*
		decodePDF.invalidate();
		decodePDF.repaint();
		*/
		decodePDF.updateUI();
		applet.validate();
	}
 
	public void openPDF(String fileName, String description) {
		this.fileName = fileName;
		this.description = description;
		currentPage = 1;
		scaling = 1.5f;
		try {
			decodePDF.openPdfFile(fileName);
			decodePDF.setPageParameters(scaling, currentPage);
			decodePDF.decodePage(currentPage);
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closePDF() {
		fileName = null;
		try {
			decodePDF.closePdfFile();
			decodePDF.clearScreen();
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void zoom(float scale) {
		scaling = scaling*scale;
		decodePDF.setPageParameters(scaling, currentPage);
		/*
		decodePDF.updateUI();
		*/
		repaint();
	}

	public void changePage(int pageChange) {
		int newPage = currentPage+pageChange;
		if (newPage <= 0)
			newPage = 1;
		else if (newPage > decodePDF.getPageCount())
			newPage = decodePDF.getPageCount();
		if (newPage != currentPage) {
			currentPage = newPage;
			try {
				decodePDF.setFoundTextArea(null);
				decodePDF.setPageParameters(scaling, currentPage); 
				decodePDF.decodePage(currentPage);
				repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
