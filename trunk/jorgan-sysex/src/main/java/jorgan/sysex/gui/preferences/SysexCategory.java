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
package jorgan.sysex.gui.preferences;

import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jorgan.gui.preferences.category.AppCategory;
import jorgan.gui.preferences.category.JOrganCategory;
import jorgan.midi.DevicePool;
import jorgan.midi.Direction;
import jorgan.swing.FileField;
import jorgan.swing.layout.DefinitionBuilder;
import jorgan.swing.layout.DefinitionBuilder.Column;
import jorgan.sysex.SysexMapper;
import bias.Configuration;
import bias.swing.Category;
import bias.util.Property;

/**
 * {@link SysexMapper} category.
 */
public class SysexCategory extends JOrganCategory {

	private static Configuration config = Configuration.getRoot().get(
			SysexCategory.class);

	private Model input = getModel(new Property(SysexMapper.class, "input"));

	private Model output = getModel(new Property(SysexMapper.class, "output"));

	private Model mapping = getModel(new Property(SysexMapper.class, "mapping"));

	private JComboBox inputComboBox = new JComboBox();

	private JComboBox outputComboBox = new JComboBox();

	private FileField mappingField = new FileField();

	public SysexCategory() {
		config.read(this);
	}

	@Override
	public Class<? extends Category> getParentCategory() {
		return AppCategory.class;
	}

	@Override
	protected JComponent createComponent() {
		JPanel panel = new JPanel();

		DefinitionBuilder builder = new DefinitionBuilder(panel);

		Column column = builder.column();

		column.term(config.get("input").read(new JLabel()));
		column.definition(inputComboBox).fillHorizontal();

		column.term(config.get("output").read(new JLabel()));
		column.definition(outputComboBox).fillHorizontal();

		column.term(config.get("mapping").read(new JLabel()));
		column.definition(mappingField).fillHorizontal();

		return panel;
	}

	@Override
	protected void read() {
		inputComboBox
				.setModel(new DefaultComboBoxModel(getNames(Direction.IN)));
		inputComboBox.setSelectedItem(input.getValue());

		outputComboBox.setModel(new DefaultComboBoxModel(
				getNames(Direction.OUT)));
		outputComboBox.setSelectedItem(output.getValue());

		mappingField.setFile((File) mapping.getValue());
	}

	private static String[] getNames(Direction direction) {
		String[] deviceNames = DevicePool.instance().getMidiDeviceNames(
				direction);

		String[] names = new String[1 + deviceNames.length];

		System.arraycopy(deviceNames, 0, names, 1, deviceNames.length);

		return names;
	}

	@Override
	protected void write() {
		input.setValue(inputComboBox.getSelectedItem());
		output.setValue(outputComboBox.getSelectedItem());

		mapping.setValue(mappingField.getFile());
	}
}