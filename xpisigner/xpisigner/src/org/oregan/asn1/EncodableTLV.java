package org.oregan.asn1;

import java.util.Enumeration;


public class EncodableTLV extends TLV
{

    private byte[] value = null;
    private byte[] encodedLength = null;
    private byte tag;

    public EncodableTLV()
    {
        super(false);
    }

    public EncodableTLV(byte tag)
    {
        this(tag, false);
    }

    public EncodableTLV(byte tag, boolean optional)
    {
        this.tag = tag;
        setOptional(optional);
    }

    public EncodableTLV(byte tag, byte[] value)
    {
        this.tag = tag;
        setValue(value);
    }

    public TLV addChild(TLV sprog)
    {
        if (sprog instanceof EncodableTLV)
        {
            super.addChild(sprog);
        }else
        {
            byte atag = sprog.getValueRef()[sprog.getTagOffset()];
            byte[] avalue = new byte[sprog.getLength()];
            System.arraycopy(sprog.getValueRef(), sprog.getValueOffset(), avalue, 0, avalue.length);

            EncodableTLV tmp = new EncodableTLV(atag,avalue);
            super.addChild(tmp);
        }
        invalidateLengths();
        return sprog;
    }

    protected void calcLengths()
    {
        if (getValueRef() != null)
        {
            super.calcLengths();
        } else
        {
            if (children.size() == 0)
            {

                length = getValue().length;
                encodedLength = ASN1.encodeLength(length);
                lengthOfLength = encodedLength.length;
            } else
            {
                Enumeration en = children.elements();
                length = 0;
                while (en.hasMoreElements())
                {
                    TLV tlv = (TLV) en.nextElement();
                    length += (1 + tlv.getLengthOfLength() + tlv.getLength());
                }
                encodedLength = ASN1.encodeLength(length);
                lengthOfLength = encodedLength.length;
            }
        }
    }

    public String toString()
    {
        return "TLV [[0x" + Integer.toHexString(getTag()) + "][" + getLength() + "][..]@" + getInstanceID() + "]";
    }

    public int hashCode()
    {
        return getValue().hashCode() ^ getTag();
    }

    public boolean equals(Object obj)
    {
        EncodableTLV rhs = (EncodableTLV) obj;
        return Util.cmpByteArrays(getValue(), 0, rhs.getValue(), 0, getValue().length);
    }

    public byte[] getValue()
    {
        return (value == null ? new byte[0] : value);
    }

    public void setValue(byte[] value)
    {
        this.value = value;
        invalidateLengths();
    }

    public byte getTag()
    {
        if (this.valueRef == null)
            return this.tag;
        else
            return super.getTag();
    }

    public static EncodableTLV convert(TLV tlv)
    {
        EncodableTLV ret = new EncodableTLV(tlv.getTag());
        byte[] value = new byte[tlv.getLength()];
        System.arraycopy(tlv.getValueRef(),tlv.getValueOffset(),value,0,value.length);
        ret.setValue(value);
        return ret;
    }
}
