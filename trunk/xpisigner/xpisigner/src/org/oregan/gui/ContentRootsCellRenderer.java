package org.oregan.gui;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

class ContentRootsCellRenderer extends DefaultListCellRenderer
{

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        if (value instanceof ContentRootInfo)
        {
            ContentRootInfo contentRootInfo = (ContentRootInfo) value;
            ContentRootPanel rootPanel = new ContentRootPanel(contentRootInfo);
            rootPanel.setSelected(isSelected);
            return rootPanel;
        } else
        {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
