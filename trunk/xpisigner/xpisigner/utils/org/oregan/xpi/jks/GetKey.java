package org.oregan.xpi.jks;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Copyright 2001-2005 Similarity Vector Technologies (Sivtech) Ltd. trading as
 * Similarity Systems. All rights reserved. ATHANOR and the Similarity Systems
 * logo are trademarks or registered trademarks of
 * Similarity Systems, Wilson House, Fenian Street, Dublin 2, Ireland.
 * All rights reserved.
 * <p/>
 * This software is the confidential and proprietary information of SiVTech.
 * ("Confidential Information").  You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with SiVTech.
 */
public class GetKey
{
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableKeyException
    {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("c:\\koregan\\xpisigner\\out.jks"),"nagerok".toCharArray());

        PrivateKey key  = (PrivateKey) ks.getKey("mykey","nagerok".toCharArray());

        System.out.println("key = " + key);

        FileOutputStream out = new FileOutputStream("c:\\koregan\\xpisigner\\key.bin");
        out.write(key.getEncoded());
        out.close();


    }
}
