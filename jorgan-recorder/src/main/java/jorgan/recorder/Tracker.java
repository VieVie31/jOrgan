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
package jorgan.recorder;

import javax.sound.midi.MidiMessage;

import jorgan.disposition.Element;
import jorgan.recorder.midi.Recorder;

/**
 * A tracker of a {@link Recorder}'s track.
 */
public interface Tracker {

	public int getTrack();

	public Element getElement();

	/**
	 * TODO setRecordEnabled
	 */
	public void setRecords(boolean recording);

	/**
	 * TODO isRecordEnabled
	 */
	public boolean records();

	/**
	 * TODO setPlayEnabled
	 */
	public void setPlays(boolean playing);

	/**
	 * TODO isPlayEnabled
	 */
	public boolean plays();

	public void destroy();

	public void onPlayStarting();

	public void onRecordStarting();

	public void onPlayStopping();

	public void onRecordStopping();

	public void onPlayed(MidiMessage message);
}