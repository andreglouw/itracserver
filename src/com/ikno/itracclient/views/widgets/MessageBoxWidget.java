package com.ikno.itracclient.views.widgets;

import itracclient.Activator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ikno.dao.ITextMessage;
import com.ikno.dao.business.MOTextMessage;
import com.ikno.dao.business.MTTextMessage;
import com.ikno.dao.business.TextMessageResult;
import com.ikno.dao.business.MOTextMessage.MOTextMessageWrapper;
import com.ikno.dao.business.MTTextMessage.MTTextMessageWrapper;
import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.notification.EntityNotification;
import com.ikno.dao.notification.EntityNotification.Type;
import com.ikno.dao.persistance.ObjectWrapper;
import com.ikno.dao.persistance.PersistantObject;
import com.ikno.dao.utils.Logging;
import com.ikno.itracclient.TracController;
import com.ikno.dao.IEntityChangeListener;
import com.ikno.itracclient.utils.Formatting;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class MessageBoxWidget extends Composite implements IEntityChangeListener {
	private static final Logger logger = Logging.getLogger(MessageBoxWidget.class.getName());

	public TableViewer tableViewer;
	private Text textView;
	private Table table;
	private Class textMessageClass;
	private String companyName;
	private String assetName;
	private List<TextMessageResult> boxMessages = new ArrayList<TextMessageResult>();
	class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			Object item1 = e1;
			Object item2 = e2;
			return 0;
		}
	}
	class ContentLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			TextMessageResult message = (TextMessageResult)element;
			switch (columnIndex) {
			case 0: // Status
				return null;
//				return message.getStatus().toString();
			case 1: // Time
				Date timestamp = message.getTimestamp();
				if (timestamp != null)
					return Formatting.format(message.getTimestamp());
				return "";
			case 2: // Asset
				return message.getOriginName();
			case 3: // Asset
				return message.getRecipName();
			case 4: // Subject
				String text = message.getMessage(); 
				return text.substring(0, java.lang.Math.min(20, text.length()));
			case 5: // Recieved
				return (message.isAcknowledged() == true) ? "Yes" : "No";
			}
			return "";
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				TextMessageResult message = (TextMessageResult)element;
				/*
				if (message.getStatus() == ITextMessage.Status.BUSY)
					return ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Upload to Web 16 n p8.png");
				else if (message.getStatus() == ITextMessage.Status.READ)
					return ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Book Open Green 16 n p8.png");
				else if (message.getStatus() == ITextMessage.Status.ERROR)
					return ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Stop Document 16 n p8.png");
				else if (message.getStatus() == ITextMessage.Status.UNREAD)
					return ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/Book Green 16 n p8.png");
				else if (message.getStatus() == ITextMessage.Status.SUCCESS)
					return ResourceManager.getPluginImage(Activator.getDefault(), "images/Stock/16x16/OK 16 n p8.png");
				*/
			}
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (TextMessageResult[])inputElement;
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
	public MessageBoxWidget(Composite parent, int style, Integer[] columnWidths) {
		super(parent, style);
		setLayout(new FillLayout());

		final SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		if (columnWidths == null || columnWidths.length < 6)
			columnWidths = new Integer[]{55,115,115,130,200,40};
		tableViewer = new TableViewer(sashForm, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object[] selected = selection.toArray();
				if (selected != null && selected.length == 1) {
					TextMessageResult wrapper = (TextMessageResult)selection.getFirstElement();
					textView.setText(((TextMessageResult)wrapper).getMessage());
				} else
					textView.setText("");
			}
		});
		tableViewer.setSorter(new Sorter());
		tableViewer.setLabelProvider(new ContentLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		table = tableViewer.getTable();
		table.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn statusColumn = new TableColumn(table, SWT.NONE);
		statusColumn.setText("");
		statusColumn.setAlignment(SWT.CENTER);
		statusColumn.setWidth(columnWidths[0]);

		final TableColumn timeColumn = new TableColumn(table, SWT.NONE);
		timeColumn.setText("Time");
		timeColumn.setWidth(columnWidths[1]);

		final TableColumn assetColumn = new TableColumn(table, SWT.NONE);
		assetColumn.setText("Origin");
		assetColumn.setWidth(columnWidths[2]);

		final TableColumn recipColumn = new TableColumn(table, SWT.NONE);
		recipColumn.setText("Destination");
		recipColumn.setWidth(columnWidths[3]);

		final TableColumn subjectColumn = new TableColumn(table, SWT.NONE);
		subjectColumn.setText("Subject");
		subjectColumn.setWidth(columnWidths[4]);

		final TableColumn ackColumn = new TableColumn(table, SWT.NONE);
		ackColumn.setText("Ack");
		ackColumn.setWidth(columnWidths[5]);

		textView = new Text(sashForm, SWT.WRAP | SWT.MULTI | SWT.BORDER);
		textView.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		textView.setEditable(false);
		textView.setFont(SWTResourceManager.getFont("Verdana", 10, SWT.NONE));
		sashForm.setWeights(new int[] {2, 1 });
		//
	}

	public void buildMessages() {
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_MONTH, -7);
		Date after = date.getTime();
		List<TextMessageResult> messages = DAO.localDAO().getTextMessageResultsForCompany(companyName, assetName, after);
		if (messages != null)
			this.setInput(messages.toArray(new TextMessageResult[]{}));
		else
			this.setInput(new TextMessageResult[]{});
	}
	public void setSelection(String companyName, String assetName) {
		this.companyName = companyName;
		this.assetName = assetName;
		this.buildMessages();
	}
	public void dispose() {
		TracController.singleton().removeEntityChangeListener(this);
		super.dispose();
	}
	public void prepareListener(Class textMessageClass) {
		this.textMessageClass = textMessageClass;
		Type[] interest = new Type[]{
				EntityNotification.Type.UPDATE,
				EntityNotification.Type.SAVE_OR_UPDATE,
				EntityNotification.Type.SAVE
		};
		TracController.singleton().addEntityChangeListener(this,interest,textMessageClass.getName(),null);
	}
	public void onEntityChange(EntityNotification notification) {
		String entityName = notification.getEntityName();
		if (PersistantObject.instanceOf(entityName, MTTextMessage.class) || PersistantObject.instanceOf(entityName, MOTextMessage.class)) {
			this.buildMessages();
		}
	}

	public void onEntityNotFound(EntityNotification notification) {
		// TODO Auto-generated method stub
		
	}
	public void addMessage(TextMessageResult message) {
		if (!boxMessages.contains(message)) {
			boxMessages.add(message);
			tableViewer.add(message);
			tableViewer.reveal(message);
		}
	}
	public void setInput(TextMessageResult[] messages) {
		tableViewer.setInput(messages);
		if (messages != null && messages.length > 0) {
			tableViewer.reveal(messages[messages.length-1]);
		}
		boxMessages.addAll(Arrays.asList(messages));
	}
	public TextMessageResult[] getSelectedMessages() {
		IStructuredSelection ssel = (IStructuredSelection)tableViewer.getSelection();
		Object[] selected = ssel.toArray();
		if (selected != null) {
			TextMessageResult[] cast = new TextMessageResult[selected.length];
			int i = 0;
			for (Object object : selected) {
				cast[i] = (TextMessageResult)object;
				i++;
			}
			return cast;
		}
		return null;
	}
	public void removeMessages(TextMessageResult[] messages) {
		for (TextMessageResult message : messages) {
			if (boxMessages.contains(message)) {
				tableViewer.remove(message);
			}
		}
	}
	public Table getTable() {
		return tableViewer.getTable();
	}
	public void saveState(IMemento memento, String context) {
		if (memento != null){
			Table table = tableViewer.getTable();
			memento.putInteger(context+"TableColumnCount", table.getColumnCount());
			int idx = 0;
			for (TableColumn column : table.getColumns()) {
				memento.putInteger(context+"TableColumnWidth_"+idx++, column.getWidth());
			}
		}
	}
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
