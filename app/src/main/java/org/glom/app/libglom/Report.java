package org.glom.app.libglom;

import org.glom.app.libglom.layout.LayoutGroup;

public class Report extends Translatable {
    private LayoutGroup layoutGroup = new LayoutGroup();

    /**
     * @return
     */
    public LayoutGroup getLayoutGroup() {
        return layoutGroup;
    }

    /**
     * @param listLayoutGroups
     */
    public void setLayoutGroup(final LayoutGroup layoutGroup) {
        this.layoutGroup = layoutGroup;
    }
}
