package org.oregan.xpi.bc;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.oregan.xpi.XPIException;
import org.oregan.xpi.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 */
public class XPISignerImpl extends org.oregan.xpi.XPISigner
{
    private static final String OID_SHA1 = "1.3.14.3.2.26";

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public XPISignerImpl(String pfxfile, String password, String baseDir, ArrayList listing, String outputFile)
    {
        super(pfxfile, password, baseDir, listing, outputFile);
    }


    public File checkLocation(String location, String password)
    {
        File locP12 = new File(location);
        if (locP12.exists())
            return locP12;
        return null;
    }

    public byte[] sign(byte[] zbytes, File pfxfile, String password) throws XPIException
    {
        try
        {
            char[] cpassword = password.toCharArray();

            KeyStore ks;

            long start = System.currentTimeMillis();
            try
            {
                ks = KeyStore.getInstance("PKCS12", "BC");
                ks.load(new FileInputStream(pfxfile), cpassword);
            } catch (KeyStoreException e)
            {
                e.printStackTrace();
                throw new XPIException(e.getMessage(), Main.ERR_KEY_LOAD);
            } catch (NoSuchProviderException e)
            {
                e.printStackTrace();

                throw new XPIException(e.getMessage(), Main.ERR_KEY_LOAD);
            } catch (IOException e)
            {
                e.printStackTrace();

                if (e.getMessage().equalsIgnoreCase("PKCS12 key store mac invalid - wrong password or corrupted file."))
                {
                    throw new XPIException("Incorrect password", Main.ERR_BAD_PASSWORD);
                }
                throw new XPIException(e.getMessage(), Main.ERR_KEY_LOAD);
            } catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();

                throw new XPIException(e.getMessage(), Main.ERR_KEY_LOAD);
            } catch (CertificateException e)
            {
                e.printStackTrace();

                throw new XPIException(e.getMessage(), Main.ERR_KEY_LOAD);
            }
            long end = System.currentTimeMillis();
            println("Loaded PKCS#12 in " + (end - start) + "ms");
            X509Certificate myCert = null;
            PrivateKey myKey = null;

            Enumeration en = ks.aliases();
            while (en.hasMoreElements())
            {
                String alias = (String) en.nextElement();
                if (ks.isKeyEntry(alias))
                {
                    myCert = (X509Certificate) ks.getCertificate(alias);
                    myKey = (PrivateKey) ks.getKey(alias, cpassword);
                    break;
                }
            }
            if (myKey == null)
            {
                throw new XPIException("No private key found in " + pfxfile, Main.ERR_NO_SIGNING_KEY);
            }
            if (myCert == null)
            {
                throw new XPIException("No certificate found for private key", Main.ERR_KEY_CERT_MISMATCH);
            }

            signerDN = myCert.getSubjectDN().toString();

            ArrayList<Certificate> certs = new ArrayList<Certificate>();
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements())
            {
                String s = aliases.nextElement();
                Certificate[] chain = ks.getCertificateChain(s);
                if (chain != null)
                {
                    for (int i = 0; i < chain.length; i++)
                    {
                        Certificate certificate = chain[i];
                        if (!certs.contains(certificate))
                            certs.add(certificate);
                    }
                }
                Certificate certificate = ks.getCertificate(s);
                if(!certs.contains(certificate))
                    certs.add(certificate);
            }


            CollectionCertStoreParameters storeParameters = new CollectionCertStoreParameters(certs);
            CertStore cs = CertStore.getInstance("Collection", storeParameters, "BC");

            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSigner(myKey, myCert, OID_SHA1);

            generator.addCertificatesAndCRLs(cs);

            CMSProcessableByteArray byteArray = new CMSProcessableByteArray(zbytes);

            CMSSignedData signedData = generator.generate(PKCSObjectIdentifiers.data.getId(), byteArray, false, "BC");

            return signedData.getEncoded();

        } catch (Exception e)
        {
            e.printStackTrace();
            
            throw new XPIException("Signing failed:" + e.getMessage(),Main.ERR_SIGNING_FAILED);
        }
    }

    
}
