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
package jorgan.midimerger.gui.preferences;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import jorgan.gui.preferences.category.AppCategory;
import jorgan.gui.preferences.category.JOrganCategory;
import jorgan.midimerger.MidiMerger;
import jorgan.midimerger.MidiMergerProvider;
import bias.Configuration;
import bias.swing.Category;
import bias.util.Property;

/**
 * {@link MidiMerger} category.
 */
public class MidiMergerCategory extends JOrganCategory {

	private static Configuration config = Configuration.getRoot().get(
			MidiMergerCategory.class);

	private Model mergings = getModel(new Property(MidiMergerProvider.class,
			"mergings"));

	private JList list;

	public MidiMergerCategory() {
		config.read(this);
	}

	@Override
	public Class<? extends Category> getParentCategory() {
		return AppCategory.class;
	}

	@Override
	protected JComponent createComponent() {
		JPanel panel = new JPanel(new BorderLayout());

		list = new JList();
		panel.add(list, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		panel.add(buttonPanel, BorderLayout.EAST);

		buttonPanel.add(config.get("add").read(new JButton()));
		buttonPanel.add(config.get("remove").read(new JButton()));

		return panel;
	}

	@Override
	protected void read() {
		list.setModel(new MergingsModel());
	}

	@Override
	protected void write() {
	}

	@SuppressWarnings("unchecked")
	private List<File> getMergings() {
		return (List<File>) mergings.getValue();
	}

	private class MergingsModel extends AbstractListModel {
		@Override
		public int getSize() {
			return getMergings().size();
		}

		@Override
		public Object getElementAt(int index) {
			return getMergings().get(index).getPath();
		}
	}
}