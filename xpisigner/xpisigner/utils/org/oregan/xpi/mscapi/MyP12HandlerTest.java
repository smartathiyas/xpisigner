package org.oregan.xpi.mscapi;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.oregan.xpi.Utils;

import java.security.Security;
import java.security.KeyFactory;
import java.security.NoSuchProviderException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

import com.baltimore.jcrypto.JCryptoInit;
import com.baltimore.jcrypto.asn1.ASN1Exception;
import com.baltimore.jcrypto.coders.CoderException;
import com.baltimore.jpkiplus.pkcs7.content.SignedData;
import com.baltimore.jpkiplus.pkcs7.content.Data;
import sun.security.x509.X500Name;

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
public class MyP12HandlerTest
{
    public static void main(String[] args) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, SignatureException, InvalidKeyException
    {
        KeyStore store = KeyStore.getInstance("Windows-MY","SunMSCAPI");

        store.load(null,null);

        Enumeration<String> aliases = store.aliases();

        while (aliases.hasMoreElements())
        {
            String alias = aliases.nextElement();
            System.out.println( (store.isKeyEntry(alias)?"K":"C") + " " + alias);

            PrivateKey key = (PrivateKey) store.getKey(alias,null);
            System.out.println("key = " + key);

            System.out.println(key.getFormat());
            System.out.println(key.getAlgorithm());

            X509Certificate cert = (X509Certificate) store.getCertificate(alias);
            String subjectCN = ((X500Name)cert.getSubjectDN()).getCommonName();
            String issuerCN = ((X500Name)cert.getIssuerDN()).getCommonName();
             if(issuerCN == null)
                issuerCN = ((X500Name)cert.getIssuerDN()).getOrganizationalUnit();
            System.out.println(subjectCN + " | " + issuerCN + " | " + cert.getNotAfter() + " | " + alias);

        }
    }
}
