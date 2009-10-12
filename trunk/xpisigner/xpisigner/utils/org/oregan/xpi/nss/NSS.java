package org.oregan.xpi.nss;

import org.mozilla.jss.CertDatabaseException;
import org.mozilla.jss.CryptoManager;
import org.mozilla.jss.JSSProvider;
import org.mozilla.jss.KeyDatabaseException;
import org.mozilla.jss.pkcs7.SignerInfo;
import org.mozilla.jss.pkcs7.IssuerAndSerialNumber;
import org.mozilla.jss.pkcs7.SignedData;
import org.mozilla.jss.pkcs7.ContentInfo;
import org.mozilla.jss.asn1.InvalidBERException;
import org.mozilla.jss.asn1.OBJECT_IDENTIFIER;
import org.mozilla.jss.asn1.SET;
import org.mozilla.jss.asn1.NULL;
import org.mozilla.jss.crypto.*;
import org.mozilla.jss.pkix.cert.Certificate;
import org.mozilla.jss.pkix.cert.CertificateInfo;
import org.mozilla.jss.util.Password;
import org.mozilla.jss.util.PasswordCallback;
import org.mozilla.jss.util.PasswordCallbackInfo;
import org.oregan.xpi.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.MessageDigest;

/**
 */
public class NSS
{
    public static void main(String[] args)
    {
        final String password = "nagerok";
        try
        {

            CryptoManager.initialize("C:\\Documents and Settings\\koregan\\Application Data\\Mozilla\\Firefox\\Profiles\\jxkd9wd2.default");

            CryptoManager cm = CryptoManager.getInstance();

            Security.addProvider(new JSSProvider());


            cm.setPasswordCallback(new PasswordCallback()
            {

                public Password getPasswordFirstAttempt(PasswordCallbackInfo passwordCallbackInfo) throws GiveUpException
                {
                    return new Password(password.toCharArray());
                }

                public Password getPasswordAgain(PasswordCallbackInfo passwordCallbackInfo) throws GiveUpException
                {
                    return new Password(password.toCharArray());
                }
            });

//            CryptoStore cryptoStore = cm.getInternalKeyStorageToken().getCryptoStore();
//            X509Certificate[] certs = cryptoStore.getCertificates();
            X509Certificate[] certs = cm.getCACerts();

            Certificate myCert = null;
            PrivateKey myKey = null;
            for (int j = 0; j < certs.length; j++)
            {
                X509Certificate cert = certs[j];
                System.out.println(cert.getSubjectDN());
                System.out.println();

                byte[] der = cert.getEncoded();

                Utils.saveMessage(der,"y:\\firefox\\2.0.0.7\\cacert." + j + ".crt");

//
//                myCert = (Certificate) new Certificate.Template().decode(new ByteArrayInputStream(der));
//                try
//                {
//                    if ((myKey = cm.findPrivKeyByCert(cert)) != null)
//                    {
//                        break;
//                    }
//                } catch (ObjectNotFoundException e)
//                {
//                }
            }

//            CertificateInfo info = myCert.getInfo();
//            System.out.println("Found " + info.getSubject().getRFC1485() + "  " + myKey);
//
//            IssuerAndSerialNumber iasn = new IssuerAndSerialNumber(info.getIssuer(), info.getSerialNumber());
//            OBJECT_IDENTIFIER dataOID = ContentInfo.DATA;
//
//            byte[] message = "this is the message".getBytes();
//
//            byte[] hash = MessageDigest.getInstance("SHA1").digest(message);
//
//            SignatureAlgorithm sha1RsaOID = SignatureAlgorithm.RSASignatureWithSHA1Digest;
//
//            SignerInfo si = new SignerInfo(iasn,null, null, dataOID, hash, sha1RsaOID,myKey);
//
//            SET digAlgSET = new SET();
//            digAlgSET.addElement(DigestAlgorithm.SHA1.toOID());
//
//            ContentInfo contentInfo = new ContentInfo(ContentInfo.DATA, null);
//
//            SET certSET = new SET();
//            certSET.addElement(myCert);
//            SET crlSet = null;
//
//            SET signerInfos = new SET() ;
//            signerInfos.addElement(si);
//
//            SignedData sd = new SignedData(digAlgSET, contentInfo, certSET, crlSet, null);
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            sd.encode(baos);
//            Utils.saveMessage(baos.toByteArray(),"c:/baos.ber");
//

        } catch (KeyDatabaseException e)
        {
            e.printStackTrace();
        } catch (CertDatabaseException e)
        {
            e.printStackTrace();
        } catch (AlreadyInitializedException e)
        {
            e.printStackTrace();
        } catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        } catch (CryptoManager.NotInitializedException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
