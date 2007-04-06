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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import jorgan.disposition.Activator;
import jorgan.disposition.Console;
import jorgan.disposition.Coupler;
import jorgan.disposition.Element;
import jorgan.disposition.Initiator;
import jorgan.disposition.Keyboard;
import jorgan.disposition.Keyer;
import jorgan.disposition.Organ;
import jorgan.disposition.Regulator;
import jorgan.disposition.SoundSource;
import jorgan.disposition.Stop;
import jorgan.disposition.Swell;
import jorgan.disposition.Tremulant;
import jorgan.disposition.Variation;
import jorgan.disposition.event.OrganAdapter;
import jorgan.disposition.event.OrganEvent;
import jorgan.play.event.PlayEvent;
import jorgan.play.event.PlayListener;
import jorgan.sound.midi.MessageUtils;

/**
 * A play of an organ.
 */
public class OrganPlay {

	private final Object CHANGE_LOCK = new Object();

	private final Object RECEIVER_LOCK = new Object();

	private boolean open;

	/**
	 * Element to player mapping.
	 */
	private Map<Element, Player> players = new HashMap<Element, Player>();

	/**
	 * The handler of organ and configuration events.
	 */
	private EventHandler eventHandler = new EventHandler();

	/**
	 * All registered playerListeners.
	 */
	private List<PlayListener> listeners = new ArrayList<PlayListener>();

	private Organ organ;

	/**
	 * Creates a new organ player.
	 * 
	 * @param organ
	 *            the organ to play
	 */
	public OrganPlay(Organ organ) {
		this.organ = organ;

		organ.addOrganListener(eventHandler);

		for (int e = 0; e < organ.getElementCount(); e++) {
			createPlayer(organ.getElement(e));
		}
	}

	public Organ getOrgan() {
		return organ;
	}

	public void addPlayerListener(PlayListener listener) {
		listeners.add(listener);
	}

	public void removePlayerListener(PlayListener listener) {
		listeners.remove(listener);
	}

	public void dispose() {

		organ.removeOrganListener(eventHandler);
		organ = null;

		listeners.clear();
	}

	protected void fireClosed() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.closed();
			}
		}
	}

	protected void fireOpened() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.opened();
			}
		}
	}

	protected void firePlayerAdded(Player player) {
		if (listeners != null) {
			PlayEvent event = new PlayEvent(this, player.getElement());
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.playerAdded(event);
			}
		}
	}

	protected void firePlayerRemoved(Player player) {
		if (listeners != null) {
			PlayEvent event = new PlayEvent(this, player.getElement());
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.playerRemoved(event);
			}
		}
	}

	protected void fireProblemAdded(Player player, Problem problem) {
		if (listeners != null) {
			PlayEvent event = new PlayEvent(this, player.getElement(), problem);
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.problemAdded(event);
			}
		}
	}

	protected void fireProblemRemoved(Player player, Problem problem) {
		if (listeners != null) {
			PlayEvent event = new PlayEvent(this, player.getElement(), problem);
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.problemRemoved(event);
			}
		}
	}

	/**
	 * Fire input.
	 */
	protected void fireInputAccepted() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.inputAccepted();
			}
		}
	}

	/**
	 * Fire output.
	 */
	protected void fireOutputProduced() {
		if (listeners != null) {
			for (int l = 0; l < listeners.size(); l++) {
				PlayListener listener = listeners.get(l);
				listener.outputProduced();
			}
		}
	}

	protected Player getPlayer(Element element) {
		return players.get(element);
	}

	public boolean hasErrors(Element element) {
		Player player = getPlayer(element);
		if (player == null) {
			return false;
		}

		return player.hasErrors();
	}

	public boolean hasWarnings(Element element) {
		Player player = getPlayer(element);
		if (player == null) {
			return false;
		}

		return player.hasWarnings();
	}

	public List getProblems(Element element) {
		Player player = getPlayer(element);
		if (player == null) {
			return null;
		}

		return player.getProblems();
	}

	public void open() {
		if (open) {
			throw new IllegalStateException("already open");
		}

		synchronized (CHANGE_LOCK) {
			Iterator<Player> iterator = players.values().iterator();
			while (iterator.hasNext()) {
				Player player = iterator.next();
				player.open();
			}

			iterator = players.values().iterator();
			while (iterator.hasNext()) {
				Player player = iterator.next();
				player.elementChanged(null);
			}
		}

		synchronized (RECEIVER_LOCK) {
			open = true;
		}

		fireOpened();
	}

	public boolean isOpen() {
		return open;
	}

	public void close() {
		if (!open) {
			throw new IllegalStateException("not open");
		}

		synchronized (RECEIVER_LOCK) {
			open = false;
		}

		synchronized (CHANGE_LOCK) {
			Iterator<Player> iterator = players.values().iterator();
			while (iterator.hasNext()) {
				Player player = iterator.next();
				player.close();
			}
		}

		fireClosed();
	}

	protected void createPlayer(Element element) {
		Player player = null;

		if (element instanceof Keyboard) {
			player = new KeyboardPlayer((Keyboard) element);
		} else if (element instanceof SoundSource) {
			player = new SoundSourcePlayer((SoundSource) element);
		} else if (element instanceof Console) {
			player = new ConsolePlayer((Console) element);
		} else if (element instanceof Activator) {
			player = new ActivatorPlayer((Activator) element);
		} else if (element instanceof Stop) {
			player = new StopPlayer((Stop) element);
		} else if (element instanceof Coupler) {
			player = new CouplerPlayer((Coupler) element);
		} else if (element instanceof Swell) {
			player = new SwellPlayer((Swell) element);
		} else if (element instanceof Regulator) {
			player = new ActivationPlayer((Regulator) element);
		} else if (element instanceof Tremulant) {
			player = new TremulantPlayer((Tremulant) element);
		} else if (element instanceof Variation) {
			player = new VariationPlayer((Variation) element);
		} else if (element instanceof Initiator) {
			player = new InitiatorPlayer((Initiator) element);
		} else if (element instanceof Keyer) {
			player = new KeyerPlayer((Keyer) element);
		}

		if (player != null) {
			player.setOrganPlay(this);
			players.put(element, player);

			firePlayerAdded(player);

			player.elementChanged(null);
		}
	}

	protected void dropPlayer(Element element) {
		Player player = players.get(element);
		if (player != null) {
			players.remove(element);

			firePlayerRemoved(player);
		}
	}

	/**
	 * Create a receiver that forwards all received messages to the given
	 * player.<br>
	 * All forwarding is synchronized on this organPlay to avoid race
	 * conditions.
	 * 
	 * @param player
	 *            player to forward messages to
	 * @return receiver
	 * @see jorgan.play.Player#input(javax.sound.midi.ShortMessage)
	 */
	protected Receiver createReceiver(final Player player) {
		return new Receiver() {
			public void close() {
			}

			public void send(MidiMessage message, long timestamp) {
				synchronized (RECEIVER_LOCK) {
					if (open && MessageUtils.isShortMessage(message)) {
						synchronized (CHANGE_LOCK) {
							player.input((ShortMessage) message);
						}
					}
				}
			}
		};
	}

	private class EventHandler extends OrganAdapter {

		public void elementChanged(OrganEvent event) {
			synchronized (CHANGE_LOCK) {
				Player player = getPlayer(event.getElement());
				if (player != null) {
					player.elementChanged(event);
				}
			}
		}

		public void elementAdded(OrganEvent event) {
			synchronized (CHANGE_LOCK) {
				createPlayer(event.getElement());
			}
		}

		public void elementRemoved(OrganEvent event) {
			synchronized (CHANGE_LOCK) {
				dropPlayer(event.getElement());
			}
		}
	}
}