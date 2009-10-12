package org.oregan.asn1;

import java.math.BigInteger;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;


public class X509CertificateHolder
{
    byte[] encoded;
    String friendlyName;
    private byte[] ID;
    public static String filename;
    private TLV thisCert;
    private TLV extensionsTLV;
    private boolean basicConstraintsIsCA = false;
    private int basicConstraintsPathLength = -1;
    private boolean parsedBC = false;

    public X509CertificateHolder(String friendlyName, byte[] encoded)
    {
        this.friendlyName = friendlyName;
        this.encoded = encoded;
    }

    public String getFriendlyName()
    {
        return friendlyName;
    }

    public byte[] getEncoded()
    {
        return encoded;
    }

    public EncodableTLV getEncodable()
    {
        int lol = ASN1.getLengthOfLength(encoded,1);

        byte[] value = new byte[encoded.length - 1 - lol];
        System.arraycopy(encoded,lol+1,value,0,value.length);

        EncodableTLV ret = new EncodableTLV(encoded[0],value);
        return ret;
    }

    public void setFriendlyName(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }

    public byte[] getID()
    {
        return ID;
    }

    public void setID(byte[] id)
    {
        this.ID = id;
    }

    public TLV getSubjectDN()
    {
        _decodeCert();
        int subjectIndex = (isV3()? 5 : 4);
        return thisCert.getChild(0).getChild(subjectIndex);
    }
    public TLV getIssuerDN()
    {
        _decodeCert();
        int issuerIndex = (isV3()?3:2);
        return thisCert.getChild(0).getChild(issuerIndex);
    }

    boolean isV3()
    {
        _decodeCert();
        return (thisCert.getChild(0).getChild(0).getTag()==(byte)0xA0);
    }

    private void __findExtensions()
    {
        _decodeCert();
        if (this.extensionsTLV != null)
            return;
        TLV tbs = thisCert.getChild(0);
        for (int i = 0; i < tbs.getNumChildren(); i++)
        {
            TLV extensions = tbs.getChild(i);
            byte tag = extensions.getValueRef()[extensions.getTagOffset()];

            //            System.out.println("tag & 0x80 = " + (tag & 0x80) + " " + ((tag & 0x80) == 0x80));
            if (((tag & 0x80) == 0x80) && ((tag & 0x1f) == 3))
            {
                this.extensionsTLV = extensions.getChild(0);
                return;
            }
        }
    }

    public boolean[] getKeyUsage()
    {
        __findExtensions();
        if (extensionsTLV != null)
        {
            for (int j = 0; j < extensionsTLV.getNumChildren(); j++)
            {
                TLV extension = extensionsTLV.getChild(j);
                if (ASN1.isOID(extension.getValueRef(), extension.getValueOffset(), extension.getLength(), ASN1.OIDsKeyUsage))
                {
                    int numChildren = extension.getNumChildren();
                    TLV octetString = extension.getChild(numChildren - 1);
                    TLV isBitString = octetString.getChild(0);
                    if (isBitString.getValueRef()[isBitString.getTagOffset()] == 0x03)
                    {
                        return ASN1.decodeBitString(isBitString);
                    }
                }

            }
        }


        //none found, thats ok
        return KU_ALL;
    }

    private void _decodeCert()
    {
        if (thisCert == null)
        {
            thisCert = DERParser.decode(this.encoded);
        }
    }

    /**
     * Returns true if BasicConstraints.isCA is true.
     * @return
     */
    public boolean isCA()
    {
        __findBasicConstraints();
        return this.basicConstraintsIsCA;
    }

    public int getPathLengthConstraint()
    {
        __findBasicConstraints();
        return this.basicConstraintsPathLength;
    }

    private void __findBasicConstraints()
    {
        __findExtensions();
        if (this.parsedBC)
            return;
        try
        {
            if (extensionsTLV != null)
            {
                for (int j = 0; j < extensionsTLV.getNumChildren(); j++)
                {
                    TLV extension = extensionsTLV.getChild(j);
                    TLV oid = extension.getChild(0);
                    if (ASN1.isOID(oid.getValueRef(), oid.getValueOffset(), oid.getLength(), ASN1.OIDsBasicConstraints))
                    {
                        int numChildren = extension.getNumChildren();
                        TLV octetString = extension.getChild(numChildren - 1);
                        TLV isCA = octetString.getChild(0).getChild(0);
                        if (isCA.getValueRef()[isCA.getValueOffset()] == 0xffffffff)
                        {
                            this.basicConstraintsIsCA = true;
                        }
                        if (isCA.getNumChildren() == 2)
                        {
                            byte[] tmpNum = new byte[isCA.getChild(1).getLength()];
                            System.arraycopy(isCA.getChild(1).getValueRef(), isCA.getChild(1).getValueOffset(), tmpNum, 0, tmpNum.length);
                                int pathLen = (new BigInteger(tmpNum)).intValue();
                                //                                System.out.println("pathLen = " + pathLen);
                                this.basicConstraintsPathLength = pathLen;

                        } else
                        {
                            //                            System.out.println(" no path length constraint.");
                        }
                    }
                }
            }
        }
        finally
        {
            this.parsedBC = true;

        }

    }

    public boolean cmpDNames(TLV subject, TLV issuer)
    {
        return subject.equals(issuer);
    }

    /*boolean isSelfSigned()
    {
        return
    }*/

    public static boolean[] KU_ALL = new boolean[]{true, true, true, true, true, true, true, true, true};
    public static final int KU_DigitalSignature = 0;
    public static final int KU_NonRepudiation = 1;
    public static final int KU_KeyEncipherment = 2;
    public static final int KU_DataEncipherment = 3;
    public static final int KU_KeyAgreement = 4;
    public static final int KU_KeyCertSign = 5;
    public static final int KU_CRLSign = 6;
    public static final int KU_EncipherOnly = 7;
    public static final int KU_DecipherOnly = 8;

    public static void main(String[] args) throws IOException
    {
        File filename = new File(args[0]);

        String[] files = filename.list();
        for (int i = 0; i < files.length; i++)
        {

            File file = new File(files[i]);
            if (!file.toString().endsWith(".pem"))
            {
                main_runt(file);
            }
        }

    }

    private static void main_runt(File filename) throws IOException
    {
        FileInputStream fis = new FileInputStream(filename);
        byte[] bytes = new byte[(int) filename.length()];
        fis.read(bytes);

        System.out.print(filename + "-->");

        if (bytes[0] == 's' || bytes[0] == '-')
        {
            System.out.println("PEM");
            return;
        }

        X509CertificateHolder kev = new X509CertificateHolder("kev", bytes);

        boolean[] ku = kev.getKeyUsage();
        System.out.print("KU{");
        for (int i = 0; i < ku.length; i++)
        {
            if (i != 0)
                System.out.print(",");
            System.out.print(i + ":" + ku[i]);
        }
        System.out.print("},BC{");

        System.out.print(kev.isCA());
        System.out.print(",");
        System.out.print(kev.getPathLengthConstraint());
        System.out.println("}");
    }

    public EncodableTLV getSerial()
    {
        _decodeCert();
        return EncodableTLV.convert((isV3()?
            thisCert.getChild(0).getChild(1):
            thisCert.getChild(0).getChild(0)));
    }
}
