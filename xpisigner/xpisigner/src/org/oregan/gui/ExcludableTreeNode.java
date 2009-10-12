package org.oregan.gui;

import javax.swing.tree.DefaultMutableTreeNode;

class ExcludableTreeNode extends DefaultMutableTreeNode
{
    boolean excluded = false;


    public ExcludableTreeNode()
    {
        super();
    }

    public ExcludableTreeNode(Object userObject)
    {
        super(userObject);
    }

    public ExcludableTreeNode(Object userObject, boolean allowsChildren)
    {
        super(userObject, allowsChildren);
    }

    public boolean isExcluded()
    {
        return excluded;
    }

    public void setExcluded(boolean excluded)
    {
        this.excluded = excluded;
    }
}
