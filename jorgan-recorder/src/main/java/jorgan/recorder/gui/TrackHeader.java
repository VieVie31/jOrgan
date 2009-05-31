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
package jorgan.recorder.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jorgan.disposition.Element;
import jorgan.disposition.Elements;
import jorgan.recorder.SessionRecorder;
import jorgan.recorder.swing.IconToggle;
import bias.Configuration;

public class TrackHeader extends JPanel {

	private static Configuration config = Configuration.getRoot().get(
			TrackHeader.class);

	private SessionRecorder recorder;

	private int track;

	public TrackHeader(final SessionRecorder recorder, final int track) {
		super(new BorderLayout());

		this.recorder = recorder;
		this.track = track;

		JLabel label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setBorder(new EmptyBorder(2, 2, 2, 2));
		label.setText(getTitle());
		add(label, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		add(buttonPanel, BorderLayout.CENTER);

		IconToggle playToggle = new IconToggle() {
			@Override
			protected boolean isOn() {
				return recorder.getTracker(track).plays();
			}

			@Override
			protected void toggle() {
				if (recorder.getState() == SessionRecorder.STATE_STOP) {
					recorder.getTracker(track).setPlays(!isOn());
					super.toggle();
				}
			}
		};
		config.get("play").read(playToggle);
		buttonPanel.add(playToggle);

		IconToggle recordToggle = new IconToggle() {
			@Override
			protected boolean isOn() {
				return recorder.getTracker(track).records();
			}

			@Override
			protected void toggle() {
				if (recorder.getState() == SessionRecorder.STATE_STOP) {
					recorder.getTracker(track).setRecords(!isOn());
					super.toggle();
				}
			}
		};
		config.get("record").read(recordToggle);
		buttonPanel.add(recordToggle);

		final JPopupMenu menu = new JPopupMenu();
		menu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				init(menu);
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		label.setComponentPopupMenu(menu);
	}

	private String getTitle() {
		Element element = recorder.getElement(track);
		if (element == null) {
			return "Track " + track;
		} else {
			return Elements.getDisplayName(element);
		}
	}

	protected void init(JPopupMenu menu) {
		menu.removeAll();

		final JRadioButtonMenuItem noneItem = new JRadioButtonMenuItem();
		config.get("none").read(noneItem);
		if (recorder.getElement(track) == null) {
			noneItem.setSelected(true);
		}
		noneItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (noneItem.isSelected()) {
					recorder.setElement(track, null);
				}
			}
		});
		menu.add(noneItem);

		for (final Element element : recorder.getTrackableElements()) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(Elements
					.getDisplayName(element));
			if (recorder.getElement(track) == element) {
				item.setSelected(true);
			}
			item.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (item.isSelected()) {
						recorder.setElement(track, element);
					}
				}
			});
			menu.add(item);
		}
	}
}
