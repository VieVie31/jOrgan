/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.gui.construct;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jorgan.disposition.Element;
import jorgan.disposition.Reference;
import jorgan.disposition.event.OrganEvent;
import jorgan.disposition.event.OrganListener;
import jorgan.gui.ElementListCellRenderer;
import jorgan.gui.OrganSession;
import jorgan.gui.event.ElementSelectionEvent;
import jorgan.gui.event.ElementSelectionListener;
import jorgan.swing.BaseAction;
import jorgan.swing.list.ListUtils;
import swingx.docking.DockedPanel;
import bias.Configuration;

/**
 * Panel shows the references of elements.
 */
public class ReferencesPanel extends DockedPanel {

	private static Configuration config = Configuration.getRoot().get(
			ReferencesPanel.class);

	/**
	 * The edited organ.
	 */
	private OrganSession session;

	private List<Row> rows = new ArrayList<Row>();

	/**
	 * The listener to selection changes.
	 */
	private SelectionHandler selectionHandler = new SelectionHandler();

	private AddAction addAction = new AddAction();

	private RemoveAction removeAction = new RemoveAction();

	private JList list = new JList();

	private JToggleButton referencesToButton = new JToggleButton();

	private JToggleButton referencedFromButton = new JToggleButton();

	private JToggleButton sortNameButton = new JToggleButton();

	private JToggleButton sortTypeButton = new JToggleButton();

	private ReferencesModel referencesModel = new ReferencesModel();

	/**
	 * Create a tree panel.
	 */
	public ReferencesPanel() {

		addTool(addAction);
		addTool(removeAction);

		addToolSeparator();

		config.get("sortNameButton").read(sortNameButton);
		sortNameButton.setSelected(true);
		sortNameButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (sortNameButton.isSelected()) {
					sortTypeButton.setSelected(false);
				}
				updateReferences();
			}
		});
		addTool(sortNameButton);

		config.get("sortTypeButton").read(sortTypeButton);
		sortTypeButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (sortTypeButton.isSelected()) {
					sortNameButton.setSelected(false);
				}
				updateReferences();
			}
		});
		addTool(sortTypeButton);

		addToolSeparator();

		ButtonGroup toFromGroup = new ButtonGroup();
		config.get("referencesToButton").read(referencesToButton);
		referencesToButton.getModel().setGroup(toFromGroup);
		referencesToButton.setSelected(true);
		referencesToButton.getModel().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateReferences();
			}
		});
		addTool(referencesToButton);

		config.get("referencedFromButton").read(referencedFromButton);
		referencedFromButton.getModel().setGroup(toFromGroup);
		addTool(referencedFromButton);

		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setModel(referencesModel);
		list.setCellRenderer(new ElementListCellRenderer() {
			protected OrganSession getOrgan() {
				return session;
			}

			protected Element getElement(Object object) {
				Row row = (Row) object;

				if (getShowReferencesTo()) {
					return row.reference.getElement();
				} else {
					return row.element;
				}
			}
		});
		list.addListSelectionListener(removeAction);
		ListUtils.addActionListener(list, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Row row = rows.get(list.getSelectedIndex());

				if (getShowReferencesTo()) {
					session.getSelectionModel().setSelectedElement(
							row.reference.getElement());
				} else {
					session.getSelectionModel().setSelectedElement(row.element);
				}
			}
		});

		setScrollableBody(list, true, false);
	}

	/**
	 * Set the organ to be edited.
	 * 
	 * @param session
	 *            session to be edited
	 */
	public void setOrgan(OrganSession session) {
		if (this.session != null) {
			this.session.removeOrganListener(referencesModel);
			this.session.removeSelectionListener(selectionHandler);
		}

		this.session = session;

		if (this.session != null) {
			this.session.addOrganListener(referencesModel);
			this.session.addSelectionListener(selectionHandler);
		}

		updateReferences();
	}

	private void updateReferences() {

		rows.clear();
		referencesModel.update();

		if (session != null
				&& session.getSelectionModel().getSelectionCount() == 1) {
			Element element = session.getSelectionModel().getSelectedElement();

			if (getShowReferencesTo()) {
				for (int r = 0; r < element.getReferenceCount(); r++) {
					rows.add(new Row(element, element.getReference(r)));
				}
			} else {
				Iterator iterator = element.getReferrer().iterator();
				while (iterator.hasNext()) {
					Element referrer = (Element) iterator.next();

					List references = referrer.getReferences(element);
					for (int r = 0; r < references.size(); r++) {
						rows.add(new Row(referrer, (Reference) references
								.get(r)));
					}
				}
			}

			if (sortNameButton.isSelected()) {
				Collections.sort(rows, new RowComparator(new ElementComparator(
						true)));
			} else if (sortTypeButton.isSelected()) {
				Collections.sort(rows, new RowComparator(new ElementComparator(
						false)));
			}
			referencesModel.update();

			list.setVisible(true);
		} else {
			list.setVisible(false);
		}

		addAction.update();
	}

	public void setShowReferencesTo(boolean showReferencesTo) {
		if (showReferencesTo != referencesToButton.isSelected()) {
			if (showReferencesTo) {
				referencesToButton.setSelected(true);
			} else {
				referencedFromButton.setSelected(true);
			}
		}
	}

	public boolean getShowReferencesTo() {
		return referencesToButton.isSelected();
	}

	/**
	 * The handler of selections.
	 */
	private class SelectionHandler implements ElementSelectionListener {

		public void selectionChanged(ElementSelectionEvent ev) {
			updateReferences();
		}
	}

	/**
	 * Note that <em>Spin</em> ensures that the methods of this listeners are
	 * called on the EDT, although a change in the organ might be triggered by a
	 * change on a MIDI thread.
	 */
	private class ReferencesModel extends AbstractListModel implements
			OrganListener {

		private int size = -1;

		private void update() {
			if (size != -1) {
				if (rows.size() > size) {
					fireIntervalAdded(this, size, rows.size() - 1);
				} else if (rows.size() < size) {
					fireIntervalRemoved(this, rows.size(), size - 1);
				}
				size = rows.size();
			}
		}

		public int getSize() {

			if (rows == null) {
				size = 0;
			} else {
				size = rows.size();
			}
			return size;
		}

		public Object getElementAt(int index) {
			return rows.get(index);
		}

		public void elementAdded(OrganEvent event) {
		}

		public void elementRemoved(OrganEvent event) {
		}

		public void elementChanged(final OrganEvent event) {
			int index = rows.indexOf(event.getElement());
			if (index != -1) {
				fireContentsChanged(this, index, index);
			}
		}

		public void fireRemoved(int count) {
			if (count > 0) {
				fireIntervalRemoved(this, 0, count - 1);
			}
		}

		public void fireAdded(int count) {
			if (count > 0) {
				fireIntervalAdded(this, 0, count - 1);
			}
		}

		public void referenceChanged(OrganEvent event) {
		}

		public void referenceAdded(OrganEvent event) {
			updateReferences();
		}

		public void referenceRemoved(OrganEvent event) {
			updateReferences();
		}
	}

	private class AddAction extends BaseAction {

		private AddAction() {
			config.get("addAction").read(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			CreateReferencesWizard.showInDialog(ReferencesPanel.this, session
					.getOrgan(), session.getSelectionModel()
					.getSelectedElement());
		}

		public void update() {
			setEnabled(session != null
					&& session.getSelectionModel().getSelectionCount() == 1);
		}
	}

	private class RemoveAction extends BaseAction implements
			ListSelectionListener {

		private RemoveAction() {
			config.get("removeAction").read(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			int[] indices = list.getSelectedIndices();
			if (indices != null) {
				for (int i = indices.length - 1; i >= 0; i--) {
					Row row = rows.get(indices[i]);

					if (getShowReferencesTo()) {
						row.element.removeReference(row.reference);
					} else {
						row.element.removeReference(row.reference);
					}
				}
			}
		}

		public void valueChanged(ListSelectionEvent e) {
			setEnabled(list.getSelectedIndex() != -1);
		}
	}

	private class Row {
		private Row(Element element, Reference reference) {
			this.element = element;
			this.reference = reference;
		}

		private Element element;

		private Reference reference;
	}

	private class RowComparator implements Comparator<Row> {
		private ElementComparator comparator;

		private RowComparator(ElementComparator comparator) {
			this.comparator = comparator;
		}

		public int compare(Row row1, Row row2) {

			if (getShowReferencesTo()) {
				return comparator.compare(row1.reference.getElement(),
						row2.reference.getElement());
			} else {
				return comparator.compare(row1.element, row2.element);
			}
		}
	}
}