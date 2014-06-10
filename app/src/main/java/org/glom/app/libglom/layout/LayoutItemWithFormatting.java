package org.glom.app.libglom.layout;

public class LayoutItemWithFormatting extends LayoutItem {
    private Formatting formatting = new Formatting(); // Not null, so we have some default formatting.

    public Formatting getFormatting() {
        return formatting;
    }

    public void setFormatting(final Formatting formatting) {
        this.formatting = formatting;
    }
}
