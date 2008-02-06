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
package jorgan.creative.play;

import java.io.IOException;

import jorgan.creative.SoundFontManager;
import jorgan.creative.disposition.CreativeOutput;
import jorgan.disposition.Output;
import jorgan.disposition.event.OrganEvent;
import jorgan.play.MidiOutputPlayer;
import jorgan.session.event.Severity;

/**
 * A player for a {@link Output} element with a {@link CreativeOutput}.
 */
public class CreativeOutputPlayer extends MidiOutputPlayer<CreativeOutput> {

	private CreativeOutput clone;

	public CreativeOutputPlayer(CreativeOutput output) {
		super(output);
	}

	@Override
	protected void destroy() {
		unload();
	}

	@Override
	public void elementChanged(OrganEvent event) {
		unload();
		load();
	}

	private void load() {
		CreativeOutput output = getElement();

		clone = null;
		removeProblem(Severity.ERROR, "soundfont");
		removeProblem(Severity.ERROR, "device");

		if (output.getSoundfont() != null && output.getDevice() != null) {
			int index = getDeviceIndex(output.getDevice());

			if (index == -1) {
				addProblem(Severity.ERROR, "device", output.getDevice(),
						"noCreativeDevice");
			} else {
				try {
					new SoundFontManager().loadBank(index, output.getBank(),
							output.getSoundfont());

					clone = (CreativeOutput) output.clone();
				} catch (IOException ex) {
					addProblem(Severity.ERROR, "soundfont", output
							.getSoundfont(), "soundFontLoad");
				}
			}
		}
	}

	private int getDeviceIndex(String device) {
		SoundFontManager manager = new SoundFontManager();

		for (int d = manager.getNumDevices() - 1; d >= 0; d--) {
			if (manager.getDeviceName(d).equals(device)) {
				return d;
			}
		}

		return -1;
	}

	private void unload() {
		if (clone != null) {
			try {
				int index = getDeviceIndex(clone.getDevice());
				new SoundFontManager().clearBank(index, clone.getBank());
			} catch (IllegalArgumentException invalidDeviceOrBank) {
			}

			clone = null;
		}
	}
}
