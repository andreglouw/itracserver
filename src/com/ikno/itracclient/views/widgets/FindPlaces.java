package com.ikno.itracclient.views.widgets;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.ikno.dao.hibernate.DAO;
import com.ikno.dao.hibernate.DAO.Place;
import com.ikno.itracclient.IMappingView;
import com.ikno.itracclient.Validate;
import com.ikno.itracclient.utils.Formatting;

public class FindPlaces extends Composite {

	private TableViewer tableViewer;
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0: // Town
				return ((Place)element).getTown();
			case 1: // Province
				return ((Place)element).getProvince();
			case 2: // Country
				return ((Place)element).getCountry();
			case 3: // Suburb
				return ((Place)element).getSuburb();
			case 4: // Street
				return ((Place)element).getStreet();
			case 5: // Lat / Lon
				return Formatting.formatLatLon(((Place)element).getLongitude(),((Place)element).getLatitude());
			}
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private Table table;
	private Text longitude;
	private Text latitude;
	private Text country;
	private Text streetName;
	private Text town;
	private IMappingView mappingView = null;
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public FindPlaces(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());

		final Group findAPlaceGroup = new Group(this, SWT.NONE);
		findAPlaceGroup.setText("Search");
		final FormData fd_findAPlaceGroup = new FormData();
		fd_findAPlaceGroup.bottom = new FormAttachment(100, -5);
		fd_findAPlaceGroup.right = new FormAttachment(100, -5);
		fd_findAPlaceGroup.top = new FormAttachment(0, 5);
		fd_findAPlaceGroup.left = new FormAttachment(0, 5);
		findAPlaceGroup.setLayoutData(fd_findAPlaceGroup);
		findAPlaceGroup.setLayout(new FormLayout());

		final Group group = new Group(findAPlaceGroup, SWT.NONE);
		final FormData fd_group = new FormData();
		fd_group.top = new FormAttachment(0, 3);
		fd_group.left = new FormAttachment(100, -480);
		fd_group.right = new FormAttachment(100, -9);
		group.setLayoutData(fd_group);
		group.setLayout(new FormLayout());

		final Label streetNameLabel = new Label(group, SWT.NONE);
		streetNameLabel.setAlignment(SWT.RIGHT);
		final FormData fd_streetNameLabel = new FormData();
		fd_streetNameLabel.bottom = new FormAttachment(0, 20);
		fd_streetNameLabel.right = new FormAttachment(0, 90);
		streetNameLabel.setLayoutData(fd_streetNameLabel);
		streetNameLabel.setText("Town");

		town = new Text(group, SWT.BORDER);
		fd_streetNameLabel.top = new FormAttachment(town, 0, SWT.TOP);
		final FormData fd_town = new FormData();
		fd_town.right = new FormAttachment(100, -5);
		fd_town.bottom = new FormAttachment(0, 20);
		fd_town.left = new FormAttachment(0, 95);
		town.setLayoutData(fd_town);

		final Label townLabel = new Label(group, SWT.NONE);
		townLabel.setAlignment(SWT.RIGHT);
		final FormData fd_townLabel = new FormData();
		fd_townLabel.top = new FormAttachment(streetNameLabel, 5, SWT.BOTTOM);
		fd_townLabel.bottom = new FormAttachment(0, 45);
		fd_townLabel.right = new FormAttachment(0, 90);
		townLabel.setLayoutData(fd_townLabel);
		townLabel.setText("Street");

		streetName = new Text(group, SWT.BORDER);
		final FormData fd_streetName = new FormData();
		fd_streetName.right = new FormAttachment(town, 0, SWT.RIGHT);
		fd_streetName.bottom = new FormAttachment(0, 45);
		fd_streetName.left = new FormAttachment(0, 95);
		streetName.setLayoutData(fd_streetName);

		final Label countryLabel = new Label(group, SWT.NONE);
		countryLabel.setAlignment(SWT.RIGHT);
		final FormData fd_countryLabel = new FormData();
		fd_countryLabel.left = new FormAttachment(0, 15);
		fd_countryLabel.right = new FormAttachment(0, 89);
		countryLabel.setLayoutData(fd_countryLabel);
		countryLabel.setText("Country");

		country = new Text(group, SWT.BORDER);
		fd_countryLabel.bottom = new FormAttachment(country, 0, SWT.BOTTOM);
		fd_countryLabel.top = new FormAttachment(country, 0, SWT.TOP);
		final FormData fd_country = new FormData();
		fd_country.right = new FormAttachment(streetName, 0, SWT.RIGHT);
		fd_country.bottom = new FormAttachment(0, 70);
		fd_country.left = new FormAttachment(0, 95);
		country.setLayoutData(fd_country);

		final Button clearButton = new Button(group, SWT.NONE);
		clearButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				town.setText("");
				streetName.setText("");
				country.setText("");
				tableViewer.setInput(new Object[]{});
			}
		});
		final FormData fd_clearButton = new FormData();
		fd_clearButton.right = new FormAttachment(0, 400);
		fd_clearButton.left = new FormAttachment(0, 355);
		clearButton.setLayoutData(fd_clearButton);
		clearButton.setText("Clear");

		tableViewer = new TableViewer(group, SWT.BORDER| SWT.FULL_SELECTION);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					Place selected = (Place)selection.getFirstElement();
					FindPlaces.this.mappingView.gotoPlace(selected.getLatitude(),selected.getLongitude(), 3500, true);
				}
			}
		});
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		table = tableViewer.getTable();
		final FormData fd_table = new FormData();
		fd_table.left = new FormAttachment(country, -455, SWT.RIGHT);
		fd_table.right = new FormAttachment(country, 0, SWT.RIGHT);
		fd_table.bottom = new FormAttachment(clearButton, 116, SWT.BOTTOM);
		fd_table.top = new FormAttachment(clearButton, 5, SWT.BOTTOM);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn townTableColumn = new TableColumn(table, SWT.NONE);
		townTableColumn.setWidth(132);
		townTableColumn.setText("Town");

		final TableColumn provinceTableColumn = new TableColumn(table, SWT.NONE);
		provinceTableColumn.setWidth(100);
		provinceTableColumn.setText("Province");

		final TableColumn countryTableColumn = new TableColumn(table, SWT.NONE);
		countryTableColumn.setWidth(167);
		countryTableColumn.setText("Country");

		final TableColumn suburbTableColumn = new TableColumn(table, SWT.NONE);
		suburbTableColumn.setWidth(132);
		suburbTableColumn.setText("Suburb");

		final TableColumn streetTableColumn = new TableColumn(table, SWT.NONE);
		streetTableColumn.setWidth(147);
		streetTableColumn.setText("Street");

		final TableColumn latLonTableColumn = new TableColumn(table, SWT.NONE);
		latLonTableColumn.setWidth(147);
		latLonTableColumn.setText("Lat/Lon");

		Button searchButton;
		searchButton = new Button(group, SWT.NONE);
		fd_clearButton.bottom = new FormAttachment(searchButton, 20, SWT.TOP);
		fd_clearButton.top = new FormAttachment(searchButton, 0, SWT.TOP);
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				String townName = town.getText();
				if (townName != null && townName.length() > 0) {
					String street = streetName.getText();
					if (street != null && street.length() > 0) {
						List<Place> found = DAO.localDAO().findRoads(street.toUpperCase()+"%",townName.toUpperCase());
						if (found != null && found.size() > 0) {
							Object[] places = new Object[found.size()];
							int i = 0;
							for (Iterator<Place> pi = found.iterator();pi.hasNext();) {
								places[i++] = pi.next();
							}
							tableViewer.setInput(places);
						}
					} else {
						List<Place> found = DAO.localDAO().findPlaces(townName.toUpperCase(),"","");
						if (found != null && found.size() > 0) {
							Object[] places = new Object[found.size()];
							int i = 0;
							for (Iterator<Place> pi = found.iterator();pi.hasNext();) {
								places[i++] = pi.next();
							}
							tableViewer.setInput(places);
						}
					}
				}
			}
		});
		final FormData fd_searchButton = new FormData();
		fd_searchButton.bottom = new FormAttachment(country, 25, SWT.BOTTOM);
		fd_searchButton.top = new FormAttachment(country, 5, SWT.BOTTOM);
		searchButton.setLayoutData(fd_searchButton);
		searchButton.setText("Search");

		Button goToTown;
		goToTown = new Button(group, SWT.NONE);
		fd_searchButton.right = new FormAttachment(goToTown, 45, SWT.LEFT);
		fd_searchButton.left = new FormAttachment(goToTown, 0, SWT.LEFT);
		goToTown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection != null && selection.getFirstElement() != null) {
					Place selected = (Place)selection.getFirstElement();
					FindPlaces.this.mappingView.gotoPlace(selected.getLatitude(),selected.getLongitude(), 35000, true);
				}
			}
		});
		final FormData fd_goToTown = new FormData();
		fd_goToTown.top = new FormAttachment(0, 220);
		fd_goToTown.bottom = new FormAttachment(0, 240);
		fd_goToTown.left = new FormAttachment(0, 405);
		fd_goToTown.right = new FormAttachment(0, 445);
		goToTown.setLayoutData(fd_goToTown);
		goToTown.setText("Go To");

		Group group_1;
		group_1 = new Group(findAPlaceGroup, SWT.NONE);
		fd_group.bottom = new FormAttachment(group_1, -5, SWT.TOP);
		final FormData fd_group_1 = new FormData();
		fd_group_1.top = new FormAttachment(0, 270);
		fd_group_1.bottom = new FormAttachment(0, 315);
		fd_group_1.left = new FormAttachment(group, -471, SWT.RIGHT);
		fd_group_1.right = new FormAttachment(group, 0, SWT.RIGHT);
		group_1.setLayoutData(fd_group_1);
		group_1.setLayout(new FormLayout());

		final Label longitudexLabel = new Label(group_1, SWT.NONE);
		longitudexLabel.setAlignment(SWT.RIGHT);
		final FormData fd_longitudexLabel = new FormData();
		fd_longitudexLabel.bottom = new FormAttachment(0, 25);
		fd_longitudexLabel.right = new FormAttachment(0, 90);
		fd_longitudexLabel.top = new FormAttachment(0, 5);
		fd_longitudexLabel.left = new FormAttachment(0, 5);
		longitudexLabel.setLayoutData(fd_longitudexLabel);
		longitudexLabel.setText("Latitude (y)");

		latitude = new Text(group_1, SWT.BORDER);
		final FormData fd_latitude = new FormData();
		fd_latitude.bottom = new FormAttachment(longitudexLabel, 0, SWT.BOTTOM);
		fd_latitude.right = new FormAttachment(0, 195);
		fd_latitude.top = new FormAttachment(longitudexLabel, 0, SWT.TOP);
		fd_latitude.left = new FormAttachment(longitudexLabel, 5, SWT.RIGHT);
		latitude.setLayoutData(fd_latitude);
		latitude.addVerifyListener(new Validate.Floating());

		final Label latitudeyLabel = new Label(group_1, SWT.NONE);
		latitudeyLabel.setAlignment(SWT.RIGHT);
		final FormData fd_latitudeyLabel = new FormData();
		fd_latitudeyLabel.right = new FormAttachment(0, 265);
		fd_latitudeyLabel.bottom = new FormAttachment(latitude, 0, SWT.BOTTOM);
		fd_latitudeyLabel.top = new FormAttachment(latitude, 0, SWT.TOP);
		fd_latitudeyLabel.left = new FormAttachment(latitude, 5, SWT.RIGHT);
		latitudeyLabel.setLayoutData(fd_latitudeyLabel);
		latitudeyLabel.setText("Longitude (x)");

		longitude = new Text(group_1, SWT.BORDER);
		final FormData fd_longitude = new FormData();
		fd_longitude.bottom = new FormAttachment(latitudeyLabel, 0, SWT.BOTTOM);
		fd_longitude.right = new FormAttachment(0, 385);
		fd_longitude.top = new FormAttachment(latitudeyLabel, 0, SWT.TOP);
		fd_longitude.left = new FormAttachment(latitudeyLabel, 5, SWT.RIGHT);
		longitude.setLayoutData(fd_longitude);
		longitude.addVerifyListener(new Validate.Floating());

		final Button goToLatLon = new Button(group_1, SWT.NONE);
		goToLatLon.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FindPlaces.this.mappingView.gotoPlace(Double.parseDouble(latitude.getText()), Double.parseDouble(longitude.getText()), 35000, true);
			}
		});
		final FormData fd_goToLatLon = new FormData();
		fd_goToLatLon.bottom = new FormAttachment(longitude, 0, SWT.BOTTOM);
		fd_goToLatLon.right = new FormAttachment(0, 445);
		fd_goToLatLon.top = new FormAttachment(longitude, 0, SWT.TOP);
		fd_goToLatLon.left = new FormAttachment(0, 405);
		goToLatLon.setLayoutData(fd_goToLatLon);
		goToLatLon.setText("Go to");
		//
	}

	public void setMappingView(IMappingView mappingView) {
		this.mappingView = mappingView;
		if (mappingView != null) {
			
		}
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
