package org.oregan.xpi.tools;

import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.oregan.xpi.XPISigner;
import org.oregan.xpi.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Hashtable;

/**
 * Copyright Kevin O'Regan 2007
 */
public class GenIdentity {
    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, SignatureException, InvalidKeyException, UnrecoverableKeyException {
        Security.addProvider(new BouncyCastleProvider());
        generateCACertificate();
        generateSubCACertificate();
        generateCertificate();
    }

    private static void generateCACertificate() throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException, KeyStoreException, IOException, CertificateException {
        KeyPair kp = generateKeyPair();

        PublicKey pubKey = kp.getPublic();
        PrivateKey myKey = kp.getPrivate();

        Hashtable attrs = new Hashtable();

        attrs.put(X509Principal.C, "IE");
        attrs.put(X509Principal.O, "SMIMEr");
        attrs.put(X509Principal.OU, "Certificate Issuance");
        attrs.put(X509Principal.CN, "X SMIMEr Root CA");

        X509Principal x509Principal1 = new X509Principal(attrs);
        X509Principal x509Principal = x509Principal1;

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(198295));
        certGen.setIssuerDN(x509Principal);

        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.add(Calendar.YEAR, 20);

        certGen.setNotBefore(now.getTime());
        certGen.setNotAfter(then.getTime());

        certGen.setSubjectDN(x509Principal);
        certGen.setPublicKey(pubKey);

        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        certGen.addExtension("2.5.29.15", true,
                new X509KeyUsage(X509KeyUsage.digitalSignature | X509KeyUsage.keyCertSign));
        certGen.addExtension("2.16.840.1.113730.1.1", false,
                new NetscapeCertType(NetscapeCertType.objectSigningCA | NetscapeCertType.smimeCA | NetscapeCertType.sslCA));

        certGen.addExtension("2.5.29.19", true, new BasicConstraints(true));



        X509Certificate myCert = certGen.generate(myKey);
        Utils.saveMessage(myCert.getEncoded(), "XSMIMEr.root.cer");
        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        char[] cpassword = "nagerok".toCharArray();
        ks.load(null, null);

        ks.setKeyEntry("CA", kp.getPrivate(), cpassword, new Certificate[]{myCert});
        ks.store(new FileOutputStream("Xroot.pfx"), cpassword);
    }

    public static void generateSubCACertificate() throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException {

        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        char[] cpassword = "nagerok".toCharArray();
        ks.load(new FileInputStream("Xroot.pfx"), cpassword);

        PrivateKey caKey = (PrivateKey) ks.getKey("CA", cpassword);
        X509Certificate caCert = (X509Certificate) ks.getCertificate("CA");

        KeyPair kp = generateKeyPair();

        PublicKey pubKey = kp.getPublic();
        PrivateKey myKey = kp.getPrivate();

        Hashtable attrs = new Hashtable();

        attrs.put(X509Principal.C, "IE");
        attrs.put(X509Principal.O, "SMIMEr");
        attrs.put(X509Principal.OU, "Code Signing");
        attrs.put(X509Principal.CN, "X SMIMEr CodeSigning CA");

        X509Principal x509Principal1 = new X509Principal(attrs);
        X509Principal x509Principal = x509Principal1;

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(19215));
        certGen.setIssuerDN(x509Principal);

        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.add(Calendar.YEAR, 20);

        certGen.setNotBefore(now.getTime());
        certGen.setNotAfter(then.getTime());

        certGen.setSubjectDN(x509Principal);
        certGen.setPublicKey(pubKey);

        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        certGen.addExtension("2.5.29.15", true,
                new X509KeyUsage(X509KeyUsage.digitalSignature | X509KeyUsage.keyCertSign));
        certGen.addExtension("2.16.840.1.113730.1.1", false,
                new NetscapeCertType(NetscapeCertType.objectSigningCA
                        | NetscapeCertType.smimeCA
                        | NetscapeCertType.sslCA));

        certGen.addExtension("2.5.29.19", true, new BasicConstraints(true));

        certGen.setIssuerDN((X509Name) caCert.getSubjectDN());

        X509Certificate myCert = certGen.generate(caKey);
        Utils.saveMessage(myCert.getEncoded(), "XSMIMEr.sub.cer");
        ks.load(null, null);
        ks.setKeyEntry("SubCA", kp.getPrivate(), cpassword, new Certificate[]{myCert,caCert});
        ks.store(new FileOutputStream("Xsub.pfx"), cpassword);

    }
    public static void generateCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException {

        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        char[] cpassword = "nagerok".toCharArray();
        ks.load(new FileInputStream("Xsub.pfx"), cpassword);

        PrivateKey caKey = (PrivateKey) ks.getKey("SubCA", cpassword);
        X509Certificate caCert = (X509Certificate) ks.getCertificate("SubCA");


        KeyPair kp = generateKeyPair();

        PublicKey pubKey = kp.getPublic();
        PrivateKey myKey = kp.getPrivate();

        Hashtable attrs = new Hashtable();

        attrs.put(X509Principal.C, "IE");
        attrs.put(X509Principal.O, "O'Regan dot org");
        attrs.put(X509Principal.CN, "X XPI Signer");
        attrs.put(X509Principal.E, "xpisigner@o-regan.org");

        X509Principal x509Principal = new X509Principal(attrs);

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(999));
        certGen.setIssuerDN(new X509Principal(caCert.getSubjectDN().toString()));

        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.roll(Calendar.YEAR, true);

        certGen.setNotBefore(now.getTime());
        certGen.setNotAfter(then.getTime());

        certGen.setSubjectDN(x509Principal);
        certGen.setPublicKey(pubKey);

        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        certGen.addExtension("2.5.29.15", true,
                new X509KeyUsage(X509KeyUsage.digitalSignature));
        certGen.addExtension("2.16.840.1.113730.1.1", false,
                new NetscapeCertType(NetscapeCertType.objectSigning));

        X509Certificate myCert = certGen.generate(caKey);
        Utils.saveMessage(myCert.getEncoded(), "Xobjcert.cer");
        KeyStore ks2 = KeyStore.getInstance("PKCS12", "BC");
        char[] cpassword2 = "nagerok".toCharArray();
        ks2.load(null, null);

        ks2.setKeyEntry("obj", myKey, cpassword2, new Certificate[]{myCert, caCert});

        ks2.store(new FileOutputStream("Xuser.pfx"), cpassword2);
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

}
