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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import jorgan.disposition.Element;

public class ElementUtils {

  protected static final ResourceBundle resources = ResourceBundle.getBundle("jorgan.gui.resources");
  
  private static Map typeNames = new HashMap();
  
  public static String getElementName(Element element) {

    String name = element.getName();
    if ("".equals(name)) {
      name = resources.getString("element.emptyName");                  
    }

    return name;
  }

  public static String getElementAndTypeName(Element element, boolean alphabetic) {

    String name = getElementName(element);

    String typeName = getTypeName(element.getClass());
    if (alphabetic) {
      return name + " - " + typeName;  
    } else {
      return typeName + " - " + name;  
    }
  }
  
  public static String getTypeName(Class clazz) {

    String typeName = (String)typeNames.get(clazz);
    if(typeName == null) {
      String className = clazz.getName();
      
      int packageIndex = className.lastIndexOf('.');

      className = Character.toLowerCase(className.charAt(packageIndex + 1)) + 
                  className.substring(packageIndex + 2);
                    
      typeName = resources.getString("construct.type." + className);
      
      typeNames.put(clazz, typeName);      
    }
    
    return typeName;
  }
}