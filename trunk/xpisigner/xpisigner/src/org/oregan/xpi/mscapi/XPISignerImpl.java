package org.oregan.xpi.mscapi;

import org.oregan.asn1.ASN1;
import org.oregan.asn1.DEREncoder;
import org.oregan.asn1.EncodableTLV;
import org.oregan.asn1.X509CertificateHolder;
import org.oregan.xpi.Utils;
import org.oregan.xpi.XPIException;
import org.oregan.xpi.XPISigner;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

public class XPISignerImpl extends XPISigner
{
    public XPISignerImpl(String pfxfile, String password, String baseDir, ArrayList listing, String outputFile)
    {
        super(pfxfile, password, baseDir, listing, outputFile);
    }

    public byte[] sign(byte[] zbytes, File location, String password) throws XPIException
    {
        try
        {
            String alias = location.getName();


            KeyStore msCertStore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
            msCertStore.load(null, null);

            PrivateKey capiKeyRef = (PrivateKey) msCertStore.getKey(alias, null);
            X509Certificate[] chain = (X509Certificate[]) msCertStore.getCertificateChain(alias);


            EncodableTLV seq = new EncodableTLV(ASN1.SEQUENCE);
            seq.addChild(new EncodableTLV(ASN1.OBJECTIDENTIFIER, ASN1.OIDsSignedData));
            EncodableTLV ctx = new EncodableTLV((byte) 0xA0); //[0]
            seq.addChild(ctx);
            EncodableTLV seq2 = new EncodableTLV(ASN1.SEQUENCE);
            ctx.addChild(seq2);
            byte[] ONE = {1};
            seq2.addChild(new EncodableTLV(ASN1.INTEGER, ONE)); // Version
            // AlgID
            EncodableTLV algID = algorithmIdentifier();
            seq2.addChild(algID);
            seq2.addChild(new EncodableTLV(ASN1.SEQUENCE)).addChild(new EncodableTLV(ASN1.OBJECTIDENTIFIER, ASN1.OIDsData));

            EncodableTLV certChain = new EncodableTLV((byte) 0xA0);


            X509CertificateHolder hldr = null;
            for (int i = 0; i < chain.length; i++)
            {
                byte[] encoded = chain[i].getEncoded();
                hldr = new X509CertificateHolder("", encoded);
                EncodableTLV cert = hldr.getEncodable();
                certChain.addChild(cert);
                System.out.println("\t" + (i+1) + " cert = " + chain[i]);
            }
            seq2.addChild(certChain);


            EncodableTLV signerInfos = (new EncodableTLV(ASN1.SETOF));
            EncodableTLV signerInfo = new EncodableTLV(ASN1.SEQUENCE);
            signerInfo.addChild(new EncodableTLV(ASN1.INTEGER, ONE)); // Version
            // IASN

            EncodableTLV iasnSeq = new EncodableTLV(ASN1.SEQUENCE);
            hldr = new X509CertificateHolder(alias, chain[0].getEncoded());
            iasnSeq.addChild(hldr.getIssuerDN());
            iasnSeq.addChild(hldr.getSerial());

            signerInfo.addChild(iasnSeq);
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] messageDigest = sha1.digest(zbytes);

            // HashAlg
            byte[] oid = ASN1.OIDsSHA1;
            EncodableTLV param = new EncodableTLV(ASN1.NULL);

            EncodableTLV objId = new EncodableTLV(ASN1.SEQUENCE);
            objId.addChild(new EncodableTLV(ASN1.OBJECTIDENTIFIER, oid));
            objId.addChild(param);
            signerInfo.addChild(objId);

            // AuthAttrs
            EncodableTLV authAttrs = new EncodableTLV((byte) 0xA0);

            authAttrs.addChild(attrValue(ASN1.OIDsContentType, new EncodableTLV(ASN1.OBJECTIDENTIFIER, ASN1.OIDsData)));

            authAttrs.addChild(attrValue(ASN1.OIDsSigningTime, new EncodableTLV(ASN1.UTCTIME, Utils.toUTCTime(new Date()).getBytes())));

            authAttrs.addChild(attrValue(ASN1.OIDsMessageDigest, new EncodableTLV(ASN1.OCTETSTRING, messageDigest)));

            signerInfo.addChild(authAttrs);

            // When encoding for the signature this needs to be a SETOF not a Tagged Conntext Specific
            EncodableTLV authAttrsSeq = new EncodableTLV(ASN1.SETOF);

            authAttrsSeq.addChild(attrValue(ASN1.OIDsContentType, new EncodableTLV(ASN1.OBJECTIDENTIFIER, ASN1.OIDsData)));

            authAttrsSeq.addChild(attrValue(ASN1.OIDsSigningTime, new EncodableTLV(ASN1.UTCTIME, Utils.toUTCTime(new Date()).getBytes())));

            authAttrsSeq.addChild(attrValue(ASN1.OIDsMessageDigest, new EncodableTLV(ASN1.OCTETSTRING, messageDigest)));


            byte[] authAttrDER = DEREncoder.encode(authAttrsSeq);

            sha1.reset();

            byte[] encDigest = sha1.digest(authAttrDER);

            Signature signer = Signature.getInstance("SHA1withRSA", "SunMSCAPI");
            signer.initSign(capiKeyRef);

            signer.update(authAttrDER);
            byte[] signatureBytes = signer.sign();

            // SigAlgID
            EncodableTLV algIdRSA = new EncodableTLV(ASN1.SEQUENCE);
            algIdRSA.addChild(new EncodableTLV(ASN1.OBJECTIDENTIFIER, ASN1.OIDsRSAEncryption));
            algIdRSA.addChild(new EncodableTLV(ASN1.NULL));
            // Signature bytes

            signerInfo.addChild(algIdRSA);

            signerInfo.addChild(new EncodableTLV(ASN1.OCTETSTRING, signatureBytes));

            signerInfos.addChild(signerInfo);

            seq2.addChild(signerInfos);

            byte[] out = DEREncoder.encode(seq);
            return out;
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (NoSuchProviderException e)
        {
            e.printStackTrace();
        } catch (CertificateEncodingException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (SignatureException e)
        {
            e.printStackTrace();
        } catch (CertificateException e)
        {
            e.printStackTrace();
        } catch (InvalidKeyException e)
        {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e)
        {
            e.printStackTrace();
        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        }

        return new byte[0];
    }


    private EncodableTLV algorithmIdentifier()
    {

        byte[] oid = ASN1.OIDsSHA1;
        EncodableTLV param = new EncodableTLV(ASN1.NULL);

        EncodableTLV algID = new EncodableTLV(ASN1.SETOF);
        EncodableTLV objId = new EncodableTLV(ASN1.SEQUENCE);
        objId.addChild(new EncodableTLV(ASN1.OBJECTIDENTIFIER, oid));
        objId.addChild(param);
        algID.addChild(objId);
        return algID;
    }

    private EncodableTLV attrValue(byte[] oid, EncodableTLV value)
    {
        EncodableTLV seq = new EncodableTLV(ASN1.SEQUENCE);
        seq.addChild(new EncodableTLV(ASN1.OBJECTIDENTIFIER, oid));
        seq.addChild(new EncodableTLV(ASN1.SETOF)).addChild(value);
        return seq;
    }


    public File checkLocation(String location, String password)
    {
        return new File(location);
    }
}
