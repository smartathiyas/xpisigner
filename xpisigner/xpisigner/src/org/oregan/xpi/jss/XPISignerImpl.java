package org.oregan.xpi.jss;

import org.mozilla.jss.CryptoManager;
import org.mozilla.jss.JSSProvider;
import org.mozilla.jss.asn1.OBJECT_IDENTIFIER;
import org.mozilla.jss.asn1.OCTET_STRING;
import org.mozilla.jss.asn1.SEQUENCE;
import org.mozilla.jss.asn1.SET;
import org.mozilla.jss.asn1.UTCTime;
import org.mozilla.jss.crypto.CryptoStore;
import org.mozilla.jss.crypto.DigestAlgorithm;
import org.mozilla.jss.crypto.ObjectNotFoundException;
import org.mozilla.jss.crypto.PrivateKey;
import org.mozilla.jss.crypto.SignatureAlgorithm;
import org.mozilla.jss.crypto.X509Certificate;
import org.mozilla.jss.pkcs7.Attribute;
import org.mozilla.jss.pkcs7.ContentInfo;
import org.mozilla.jss.pkcs7.IssuerAndSerialNumber;
import org.mozilla.jss.pkcs7.SignedData;
import org.mozilla.jss.pkcs7.SignerInfo;
import org.mozilla.jss.pkix.cert.Certificate;
import org.mozilla.jss.pkix.cert.CertificateInfo;
import org.mozilla.jss.util.Password;
import org.mozilla.jss.util.PasswordCallback;
import org.mozilla.jss.util.PasswordCallbackInfo;
import org.oregan.xpi.Main;
import org.oregan.xpi.Utils;
import org.oregan.xpi.XPIException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 */
public class XPISignerImpl extends org.oregan.xpi.XPISigner
{
    public XPISignerImpl(String pfxfile, String password, String baseDir, ArrayList listing, String outputFile)
    {
        super(pfxfile, password, baseDir, listing, outputFile);
    }

    public byte[] sign(byte[] zbytes, File location, final String password) throws XPIException
    {
        try
        {
            if (!properties.containsKey("jss.initialised"))
                CryptoManager.initialize(location.getAbsolutePath());

            CryptoManager cm = CryptoManager.getInstance();

            Security.addProvider(new JSSProvider());

            if (properties.containsKey("jss.password.callback"))
            {
                cm.setPasswordCallback((PasswordCallback) properties.get("jss.password.callback"));
            } else
            {
                cm.setPasswordCallback(new PasswordCallback()
                {
                    public Password getPasswordFirstAttempt(PasswordCallbackInfo passwordCallbackInfo) throws GiveUpException
                    {
                        return new Password(password.toCharArray());
                    }

                    public Password getPasswordAgain(PasswordCallbackInfo passwordCallbackInfo) throws GiveUpException
                    {
                        throw new GiveUpException();
                    }
                });
            }
            CryptoStore cryptoStore = cm.getInternalKeyStorageToken().getCryptoStore();
            X509Certificate[] certs = cryptoStore.getCertificates();

            Certificate myCert = null;
            X509Certificate cert = null;

            PrivateKey myKey = null;
            for (int j = 0; j < certs.length; j++)
            {
                cert = certs[j];
                byte[] der = cert.getEncoded();

                myCert = (Certificate) new Certificate.Template().decode(new ByteArrayInputStream(der));
                try
                {
                    if ((myKey = cm.findPrivKeyByCert(cert)) != null)
                    {
                        break;
                    }
                } catch (ObjectNotFoundException e)
                {
                    System.out.println("e = " + e);
                }
            }


            if (myKey == null)
            {
                throw new XPIException("No private key found, password may be incorrect", Main.ERR_NO_SIGNING_KEY);
            }
            if (myCert == null)
            {
                throw new XPIException("No certificate found for private key", Main.ERR_KEY_CERT_MISMATCH);
            }

            CertificateInfo info = myCert.getInfo();


            this.signerDN = info.getSubject().getRFC1485();

            properties.put("signing.dn", signerDN);

            IssuerAndSerialNumber iasn = new IssuerAndSerialNumber(info.getIssuer(), info.getSerialNumber());

            OBJECT_IDENTIFIER dataOID = ContentInfo.DATA;

            byte[] hash = MessageDigest.getInstance("SHA1").digest(zbytes);

            SignatureAlgorithm sha1RsaOID = SignatureAlgorithm.RSASignatureWithSHA1Digest;

            SET authAttrs = new SET();

            Attribute contentType = new Attribute(new OBJECT_IDENTIFIER("1.2.840.113549.1.9.3"), ContentInfo.DATA);
            Attribute messageDigest = new Attribute(new OBJECT_IDENTIFIER("1.2.840.113549.1.9.4"), new OCTET_STRING(hash));
            Attribute signingTime = new Attribute(new OBJECT_IDENTIFIER("1.2.840.113549.1.9.5"), new UTCTime(new Date()));

            authAttrs.addElement(contentType);
            authAttrs.addElement(messageDigest);
            authAttrs.addElement(signingTime);

            SignerInfo si = new SignerInfo(iasn, authAttrs, null, dataOID, hash, sha1RsaOID, myKey);

            SET digAlgSET = new SET();
            SEQUENCE digAlgSeq = new SEQUENCE();
            digAlgSeq.addElement(DigestAlgorithm.SHA1.toOID());
            digAlgSET.addElement(digAlgSeq);

            ContentInfo contentInfo = new ContentInfo(ContentInfo.DATA, null);

            SET certSET = new SET();

            X509Certificate[] chain = cm.buildCertificateChain(cert);

            for (int i = 0; i < chain.length; i++)
            {
                X509Certificate x509Certificate = chain[i];
                byte[] der = x509Certificate.getEncoded();
                Certificate tmp = (Certificate) new Certificate.Template().decode(new ByteArrayInputStream(der));
                certSET.addElement(tmp);
            }

            SET crlSet = null;

            SET signerInfos = new SET();
            signerInfos.addElement(si);

            SignedData sd = new SignedData(digAlgSET, contentInfo, certSET, crlSet, signerInfos);

            ContentInfo content = new ContentInfo(ContentInfo.SIGNED_DATA, sd);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            content.encode(baos);

            return baos.toByteArray();
        } catch (Exception e)
        {
            throw new XPIException("Signing failed:" + e.getMessage(), Main.ERR_SIGNING_FAILED);
        }
    }

    public static File locateFirefox() throws IOException
    {
        Properties props = System.getProperties();

        String userHome = props.getProperty("user.home");

        File home = new File(userHome);

        File appData = new File(home, getFirefoxHomeDir());
        if (!appData.exists())
        {
            String alternate = System.getProperty("appdata");
            if (alternate != null)
            {
                File altAppData = new File(alternate);
                if (altAppData.exists())
                    appData = altAppData;
                else
                {
                    System.out.println("Failed to auto-find firefox at:");
                    System.out.println("\t" + appData);
                    System.out.println("\t" + alternate);
                }
            } else
            {
                System.out.println("Failed to auto-find firefox at:");
                System.out.println("\t" + appData);
            }
        }

        File firefoxHome = new File(appData, "Mozilla/Firefox");
        if (firefoxHome.exists())
        {
            File ffINI = new File(firefoxHome, "profiles.ini");

            int profileIndex = 0;

            //todo: Find the default, you've done this in Multisigner.

            Map map = Utils.slurpINI(ffINI);

            String profileKey = "Profile" + profileIndex;
            if (map.containsKey(profileKey))
            {
                Properties ffprops = (Properties) map.get(profileKey);
                if (ffprops.containsKey("Path"))
                {
                    return new File(ffINI.getParentFile(), (String) ffprops.get("Path")).getAbsoluteFile();
                }
            }
        } else
        {
            System.out.println("Failed to auto-find firefox at:");
            System.out.println("\t" + firefoxHome);
            System.out.println("Pass the location of your Firefox profile instead of \'-auto\'.");
            System.exit(Main.ERR_AUTO_FIND_FAILED);
        }
        return null;
    }

    private static String getFirefoxHomeDir()
    {
        String osname = System.getProperty("os.name");
        if(osname.startsWith("Windows"))
        {
            String appdata = System.getProperty("appdata");
            if(appdata==null)
                return "Application Data";
            else
                return appdata;
        }else if(osname.startsWith("Linux"))
        {
            return ".mozilla";
        }
        return ".mozilla";
    }

    public File checkLocation(String location, String password)
    {
        if (location == null)
            return null;
        if (location.equals("/auto") || location.equals("-auto"))
        {
            try
            {
                return locateFirefox();
            } catch (Exception e)
            {
                return null;
            }
        } else
        {
            File ffLocation = new File(location);
            File db = new File(ffLocation, "key3.db");
            if (db.exists())
                return ffLocation;
            return null;
        }
    }
}
