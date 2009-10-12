package org.oregan.gui;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import java.awt.Component;
import java.awt.Color;
import java.io.File;

class MyTreeCellRenderer extends DefaultTreeCellRenderer
{
    private ImageIcon certificate;
    private ImageIcon document;


    public MyTreeCellRenderer()
    {
        super();
        document = new ImageIcon(getClass().getResource("document.png"));
        certificate = new ImageIcon(getClass().getResource("certificate.png"));
        setLeafIcon(document);

    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {

        MyTreeCellRenderer rdr = (MyTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);


        ExcludableTreeNode currentNode = (ExcludableTreeNode) value;

        if (currentNode.getUserObject() instanceof File)
        {
            File f = (File) currentNode.getUserObject();
            rdr.setText(f.getName());

            if (f.isDirectory())
            {
                if(expanded)
                {
                    rdr.setIcon(getDefaultOpenIcon());
                }else
                {
                    rdr.setIcon(getDefaultClosedIcon());
                }
            } else
            {
                if (f.getName().endsWith(".cer")
                    || f.getName().endsWith(".crt"))
                {
                    rdr.setIcon(certificate);
                } else
                {
                    rdr.setIcon(document);
                }
            }
            if (currentNode.isExcluded())
            {
                setForeground(Color.gray);
            } else
            {
                setForeground(Color.black);
            }
        }
        return rdr;
    }
}
