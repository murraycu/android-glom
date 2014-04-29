package org.glom.app.libglom.layout;

import android.text.TextUtils;
import android.util.Log;

import org.glom.app.libglom.NumericFormat;

import java.io.Serializable;

public class Formatting implements Serializable {

    private static final long serialVersionUID = -2848253819745789939L;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO;
    // @formatter:on
    private int multilineHeightLines = 1;
    private String textFormatColorForeground = "";
    private String textFormatColorBackground = "";
    private NumericFormat numericFormat = new NumericFormat();

    /*
     * Converts a Gdk::Color (16-bits per channel) to an HTML color (8-bits per channel) by discarding the least
     * significant 8-bits in each channel.
     */
    private static String convertGdkColorToHtmlColor(final String gdkColor) {
        if (TextUtils.isEmpty(gdkColor)) {
            return "";
        }

        if (gdkColor.length() == 13) {
            return gdkColor.substring(0, 3) + gdkColor.substring(5, 7) + gdkColor.substring(9, 11);
        } else if (gdkColor.length() == 7) {
            // This shouldn't happen but let's deal with it if it does.
            Log.e("android-glom", "Expected a 13 character string but received a 7 character string. Returning received string.");
            return gdkColor;
        } else {
            Log.e("android-glom", "Did not receive a 13 or 7 character string. Returning black HTML color code.");
            return "#000000";
        }
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(final HorizontalAlignment alignment) {
        horizontalAlignment = alignment;
    }

    /**
     * Get the number of lines of text that should be displayed.
     *
     * @returns the number of lines of text
     */
    public int getTextFormatMultilineHeightLines() {
        return multilineHeightLines;
    }

    /**
     * Set the number of lines of text that should be displayed.
     *
     * @param value number of lines of text that should be displayed
     * @returns the number of lines of text
     */
    public void setTextFormatMultilineHeightLines(final int value) {
        this.multilineHeightLines = value;
    }

    /**
     * Get the foreground color to use for text when displaying a field value.
     * <p/>
     * This should be overridden by {@link GlomNumericFormat#setUseAltForegroundColorForNegatives(boolean)} if that is
     * active.
     *
     * @returns the text foreground color in GdkColor color format
     */
    public String getTextFormatColorForeground() {
        return textFormatColorForeground;
    }

    /**
     * Set the foreground color to use for text when displaying a field value.
     *
     * @param color the text foreground color in GdkColor color format
     */
    public void setTextFormatColorForeground(final String color) {
        this.textFormatColorForeground = color;
    }

    /*
     * Get the foreground color to use for text when displaying a field value.
     *
     * This should be overridden by {@link GlomNumericFormat#setUseAltForegroundColorForNegatives(boolean)} if that is
     * active.
     *
     * @returns the text foreground color in HTML color format
     */
    public String getTextFormatColorForegroundAsHTMLColor() {
        return convertGdkColorToHtmlColor(textFormatColorForeground);
    }

    /**
     * Get the background color to use for text when displaying a field value.
     *
     * @returns the text background color in GdkColor color format
     */
    public String getTextFormatColorBackground() {
        return textFormatColorBackground;
    }

    /**
     * Set the background color to use for text when displaying a field value.
     *
     * @param color a text background color in HTML color format
     */
    public void setTextFormatColorBackground(final String color) {
        this.textFormatColorBackground = color;
    }

    /**
     * Get the background color to use for text when displaying a field value.
     *
     * @returns the text background color in HTML color format
     */
    public String getTextFormatColorBackgroundAsHTMLColor() {
        return convertGdkColorToHtmlColor(textFormatColorBackground);
    }

    public NumericFormat getNumericFormat() {
        return numericFormat;
    }

    public void setNumericFormat(final NumericFormat numericFormat) {
        this.numericFormat = numericFormat;
    }

    // @formatter:off
    public enum HorizontalAlignment {
        HORIZONTAL_ALIGNMENT_AUTO, // For instance, RIGHT for numeric fields.
        HORIZONTAL_ALIGNMENT_LEFT,
        HORIZONTAL_ALIGNMENT_RIGHT
    }
}
