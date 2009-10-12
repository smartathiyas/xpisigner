package org.oregan.xpi;

import java.io.File;

public class XPIInfo
{
    private File file;
    private String signerDN;
    private int numEntries;

    public XPIInfo(File file, String signerDN, int numEntries)
    {
        this.file = file;
        this.signerDN = signerDN;
        this.numEntries = numEntries;
    }

    public File getFile()
    {
        return file;
    }

    public String getSignerDN()
    {
        return signerDN;
    }

    public int getNumEntries()
    {
        return numEntries;
    }
}
