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
package jorgan.disposition;

import java.util.List;

import jorgan.disposition.Input.InputMessage;
import jorgan.disposition.Output.OutputMessage;

/**
 * Abstract base class for an activateable elements.
 */
public abstract class Activateable extends Momentary implements Engageable {

	private boolean active = false;

	private boolean locking = true;

	public void setActive(boolean active) {
		if (this.active != active) {
			this.active = active;

			fireChanged(false);
		}
	}

	public boolean isActive() {
		return active;
	}

	public boolean isLocking() {
		return locking;
	}

	public void setLocking(boolean locking) {
		this.locking = locking;

		fireChanged(true);
	}

	/**
	 * Is this element engaged, either explicitely through
	 * {@link #setActive(boolean)} or from a referencing {@link Activating}.
	 * 
	 * @return <code>true</code> if engaged
	 * 
	 * @see #setActive(boolean)
	 * @see Activating#activates(Activateable)
	 */
	public boolean isEngaged() {
		if (active) {
			return true;
		}

		for (Activating activating : getReferrer(Activating.class)) {
			if (activating.activates(this)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * If a referring {@link Activating} changes, this element changes too.
	 * 
	 * @see #isEngaged()
	 */
	@Override
	public void referrerChanged(Element element) {
		if (element instanceof Activating) {
			fireChanged(false);
		}
	}

	public List<Class<? extends Message>> getMessageClasses() {
		List<Class<? extends Message>> names = super.getMessageClasses();

		names.add(Activate.class);
		names.add(Deactivate.class);
		names.add(Activated.class);
		names.add(Deactivated.class);

		return names;
	}

	public static class Activate extends InputMessage {
		@Override
		protected int getOrder() {
			return 0;
		}
	}

	public static class Deactivate extends InputMessage {
		@Override
		protected int getOrder() {
			return 1;
		}
	}
	
	public static class Activated extends OutputMessage {
		@Override
		protected int getOrder() {
			return 10;
		}
	}

	public static class Deactivated extends OutputMessage {
		@Override
		protected int getOrder() {
			return 11;
		}
	}
}