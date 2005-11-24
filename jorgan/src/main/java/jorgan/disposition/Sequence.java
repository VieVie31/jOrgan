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

public class Sequence extends Counter {

  protected boolean canReference(Class clazz) {
    return Combination.class == clazz;  
  }

  protected void removeReference(Reference reference) {
      super.removeReference(reference);
      
      setCurrent(0);
  }
  
  protected void combinationGet(Combination combination) {
    
    setCurrent(references.indexOf(getReference(combination)));
  }

  protected void change(int delta) {
    if (getReferenceCount() == 0) {
      setCurrent(0);
    } else {
      int current = (getCurrent() + delta) % getReferenceCount();
      
      setCurrent(current);
          
      Reference reference = getReference(current);
      ((Combination)reference.getElement()).recall();
    }
  }
}