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
package jorgan.gui.construct;

import java.awt.*;
import java.util.prefs.*;

import jorgan.config.prefs.*;

/**
 * Configuration of the construct package.
 */
public class Configuration extends PreferencesConfiguration {

  private static final int     GRID  = 10;  
  private static final Color   COLOR = Color.blue;

  private static Configuration sharedInstance = new Configuration();

  private int     grid;
  private Color   color;

  protected void restore(Preferences prefs) {
    grid  = getInt  (prefs, "grid", GRID);
    color = getColor(prefs, "color", COLOR);
  }

  protected void backup(Preferences prefs) {
    putInt  (prefs, "grid", grid);
    putColor(prefs, "color", color);
  }

  public int getGrid() {
    return grid;
  }

  public Color getColor() {
    return color;
  }

  public void setGrid(int grid) {
    this.grid = grid;
    
    fireConfigurationChanged();
  }

  public void setColor(Color color) {
    this.color = color;
    
    fireConfigurationChanged();
  }

  /**
   * Get the shared configuration.
   *
   * @return configuration
   */
  public static Configuration instance() {
    return sharedInstance;
  }
}