package org.oregan.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.io.File;

class ContentRootPanel extends JPanel
{
    private Color selectedColour = new Color(222, 242, 255);
    private Color deselectedColour = Color.white;
    boolean selected = false;
    private ContentRootInfo info;
    private JList pane;
    private JLabel path;
    private JPanel content;

    public ContentRootPanel(ContentRootInfo info)
    {
        this.info = info;
        init();
    }

    private void init()
    {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        String absPath = info.getRootFile().getAbsolutePath();

        path = new JLabel(absPath);
        path.setFont(path.getFont().deriveFont(Font.BOLD));
        path.setOpaque(false);
        add(path, BorderLayout.NORTH);

        content = new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createTitledBorder("Excluded"));

        File[] allex = info.getExclusions();
        for (int i = 0; i < allex.length; i++)
        {
            File file = allex[i];
            String relPath = file.getAbsolutePath().substring(absPath.length() + 1);
            content.add(new AListCellRenderer(relPath,this));
        }
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);

    }

    public Dimension getPreferredSize()
    {
        Dimension dimension = super.getPreferredSize();
        int height = (int) (path.getPreferredSize().getHeight()
            + content.getPreferredSize().getHeight()
            + 10);
        return new Dimension((int) dimension.getWidth(), height);
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        if (selected)
        {
            setBackground(selectedColour);
        } else
        {
            setBackground(deselectedColour);
        }
        this.selected = selected;
    }
}

class AListCellRenderer extends JPanel
{

    public AListCellRenderer(String ex, ContentRootPanel parent)
    {
        JLabel name = new JLabel();
        setOpaque(false);
        setLayout(new BorderLayout());
        add(name,BorderLayout.WEST);
        name.setText(ex);
    }
}