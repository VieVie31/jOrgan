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
package jorgan.gui.preferences.category;

import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import jorgan.gui.GUI;
import jorgan.swing.GridBuilder;
import bias.Configuration;
import bias.swing.Category;
import bias.util.Property;

/**
 * {@link GUI} category.
 */
public class GUICategory extends JOrganCategory {

	private static Configuration config = Configuration.getRoot().get(
			GUICategory.class);

	private Model useSystemLookAndFeel = getModel("jorgan/gui/GUI",
			new Property(GUI.class, "useSystemLookAndFeel"));

	private Model showAboutOnStartup = getModel("jorgan/gui/GUI", new Property(
			GUI.class, "showAboutOnStartup"));

	private JCheckBox useSystemLookAndFeelCheckBox = new JCheckBox();

	private JCheckBox showAboutOnStartupCheckBox = new JCheckBox();

	public GUICategory() {
		config.read(this);
	}

	protected JComponent createComponent() {
		JPanel panel = new JPanel(new GridBagLayout());

		GridBuilder builder = new GridBuilder(new double[] { 1.0d });

		builder.nextRow();

		config.get("useSystemLookAndFeelCheckBox").read(
				useSystemLookAndFeelCheckBox);
		panel.add(useSystemLookAndFeelCheckBox, builder.nextColumn());

		builder.nextRow();

		config.get("showAboutOnStartupCheckBox").read(
				showAboutOnStartupCheckBox);
		panel.add(showAboutOnStartupCheckBox, builder.nextColumn());

		builder.nextRow();

		return panel;
	}

	public Class<? extends Category> getParentCategory() {
		return AppCategory.class;
	}

	protected void read() {
		useSystemLookAndFeelCheckBox.setSelected((Boolean) useSystemLookAndFeel
				.getValue());
		showAboutOnStartupCheckBox.setSelected((Boolean) showAboutOnStartup
				.getValue());
	}

	protected void write() {
		useSystemLookAndFeel
				.setValue(useSystemLookAndFeelCheckBox.isSelected());
		showAboutOnStartup.setValue(showAboutOnStartupCheckBox.isSelected());
	}
}