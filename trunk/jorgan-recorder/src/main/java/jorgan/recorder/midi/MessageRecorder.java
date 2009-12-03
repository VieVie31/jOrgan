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
package jorgan.recorder.midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import jorgan.util.AbstractIterator;

public class MessageRecorder {

	public static final long SECOND = 1000;

	public static final long MINUTE = 60 * SECOND;

	private Sequence sequence;

	private Track[] tracks;

	private long currentTick;

	private State state;

	public MessageRecorder(Sequence sequence) {

		this.sequence = sequence;

		tracks = sequence.getTracks();
		currentTick = 0;
		
		new Stopped();
	}

	public Sequence getSequence() {
		return sequence;
	}

	public int getTrackCount() {
		return tracks.length;
	}

	public long getTime() {
		return tickToMillis(getCurrentTick());
	}

	public long getTotalTime() {
		return tickToMillis(getTotalTicks());
	}

	public void setTime(long time) {
		if (time < 0) {
			throw new IllegalArgumentException("negative time");
		}

		setTick(millisToTick(time));
	}

	private void setTick(long tick) {
		stop();

		this.currentTick = Math.min(tick, getTotalTicks() + 1);
	}

	private long getCurrentTick() {
		return state.currentTick();
	}

	private long getTotalTicks() {
		return state.totalTicks();
	}

	public void first() {
		setTick(0);
	}

	public void last() {
		setTick(sequence.getTickLength() + 1);
	}

	public boolean isLast() {
		return currentTick == sequence.getTickLength() + 1; 
	}
	
	public void start() {
		stop();

		new Running();
	}

	public boolean isRunning() {
		return state.getClass() == Running.class;
	}

	public boolean isStopped() {
		return state.getClass() == Stopped.class;
	}

	public void stop() {
		if (state != null) {
			if (state instanceof Stopped) {
				// already stopped
				return;
			}

			state.stopping();
		}

		new Stopped();
	}

	/**
	 * Record the given message. <br>
	 * Note that recording is supported when this recorder {@link #isStopped()}
	 * too.
	 * 
	 * @param track
	 *            track to add message to
	 * @param message
	 *            message to add
	 * @throws IllegalArgumentException
	 *             if track or message is invalid
	 */
	public void record(int track, MidiMessage message) {

		if (track >= tracks.length) {
			throw new IllegalArgumentException("invalid track");
		}

		if (SequenceUtils.isEndOfTrack(message)) {
			throw new IllegalArgumentException("endOfTrack is invalid");
		}

		state.record(track, message);
	}

	protected void onPlayed(int track, MidiMessage message) {
	}

	protected void onLast() {
	}

	protected void onStarting() {
	}

	protected void onStopping() {
	}

	public long millisToTick(long millis) {
		float division = sequence.getDivisionType();
		if (division == Sequence.PPQ) {
			// default tempo is 120 beats per minute -> 2 beats per seconds
			division = 2.0f;
		}

		return Math.round(((double) millis) * division
				* sequence.getResolution() / 1000);
	}

	public long tickToMillis(long tick) {
		float division = sequence.getDivisionType();
		if (division == Sequence.PPQ) {
			// default tempo is 120 beats per minute -> 2 beats per seconds
			division = 2.0f;
		}

		return Math.round(((double) tick) * 1000 / division
				/ sequence.getResolution());
	}

	public Iterable<MidiEvent> events(final int track) {
		return events(track, 0, Long.MAX_VALUE);
	}

	public Iterable<MidiEvent> eventsAtTick(final int track, final long tick) {
		return events(track, tick, tick + 1);
	}

	public Iterable<MidiEvent> eventsToCurrent(final int track) {
		return eventsToTick(track, currentTick);
	}

	public Iterable<MidiEvent> eventsToTick(final int track, final long toTick) {
		return events(track, 0, toTick);
	}

	public Iterable<MidiEvent> eventsFromCurrent(final int track) {
		return eventsFromTick(track, currentTick);
	}

	public Iterable<MidiEvent> eventsFromTick(final int track,
			final long fromTick) {
		return events(track, fromTick, Long.MAX_VALUE);
	}

	private Iterable<MidiEvent> events(final int track, final long fromTick,
			final long toTick) {

		return new AbstractIterator<MidiEvent>() {
			private int index = SequenceUtils.getIndex(tracks[track], fromTick) - 1;

			public boolean hasNext() {
				if (index == tracks[track].size() - 1) {
					return false;
				}

				MidiEvent event = tracks[track].get(index + 1);
				return !SequenceUtils.isEndOfTrack(event.getMessage())
						&& event.getTick() < toTick;
			}

			public MidiEvent next() {
				index++;

				MidiEvent event = tracks[track].get(index);

				return event;
			}

			@Override
			public void remove() {
				tracks[track].remove(tracks[track].get(index));
				index--;
			}
		};
	}

	private abstract class State {
		public State() {
			state = this;
		}

		public abstract void record(int track, MidiMessage message);

		public abstract long currentTick();

		public abstract long totalTicks();

		public void stopping() {
			onStopping();
		}
	}

	private class Stopped extends State {

		public long currentTick() {
			return currentTick;
		}

		@Override
		public long totalTicks() {
			return sequence.getTickLength();
		}

		@Override
		public void record(int track, MidiMessage message) {
			tracks[track].add(new MidiEvent(message, currentTick()));
		}
	}

	private class Running extends State implements Runnable {

		private long initialTick;

		private long startMillis;

		private int[] indices;

		private Thread thread;

		public Running() {
			initialTick = currentTick;

			indices = new int[tracks.length];
			for (int t = 0; t < tracks.length; t++) {
				indices[t] = SequenceUtils.getIndex(tracks[t], initialTick);
			}

			startMillis = System.currentTimeMillis();
			thread = new Thread(this);

			onStarting();

			thread.start();
		}

		public long currentTick() {
			if (thread == null) {
				return currentTick;
			} else {
				return initialTick
						+ millisToTick(System.currentTimeMillis() - startMillis);
			}
		}

		@Override
		public long totalTicks() {
			return Math.max(sequence.getTickLength(), currentTick());
		}

		public synchronized void run() {
			while (thread != null) {
				playCurrentEvents();
				
				try {
					MidiEvent event = nextEvent();
					if (event == null) {
						onLast();
						wait();
					} else {
						long sleepMillis = startMillis
								+ tickToMillis(event.getTick() - initialTick)
								- System.currentTimeMillis();
						if (sleepMillis > 0) {
							wait(sleepMillis);
						}
					}
				} catch (InterruptedException interrupted) {
				}
			}

			notifyAll();
		}

		private void playCurrentEvents() {
			currentTick = currentTick();
			for (int track = 0; track < tracks.length; track++) {
				playEvents(track);
			}
		}

		private void playEvents(int track) {
			while (indices[track] < tracks[track].size()) {
				MidiEvent event = tracks[track].get(indices[track]);

				if (SequenceUtils.isEndOfTrack(event.getMessage())) {
					break;
				}
				
				if (event.getTick() > currentTick) {
					break;
				}

				onPlayed(track, event.getMessage());
				indices[track]++;
			}
		}

		private MidiEvent nextEvent() {
			MidiEvent nextEvent = null;

			for (int t = 0; t < tracks.length; t++) {
				if (indices[t] < tracks[t].size()) {
					MidiEvent event = tracks[t].get(indices[t]);
					if (!SequenceUtils.isEndOfTrack(event.getMessage())) {
						if (nextEvent == null
								|| event.getTick() < nextEvent.getTick()) {
							nextEvent = event;
						}
					}
				}
			}

			return nextEvent;
		}

		@Override
		public synchronized void record(int track, MidiMessage message) {
			playCurrentEvents();

			tracks[track].add(new MidiEvent(message, currentTick));
			indices[track]++;
		}

		@Override
		public synchronized void stopping() {
			playCurrentEvents();
			
			thread.interrupt();
			thread = null;
			try {
				wait();
			} catch (InterruptedException interrupted) {
			}
			
			super.stopping();

			SequenceUtils.shrink(sequence);
			
			// step behind last tick
			currentTick = Math.min(currentTick, sequence.getTickLength()) + 1;
		}
	}
}