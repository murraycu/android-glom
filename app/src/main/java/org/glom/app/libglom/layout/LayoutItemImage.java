package org.glom.app.libglom.layout;

import org.glom.app.libglom.DataItem;

public class LayoutItemImage extends LayoutItemWithFormatting {
    // TODO: Use byte[] instead?
    private DataItem image = null;

    public DataItem getImage() {
        return image;
    }

    public void setImage(final DataItem image) {
        this.image = image;
    }
}
