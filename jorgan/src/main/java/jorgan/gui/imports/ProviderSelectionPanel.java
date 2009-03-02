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
package jorgan.gui.imports;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jorgan.gui.imports.spi.ImportProvider;
import jorgan.gui.imports.spi.ProviderRegistry;

/**
 * A selection of an import method.
 */
public class ProviderSelectionPanel extends JPanel {

	private JScrollPane scrollPane = new JScrollPane();

	private JList list = new JList();

	private List<ImportProvider> providers = new ArrayList<ImportProvider>();

	/**
	 * Constructor.
	 */
	public ProviderSelectionPanel() {
		setLayout(new BorderLayout(10, 10));

		scrollPane.getViewport().setBackground(Color.white);
		add(scrollPane, BorderLayout.CENTER);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						firePropertyChange("selectedImportProvider", null, null);
					}
				});
		scrollPane.setViewportView(list);

		setImportProviders(ProviderRegistry.lookup());
	}

	/**
	 * Set the {@link ImportProvider}s to choose from.
	 * 
	 * @param providers
	 *            providers
	 */
	public void setImportProviders(List<ImportProvider> providers) {
		this.providers = providers;

		list.setModel(new ProvidersModel());
	}

	/**
	 * Get the selected provider.
	 * 
	 * @return provider the selected provider
	 */
	public ImportProvider getSelectedImportProvider() {
		int index = list.getSelectedIndex();
		if (index == -1) {
			return null;
		} else {
			return providers.get(index);
		}
	}

	private class ProvidersModel extends AbstractListModel {

		public int getSize() {
			return providers.size();
		}

		public Object getElementAt(int index) {
			ImportProvider provider = providers.get(index);

			return provider.getName();
		}
	}
}