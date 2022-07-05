package com.ikno.itracclient.views;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IEntityChangeListener;
import com.ikno.dao.ITextMessage;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.Faction;
import com.ikno.dao.business.MOTextMessage;
import com.ikno.dao.business.MTTextMessage;
import com.ikno.dao.business.PointIncident;
import com.ikno.dao.business.SimpleStatus;
import com.ikno.dao.business.Unit;
import com.ikno.dao.business.User;
import com.ikno.dao.business.Asset.AssetWrapper;
import com.ikno.dao.business.Faction.FactionWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.TracController;
import com.ikno.itracclient.actions.EditFaction;
import com.ikno.itracclient.dialogs.AssetMessageDialog;
import com.ikno.itracclient.dialogs.PrintDialog;
import com.ikno.itracclient.utils.Formatting;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class AssetStatusView extends ViewPart implements IEntityChangeListener, ISelectionListener {
	public AssetStatusView() {
	}
	private Canvas unreadImage;
	private Text unprocOutboxCount;
	private Text unreadInboxCount;
	private Button reportButton;
	private Canvas canvas;
	private static final Logger logger = Logging.getLogger(AssetStatusView.class.getName());
	public static final String ID = "com.ikno.itracclient.views.AssetStatusView"; //$NON-NLS-1$

	private AssetMessageDialog messageDialog;
	private PrintDialog printDialog;
	private Button editAssetButton;
	private Button zoomToButton;
	private Text status;
	private List<MOTextMessage> inboxMessages;
	private List<MTTextMessage> outboxMessages;
	private List<AssetWrapper> currentAssets = null;
	private Integer[] inboxColumnWidths = null;
	private Integer[] outboxColumnWidths = null;

	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return historyDeltas;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private ComboViewer historyDeltaList;
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return element.toString();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return ((HistoryDelta)e1).compareTo((HistoryDelta)e2);
		}
	}
	private Combo historyCombo;
	private class HistoryDelta implements Comparable {
		public int deltaMinutes;
		public String display;
		public HistoryDelta(String display,int deltaMinutes) {
			this.display = display;
			this.deltaMinutes = deltaMinutes;
		}
		public void showIncidents(Asset asset,Calendar relativeTo) {
			IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
			if (relativeTo == null)
				relativeTo = Calendar.getInstance();
			List<PointIncident> incidents = null;
			if (deltaMinutes == 0) {
				incidents = Arrays.asList(asset.getLastPointIncident());
			} else {
				Calendar from = (Calendar)relativeTo.clone();
				from.add(Calendar.MINUTE, -deltaMinutes);
				Calendar to = (Calendar)relativeTo.clone();
				incidents = DAO.localDAO().historicalPointIncidents(asset, from, to);
			}
			PointIncident[] history = null;
			if (incidents != null && incidents.size() > 0)
				history = incidents.toArray(new PointIncident[]{});
			mappingView.AddAssetWithHistory(asset,history,true);
		}
		public int compareTo(Object arg0) {
			if (deltaMinutes < ((HistoryDelta)arg0).deltaMinutes)
				return -1;
			if (deltaMinutes > ((HistoryDelta)arg0).deltaMinutes)
				return 1;
			return 0;
		}
		public String toString() {
			return display;
		}
	}
	private HistoryDelta[] historyDeltas = new HistoryDelta[] {
		new HistoryDelta("Last Reported Position",0),
		new HistoryDelta("Previous 30 Minutes",30),
		new HistoryDelta("Previous 2 Hours",2*60),
		new HistoryDelta("Previous 6 Hours",6*60),
		new HistoryDelta("Previous 12 Hours",12*60),
		new HistoryDelta("Previous 24 Hours",24*60),
		new HistoryDelta("Previous 2 Days",2*24*60),
		new HistoryDelta("Previous 7 Days",7*24*60)
	};
	private Text incident;
	private EditFaction editFaction = null;
	private PropertyDialogAction propertyDialogAction = null;
	private Text heading;
	private Text altitude;
	private Label speedIndicator;
	private Text speed;
	private Button viewOnMapButton;
	private ComboViewer assetList;
	private ComboViewer contractListViewer;
	private Text location;
	private Text recorded;
	class AssetSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null || e2 == null)
				return 0;
			String n1 = ((AssetWrapper)e1).getAssetName();
			String n2 = ((AssetWrapper)e2).getAssetName();
			return n1.compareTo(n2);
		}
	}
	class AssetListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((AssetWrapper)element).getAssetName();
		}
		public Image getImage(Object element) {
			return null;
		}
	}
	class AssetListContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement != null)
				return ((List<AssetWrapper>)inputElement).toArray();
			return new Object[]{};
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class ClientSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 == null || e2 == null)
				return 0;
			String n1 = ((Client)e1).getClientName();
			String n2 = ((Client)e2).getClientName();
			return n1.compareTo(n2);
		}
	}
	class ClientListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element == null)
				return "";
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
			return ((List<Client>)inputElement).toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public class InfoBarPainter implements PaintListener {
		private Image background = null;
		private Font font = null;
		private Color foreground = null;
		
		public InfoBarPainter() {
			background = ResourceManager.getPluginImage(Activator.getDefault(), "images/infobar.jpg");
			font = new Font(Display.getCurrent(),"DS-Digital Bold Italic",18,SWT.BOLD|SWT.ITALIC);
//			foreground = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
//			foreground = SWTResourceManager.getColor(new RGB(82,198,255));
			foreground = SWTResourceManager.getColor(new RGB(0,74,111));
		}
		public void paintControl(PaintEvent e) {
//			e.gc.drawImage(background, 0, 0);
			String dateTime = Formatting.format(Calendar.getInstance().getTime(),formatter);
			e.gc.setFont(font);
			e.gc.setForeground(foreground);
			Rectangle area = canvas.getClientArea();
			Point size = e.gc.stringExtent(dateTime);
			int x = (area.width/2)-(size.x/2);
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
						AssetStatusView.this.schedule();
					} catch (Exception t) {}
				}
			});
		}
	}
	
	public void setShowSeconds(boolean showSeconds) {
		if (showSeconds == true) {
			timeByDiff = false;
			interval = 1;
			formatter = "EEE, dd MMM yyyy HH:mm:ss z";
		} else {
			timeByDiff = true;
			interval = 60;
			formatter = "EEE, dd MMM yyyy HH:mm z";
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
	private Combo assetCombo;
	private Combo clientList;
	private boolean timeByDiff;
	private int interval;
	private String formatter;
	private Timer timer = null;
	private Button btnTextMessage;

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(SWTResourceManager.getColor(149, 168, 179));
		container.setLayout(new FormLayout());
		
		canvas = new Canvas(container, SWT.NONE);
		FormData fd_canvas = new FormData();
		fd_canvas.bottom = new FormAttachment(0, 35);
		fd_canvas.right = new FormAttachment(100, -12);
		fd_canvas.top = new FormAttachment(0, 3);
		canvas.setLayoutData(fd_canvas);
//		canvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		canvas.setBackground(SWTResourceManager.getColor(149, 168, 179));
		canvas.addPaintListener(new InfoBarPainter());
		this.setShowSeconds(true);

		final Label groupLabel = new Label(container, SWT.NONE);
		FormData fd_groupLabel = new FormData();
		fd_groupLabel.bottom = new FormAttachment(0, 65);
		fd_groupLabel.right = new FormAttachment(0, 69);
		fd_groupLabel.top = new FormAttachment(0, 41);
		fd_groupLabel.left = new FormAttachment(0);
		groupLabel.setLayoutData(fd_groupLabel);
		groupLabel.setAlignment(SWT.RIGHT);
//		groupLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		groupLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		groupLabel.setText("Contract");

		contractListViewer = new ComboViewer(container, SWT.NONE);
		contractListViewer.setSorter(new ClientSorter());
		contractListViewer.setLabelProvider(new ClientListLabelProvider());
		contractListViewer.setContentProvider(new ClientListContentProvider());
		final User user = TracController.getLoggedIn();
		List<Client> clients = user.getClients();
		if (clients == null)
			clients = new ArrayList<Client>();
		contractListViewer.setInput(clients);
		clientList = contractListViewer.getCombo();
		FormData fd_clientList = new FormData();
		fd_clientList.top = new FormAttachment(0, 41);
		fd_clientList.left = new FormAttachment(0, 75);
		clientList.setLayoutData(fd_clientList);
		clientList.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				IStructuredSelection selection = (IStructuredSelection)contractListViewer.getSelection();
				User user = TracController.getLoggedIn();
				if (selection == null || selection.getFirstElement() == null) {
					currentAssets = DAO.localDAO().getAssetWrappersForUser(user,true);
					assetList.setInput(currentAssets);
//					viewOnMapButton.setEnabled(false);
					zoomToButton.setEnabled(false);
					if (user.fullfillsRole(User.Roles.SYSTEMADMIN) ||
						user.fullfillsRole(User.Roles.ASSETADMIN)) {
						editAssetButton.setEnabled(false);
					}
					reportButton.setEnabled(false);
				} else {
					Client client = (Client)selection.getFirstElement();
					currentAssets = DAO.localDAO().getAssetWrappersForClient(user, client);
					assetList.setInput(currentAssets);
//					viewOnMapButton.setEnabled(true);
					clear();
				}
			}
		});
		clientList.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		clientList.setVisibleItemCount(10);

		final Label assetLabel = new Label(container, SWT.NONE);
		fd_canvas.left = new FormAttachment(0);
		FormData fd_assetLabel = new FormData();
		fd_assetLabel.bottom = new FormAttachment(0, 95);
		fd_assetLabel.right = new FormAttachment(0, 70);
		fd_assetLabel.top = new FormAttachment(0, 71);
		fd_assetLabel.left = new FormAttachment(0);
		assetLabel.setLayoutData(fd_assetLabel);
//		assetLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		assetLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		assetLabel.setAlignment(SWT.RIGHT);
		assetLabel.setText("Asset");

		assetList = new ComboViewer(container, SWT.READ_ONLY);
		assetList.setSorter(new AssetSorter());
		assetList.setLabelProvider(new AssetListLabelProvider());
		assetList.setContentProvider(new AssetListContentProvider());
		assetList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				clear();
				if (selection == null) {
					zoomToButton.setEnabled(false);
					if (user.fullfillsRole(User.Roles.SYSTEMADMIN) ||
						user.fullfillsRole(User.Roles.ASSETADMIN)) {
						editAssetButton.setEnabled(false);
					}
					reportButton.setEnabled(false);
					historyDeltaList.getCombo().setEnabled(false);
					btnTextMessage.setVisible(false);
					btnTextMessage.setEnabled(false);
				} else {
					zoomToButton.setEnabled(true);
					if (user.fullfillsRole(User.Roles.SYSTEMADMIN) ||
						user.fullfillsRole(User.Roles.ASSETADMIN)) {
						editAssetButton.setEnabled(true);
					}
					reportButton.setEnabled(true);
					historyDeltaList.getCombo().setEnabled(true);
					historyDeltaList.setSelection(null);
					AssetWrapper wrapper = (AssetWrapper)selection.getFirstElement();
					if (wrapper == null) {
						// Some contract selected, clear and select the new asset
						if (clientList.getSelectionIndex() != -1) {
							clientList.select(-1);
						}
					} else {
						Asset asset = wrapper.getObject();
						for (Iterator<Unit> ui = asset.getUnits().iterator(); ui.hasNext();) {
							Unit unit = ui.next();
							PointIncident point = (PointIncident)unit.getLastIncident(PointIncident.class.getSimpleName());
							if (point != null) {
								setIncident(point);
								break;
							}
						}
						Calendar yesterday = Calendar.getInstance();
						yesterday.add(Calendar.HOUR, -24);
						SimpleStatus lastStatus = DAO.localDAO().lastSimpleStatusForAssetAfter(asset, yesterday.getTime(), new SimpleStatus.Level[]{SimpleStatus.Level.SEVERE,SimpleStatus.Level.WARNING});
						setIncidentText(lastStatus);
						lastStatus = DAO.localDAO().lastSimpleStatusForAsset(asset, new SimpleStatus.Level[]{SimpleStatus.Level.INFO});
						setStatusText(lastStatus);
						if (asset.textMessageSupported()) {
							btnTextMessage.setVisible(true);
							btnTextMessage.setEnabled(true);
						} else {
							btnTextMessage.setVisible(false);
							btnTextMessage.setEnabled(false);
						}
					}
				}
			}
		});
		currentAssets = DAO.localDAO().getAssetWrappersForUser(user,true);
		assetList.setInput(currentAssets);
		assetCombo = assetList.getCombo();
		FormData fd_assetCombo = new FormData();
		fd_assetCombo.right = new FormAttachment(100, -169);
		fd_assetCombo.top = new FormAttachment(0, 70);
		fd_assetCombo.left = new FormAttachment(0, 75);
		assetCombo.setLayoutData(fd_assetCombo);
		assetCombo.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		assetCombo.setVisibleItemCount(10);

		final Label historyLabel = new Label(container, SWT.NONE);
		FormData fd_historyLabel = new FormData();
		fd_historyLabel.bottom = new FormAttachment(0, 125);
		fd_historyLabel.right = new FormAttachment(0, 69);
		fd_historyLabel.top = new FormAttachment(0, 101);
		fd_historyLabel.left = new FormAttachment(0);
		historyLabel.setLayoutData(fd_historyLabel);
//		historyLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		historyLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		historyLabel.setAlignment(SWT.RIGHT);
		historyLabel.setText("History");

		Label lastMessageLabel;
		lastMessageLabel = new Label(container, SWT.NONE);
		FormData fd_lastMessageLabel = new FormData();
		fd_lastMessageLabel.bottom = new FormAttachment(0, 155);
		fd_lastMessageLabel.top = new FormAttachment(0, 131);
		fd_lastMessageLabel.left = new FormAttachment(0);
		lastMessageLabel.setLayoutData(fd_lastMessageLabel);
//		lastMessageLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		lastMessageLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		lastMessageLabel.setAlignment(SWT.RIGHT);
		lastMessageLabel.setText("Last Message");

		recorded = new Text(container, SWT.BORDER);
		FormData fd_recorded = new FormData();
		fd_recorded.bottom = new FormAttachment(0, 154);
		fd_recorded.right = new FormAttachment(100, -12);
		fd_recorded.top = new FormAttachment(0, 130);
		fd_recorded.left = new FormAttachment(0, 75);
		recorded.setLayoutData(fd_recorded);
		recorded.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.BOLD));
		recorded.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		recorded.setEditable(false);

		Label positionLabel;
		positionLabel = new Label(container, SWT.NONE);
		FormData fd_positionLabel = new FormData();
		fd_positionLabel.right = new FormAttachment(0, 69);
		fd_positionLabel.top = new FormAttachment(0, 160);
		fd_positionLabel.left = new FormAttachment(0);
		positionLabel.setLayoutData(fd_positionLabel);
//		positionLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		positionLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		positionLabel.setAlignment(SWT.RIGHT);
		positionLabel.setText("Position");

		location = new Text(container, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		FormData fd_location = new FormData();
		fd_location.right = new FormAttachment(100, -12);
		fd_location.top = new FormAttachment(0, 160);
		fd_location.left = new FormAttachment(0, 75);
		location.setLayoutData(fd_location);
		location.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.BOLD));
		location.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		location.setEditable(false);

		viewOnMapButton = new Button(container, SWT.NONE);
		FormData fd_viewOnMapButton = new FormData();
		fd_viewOnMapButton.top = new FormAttachment(0, 41);
		fd_viewOnMapButton.right = new FormAttachment(100, -122);
		viewOnMapButton.setLayoutData(fd_viewOnMapButton);
		viewOnMapButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)contractListViewer.getSelection();
				Asset[] assets = null;
				if (selection == null || selection.getFirstElement() == null) {
					User user = TracController.getLoggedIn();
					List<Asset> lassets = DAO.localDAO().getAssetsForUser(user);
					if (lassets != null)
						assets = lassets.toArray(new Asset[]{});
				} else {
					Client client = (Client)selection.getFirstElement();
					assets = client.getAssets().toArray(new Asset[]{}); 
				}
				if (assets != null) {
					Arrays.sort(assets);
					for (Asset asset : assets) {
						IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
						mappingView.AddAssetWithHistory(asset,null,false);
					}
				}
			}
		});
		viewOnMapButton.setText("Show");
		viewOnMapButton.setEnabled(true);

		Label speedLabel;
		speedLabel = new Label(container, SWT.NONE);
		fd_positionLabel.bottom = new FormAttachment(speedLabel, -6);
		FormData fd_speedLabel = new FormData();
		fd_speedLabel.bottom = new FormAttachment(0, 246);
		fd_speedLabel.right = new FormAttachment(0, 70);
		fd_speedLabel.top = new FormAttachment(0, 226);
		fd_speedLabel.left = new FormAttachment(0);
		speedLabel.setLayoutData(fd_speedLabel);
//		speedLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		speedLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		speedLabel.setAlignment(SWT.RIGHT);
		speedLabel.setText("Speed");

		speed = new Text(container, SWT.BORDER);
		FormData fd_speed = new FormData();
		fd_speed.bottom = new FormAttachment(0, 246);
		fd_speed.right = new FormAttachment(0, 115);
		fd_speed.top = new FormAttachment(0, 226);
		fd_speed.left = new FormAttachment(0, 75);
		speed.setLayoutData(fd_speed);
		speed.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		speed.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		speed.setEditable(false);

		speedIndicator = new Label(container, SWT.NONE);
		FormData fd_speedIndicator = new FormData();
		fd_speedIndicator.bottom = new FormAttachment(0, 246);
		fd_speedIndicator.right = new FormAttachment(0, 161);
		fd_speedIndicator.top = new FormAttachment(0, 226);
		fd_speedIndicator.left = new FormAttachment(0, 121);
		speedIndicator.setLayoutData(fd_speedIndicator);
//		speedIndicator.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		speedIndicator.setBackground(SWTResourceManager.getColor(149, 168, 179));

		Label altitudeLabel;
		altitudeLabel = new Label(container, SWT.NONE);
		FormData fd_altitudeLabel = new FormData();
		fd_altitudeLabel.bottom = new FormAttachment(speedIndicator, 20);
		fd_altitudeLabel.top = new FormAttachment(speedIndicator, 0, SWT.TOP);
		fd_altitudeLabel.left = new FormAttachment(speedIndicator, 6);
		altitudeLabel.setLayoutData(fd_altitudeLabel);
//		altitudeLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		altitudeLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		altitudeLabel.setAlignment(SWT.RIGHT);
		altitudeLabel.setText("Altitude");

		altitude = new Text(container, SWT.BORDER);
		FormData fd_altitude = new FormData();
		fd_altitude.bottom = new FormAttachment(altitudeLabel, 20);
		fd_altitude.top = new FormAttachment(altitudeLabel, 0, SWT.TOP);
		fd_altitude.left = new FormAttachment(altitudeLabel, 6);
		altitude.setLayoutData(fd_altitude);
		altitude.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		altitude.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		altitude.setEditable(false);

		Label headingLabel;
		headingLabel = new Label(container, SWT.NONE);
		fd_location.bottom = new FormAttachment(0, 220);
		FormData fd_headingLabel = new FormData();
		fd_headingLabel.bottom = new FormAttachment(0, 246);
		fd_headingLabel.top = new FormAttachment(0, 226);
		headingLabel.setLayoutData(fd_headingLabel);
//		headingLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		headingLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		headingLabel.setAlignment(SWT.RIGHT);
		headingLabel.setText("Heading");

		heading = new Text(container, SWT.BORDER);
		fd_headingLabel.right = new FormAttachment(heading, -6);
		FormData fd_heading = new FormData();
		fd_heading.bottom = new FormAttachment(0, 246);
		fd_heading.right = new FormAttachment(100, -13);
		heading.setLayoutData(fd_heading);
		heading.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		heading.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		heading.setEditable(false);

		final Label statusLabel = new Label(container, SWT.NONE);
		FormData fd_statusLabel = new FormData();
		fd_statusLabel.bottom = new FormAttachment(0, 275);
		fd_statusLabel.right = new FormAttachment(0, 69);
		fd_statusLabel.top = new FormAttachment(0, 251);
		fd_statusLabel.left = new FormAttachment(0);
		statusLabel.setLayoutData(fd_statusLabel);
//		statusLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		statusLabel.setBackground(SWTResourceManager.getColor(149, 168, 179));
		statusLabel.setAlignment(SWT.RIGHT);
		statusLabel.setText("Incident");

		incident = new Text(container, SWT.BORDER);
		fd_heading.top = new FormAttachment(0, 226);
		FormData fd_incident = new FormData();
		fd_incident.bottom = new FormAttachment(0, 274);
		fd_incident.right = new FormAttachment(100, -12);
		fd_incident.top = new FormAttachment(0, 250);
		fd_incident.left = new FormAttachment(0, 75);
		incident.setLayoutData(fd_incident);
		incident.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		incident.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		incident.setEditable(false);

		historyDeltaList = new ComboViewer(container, SWT.READ_ONLY);
		historyDeltaList.setContentProvider(new ContentProvider());
		historyDeltaList.setSorter(new Sorter());
		historyDeltaList.setLabelProvider(new ListLabelProvider());
		historyDeltaList.setInput(new Object());
		historyCombo = historyDeltaList.getCombo();
		fd_clientList.right = new FormAttachment(100, -169);
		FormData fd_historyCombo = new FormData();
		fd_historyCombo.right = new FormAttachment(100, -169);
		fd_historyCombo.top = new FormAttachment(0, 101);
		fd_historyCombo.left = new FormAttachment(0, 75);
		historyCombo.setLayoutData(fd_historyCombo);
		historyCombo.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		historyCombo.setEnabled(false);
		historyCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)assetList.getSelection();
				if (selection != null) {
					Asset asset = ((AssetWrapper)selection.getFirstElement()).getObject();
					selection = (IStructuredSelection) historyDeltaList.getSelection();
					HistoryDelta delta = (HistoryDelta) selection.getFirstElement();
					delta.showIncidents(asset, null);
				}
			}
		});

		Label statusLabel_1;
		statusLabel_1 = new Label(container, SWT.NONE);
		FormData fd_statusLabel_1 = new FormData();
		fd_statusLabel_1.bottom = new FormAttachment(0, 305);
		fd_statusLabel_1.right = new FormAttachment(0, 69);
		fd_statusLabel_1.top = new FormAttachment(0, 281);
		fd_statusLabel_1.left = new FormAttachment(0);
		statusLabel_1.setLayoutData(fd_statusLabel_1);
//		statusLabel_1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		statusLabel_1.setBackground(SWTResourceManager.getColor(149, 168, 179));
		statusLabel_1.setAlignment(SWT.RIGHT);
		statusLabel_1.setText("Status");

		status = new Text(container, SWT.BORDER);
		FormData fd_status = new FormData();
		fd_status.bottom = new FormAttachment(0, 304);
		fd_status.right = new FormAttachment(100, -12);
		fd_status.top = new FormAttachment(0, 280);
		fd_status.left = new FormAttachment(0, 75);
		status.setLayoutData(fd_status);
		status.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		status.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		status.setEditable(false);

		zoomToButton = new Button(container, SWT.NONE);
		FormData fd_zoomToButton = new FormData();
		fd_zoomToButton.right = new FormAttachment(100, -119);
		fd_zoomToButton.top = new FormAttachment(0, 71);
		zoomToButton.setLayoutData(fd_zoomToButton);
		zoomToButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)assetList.getSelection();
				if (selection != null) {
					Asset asset = ((AssetWrapper)selection.getFirstElement()).getObject();
					IMappingView mappingView = (IMappingView)TracController.singleton().getCurrentMappingView();
					mappingView.AddAssetWithHistory(asset,null,true);
				}
			}
		});
		zoomToButton.setEnabled(false);
		zoomToButton.setText("Go To");
		editAssetButton = new Button(container, SWT.NONE);
		FormData fd_editAssetButton = new FormData();
		fd_editAssetButton.right = new FormAttachment(100, -19);
		fd_editAssetButton.top = new FormAttachment(0, 70);
		editAssetButton.setLayoutData(fd_editAssetButton);
		editAssetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				propertyDialogAction.selectionChanged(assetList.getSelection());
				propertyDialogAction.run();
			}
		});
		editAssetButton.setEnabled(false);
		editAssetButton.setText("Edit");
		if (user.fullfillsRole(User.Roles.SYSTEMADMIN) ||
			user.fullfillsRole(User.Roles.ASSETADMIN)) {
			editAssetButton.setEnabled(true);
		}

		reportButton = new Button(container, SWT.NONE);
		fd_editAssetButton.left = new FormAttachment(reportButton, 6);
		FormData fd_reportButton = new FormData();
		fd_reportButton.right = new FormAttachment(100, -66);
		fd_reportButton.top = new FormAttachment(0, 70);
		reportButton.setLayoutData(fd_reportButton);
		reportButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selected = (IStructuredSelection)assetList.getSelection();
				if (selected != null && selected.getFirstElement() != null) {
					if (printDialog == null)
						printDialog = new PrintDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
					printDialog.open(((AssetWrapper)selected.getFirstElement()).getObject());
				}
			}
		});
		reportButton.setEnabled(false);
		reportButton.setText("Report");
		
		btnTextMessage = new Button(container, SWT.NONE);
		btnTextMessage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selected = (IStructuredSelection)assetList.getSelection();
				if (selected != null && selected.getFirstElement() != null) {
					if (messageDialog == null)
						messageDialog = new AssetMessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),(AssetWrapper)selected.getFirstElement());
					else
						messageDialog.setAsset((AssetWrapper)selected.getFirstElement());
					if (messageDialog.isOpen)
						messageDialog.getParent().setFocus();
					else
						messageDialog.open();
				}
			}
		});
		// $hide<<$
		btnTextMessage.setVisible(false);
		btnTextMessage.setEnabled(false);
		// $hide>>$
		FormData fd_btnTextMessage = new FormData();
		fd_btnTextMessage.top = new FormAttachment(status, 6);
		fd_btnTextMessage.left = new FormAttachment(statusLabel_1, 6);
		btnTextMessage.setLayoutData(fd_btnTextMessage);
		btnTextMessage.setText("Text Message");

		//
		createActions();
		initializeToolBar();
		initializeMenu();
		
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE,
				EntityNotification.Type.DELETE
				};
		logger.finest("adding as EntityChangeListener");
		TracController.singleton().addEntityChangeListener(this, interest);
		TracController.singleton().addMappingSelectListener(this);
		this.getViewSite().getPage().addSelectionListener(ConsoleView.ID, (ISelectionListener)this);
		this.getViewSite().getPage().addSelectionListener(IncidentView.ID, (ISelectionListener)this);
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		editFaction = new EditFaction();
		propertyDialogAction = new PropertyDialogAction(this.getSite(),assetList);
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	public void setIncident(PointIncident point) {
		if (point != null) {
			recorded.setText(Formatting.format(point.getTimestamp(),"yyyy/MM/dd HH:mm:ss z"));
			heading.setText(String.format("%d",point.getCourse()));
			speed.setText(String.format("%.0f", point.getAssetSpeed()));
			speedIndicator.setText(point.getAssetSpeedIndicator());
			altitude.setText(String.format("%.0f", point.getAltitude()));
			location.setText(point.getLocation());
		}
	}
	public void setIncidentText(SimpleStatus simpleStatus) {
		String statusStr = "OK";
		if (simpleStatus != null) {
			statusStr = String.format("%s @ %s", simpleStatus.getFullDescription(), Formatting.format(simpleStatus.getTimestamp(),"yyyy/MM/dd HH:mm:ss z"));
			if (simpleStatus.getLevel() == SimpleStatus.Level.SEVERE)
				incident.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			else
				incident.setForeground(SWTResourceManager.getColor(253, 192, 66));
			incident.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.BOLD));
		} else {
			incident.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
			incident.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.BOLD));
		}
		incident.setText(statusStr);
	}
	public void setStatusText(SimpleStatus simpleStatus) {
		String statusStr = "OK";
		if (simpleStatus != null) {
			statusStr = String.format("%s @ %s", simpleStatus.getFullDescription(), Formatting.format(simpleStatus.getTimestamp(),"yyyy/MM/dd HH:mm:ss z"));
		}
		status.setText(statusStr);
		
	}
	public void clear() {
		heading.setText("");
		speed.setText("");
		speedIndicator.setText("");
		altitude.setText("");
		recorded.setText("");
		location.setText("");
		status.setText("");
		incident.setText("");
	}
	
	public void dispose() {
		TracController.singleton().removeEntityChangeListener(this);
		timer.cancel();
		super.dispose();
	}
	public void onEntityNotFound(EntityNotification notification) {
	}
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		long objectId = notification.getObjectId();
		User user = TracController.getLoggedIn();
		if (PersistantObject.instanceOf(entityName,MOTextMessage.class)) {
			unreadInboxCount.setText(String.valueOf(DAO.localDAO().getMOTextMessageCountForUser(user, ITextMessage.Status.UNREAD)));
		} else if (PersistantObject.instanceOf(entityName,MTTextMessage.class)) {
			unprocOutboxCount.setText(String.valueOf(DAO.localDAO().getMTTextMessageCountForUser(user, ITextMessage.Status.BUSY)));
		} else {
			IStructuredSelection ssel = (IStructuredSelection)assetList.getSelection();
			AssetWrapper selected = (AssetWrapper)ssel.getFirstElement();
			if (selected != null) {
				long selectedId = selected.getId();
				boolean isSelected = false;
				if (selectedId == objectId) {
					isSelected = true;
					try {
						selected.refreshObject();
					} catch (Exception e) {
						logger.severe("Error refreshing resolved AssetWrapper object with ID "+selected.getId());
					}
				} else if (PersistantObject.instanceOf(entityName,Unit.class)) {
					if (notification.getLongAttribute("assetId") == selectedId)
						isSelected = true;
				} else if (PersistantObject.instanceOf(entityName,Faction.class)) {
					IStructuredSelection fsel = (IStructuredSelection)contractListViewer.getSelection();
					List<FactionWrapper> factions = DAO.localDAO().getFactionWrappersForUser(user.getId());
					if (factions == null)
						factions = new ArrayList<FactionWrapper>();
					contractListViewer.setInput(factions);
					contractListViewer.setSelection(fsel);
					isSelected = true;
				} else if (PersistantObject.instanceOf(entityName,PointIncident.class)) {
					if (notification.getLongAttribute("assetId") == selectedId) {
						setIncident((PointIncident)notification.getResolved());
					}
				} else if (PersistantObject.instanceOf(entityName,SimpleStatus.class)) {
					if (notification.getLongAttribute("assetId") == selectedId) {
						SimpleStatus simpleStatus = (SimpleStatus)notification.getResolved();
						if (simpleStatus.getLevel() == SimpleStatus.Level.INFO)
							setStatusText(simpleStatus);
						else if (simpleStatus.getLevel() == SimpleStatus.Level.SEVERE || 
								simpleStatus.getLevel() == SimpleStatus.Level.WARNING)
							setIncidentText(simpleStatus);
					}
				}
				if (isSelected) {
					System.out.println("Need to force selection...");
					assetList.refresh(selected);
					assetList.setSelection(ssel);
				} else {
					for (AssetWrapper assetWrapper : currentAssets) {
						if (assetWrapper.getId() == notification.getObjectId()) {
							logger.finest("Previously resolved AssetWrapper found for notification, will refresh");
							try {
								assetWrapper.refreshObject();
							} catch (Exception e) {
								logger.severe("Error refreshing resolved AssetWrapper object with ID "+assetWrapper.getId());
							}
						}
					}
				}
			}
		}
	}
	public AssetWrapper getAssetWrapper() {
		IStructuredSelection ssel = (IStructuredSelection)assetList.getSelection();
		return (AssetWrapper)ssel.getFirstElement();
	}
	public Asset getAsset() {
		AssetWrapper selected = this.getAssetWrapper();
		if (selected != null)
			return selected.getObject();
		return null;
	}
	@Override
	public void setFocus() {
		// Set the focus
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer tableColumnCount = memento.getInteger("inboxTableColumnCount");
			if (tableColumnCount != null) {
				inboxColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("inboxTableColumnWidth_"+idx);
					if (cw != null)
						inboxColumnWidths[idx] = cw; 
				}
			}
			tableColumnCount = memento.getInteger("outboxTableColumnCount");
			if (tableColumnCount != null) {
				outboxColumnWidths = new Integer[tableColumnCount];
				for (int idx = 0;idx < tableColumnCount;idx++) {
					Integer cw = memento.getInteger("outboxTableColumnWidth_"+idx);
					if (cw != null)
						outboxColumnWidths[idx] = cw; 
				}
			}
		}
	}
	public void saveState(IMemento memento) {
		if (memento != null) {
		}
		super.saveState(memento);
	}

	public void changeAssetSelection(final ISelection selection) {
		StructuredSelection ssel = (StructuredSelection)selection;
		Object object = ssel.getFirstElement();
		if (assetList.getCombo().indexOf(((AssetWrapper)object).getAssetName()) == -1) {
			if (clientList.getSelectionIndex() != -1) {
				clientList.deselectAll();
				assetList.setSelection(selection, true);
			}
		} else {
			assetList.setSelection(selection, true);
		}
	}
	public void selectionChanged(IWorkbenchPart part, final ISelection selection) {
		StructuredSelection ssel = (StructuredSelection)selection;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				changeAssetSelection(selection);
			}
		});
	}
}
