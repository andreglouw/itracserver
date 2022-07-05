package com.ikno.itracclient;

import itracclient.Activator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.part.ViewPart;

import com.ikno.dao.IEntityChangeListener;
import com.ikno.dao.business.Asset;
import com.ikno.dao.business.Client;
import com.ikno.dao.business.User;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.NotificationCenter;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.notification.Listener.DisconnectListener;
import com.ikno.dao.notification.Listener.NotificationListener;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.falconview.FalconView;
import com.ikno.itracclient.googleearth.GoogleEarth;
import com.ikno.itracclient.googleearth.GoogleImageAlbum;
import com.ikno.itracclient.preferences.PreferenceConstants;
import com.ikno.itracclient.startup.ApplicationWorkbenchWindowAdvisor;
import com.ikno.itracclient.views.AssetView;
import com.ikno.itracclient.views.ConsoleView;
import com.ikno.itracclient.views.ReportViewer;
import com.ikno.itracclient.worldwind.ActiveWorldWindView;
import com.ikno.dao.notification.EntityNotification.NotificationFilter;

public class TracController implements ITracController, NotificationListener, DisconnectListener {
	private static final Logger logger = Logging.getLogger(TracController.class.getName());
	private static Preferences preferenceStore = Activator.getDefault().getPluginPreferences();

	private static TracController _controller = null;
	private User loggedIn = null;
	private IWorkbenchConfigurer configurer;
	private Map<NotificationFilter,List<IEntityChangeListener>> changeListeners = Collections.synchronizedMap(new HashMap<NotificationFilter,List<IEntityChangeListener>>());
	private List<ITrackListener> trackListeners = new ArrayList<ITrackListener>();
	private IMappingView currentMappingView = null;
	private List<IMappingView> mappingViews = new ArrayList<IMappingView>();
	private List<ViewPart> mappingListeners = new ArrayList<ViewPart>();
	private ObjectRefresher nextTask = null;
	private Timer timer = null;
	private static int lastActiveMap = 0;
	public static int activeMapLimit = com.ikno.dao.utils.Configuration.configCenter().getInteger("com.ikno.activemaplimit",1);
	private static GoogleImageAlbum googleImageAlbum = null;
	
	public static TracController singleton() {
		if (_controller == null) {
			_controller = new TracController();
		}
		return _controller;
	}
	public TracController() {}
	public static void postStartup() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			page.showView(ConsoleView.ID,null,IWorkbenchPage.VIEW_CREATE);
		} catch (PartInitException e) {
			System.out.println("Error initialising Console view...");
		}
		try {
			page.showView(ReportViewer.ID,null,IWorkbenchPage.VIEW_CREATE);
		} catch (PartInitException e) {
			System.out.println("Error initialising Report Viewer...");
		}
		googleImageAlbum = new GoogleImageAlbum();
	}
	public static GoogleImageAlbum getGoogleImageAlbum() {
		return googleImageAlbum;
	}
	public static void shutdown() {
		googleImageAlbum.dispose();
		singleton()._shutdown();
	}
	public static void setLoggedIn(User loggedIn) {
		singleton()._setLoggedIn(loggedIn);
	}
	public static User getLoggedIn() {
		return singleton()._getLoggedIn();
	}
	public static void setConfigurer(IWorkbenchConfigurer configurer) {
		singleton()._setConfigurer(configurer);
	}
	public static IWorkbenchConfigurer getConfigurer() {
		return singleton()._getConfigurer();
	}
	public static List<Client> getClients() {
		return singleton()._getClients();
	}
	public static String nextActiveMapId() {
		if (lastActiveMap < activeMapLimit)
			return "ActiveMapView_"+(++lastActiveMap);
		return null;
	}
	public static Color pinColor(Asset asset) {
		String pinRGB = asset.getPinRGBValue();
		String[] tokens = null;
		if (pinRGB == null) {
			Preferences preferenceStore = Activator.getDefault().getPluginPreferences();
			pinRGB = preferenceStore.getString(String.format("%d_PIN_COLOR", asset.getId()));
			if (pinRGB == null || pinRGB.length() == 0) {
				pinRGB = preferenceStore.getDefaultString(PreferenceConstants.PIN_COLOR);
			}
		}
		tokens = pinRGB.split(",");
		if (tokens != null) {
			RGB rgb = new RGB(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]));
			return new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
		}
		return null;
	}
	public static Color trackColor(Asset asset) {
		String pinRGB = asset.getTrackRGBValue();
		String[] tokens = null;
		if (pinRGB == null) {
			Preferences preferenceStore = Activator.getDefault().getPluginPreferences();
			pinRGB = preferenceStore.getString(String.format("%d_TRACK_COLOR", asset.getId()));
			if (pinRGB == null || pinRGB.length() == 0) {
				pinRGB = preferenceStore.getDefaultString(PreferenceConstants.TRACK_COLOR);
			}
		}
		tokens = pinRGB.split(",");
		if (tokens != null) {
			RGB rgb = new RGB(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]));
			return new Color(PlatformUI.getWorkbench().getDisplay(),rgb);
		}
		return null;
	}
	public void _setLoggedIn(User loggedIn) {
		this.loggedIn = loggedIn;
	}
	public User _getLoggedIn() {
		return loggedIn;
	}
	public void _setConfigurer(IWorkbenchConfigurer configurer) {
		this.configurer = configurer;
	}
	public IWorkbenchConfigurer _getConfigurer() {
		return configurer;
	}
	public List<Client> _getClients() {
		return this.loggedIn.getClients();
	}
	public void _shutdown() {
		if (this.timer != null) {
			this.timer.cancel();
		}
	}
	private Boolean userNotifiedOfNoAdd = false;
	
	public class AddEntityChangeListener implements Runnable {
		private IEntityChangeListener listener = null;
		private Type[] interest = null;
		private String entityName = null;
		private Map<String,Object> keyAttributes = null;
		public AddEntityChangeListener(IEntityChangeListener listener, Type[] interest, String entityName, Map<String,Object> keyAttributes) {
			this.listener = listener;
			this.interest = interest;
			this.entityName = entityName;
			this.keyAttributes = keyAttributes;
		}
		public void run() {
			NotificationFilter filter = new NotificationFilter(loggedIn, entityName,interest,keyAttributes);
			boolean mustRegister = false;
			synchronized(changeListeners) {
				List<IEntityChangeListener> listeners = changeListeners.get(filter);
				if (listeners == null) {
					logger.finest("Adding new Entity notification listener: "+listener+", on entity: "+entityName);
					listeners = new ArrayList<IEntityChangeListener>();
					changeListeners.put(filter, listeners);
					// First time for this type, must register with NotificationCenter
					mustRegister = true;
				}
				if (!listeners.contains(listener))
					listeners.add(listener);
			}
			if (mustRegister) {
				try {
					logger.finest("Registering listener with NotificationCenter");
					NotificationCenter.addEntityListener(entityName,filter.getQualifier(),
							TracController.this,TracController.this);
					synchronized (userNotifiedOfNoAdd) {
						userNotifiedOfNoAdd = false;
					}
				} catch (Exception e) {
					synchronized (userNotifiedOfNoAdd) {
						if (userNotifiedOfNoAdd)
							return;
					}
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						public void run() {
							synchronized (userNotifiedOfNoAdd) {
								userNotifiedOfNoAdd = true;
							}
							logger.info("Notifying user of connection error...");
							MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
									"Connection Error", 
									"Could not connect to Server, auto notifications will be disabled.\n" +
							"Please contact System Administrator.");
						}
					});
				}
			}
		}
		
	}
	public void addEntityChangeListener(IEntityChangeListener listener, Type[] interest, String entityName, Map<String,Object> keyAttributes) {
		AddEntityChangeListener runnable = new AddEntityChangeListener(listener, interest, entityName, keyAttributes);
		(new Thread(runnable)).start();
	}
	public void addEntityChangeListener(IEntityChangeListener listener, Type[] interest) {
		addEntityChangeListener(listener,interest,"*",null);
	}
	public void addEntityChangeListener(IEntityChangeListener listener, Type[] interest, Map<String,Object> keyAttributes) {
		addEntityChangeListener(listener,interest,"*",keyAttributes);
	}
	public void removeEntityChangeListener(IEntityChangeListener listener) {
		boolean hasListeners = false;
		for (Iterator<Map.Entry<NotificationFilter, List<IEntityChangeListener>>> i = changeListeners.entrySet().iterator();i.hasNext();) {
			List<IEntityChangeListener> listeners = i.next().getValue();
			listeners.remove(listener);
			if (listeners.size() > 0)
				hasListeners = true;
		}
		if (hasListeners == false)
			NotificationCenter.removeEntityListener(this);
	}
	public class ObjectRefresher extends TimerTask {
		private List<EntityNotification> notifications = new ArrayList<EntityNotification>();
		private Boolean running = false;
		private long threadId;
		
		public void run() {
			threadId = Thread.currentThread().getId();
			logger.finer("run() synchronize on running...");
			synchronized(running) {
				running = true;
			}
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					for (Iterator<EntityNotification> mi = notifications.iterator();mi.hasNext();) {
						EntityNotification notification = mi.next();
						if (notification.forcesUserUpdate()) {
							try {
								DAO.localDAO().refresh(TracController.this.loggedIn);
							} catch (Exception e) {
								logger.log(Level.SEVERE,"Error refreshing logged in user", e);
							}
							break;
						}
					}
					Map<EntityNotification,List<IEntityChangeListener>> notified = new HashMap<EntityNotification,List<IEntityChangeListener>>();
					for (Iterator<EntityNotification> mi = notifications.iterator();mi.hasNext();) {
						EntityNotification notification = mi.next();
						List<IEntityChangeListener> listeners = notified.get(notification);
						if (listeners == null) {
							listeners = new ArrayList<IEntityChangeListener>();
							notified.put(notification, listeners);
						}
						logger.finest("Trying to match Listener for Entity "+notification.getEntityName());
						boolean notifHandled = false;
						for (Iterator<Map.Entry<NotificationFilter, List<IEntityChangeListener>>> cli = changeListeners.entrySet().iterator();cli.hasNext();) {
							Map.Entry<NotificationFilter, List<IEntityChangeListener>> entry = cli.next();
							if (entry.getKey().notificationFits(notification)) {
								notifHandled = true;
								for (Iterator<IEntityChangeListener> ci = entry.getValue().iterator();ci.hasNext();) {
									IEntityChangeListener listener = ci.next();
									if (listeners.contains(listener))
										continue;
									logger.fine("Change notification for entity "+notification.getEntityName()+" fits listener ["+listener+"]");
									try {
										if (notification.getNotificationType() == Type.DELETE)
											listener.onEntityNotFound(notification);
										else
											listener.onEntityChange(notification);
										listeners.add(listener);
									} catch (Exception e) {
										logger.log(Level.SEVERE,"Error informing listener of EntityChange: ",e);
									}
								}
							}
						}
						if (!notifHandled)
							logger.fine("### Notification has not been succesfully handled...");
					}
					logger.fine("Thread "+threadId+", end of notification event...\n\n\n");
				}
			});
		}
		
		public Object modifiedObject(Object object) {
			return object;
		}
		
		public boolean addNotification(EntityNotification notification) {
			logger.finer("addNotification() synchronize on running...");
			synchronized(running) {
				if (running)
					return false;
				if (notifications.contains(notification))
					notifications.remove(notification);
				notifications.add(notification);
			}
			return true;
		}
	}
	private synchronized void scheduleNextTask() {
		logger.fine("Will start building next Notifier task");
		nextTask = new ObjectRefresher();
		this.timer = new Timer(true);
		this.timer.schedule(nextTask, 5000);
	}
	public void onEntityChange(EntityNotification notification) {
		logger.info("Received notification "+notification);
		if (nextTask == null)
			scheduleNextTask();
		if (!nextTask.addNotification(notification)) {
			// Means the last scheduled task is allready running, start a new one
			scheduleNextTask();
			// And schedule this notification for then
			if (!nextTask.addNotification(notification)) {
				logger.severe("Critical ERROR - cannot add notification to new Task");
			}
		}
	}
	
	private Boolean userNotifiedOfDisconnect = false;
	
	public void onDisconnect() {
		synchronized (userNotifiedOfDisconnect) {
			if (userNotifiedOfDisconnect)
				return;
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				synchronized (userNotifiedOfDisconnect) {
					userNotifiedOfDisconnect = true;
				}
				System.out.println("Network connection lost, notifying user...");
				logger.severe("Network connection lost, notifying user...");
				boolean retry = true;
				while (retry) {
					retry = MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Connection Error", 
							"Connection to network lost, do you wish to retry connecting?\n" +
					"If this error persists, please contact System Administrator.");
					if (retry) {
						logger.severe("Network disconnect error, user has opted to retry...");
						try {
							NotificationCenter.reconnectEntityListener(TracController.this);
							logger.severe("Successfull reconnect...");
							DAO.localDAO().reconnectDAO();
							DAO.localDAO().refresh(TracController.this.loggedIn,true);
							AssetView assetView = (AssetView)ApplicationWorkbenchWindowAdvisor.getView(AssetView.ID);
							if (assetView != null)
								assetView.populateView();
							retry = false;
							System.out.println("Finished reconnect, returning to user");
						} catch (Exception e) {
							logger.log(Level.SEVERE,"Error reconnecting: ",e);
						}
					} else
						logger.severe("User has opted not to retry...");
				}
			}
		});
	}
	public void addMappingViewPart(IMappingView mappingView) {
		if (!this.mappingViews.contains(mappingView)) {
			this.mappingViews.add(mappingView);
			for (ViewPart listener : mappingListeners) {
				listener.getViewSite().getPage().addSelectionListener(mappingView.getViewId(), (ISelectionListener)listener);
			}
		}
	}
	public IMappingView getCurrentMappingView() {
		return this.currentMappingView;
	}
	public void setCurrentMappingView(IMappingView currentMappingView) {
		this.currentMappingView = currentMappingView;
	}
	
	public void addMappingSelectListener(ViewPart listener) {
		if (!mappingListeners.contains(listener))
			mappingListeners.add(listener);
		for (IMappingView mappingView : this.mappingViews) {
			listener.getViewSite().getPage().addSelectionListener(mappingView.getViewId(), (ISelectionListener)listener);
		}
		/*
		String partName = listener.getPartName();
		String mapping = System.getProperty("com.ikno.config.mapping",null);
		if (partName.equals(ActiveWorldWindView.class.getName()))
			listener.getViewSite().getPage().addSelectionListener(ActiveWorldWindView.ID, (ISelectionListener)listener);
		else if (partName.equals(GoogleEarth.class.getName()))
			listener.getViewSite().getPage().addSelectionListener(GoogleEarth.ID, (ISelectionListener)listener);
		else if (partName.equals(FalconView.class.getName()))
			listener.getViewSite().getPage().addSelectionListener(FalconView.ID, (ISelectionListener)listener);
		*/
	}
	public void addTrackListener(ITrackListener listener) {
		if (!trackListeners.contains(listener)) {
			trackListeners.add(listener);
			currentMappingView.addTrackListener(listener);
		}
	}
	public void removeTrackListener(ITrackListener listener) {
		trackListeners.remove(listener);
		currentMappingView.removeTrackListener(listener);
	}
}
