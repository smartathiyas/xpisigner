package org.oregan.pfx;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;

public class PFXUpdate
{
    private static final String ffVersion = "2.0.0.3";
    private static final String CERT_EXTNS = "cer,crt,pem,der,ber";
    private static final String CHAIN_EXTNS = "p7b,p7s,p7c";

    public static void main(String[] args) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, CRLException
    {
        Security.addProvider(new BouncyCastleProvider());
        try
        {
            if (args.length < 2)
            {
                usage();
                System.exit(1);
            }


            String pfxFile = args[0];
            String pfxPassword = args[1];

            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");

            System.out.print("Loading the default root certificates shipped with Firefox " + ffVersion + "...");
            System.out.flush();
            Set<TrustAnchor> firefoxRootCerts = loadTrustedCertificates();
            System.out.println("done. Loaded " + firefoxRootCerts.size() + " certificates");
            ArrayList<X509Certificate> additional = new ArrayList<X509Certificate>();

            if (args.length >= 4)
            {
                if (args.length % 2 == 0)
                {
                    System.out.println("Adding additional certificates from command line...");
                    for (int i = 3; i < args.length; i += 2)
                    {
                        String pathname = args[i];
                        String ext = pathname.substring(pathname.lastIndexOf('.') + 1).toLowerCase();

                        File additionalFile = new File(pathname);

                        if (CERT_EXTNS.indexOf(ext) >= 0)
                        {
                            System.out.println("Loading certificate from: " + additionalFile);
                            additional.add(loadCertificate(cf, additionalFile));
                        } else if (CHAIN_EXTNS.indexOf(ext) >= 0)
                        {
                            System.out.println("Loading certificate chain from: " + additionalFile);
                            additional.addAll(loadCertificates(cf, additionalFile));
                        } else
                        {
                            System.out.println("Unrecognised extension '" + ext + "'");
                            System.out.println("Known certificate extensions: " + CERT_EXTNS);
                            System.out.println("Known certificate chain extensions: " + CHAIN_EXTNS);
                        }
                    }
                } else
                {
                    usage();
                    System.exit(1);
                }
            }
            System.out.print("Loading your PFX...");
            System.out.flush();
            KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
            char[] password = pfxPassword.toCharArray();
            ks.load(new FileInputStream(pfxFile), password);
            System.out.println("done.");
            System.out.println("Analysing contents...");
            ArrayList<PrivateKey> keyList = new ArrayList<PrivateKey>();
            ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();

            Enumeration en = ks.aliases();
            String keyAlias = "";
            while (en.hasMoreElements())
            {
                String alias = (String) en.nextElement();
                if (ks.isKeyEntry(alias))
                {
                    keyList.add((PrivateKey) ks.getKey(alias, password));
                    keyAlias = alias;
                }
                if (ks.isCertificateEntry(alias))
                {
                    certList.add((X509Certificate) ks.getCertificate(alias));
                }
            }

            certList.addAll(additional);

            if (keyAlias == null || keyAlias.length() == 0)
            {
                System.out.println("No private key found, cannot continue!");
                System.exit(2);
            }
            if (keyList.size() > 1)
            {
                System.out.println("More than one private key found, cannot continue!");
                System.exit(2);

            }

            System.out.println("Found private key : '" + keyAlias + "'");

            X509Certificate myCert = (X509Certificate) ks.getCertificate(keyAlias);

            System.out.println("Private key matches certificate: '" + myCert.getSubjectDN() + "'");

            CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters(certList);

            CertStore store = CertStore.getInstance("Collection", ccsp, "BC");

//                Searching for rootCert by subjectDN without CRL


            CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX", "BC");
            X509CertSelector targetConstraints = new X509CertSelector();
            targetConstraints.setSubject(myCert.getSubjectX500Principal().getEncoded());

            PKIXBuilderParameters params = new PKIXBuilderParameters(firefoxRootCerts, targetConstraints);
            params.setRevocationEnabled(false);
            params.addCertStore(store);

            try
            {
                PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) cpb.build(params);

                System.out.println("Found path from your certificate to a trusted root certificate");

                CertPath path = result.getCertPath();
                List<? extends Certificate> workingPath = path.getCertificates();

                X509Certificate[] array = new X509Certificate[workingPath.size()];

                for (int i = 0; i < workingPath.size(); i++)
                {
                    X509Certificate certificate = (X509Certificate) workingPath.get(i);
                    array[i] = certificate;
                }

                dumpChain(array);

                KeyStore newKeyStore = KeyStore.getInstance("PKCS12", "BC");
                newKeyStore.load(null, null);
                newKeyStore.setKeyEntry(keyAlias, ks.getKey(keyAlias, password), password, array);
                for (int i = 1; i < array.length; i++)
                {
                    X509Certificate x509Certificate = array[i];
                    newKeyStore.setCertificateEntry("Cert" + (i), x509Certificate);
                }

                File inFile = new File(pfxFile);
                File outDir = inFile.getParentFile();
                File outFile = new File(outDir, "updated.pfx");
                if (outFile.exists())
                {
                    outFile.delete();
                    outFile = new File(outDir, "updated.pfx");
                }

                newKeyStore.store(new FileOutputStream(outFile), password);
                System.out.println();
                System.out.println("Stored updated PFX as " + outFile.getAbsolutePath());
                System.exit(0);

            } catch (CertPathBuilderException e)
            {
                System.out.println();
                System.out.println("Error: Can't find a path from your certificate to a trusted root");

                System.out.println("Require the certificate issued to:");
                System.out.println();
                System.out.println("'" + myCert.getIssuerDN() + "'");
                System.out.println();
                System.out.println("Export this certificate from Internet Explorer or Firefox and");
                System.out.println("re-run PFXUdate with the '-add <certfile>' option");

            } catch (InvalidAlgorithmParameterException e)
            {
                //e.printStackTrace();
            }

        } catch (NoSuchProviderException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (CertificateException e)
        {
            e.printStackTrace();
        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e)
        {


        } catch (InvalidAlgorithmParameterException e)
        {
            e.printStackTrace();
        }


    }

    private static void dumpChain(X509Certificate[] array)
    {
        String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
        int indent = 0;
        System.out.println("Certificate chain - root to end entity...");
        for (int i = array.length - 1; i >= 0; i--)
        {
            if (i == array.length - 1)
            {
                System.out.println(tabs.substring(0, indent++) + array[i].getIssuerDN() + " (Root Store)");
            }

            System.out.println(tabs.substring(0, indent++) + array[i].getSubjectDN());

        }

    }

    private static void usage()
    {
        System.out.println("PFXUpdate - Prepares your digital id for signing Firefox extensions");
        showFFVersion();
        System.out.println("");
        System.out.println("Usage");
        System.out.println("pfxupdate.cmd <pfxfile> <password> [-add <filename>");
        System.out.println();

        System.out.println("pfxfile         PFX or PKCS#12 key store exported from browser.");
        System.out.println("password        The password protecting pfxfile, or \"\" if there is no password");
        System.out.println();
        System.out.println("You can add missing certificates to your pfx to make up a working chain.");
        System.out.println("Export certificates from the browser either individually (cer, crt files)");
        System.out.println("or as certificate chains (p7c, p7b files.");
        System.out.println("Multiple -add <filename> can be used.");
        System.out.println("");
        System.out.println("Examples");
        System.out.println("  pfxupdate.cmd c:\\certs\\myId.pfx pa55word");
        System.out.println("  pfxupdate.cmd c:\\certs\\myId.pfx pa55word -add c:\\certs\\intermediate.crt");
        System.out.println("  pfxupdate.cmd c:\\certs\\myId.pfx pa55word -add c:\\certs\\chain.p7b");
            }

    private static void showFFVersion()
    {
        System.out.println("Using certificate list from Firefox v" + ffVersion);
    }

    private static Collection<? extends X509Certificate> loadCertificates(CertificateFactory cf, File additionalFile) throws FileNotFoundException, CertificateException
    {
        return (Collection<? extends X509Certificate>) cf.generateCertificates(new FileInputStream(additionalFile));
    }

    private static Set<TrustAnchor> loadTrustedCertificates() throws NoSuchProviderException, CertificateException, IOException
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        Set<TrustAnchor> certs = new HashSet<TrustAnchor>();

        Class<PFXUpdate> aClass = PFXUpdate.class;
        int i = 0;

        URL url;
        do
        {
            url = aClass.getResource("/firefox/" + ffVersion + "/cacert." + i + ".crt");
            if (url != null)
            {
                X509Certificate tmp = (X509Certificate) cf.generateCertificate(url.openStream());
                certs.add(new TrustAnchor(tmp, null));
            }
            i++;
        } while (url != null);
        return certs;
    }

    private static Set<TrustAnchor> loadTrustedCertificatesHDD() throws NoSuchProviderException, CertificateException, IOException
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        Set<TrustAnchor> certs = new HashSet<TrustAnchor>();
        File trustedFolder = new File("firefox_2.0.0.3");
        File[] certFiles = trustedFolder.listFiles();
        for (int i = 0; i < certFiles.length; i++)
        {
            File certFile = certFiles[i];
            X509Certificate cert = loadCertificate(cf, certFile);
            certs.add(new TrustAnchor(cert, null));
        }
        return certs;
    }

    private static X509Certificate loadCertificate(CertificateFactory cf, File certFile)
            throws CertificateException, FileNotFoundException
    {
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream(certFile));
        return cert;
    }
}
