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
package jorgan.play;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import jorgan.disposition.Element;
import jorgan.disposition.Keyboard;
import jorgan.disposition.Input.InputMessage;
import jorgan.disposition.Keyboard.PressKey;
import jorgan.disposition.Keyboard.ReleaseKey;
import jorgan.disposition.event.OrganEvent;
import jorgan.midi.MessageUtils;
import jorgan.midi.mpl.Context;
import jorgan.session.event.Severity;

/**
 * A player of an keyboard.
 */
public class KeyboardPlayer extends Player<Keyboard> {

	/**
	 * The currently pressed keys.
	 */
	private boolean[] pressed = new boolean[128];

	private Transmitter transmitter;

	/**
	 * Create player for the given keyboard.
	 * 
	 * @param keyboard
	 *            keyboard to play
	 */
	public KeyboardPlayer(Keyboard keyboard) {
		super(keyboard);
	}

	@Override
	public void elementChanged(OrganEvent event) {
		super.elementChanged(event);
		
		Keyboard keyboard = getElement();

		if (keyboard.getInput() == null) {
			addProblem(Severity.WARNING, "input", "noDevice", keyboard
					.getInput());
		} else {
			removeProblem(Severity.WARNING, "input");
		}
	}

	@Override
	protected void openImpl() {
		Keyboard keyboard = getElement();

		removeProblem(Severity.ERROR, "input");
		if (keyboard.getInput() != null) {
			try {
				transmitter = getOrganPlay().createTransmitter(
						keyboard.getInput());
				transmitter.setReceiver(new ReceiverImpl());
			} catch (MidiUnavailableException ex) {
				addProblem(Severity.ERROR, "input", "deviceUnavailable",
						keyboard.getInput());
			}
		}
	}

	@Override
	protected void closeImpl() {
		for (int p = 0; p < pressed.length; p++) {
			pressed[p] = false;
		}

		if (transmitter != null) {
			transmitter.close();
			transmitter = null;
		}
	}

	@Override
	protected void input(InputMessage message, Context context) {
		if (message instanceof PressKey) {
			int pitch = Math.round(context.get(PressKey.PITCH));
			if (pitch < 0 || pitch > 127) {
				addProblem(Severity.ERROR, message, "pitchInvalid", pitch);
				return;
			}
			int velocity = Math.round(context.get(PressKey.VELOCITY));
			if (velocity < 0 || velocity > 127) {
				addProblem(Severity.ERROR, message, "velocityInvalid", pitch);
				return;
			}
			press(pitch, velocity);
		} else if (message instanceof ReleaseKey) {
			int pitch = Math.round(context.get(ReleaseKey.PITCH));
			if (pitch < 0 || pitch > 127) {
				addProblem(Severity.ERROR, message, "pitchInvalid", pitch);
				return;
			}
			release(pitch);
		} else {
			super.input(message, context);
		}
	}

	private void press(int pitch, int velocity) {
		Keyboard keyboard = getElement();

		if (!pressed[pitch]) {
			pressed[pitch] = true;

			for (int e = 0; e < keyboard.getReferenceCount(); e++) {
				Element element = keyboard.getReference(e).getElement();

				KeyablePlayer<?> player = (KeyablePlayer<?>) getOrganPlay()
						.getPlayer(element);
				if (player != null) {
					player.keyDown(pitch, velocity);
				}
			}
		}
	}

	private void release(int pitch) {

		Keyboard keyboard = getElement();

		if (pressed[pitch]) {
			pressed[pitch] = false;

			for (int e = 0; e < keyboard.getReferenceCount(); e++) {
				Element element = keyboard.getReference(e).getElement();

				KeyablePlayer<?> player = (KeyablePlayer<?>) getOrganPlay()
						.getPlayer(element);
				if (player != null) {
					player.keyUp(pitch);
				}
			}
		}
	}

	private class ReceiverImpl implements Receiver {
		public void close() {
		}

		public void send(MidiMessage message, long timeStamp) {
			if (MessageUtils.isShortMessage(message)) {
				received((ShortMessage) message);
			}
		}
	}
}