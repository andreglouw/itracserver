package com.ikno.itracclient.views.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.User;

public class SelectionDetail extends Composite {

	public class QueryDetail {
		private Client client;
		private Asset asset;
		private Calendar from;
		private Calendar to;
		// Warn if selecting for more than 2 weeks (14 days)
		private long limit = 14*24*60*60;
		public QueryDetail(Client client, Asset asset, Calendar start, int delta, String period) {
			this.client = client;
			this.asset = asset;
			from = (Calendar)start.clone();
			if (period.equals("Seconds") || period.equals("Minutes") || period.equals("Hours") || period.equals("Days") || period.equals("Weeks")) {
				int seconds = delta * 
				(period.equals("Minutes") ? 60 : 
					period.equals("Hours") ? (60*60) : 
						period.equals("Days") ? (24*60*60) : 
							period.equals("Weeks") ? (7*24*60*60) : 1);
				from.add(Calendar.SECOND,seconds*-1);
			} else if (period.equals("Months")) {
				from.add(Calendar.MONTH, delta*-1);
			} else if (period.equals("Years")) {
				from.add(Calendar.YEAR, delta*-1);
			}
			to = start;
		}
		public QueryDetail(Client client, Asset asset, Calendar from, Calendar to) {
			this.client = client;
			this.asset = asset;
			this.from = from;
			this.to = to;
		}
		public boolean isValid() {
			long delta = (to.getTimeInMillis()-from.getTimeInMillis());
			return delta > 0;
		}
		public boolean shouldWarn() {
			long delta = (to.getTimeInMillis()-from.getTimeInMillis())/1000;
			if (delta > limit)
				return true;
			return false;
		}
		public Calendar getFrom() {
			return from;
		}
		public void setFrom(Calendar from) {
			this.from = from;
		}
		public Calendar getTo() {
			return to;
		}
		public void setTo(Calendar to) {
			this.to = to;
		}
		public Client getClient() {
			return client;
		}
		public void setClient(Client client) {
			this.client = client;
		}
		public Asset getAsset() {
			return asset;
		}
		public void setAsset(Asset asset) {
			this.asset = asset;
		}
	}
	public interface IQuerySelection {
		public void selectionChanged(Client client, Asset asset);
		public void queryProcessed(QueryDetail queryDetail);
	}
	
	private ComboViewer assetListViewer;
	private Combo assetList;
	private ComboViewer clientListViewer;
	private Combo clientList;
	private Button deltaButton;
	private Label lastLabel;
	private DateTime toDate;
	private DateTime toTime;
	private DateTime fromDate;
	private DateTime fromTime;
	private Spinner delta;
	private Combo list;
	private IQuerySelection callback = null;
	
	class AssetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Asset)e1).getAssetName().compareTo(((Asset)e2).getAssetName());
		}
	}
	class AssetListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Asset)element).getAssetName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class AssetListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement == null)
				return new Object[]{};
			Client selected = (Client)inputElement;
			List<Asset> assets = new ArrayList<Asset>();
			for (Asset asset : selected.getAssets()) {
				if (asset.isSuspended())
					continue;
				assets.add(asset);
			}
			return assets.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ClientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((Client)e1).getClientName().compareTo(((Client)e2).getClientName());
		}
	}
	class ClientListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Client)element).getClientName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class ClientListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement == null)
				return new Object[]{};
			User selected = (User)inputElement;
			return selected.getClients().toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public SelectionDetail(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormLayout formLayout = new FormLayout();
		setLayout(formLayout);

		final Group deltaTimeGroup = new Group(this, SWT.NONE);
		deltaTimeGroup.setToolTipText("Look at the last xx number of units (Hours, Days etc...)");
		deltaTimeGroup.setText("Delta Time");
		deltaTimeGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData fd_deltaTimeGroup = new FormData();
		fd_deltaTimeGroup.bottom = new FormAttachment(0, 100);
		fd_deltaTimeGroup.top = new FormAttachment(0, 55);
		fd_deltaTimeGroup.right = new FormAttachment(100, -5);
		fd_deltaTimeGroup.left = new FormAttachment(0, 5);
		deltaTimeGroup.setLayoutData(fd_deltaTimeGroup);
		deltaTimeGroup.setLayout(new FormLayout());

		lastLabel = new Label(deltaTimeGroup, SWT.NONE);
		lastLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lastLabel.setAlignment(SWT.RIGHT);
		final FormData fd_lastLabel = new FormData();
		fd_lastLabel.bottom = new FormAttachment(0, 25);
		fd_lastLabel.top = new FormAttachment(0, 5);
		fd_lastLabel.right = new FormAttachment(0, 55);
		fd_lastLabel.left = new FormAttachment(0, 5);
		lastLabel.setLayoutData(fd_lastLabel);
		lastLabel.setText("Previous");

		list = new Combo(deltaTimeGroup, SWT.NONE);
		list.setToolTipText("Use arrow keys to scroll between different units");
		list.setItems(new String[] {"Minutes", "Hours", "Days", "Weeks", "Months"});
		list.select(1);
		final FormData fd_list = new FormData();
		fd_list.left = new FormAttachment(0, 108);
		fd_list.right = new FormAttachment(0, 180);
		list.setLayoutData(fd_list);

		deltaButton = new Button(deltaTimeGroup, SWT.NONE);
		deltaButton.setToolTipText("Submit the query to retrieve incidents contained within the selection");
		deltaButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)clientListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					return;
				}
				Client client = (Client)selection.getFirstElement();
				selection = (IStructuredSelection)assetListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					return;
				}
				Asset asset = (Asset)selection.getFirstElement();
				QueryDetail detail = new QueryDetail(client, asset, Calendar.getInstance(),
						delta.getSelection(),
						list.getItem(list.getSelectionIndex()));
				if (!detail.isValid()) {
					System.out.println("Invalid...");
					return;
				}
				if (!detail.shouldWarn()) {
					System.out.println("Should warn...");
				}
				if (callback != null) {
					callback.queryProcessed(detail);
				}
			}
		});
		final FormData fd_deltaButton = new FormData();
		fd_deltaButton.right = new FormAttachment(0, 235);
		fd_deltaButton.top = new FormAttachment(list, -20, SWT.BOTTOM);
		fd_deltaButton.bottom = new FormAttachment(list, 0, SWT.BOTTOM);
		fd_deltaButton.left = new FormAttachment(0, 185);
		deltaButton.setLayoutData(fd_deltaButton);
		deltaButton.setText("Request");

		delta = new Spinner(deltaTimeGroup, SWT.BORDER);
		delta.setSelection(24);
		delta.setToolTipText("Select number of units (Minutes, Hours, Days etc...)");
		fd_list.bottom = new FormAttachment(delta, 20, SWT.TOP);
		fd_list.top = new FormAttachment(delta, 0, SWT.TOP);
		final FormData fd_delta = new FormData();
		fd_delta.bottom = new FormAttachment(lastLabel, 0, SWT.BOTTOM);
		fd_delta.right = new FormAttachment(0, 105);
		fd_delta.top = new FormAttachment(lastLabel, 0, SWT.TOP);
		fd_delta.left = new FormAttachment(lastLabel, 5, SWT.RIGHT);
		delta.setLayoutData(fd_delta);
		deltaTimeGroup.setTabList(new Control[] {lastLabel, delta, list, deltaButton});

		Group periodGroup;
		periodGroup = new Group(this, SWT.NONE);
		periodGroup.setToolTipText("Look at a specific period");
		periodGroup.setText("Period");
		periodGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		periodGroup.setLayout(null);
		final FormData fd_periodGroup = new FormData();
		fd_periodGroup.bottom = new FormAttachment(100, -21);
		fd_periodGroup.top = new FormAttachment(deltaTimeGroup, 0, SWT.BOTTOM);
		fd_periodGroup.right = new FormAttachment(100, -5);
		fd_periodGroup.left = new FormAttachment(0, 5);
		periodGroup.setLayoutData(fd_periodGroup);

		fromDate = new DateTime(periodGroup, SWT.DROP_DOWN | SWT.CALENDAR | SWT.SHORT);
		fromDate.setBounds(10, 21, 225, 143);
		fromDate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				System.out.println("From changed...");
			}
		});
		fromDate.setToolTipText("Starting from this date");

		fromTime = new DateTime(periodGroup, SWT.TIME);
		fromTime.setBounds(10, 170, 99, 24);
		fromTime.setToolTipText("Starting from this time");

		toDate = new DateTime(periodGroup, SWT.DROP_DOWN | SWT.CALENDAR | SWT.SHORT);
		toDate.setBounds(241, 21, 225, 143);
		toDate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				System.out.println("To changed...");
			}
		});
		toDate.setToolTipText("Ending on this date");

		toTime = new DateTime(periodGroup, SWT.TIME);
		toTime.setBounds(241, 170, 99, 24);
		toTime.setToolTipText("Ending at this time");

		final Button periodButton = new Button(periodGroup, SWT.NONE);
		periodButton.setBounds(412, 169, 54, 25);
		periodButton.setToolTipText("Submit the query to retrieve incidents contained within the selection");
		periodButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				Calendar from = Calendar.getInstance();
				from.set(fromDate.getYear(),fromDate.getMonth(),fromDate.getDay(),fromTime.getHours(),fromTime.getMinutes(),fromTime.getSeconds());
				Calendar to = Calendar.getInstance();
				to.set(toDate.getYear(),toDate.getMonth(),toDate.getDay(),toTime.getHours(),toTime.getMinutes(),toTime.getSeconds());
				IStructuredSelection selection = (IStructuredSelection)clientListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					return;
				}
				Client client = (Client)selection.getFirstElement();
				selection = (IStructuredSelection)assetListViewer.getSelection();
				if (selection == null || selection.getFirstElement() == null) {
					return;
				}
				Asset asset = (Asset)selection.getFirstElement();

				QueryDetail detail = new QueryDetail(client,asset,from,to);
				if (!detail.isValid()) {
					System.out.println("Invalid...");
					return;
				}
				if (detail.shouldWarn()) {
					System.out.println("Should warn...");
				}
				if (callback != null) {
					callback.queryProcessed(detail);
				}
			}
		});
		periodButton.setText("Request");

		clientListViewer = new ComboViewer(this, SWT.None);
		clientListViewer.setSorter(new ClientSorter());
		clientListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				System.out.println("selectionChanged Thread Id:"+Thread.currentThread().getId());
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Client client = (Client)selection.getFirstElement();
				assetListViewer.setInput(client);
				assetList.select(0);
				Asset asset = null;
				selection = (IStructuredSelection)assetListViewer.getSelection();
				if (selection != null || selection.getFirstElement() != null) {
					asset = (Asset)selection.getFirstElement();
				}
				if (callback != null)
					callback.selectionChanged(client, asset);
			}
		});
		final Label clientLabel = new Label(this, SWT.NONE);
		clientLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		clientLabel.setAlignment(SWT.RIGHT);
		final FormData fd_clientLabel = new FormData();
		fd_clientLabel.right = new FormAttachment(0, 50);
		fd_clientLabel.bottom = new FormAttachment(0, 25);
		fd_clientLabel.top = new FormAttachment(0, 5);
		fd_clientLabel.left = new FormAttachment(0, 5);
		clientLabel.setLayoutData(fd_clientLabel);
		clientLabel.setText("Contract");

		clientListViewer.setContentProvider(new ClientListContentProvider());
		clientListViewer.setInput(null);
		clientListViewer.setLabelProvider(new ClientListLabelProvider());
		clientList = clientListViewer.getCombo();
		clientList.setToolTipText("Select the client");
		FormData fd_clientCombo;
		fd_clientCombo = new FormData();
		fd_clientCombo.right = new FormAttachment(deltaTimeGroup, 0, SWT.RIGHT);
		fd_clientCombo.left = new FormAttachment(clientLabel, 5, SWT.RIGHT);
		clientList.setLayoutData(fd_clientCombo);

		Label assetLabel = new Label(this, SWT.NONE);
		assetLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		assetLabel.setAlignment(SWT.RIGHT);
		final FormData fd_assetLabel = new FormData();
		fd_assetLabel.right = new FormAttachment(clientList, -5, SWT.LEFT);
		fd_assetLabel.bottom = new FormAttachment(0, 50);
		fd_assetLabel.top = new FormAttachment(0, 30);
		fd_assetLabel.left = new FormAttachment(0, 5);
		assetLabel.setLayoutData(fd_assetLabel);
		assetLabel.setText("Asset");

		assetListViewer = new ComboViewer(this, SWT.BORDER);
		assetListViewer.setSorter(new AssetSorter());
		assetListViewer.setLabelProvider(new AssetListLabelProvider());
		assetListViewer.setContentProvider(new AssetListContentProvider());
		assetListViewer.setInput(null);
		assetListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Asset asset = (Asset)selection.getFirstElement();
				selection = (IStructuredSelection)clientListViewer.getSelection();
				Client client = null;
				if (selection != null || selection.getFirstElement() != null) {
					client = (Client)selection.getFirstElement();
				}
				if (callback != null)
					callback.selectionChanged(client, asset);
			}
		});
		assetList = assetListViewer.getCombo();
		fd_clientCombo.top = new FormAttachment(assetList, -26, SWT.TOP);
		fd_clientCombo.bottom = new FormAttachment(assetList, -5, SWT.TOP);
		assetList.setToolTipText("Select the asset");
		final FormData fd_assetCombo = new FormData();
		fd_assetCombo.right = new FormAttachment(deltaTimeGroup, 0, SWT.RIGHT);
		fd_assetCombo.top = new FormAttachment(deltaTimeGroup, -26, SWT.TOP);
		fd_assetCombo.bottom = new FormAttachment(deltaTimeGroup, -5, SWT.TOP);
		fd_assetCombo.left = new FormAttachment(clientList, 0, SWT.LEFT);
		assetList.setLayoutData(fd_assetCombo);
		Calendar now = Calendar.getInstance();
		toDate.setDay(now.get(Calendar.DAY_OF_MONTH));
		toDate.setMonth(now.get(Calendar.MONTH));
		toDate.setYear(now.get(Calendar.YEAR));
		toTime.setHours(now.get(Calendar.HOUR_OF_DAY)+1);
		toTime.setMinutes(0);
		toTime.setSeconds(0);
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_YEAR, -1);
		fromDate.setDay(start.get(Calendar.DAY_OF_MONTH));
		fromDate.setMonth(start.get(Calendar.MONTH));
		fromDate.setYear(start.get(Calendar.YEAR));
		fromTime.setHours(start.get(Calendar.HOUR_OF_DAY));
		fromTime.setMinutes(0);
		fromTime.setSeconds(0);
		//
	}

	public void setContext(User user, Asset asset) {
		clientListViewer.setInput(user);
		if (asset != null && clientList.indexOf(asset.getClient().getClientName()) != -1)
			clientList.select(clientList.indexOf(asset.getClient().getClientName()));
		else
			clientList.select(0);
		IStructuredSelection selection = (IStructuredSelection)clientListViewer.getSelection();
		if (selection == null) {
			clientList.select(-1);
		} else {
			Client selected = (Client)selection.getFirstElement();
			assetListViewer.setInput(selected);
			if (asset != null && assetList.indexOf(asset.getAssetName()) != -1)
				assetList.select(assetList.indexOf(asset.getAssetName()));
		}
	}
	
	public void setCallback(IQuerySelection callback) {
		this.callback = callback;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
