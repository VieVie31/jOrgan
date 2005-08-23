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
package jorgan.io.disposition;

import java.io.*;

import org.xml.sax.*;

import jorgan.disposition.*;
import jorgan.xml.*;
import jorgan.xml.handler.*;

public abstract class KeyableHandler extends ActivateableHandler {

  public KeyableHandler(AbstractReader reader, Attributes attributes) {
    super(reader, attributes);
  }

  public KeyableHandler(AbstractWriter writer, String tag) {
    super(writer, tag);
  }

  protected abstract Keyable getKeyable();

  protected Activateable getActivateable() {
    return getKeyable();
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes) {

    if ("action".equals(qName)) {
      new IntegerHandler(getReader()) {
        public void finished() {
          getKeyable().setAction(getInteger());
        }
      };
    } else if ("transpose".equals(qName) || "shift".equals(qName)) {
        new IntegerHandler(getReader()) {
          public void finished() {
            getKeyable().setTranspose(getInteger());
          }
        };
    } else {
      super.startElement(uri, localName, qName, attributes);
    }
  }

  public void children() throws IOException {
    super.children();

    new IntegerHandler(getWriter(), "action"   , getKeyable().getAction()).start();
    new IntegerHandler(getWriter(), "transpose", getKeyable().getTranspose()).start();
  }
}
