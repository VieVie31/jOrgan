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
package jorgan.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jorgan.gui.console.View;

/**
 * A text layer.
 */
public class TextLayer extends Layer {

    private static Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
    
    private int alignment = CENTER;
    
    private Font font = new Font("Arial", Font.PLAIN, 12);

    private Color color = Color.black;

    private boolean antialiased = false;

    private transient List lines;

    private transient int linesWidth;

    private transient int linesHeight;

    private String text;

    public Font getFont() {
        return font;
    }

    public Color getColor() {
        return color;
    }

    public boolean isAntialiased() {
        return antialiased;
    }

    /**
     * Calculate the width based on the text's width.
     * 
     * @return width    the text's width
     */
    protected int calcWidth() {
        return linesWidth;
    }

    /**
     * Calculate the height based on the text's height.
     * 
     * @return height   the text's heigth
     */
    protected int calcHeight() {
        return linesHeight;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font of label cannot be null");
        }
        this.font = font;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color of label cannot be null");
        }
        this.color = color;
    }

    public void setAntialiased(boolean b) {
        antialiased = b;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setView(View view) {
        super.setView(view);
        
        Matcher matcher = pattern.matcher(this.text);
        
        StringBuffer text = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = view.getText(key);
            if (value == null) {
                value = "";
            } else {
                value = value.trim();
            }
            matcher.appendReplacement(text, value);
        }
        matcher.appendTail(text);

        breakLines(text.toString().trim(), view.getConsolePanel().getFontMetrics(font));
    }

    /**
     * Break the text into lines.
     */
    protected void breakLines(String text, FontMetrics metrics) {

        lines = new ArrayList();
        linesWidth = 0;
        linesHeight = 0;

        char[] chars = text.toCharArray();
        int start = 0;
        int end = chars.length;

        int height = 0;

        // more characters left?
        while (start != end) {
            // take all remaining characters
            int length = end - start;

            // honour line break (multiple whitespace)
            int lineBreak = text.indexOf("  ", start);
            if (lineBreak != -1) {
                length = lineBreak - start;
            }

            // width exceeded?
            if (getWidth() > 0) {
                while (metrics.charsWidth(chars, start, length) > getWidth()) {

                    // seek word break (single whitespace)
                    int wordBreak = text.lastIndexOf(' ', start + length - 1);
                    if (length > 1) {
                        // seek intra word break (hyphen)
                        wordBreak = Math.max(wordBreak, text.lastIndexOf('-',
                                start + length - 2) + 1);
                    }

                    // wordBreak before start?
                    if (wordBreak <= start) {
                        // decrease length until width fits
                        while (length > 1
                                && metrics.charsWidth(chars, start, length) > getWidth()) {
                            length--;
                        }
                    } else {
                        // let wordBreak decide length
                        length = wordBreak - start;
                    }
                }
            }

            Line line = new Line(chars, start, length, metrics);
            height += line.ascent + line.descent;
            if (getHeight() > 0 && height > getHeight()) {
                return;
            }

            if (lines.size() > 0) {
                linesHeight += line.leading;
            }
            linesHeight += line.ascent + line.descent;
            linesWidth = Math.max(linesWidth, line.width);
            lines.add(line);

            start = start + length;

            // trim leading whitespace
            while (start < end && chars[start] == ' ') {
                start++;
            }
        }
    }

    protected void draw(Graphics2D g, int x, int y, int width, int height) {
        g.setFont(font);
        g.setColor(color);

        Object wasAntialiased = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (antialiased) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        if (alignment == CENTER || alignment == RIGHT || alignment == LEFT) {
            y += height / 2 - linesHeight / 2;
        } else if (alignment == BOTTOM || alignment == BOTTOM_RIGHT || alignment == BOTTOM_LEFT) {
            y += height - linesHeight;
        }

        for (int l = 0; l < lines.size(); l++) {
            Line line = (Line) lines.get(l);

            if (l > 0) {
                y += line.leading;
            }

            int alignedX = x;
            if (alignment == TOP || alignment == CENTER || alignment == BOTTOM) {
                alignedX = x + width / 2 - line.width / 2;
            } else if (alignment == RIGHT || alignment == TOP_RIGHT || alignment == BOTTOM_RIGHT) {
                alignedX = x + width - line.width;
            }

            line.draw(g, alignedX, y);

            y += line.ascent + line.descent;
        }
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, wasAntialiased);
    }

    public Object clone() {
        TextLayer clone = (TextLayer) super.clone();

        return clone;
    }

    private class Line {
        private String text;

        private int ascent;

        private int descent;

        private int leading;

        private int width;

        public Line(char[] chars, int start, int length, FontMetrics metrics) {
            this.text = new String(chars, start, length);

            this.ascent = metrics.getAscent();
            this.descent = metrics.getDescent();
            this.leading = metrics.getLeading();

            this.width = metrics.charsWidth(chars, start, length);
        }

        public void draw(Graphics2D g, int x, int y) {

            g.drawString(text, x, y + ascent);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}