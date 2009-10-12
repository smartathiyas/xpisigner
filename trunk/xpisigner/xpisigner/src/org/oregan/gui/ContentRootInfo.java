package org.oregan.gui;

import java.io.File;
import java.util.ArrayList;

class ContentRootInfo
{
    File rootPath;
    ArrayList<File> excluded;

    public ContentRootInfo(File rootPath)
    {
        this.rootPath = rootPath;
        excluded = new ArrayList<File>();
    }

    public void exclude(File path)
    {
        if(!excluded.contains(path))
           excluded.add(path);
    }

    public File[] getExclusions()
    {
        File[] ret = new File[excluded.size()];
        excluded.toArray(ret);
        return ret;
    }

    public File getRootFile()
    {
        return rootPath;
    }

    public String toString()
    {
        return rootPath.getAbsolutePath();
    }

    public void removeExclusion(File toBeExcluded)
    {
        excluded.remove(toBeExcluded);
    }
}
