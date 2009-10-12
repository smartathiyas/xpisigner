package org.oregan.tools;

import com.chrysalisits.crypto.LunaJCAProvider;

/**
 * Created by IntelliJ IDEA.
 * User: koregan
 * Date: Feb 18, 2009
 * Time: 9:12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class LunaSA
{
    public static void main(String[] args)
    {
        LunaJCAProvider prov = new LunaJCAProvider();
        System.out.println("prov = " + prov);
        System.out.println("prov.getName() = " + prov.getName());
    }
}
