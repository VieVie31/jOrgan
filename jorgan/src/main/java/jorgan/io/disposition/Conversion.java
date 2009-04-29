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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Conversion {

	private static final Logger logger = Logger.getLogger(Conversion.class
			.getName());

	private static Conversion[] conversions = new Conversion[] {
			new Conversion("<organ>", "convert1.0To2.0-beta.xsl"),
			new Conversion("<organ *version=\"(2\\.0-beta.*|2\\.0-RC1)\" *>",
					"convert2.0-betaTo2.0.xsl"),
			new Conversion("<organ *version=\"2\\.0\" *>",
					"convert2.0To2.1-beta.xsl"),
			new Conversion("<organ *version=\"2\\.1-beta.*\" *>",
					"convert2.1-betaTo2.1.xsl"),
			new Conversion("<organ *version=\"2\\.1.*\" *>",
					"convert2.1To2.2-beta.xsl"),
			new Conversion("<organ *version=\"2\\.2-beta.*\" *>",
					"convert2.2-betaTo2.2.xsl"),
			new Conversion("<organ *version=\"2\\.2.*\" *>",
					"convert2.2To2.3-beta.xsl"),
			new Conversion("<organ *version=\"2\\.3-beta.*\" *>",
					"convert2.3-betaTo2.3.xsl"),
			new Conversion("<organ *version=\"2\\.3.*\" *>",
					"convert2.3To2.4-beta.xsl"),
			new Conversion("<organ *version=\"2\\.4-beta.*\" *>",
					"convert2.4-betaTo2.4.xsl"),
			new Conversion("<organ *version=\"2\\.4.*\" *>",
					"convert2.4To3.0-beta.xsl"),
			new Conversion("<organ *version=\"3\\.0-beta.*\" *>",
					"convert3.0-betaTo3.0.xsl"),
			new Conversion("<organ *version=\"3\\.0\" *>",
					"convert3.0To3.1.xsl"),
			new Conversion("<organ *version=\"3\\.[1|2].*\" *>",
					"convert3.1To3.3.xsl"),
			new Conversion("<organ *version=\"3\\.[3|4].*\" *>",
					"convert3.3To3.5-beta.xsl"),
			new Conversion("<organ *version=\"3\\.5-beta.*\" *>",
					"convert3.5-betaTo3.5.xsl"),
			new Conversion("<organ *version=\"3\\.5\" *>",
					"convert3.5To3.5.1.xsl") };

	private String pattern;

	private String xsl;

	public Conversion(String pattern, String xsl) {
		this.pattern = pattern;
		this.xsl = xsl;
	}

	public boolean isApplicable(String header) {
		Pattern pattern = Pattern.compile(this.pattern);
		Matcher matcher = pattern.matcher(header);

		return matcher.find();
	}

	public InputStream convert(InputStream in) throws TransformerException,
			IOException {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", new Integer(4));

		Transformer transform = factory.newTransformer(new StreamSource(
				Conversion.class.getResourceAsStream(xsl)));

		transform.setOutputProperty(OutputKeys.INDENT, "yes");

		File temp = File.createTempFile(xsl + ".", ".xml");

		transform.transform(new StreamSource(in), new StreamResult(temp));

		in.close();

		return new FileInputStream(temp);
	}

	public static InputStream convertAll(InputStream in)
			throws TransformerException, IOException {

		in = new BufferedInputStream(in);

		String header = getHeader(in);

		int index = 0;
		while (index < conversions.length) {
			if (conversions[index].isApplicable(header)) {
				logger.log(Level.INFO, "applicable '"
						+ conversions[index].pattern + "'");
				break;
			}
			index++;
		}

		while (index < conversions.length) {
			logger.log(Level.INFO, "applying '" + conversions[index].xsl + "'");

			in = conversions[index].convert(in);
			index++;
		}

		return in;
	}

	private static String getHeader(InputStream in) throws IOException {
		in.mark(2048);

		byte[] bytes = new byte[1024];
		int offset = 0;
		while (offset != -1 && offset < bytes.length) {
			offset = in.read(bytes, offset, bytes.length - offset);
		}

		in.reset();

		String header = new String(bytes, "UTF-8");

		return header;
	}
}