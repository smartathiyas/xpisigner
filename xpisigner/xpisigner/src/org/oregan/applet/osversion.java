package org.oregan.applet;

import javax.swing.JApplet;

public class osversion extends JApplet
{
    private String osName;
    private String osVersion;

    public void init()
    {
        super.init();
        System.out.println("x");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        System.out.println(osName + " " + osVersion );
    }

    public String getOSName()
    {
        return osName;
    }

    public String getOSVersion()
    {
        return osVersion;
    }
}
