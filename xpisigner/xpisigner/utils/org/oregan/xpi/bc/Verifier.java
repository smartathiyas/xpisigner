package org.oregan.xpi.bc;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cms.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.oregan.xpi.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 */
public class Verifier
{
    private static final String META_INF_ZIGBERT_RSA = "META-INF/zigbert.rsa";
    private static final String META_INF_ZIGBERT_SF = "META-INF/zigbert.sf";
    private static final String META_INF_MANIFEST_MF = "META-INF/manifest.mf";
    private static final String MD5_DIGEST = "MD5-Digest";
    private static final String SHA1_DIGEST = "SHA1-Digest";


    public static void main(String[] args) throws ParserConfigurationException
    {
        new Verifier().Report(new File(args[0]), "http://o-regan.org/adblock.xpi");
    }

    private Hashtable<String, Object> report = new Hashtable<String, Object>();


    public void Report(File xpi, String originalURI) throws ParserConfigurationException
    {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("VerificationReport");
        doc.appendChild(root);

        Element properties = doc.createElement("Properties");
        root.appendChild(properties);
        properties.setAttribute("CreatedOn", new Date().toString()); //todo dateformatter
        properties.setAttribute("CreatedBy", "Kevin O'Regan http://o-regan.org");


        Element xpiEl = doc.createElement("XPI");
        root.appendChild(xpiEl);
        xpiEl.setAttribute("name", xpi.getName());
        xpiEl.setAttribute("size", "" + xpi.length());
        xpiEl.setAttribute("uri", originalURI);


        try
        {
            Element files = doc.createElement("Files");
            xpiEl.appendChild(files);

            JarFile jf = new JarFile(xpi,false);  // false is to not try verifying the jar.

            ArrayList<String> entryPaths = new ArrayList<String>();
            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements())
            {
                JarEntry jarEntry = en.nextElement();
                Element file = doc.createElement("XPIEntry");
                files.appendChild(file);
                file.setAttribute("name", jarEntry.getName());
                file.setAttribute("originalSize", "" + jarEntry.getSize());
                file.setAttribute("compressedSize", "" + jarEntry.getCompressedSize());
                file.setAttribute("CRC", "" + jarEntry.getCrc());
                file.setAttribute("filetime", "" + jarEntry.getTime());
                entryPaths.add(jarEntry.getName());
            }

            Element checks = doc.createElement("Checks");
            xpiEl.appendChild(checks);
            Element verification = doc.createElement("Verification");
            xpiEl.appendChild(verification);

            if (entryPaths.contains(META_INF_ZIGBERT_RSA))
            {
                verification.setAttribute("hasSignature","true");


                boolean ziggyFirst = (META_INF_ZIGBERT_RSA.compareTo(entryPaths.get(0)) == 0);
                Element fileOrder = doc.createElement("Order");
                Element zigbertEl = doc.createElement("ZIGBERT_RSA");
                zigbertEl.setAttribute("isFirst", "" + ziggyFirst);
                fileOrder.appendChild(zigbertEl);
                checks.appendChild(fileOrder);


                ZipEntry pkcs7Signature = jf.getEntry(META_INF_ZIGBERT_RSA);
                ByteArrayOutputStream tmpP7 = new ByteArrayOutputStream();
                InputStream in = jf.getInputStream(pkcs7Signature);
                Utils.slurp(in, tmpP7);
//
                ZipEntry signManifest = jf.getEntry(META_INF_ZIGBERT_SF);
                ByteArrayOutputStream tmpSF = new ByteArrayOutputStream();
                in = jf.getInputStream(signManifest);
                Utils.slurp(in, tmpSF);
//
                byte[] signature = tmpP7.toByteArray();
                byte[] content = tmpSF.toByteArray();
//
                boolean verifies = verify(signature, content, verification, doc);
//
                System.out.println("Signature on signature manifest:" + verifies);
//
                ArrayList<String> verifiedEntries = new ArrayList<String>();
                verifies = checkConsistency(jf, verifiedEntries, checks, doc);
                System.out.println("Signature manifest matches manifest:" + verifies);


                entryPaths.remove(META_INF_MANIFEST_MF);
                entryPaths.remove(META_INF_ZIGBERT_RSA);
                entryPaths.remove(META_INF_ZIGBERT_SF);

                if (entryPaths.size() != verifiedEntries.size())
                {
                    System.out.println("Mismatched count of manifest versus actual contents");
                }
                if (!Arrays.equals(entryPaths.toArray(), verifiedEntries.toArray()))
                {
                    System.out.println("Mismatched entries");
                }
            }else
            {
               verification.setAttribute("hasSignature","false");
            }
            try
            {
                XMLSerializer serializer = new XMLSerializer();
                serializer.setOutputCharStream(
                        new java.io.FileWriter(new File(xpi.getAbsoluteFile() + ".xml")));
                serializer.serialize(doc);

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

//        System.out.println("report = " + report);

    }

    private boolean checkConsistency(JarFile jarFile, ArrayList<String> verifiedEntries, Element checks, Document root) throws NoSuchAlgorithmException, IOException
    {
        Element manifestEl = root.createElement("Manifest");
        checks.appendChild(manifestEl);


        ZipEntry javaManifest = jarFile.getEntry(META_INF_MANIFEST_MF);
        InputStream manifest = jarFile.getInputStream(javaManifest);

        ZipEntry zigbertManifest = jarFile.getEntry(META_INF_ZIGBERT_SF);
        InputStream zigbert = jarFile.getInputStream(zigbertManifest);

        XPIManifest javaOne = new XPIManifest(manifest);
        XPIManifest signedOne = new XPIManifest(zigbert);

        XPIManifest.Entry entry = null;
        XPIManifest.Entry sigEntry = null;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        boolean isOK = true;
        try
        {
            // Read the Manifest Headers first...
            entry = javaOne.readEntry();
            sigEntry = signedOne.readEntry();
            if (!compareEntries(entry, sigEntry))
            {
                isOK = false;
            }
            Element isHeaderOK = root.createElement("Header");
            manifestEl.appendChild(isHeaderOK);
            isHeaderOK.setAttribute("passed", "" + isOK);

            // All the rest should have files associated with them.
            while ((entry = javaOne.readEntry()) != null)
            {
                boolean entryOK = true;
                String key = "Name";
                if (entry.containsKey(key))
                {
                    String fileName = entry.getValue(key);

                    Element isFileOK = root.createElement("File");
                    isFileOK.setAttribute("path", (fileName));
                    manifestEl.appendChild(isFileOK);

                    sigEntry = signedOne.readEntry();
                    if (!compareEntries(entry, sigEntry))
                    {
                        entryOK = false;
                        isFileOK.setAttribute("manifestCheck", "false");
                    }

                    ZipEntry zipEntry = jarFile.getEntry(fileName);
                    if (zipEntry == null)// Not found
                    {
                        entryOK = false;
                        isFileOK.setAttribute("existsInArchive", "false");

                    }
                    md5.reset();
                    sha1.reset();

                    InputStream inputStream = jarFile.getInputStream(zipEntry);
                    DigestInputStream in = new DigestInputStream(inputStream, md5);
                    DigestInputStream in2 = new DigestInputStream(in, sha1);
                    Utils.drain(in2);
                    in2.close();

                    String calcMD5 = Utils.toB64(in.getMessageDigest().digest());
                    String calcSHA1 = Utils.toB64(in2.getMessageDigest().digest());

                    if (calcMD5.compareTo(entry.getValue(MD5_DIGEST)) != 0)
                    {
                        entryOK = false;
                        isFileOK.setAttribute("md5EntryMatchesCalculated", "false");

                    }
                    if (calcSHA1.compareTo(entry.getValue(SHA1_DIGEST)) != 0)
                    {
                        entryOK = false;
                        isFileOK.setAttribute("sha1EntryMatchesCalculated", "false");

                    }
                    if (entryOK)
                        verifiedEntries.add(fileName);
                    if (!entryOK)
                        isOK = entryOK;
                    isFileOK.setAttribute("digestsMatch", "" + entryOK);
                }
            }
        } catch (IOException e)
        {
            return false;
        }
        return isOK;
    }

    private boolean compareEntries(XPIManifest.Entry entry, XPIManifest.Entry sigEntry)
    {
        String md5H = entry.getMD5Hash();
        String sigVal = sigEntry.getValue(MD5_DIGEST);
        String sha1H = entry.getSHA1Hash();
        String sha1Value = sigEntry.getValue(SHA1_DIGEST);

        return md5H.compareTo(sigVal) == 0 && sha1H.compareTo(sha1Value) == 0;
    }

    public boolean verify(byte[] bytes, byte[] bytes2, Element verification, Document root)
    {

        Element p7 = root.createElement("P7Signature");
        verification.appendChild(p7);

        Security.addProvider(new BouncyCastleProvider());
        try
        {
            CMSProcessableByteArray data = new CMSProcessableByteArray(bytes2);

            CMSSignedData sd = new CMSSignedData(data, bytes);
            CertStore certsAndCrls = sd.getCertificatesAndCRLs("Collection", "BC");
            Collection<? extends Certificate> allCerts = certsAndCrls.getCertificates(new X509CertSelector());
            Element certificates = root.createElement("Certificates");
            p7.appendChild(certificates);
            for (Iterator<? extends Certificate> iterator = allCerts.iterator(); iterator.hasNext();)
            {
                X509Certificate certificate = (X509Certificate) iterator.next();
                Element certEl = showCertificate(root, certificate);
                certificates.appendChild(certEl);
            }

            SignerInformationStore signers = sd.getSignerInfos();
            Collection c = signers.getSigners();
            Element signerInfoEl = root.createElement("SignerInfo");
            p7.appendChild(signerInfoEl);

            signerInfoEl.setAttribute("size", "" + signers.size());

            boolean anyValid = false;
            for (Object aC : c)
            {

                SignerInformation signer = (SignerInformation) aC;
                Element signerEl = root.createElement("Signer");
                signerInfoEl.appendChild(signerEl);


                SignerId signerId = signer.getSID();
                Element iasn = root.createElement("IssuerAndSerialNumber");
                iasn.setAttribute("issuer", signerId.getIssuerAsString());
                iasn.setAttribute("serial", signerId.getSerialNumber().toString(16));

                signerEl.appendChild(iasn);


                Collection certCollection = certsAndCrls.getCertificates(signerId);
                Iterator certIt = certCollection.iterator();
                X509Certificate cert = (X509Certificate) certIt.next();

                boolean result = signer.verify(cert, "BC");
                signerEl.setAttribute("verifies", "" + result);

                System.out.println("");
                if (!anyValid)
                    anyValid = result;

                Element aa = root.createElement("AuthenticatedAttributes");
                signerEl.appendChild(aa);

                Element ua = root.createElement("UnauthenticatedAttributes");
                signerEl.appendChild(ua);

                // Authenticated Att
                AttributeTable signedAttrs = signer.getSignedAttributes();
                if (signedAttrs != null)
                    parseAttributes(signedAttrs, root, aa);

                AttributeTable unAuthAttrs = signer.getUnsignedAttributes();
                if (unAuthAttrs != null)
                    parseAttributes(unAuthAttrs, root, ua);


            }

            return anyValid;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private void parseAttributes(AttributeTable signedAttrs, Document root, Element aa)
    {
        Hashtable hashtable = signedAttrs.toHashtable();
        Enumeration en = hashtable.keys();
        while (en.hasMoreElements())
        {
            DERObjectIdentifier oidStr = (DERObjectIdentifier) en.nextElement();
            String name = "OID_" + oidStr.getId().replace('.', '_');
            Element oid = root.createElement(name);
            aa.appendChild(oid);

            Attribute attr = (Attribute) hashtable.get(oidStr);
            ASN1Set values = attr.getAttrValues();
            for (int i = 0; i < values.size(); i++)
            {
                Element val = root.createElement("Value");
                oid.appendChild(val);
                DEREncodable derEncodable = values.getObjectAt(i);
                val.appendChild(root.createTextNode(derEncodable.toString()));
            }

        }
    }

    private Element showCertificate(Document root, X509Certificate cert)
    {
        Element certificate = root.createElement("X509Certificate");
        certificate.setAttribute("version", "" + cert.getVersion());
        Element subject = root.createElement("SubjectDN");
        subject.appendChild(root.createTextNode(cert.getSubjectDN().getName()));
        certificate.appendChild(subject);
        Element issuer = root.createElement("IssuerDN");
        issuer.appendChild(root.createTextNode(cert.getIssuerDN().getName()));
        certificate.appendChild(issuer);

        Element serialNumber = root.createElement("SerialNumber");
        certificate.appendChild(serialNumber);
        serialNumber.appendChild(root.createTextNode(cert.getSerialNumber().toString(16)));

        return certificate;
    }

}

class XPIManifest extends InputStream
{
    private InputStream in;
    private MessageDigest md5;
    private MessageDigest sha;


    public XPIManifest(InputStream in)
    {
        this.in = in;
        try
        {
            md5 = MessageDigest.getInstance("MD5");
            sha = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e)
        {
            //
        }

    }

    public int read() throws IOException
    {
        return in.read();
    }

    public Entry readEntry() throws IOException
    {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        int h = 0;
        while (true)
        {
            h = i;
            i = in.read();
            if (i == -1)
            {
                break;
            }
            if ((h == '\n') && (i == '\n'))
            {
                break;
            }
            buf.append((char) i);

        }
        if (buf.length() == 0)
            return null;

        final Entry entry = new Entry(buf.toString());
//        System.out.println(entry);
        return entry;
    }

    class Entry
    {
        private String md5Hash;
        private String sha1Hash;
        private Properties props = new Properties();

        Entry(String input) throws IOException
        {
            byte[] bytes = null;
            try
            {
                bytes = input.getBytes("LATIN1");
            } catch (UnsupportedEncodingException e)
            {
                //
            }
            md5.reset();
            sha.reset();
            md5Hash = Utils.toB64(md5.digest(bytes));
            sha1Hash = Utils.toB64(sha.digest(bytes));

            String[] lines = input.trim().split("\n");
            for (int i = 0; i < lines.length; i++)
            {
//                System.out.println(lines[i]);
                String[] nameVal = lines[i].split(":", 2);
                if (nameVal != null && nameVal.length == 2)
                    props.setProperty(nameVal[0].trim(), nameVal[1].trim());
//                else
//                    System.err.println(nameVal + "/" + lines[i]);
            }
        }

        public String getMD5Hash()
        {
            return md5Hash;
        }

        public String getSHA1Hash()
        {
            return sha1Hash;
        }

        public String getValue(String key)
        {
            return props.getProperty(key);
        }

        public String toString()
        {
            return props.toString() + " " + md5Hash + " " + sha1Hash;
        }

        public boolean containsKey(String key)
        {
            return props.containsKey(key);
        }
    }
}


