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
package jorgan.midi;

import javax.sound.midi.*;
import javax.sound.midi.spi.MidiDeviceProvider;

import jorgan.sound.midi.Loopback;

/**
 * The provider of a <code>Keyboard</code> device.
 * 
 * @see jorgan.gui.midi.KeyboardPane
 */
public class KeyboardProvider extends MidiDeviceProvider {

	/**
	 * The name of this device.
	 */
	public static final String DEVICE_NAME = "jOrgan Keyboard";

	/**
	 * The device info for this providers device.
	 */
	private static final Info info = new Info();

	/**
	 * The used loopback.
	 */
	private static Loopback loopback;

	public MidiDevice.Info[] getDeviceInfo() {

		return new MidiDevice.Info[] { info };
	}

	public MidiDevice getDevice(MidiDevice.Info info) {
		if (KeyboardProvider.info == info) {
			return getLoopback();
		}

		return null;
	}

	/**
	 * The info class for this device.
	 */
	protected static class Info extends MidiDevice.Info {

		/**
		 * Constructor.
		 */
		public Info() {
			super(DEVICE_NAME, "jOrgan", "Keyboard of jOrgan", "1.0");
		}
	}

	/**
	 * Get the loopback for this device.
	 * 
	 * @return the lookback
	 */
	public static Loopback getLoopback() {
		if (loopback == null) {
			loopback = new Loopback(info, false, true);
		}
		return loopback;
	}
}
