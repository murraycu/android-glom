package org.glom.app.libglom.layout;

public class LayoutItemText extends LayoutItemWithFormatting {
    private StaticText text = new StaticText();

    public StaticText getText() {
        return text;
    }

    /**
     * @param text
     */
    public void setText(final StaticText text) {
        this.text = text;
    }

}
