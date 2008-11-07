/*
 * jOrgan - Java Virtual Pipe Organ
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
package jorgan.skin;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Style.
 */
public class Skin implements Resolver {

	private String name = "";

	private ArrayList<Style> styles = new ArrayList<Style>();

	private transient SkinSource source;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			name = "";
		}
		this.name = name;
	}

	public int getStyleCount() {
		return styles.size();
	}

	public String[] getStyleNames() {
		String[] names = new String[1 + styles.size()];

		for (int s = 0; s < styles.size(); s++) {
			names[s + 1] = getStyle(s).getName();
		}

		return names;
	}

	public Style getStyle(int index) {
		return styles.get(index);
	}

	public void addStyle(Style style) {
		styles.add(style);
	}

	public void setSource(SkinSource source) {
		this.source = source;
	}

	public SkinSource getSource() {
		return source;
	}

	public Skin getSkin() {
		return this;
	}

	public Style createStyle(String styleName) {

		for (int s = 0; s < styles.size(); s++) {
			Style style = styles.get(s);
			if (style.getName().equals(styleName)) {
				Style clone = (Style) style.clone();

				initResolver(clone);

				return clone;
			}
		}
		return null;
	}

	private void initResolver(Layer layer) {
		layer.setResolver(this);

		if (layer instanceof CompositeLayer) {
			CompositeLayer compositeLayer = (CompositeLayer) layer;

			for (Layer child : compositeLayer.getChildren()) {
				initResolver(child);
			}
		}
	}

	public URL resolve(String name) {
		String locale = Locale.getDefault().toString();

		int suffix = name.lastIndexOf('.');
		if (suffix == -1) {
			suffix = name.length();
		}

		while (!locale.isEmpty()) {
			String localized = name.substring(0, suffix) + "_" + locale
			+ name.substring(suffix);
			
			URL url = source.getURL(localized);
			if (probe(url)) {
				return url;
			}

			locale = locale.substring(0, Math.max(0, locale.lastIndexOf('_')));
		}
		return source.getURL(name);
	}

	private boolean probe(URL url) {
		URLConnection connection;
		try {
			connection = url.openConnection();

			return connection.getContentLength() > 0;
		} catch (IOException ex) {
			return false;
		}
	}
}