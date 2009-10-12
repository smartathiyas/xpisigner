package org.oregan.xpi.bc;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.Security;
import java.security.KeyPair;

/**
 * Copyright Informatica Corporation 2007
 */
public class PVK2PEM2PFX
{
    public static void main(String[] args) throws IOException, InterruptedException
    {

        File file = new File("cpp\\release\\pvk2.exe").getCanonicalFile();
        String infile = "y:\\pvkland\\my.pvk";
        String outfile = "y:\\pvkland\\my.pem";
        String pin = "nagerok";


        ProcessBuilder procBuilder = new ProcessBuilder(file.toString(), "-in", infile, "-out", outfile, "-pin", pin);

        Process proc = procBuilder.start();

        int result = proc.waitFor();

        System.out.println("ERRORCODE=" + result);









        Security.addProvider(new BouncyCastleProvider());
        PEMReader rdr = new PEMReader(new FileReader("y:\\pvkland\\4.pem"),new PasswordFinder(){

            public char[] getPassword()
            {
                return "nagerok".toCharArray();
            }
        });

        Object oo = rdr.readObject();
        if (oo instanceof KeyPair)
        {
            KeyPair keyPair = (KeyPair) oo;
            System.out.println("keyPair = " + keyPair.getPrivate());
            System.out.println("keyPair = " + keyPair.getPublic());
        }


    }


}
