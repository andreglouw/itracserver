package com.ikno.itracclient.views.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintService;

import javax.swing.ProgressMonitor;
import javax.swing.Timer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PrinterOptions;
import org.jpedal.utils.Messages;

import com.ikno.dao.utils.Logging;

public class Printer {
	private static final Logger logger = Logging.getLogger(Printer.class.getName());

	private int rangeStart = 1;
	private int	rangeEnd = 1;
	/**type of printing - all, odd, even*/
	private int subset = PrinterOptions.ALL_PAGES;
	/**Check to see if Printing cancelled*/
	private boolean wasCancelled = false;
	/**Allow Printing Cancelled to appear once*/
	private boolean messageShown = false;
	private boolean pagesReversed = false;
	/**provide user with visual clue to print progress*/
	private Timer updatePrinterProgress = null;
	private ProgressMonitor status = null;

	public void printPDF(final PdfDecoder decodePDF, final java.awt.Frame frame) {
		boolean printFile = false;
		try {
			//setup print job and objects
			PrinterJob printJob = PrinterJob.getPrinterJob();
			PageFormat pf = printJob.defaultPage();
			
			logger.finer("------------------------");
			logger.finer("Default Page format used="+pf);
			logger.finer("Orientation="+pf.getOrientation());
			logger.finer("Width="+pf.getWidth()+" imageableW="+pf.getImageableWidth());
			logger.finer("Height="+pf.getHeight()+" imageableH="+pf.getImageableHeight());
			logger.finer("------------------------");
			
			/**
			 * default page size
			 */
			Paper paper = new Paper();
			paper.setSize(595, 842);
			paper.setImageableArea(43, 43, 509, 756);
			pf.setPaper(paper);
			//VERY useful for debugging! (shows the imageable
			// area as a green box bordered by a rectangle)
			//decode_pdf.showImageableArea();
			//setup JPS to use JPedal
			printJob.setPageable(decodePDF);
			decodePDF.setPageFormat(pf); 
			
			int p1 = 1, p2 = decodePDF.getPageCount();
			PrintDialog dialog = new PrintDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			dialog.setStartPage(p1);
			dialog.setEndPage(p2);
			
			PrinterData printerData = dialog.open();
			if (printerData != null) {
				printFile = true;
				if (printerData.scope == PrinterData.PAGE_RANGE) {
					p1 = printerData.startPage;
					p2 = printerData.endPage;
				}
				if (p2 > decodePDF.getPageCount())
					p2 = decodePDF.getPageCount();
				if (p1 < p2) {
					rangeStart = p1;
					rangeEnd = p2;
				} else {
					rangeStart = p2;
					rangeEnd = p1;
				}
				decodePDF.setPagePrintRange(p1,p2);
				/*
				status = new ProgressMonitor(frame,"","",1,100);
				updatePrinterProgress = new Timer(1000,new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						int currentPage = decodePDF.getCurrentPrintPage();
						if (currentPage > 0)
							updatePrinterProgess(decodePDF,currentPage);
						//make sure turned off
						if (currentPage == -1) {
							updatePrinterProgress.stop();
							status.close();
						}        
					}
				});
				updatePrinterProgress.setRepeats(true);
				updatePrinterProgress.start();
				*/
				printJob.print();
			}
		} catch (PrinterException ee) {
			logger.log(Level.SEVERE,"Exception while printing: ",ee);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Exception while printing: ",e);
		} catch (Error err) {
			logger.log(Level.SEVERE,"Exception while printing: ",err);
		}
		
		/**
		 * visual print update progress box
		 */
		if (updatePrinterProgress != null) {
			updatePrinterProgress.stop();
			status.close();
		}
		/**report any or our errors 
		 * (we do it this way rather than via PrinterException as MAC OS X has a nasty bug in PrinterException)
		 */
		if (!printFile && !decodePDF.isPageSuccessful()) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Printing", 
					decodePDF.getPageFailureMessage());
		}
		//redraw to clean up
		decodePDF.invalidate();
		decodePDF.updateUI();
		decodePDF.repaint();
		if (printFile && !wasCancelled) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Printing", 
			"Finished printing");
			decodePDF.resetCurrentPrintPage();
		}
	}
	private String[] getAvailablePrinters() {
		PrintService[] service = PrinterJob.lookupPrintServices();
		int noOfPrinters = service.length;
		
		String[] serviceNames = new String[noOfPrinters];
		
		for (int i=0;i < noOfPrinters;i++)
			serviceNames[i] = service[i].getName();
		return serviceNames;
	}

	/**visual print indicator*/
	private String dots=".";

	private void updatePrinterProgess(PdfDecoder decode_pdf,int currentPage) {
		//Calculate no of pages printing
		int noOfPagesPrinting = (rangeEnd-rangeStart+1);

		//Calculate which page we are currently printing
		int currentPrintingPage = (currentPage-rangeStart);

		int actualCount = noOfPagesPrinting;
		int actualPage = currentPrintingPage;
		int actualPercentage = (int)(((float)actualPage/(float)actualCount)*100); 

		if (status.isCanceled()) {
			decode_pdf.stopPrinting();
			updatePrinterProgress.stop();
			status.close();
			wasCancelled = true;
			if (!messageShown) {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Printing", 
						"Printing cancelled");
				messageShown = true;
			}
			return;
		}
		//update visual clue
		dots = dots+'.';
		if (dots.length() > 8)
			dots = ".";
		//allow for backwards
		boolean isBackwards = (currentPrintingPage <= 0);
		if (rangeStart == rangeEnd)
			isBackwards = false;
		if (isBackwards)
			noOfPagesPrinting = (rangeStart-rangeEnd+1);
		int percentage = (int)(((float)currentPrintingPage/(float)noOfPagesPrinting)*100);

		if (!isBackwards && percentage < 1)
			percentage = 1;
		//invert percentage so percentage works correctly
		if (isBackwards) {
			percentage = -percentage;
			currentPrintingPage = -currentPrintingPage;
		}

		if (pagesReversed)
			percentage = 100-percentage;

		status.setProgress(percentage);
		String message = "";

		if (subset == PrinterOptions.ODD_PAGES_ONLY) {
			actualCount = ((actualCount/2)+1);
			actualPage = actualPage/2;

		} else if (subset == PrinterOptions.EVEN_PAGES_ONLY) {
			actualCount = ((actualCount/2)+1);
			actualPage = actualPage/2;
		}

		/*
		 * allow for printing 1 page 
		 * Set to page 1 of 1 like Adobe
		 */
		if (actualCount == 1) {
			actualPercentage = 50;
			actualPage = 1;
			status.setProgress(actualPercentage);
		}

		message = actualPage+" "+Messages.getMessage("PdfViewerPrint.Of")+ ' ' +
		actualCount + ": " + actualPercentage + '%' + ' ' +dots;

		if (pagesReversed) {
			message=(actualCount-actualPage) + " "+Messages.getMessage("PdfViewerPrint.Of")+ ' ' +
			actualCount + ": " + percentage + '%' + ' ' +dots;
			status.setNote(Messages.getMessage("PdfViewerPrint.ReversedPrinting")+ ' ' + message);
		} else if (isBackwards)
			status.setNote(Messages.getMessage("PdfViewerPrint.ReversedPrinting")+ ' ' + message);
		else
			status.setNote(Messages.getMessage("PdfViewerPrint.Printing")+ ' ' + message);
	}

	private void setPrinter(PrinterJob printJob, String chosenPrinter) throws PrinterException, PdfException {
		PrintService[] service = PrinterJob.lookupPrintServices(); //list of printers
		boolean matchFound = false;
		int count = service.length;
		for (int i=0;i < count;i++) {
			if (service[i].getName().equals(chosenPrinter)) {
				printJob.setPrintService(service[i]);
				//System.out.println("Set to "+service[i]);
				i = count;
				matchFound = true;
			}
		}
		if (!matchFound)
			throw new PdfException("Unknown printer "+chosenPrinter);
	}
}
