package org.oregan.asn1;

import java.util.Enumeration;
import java.util.Vector;


public class TLV implements Vistable
{
    private static int instanceCount = 0;

    private int instanceID = 0;

    private int tagOffset = 0;

    private int lengthOffset = 0;

    private int valueOffset = 0;

    protected byte[] valueRef;

    protected int length = -1;

    protected int lengthOfLength = -1;

    protected Vector children = new Vector();

    private boolean optional = false;

    private TLV parent = null;

    public TLV()
    {
        this(false);
    }

    public TLV(boolean temporaryTLV)
    {
        if (temporaryTLV)
            instanceID = -instanceCount;
        else
            instanceID = instanceCount++;
    }

    public TLV addChild(TLV sprog)
    {
        children.addElement(sprog);
        sprog.setParent(this);
        return sprog;
    }

    public TLV getChild(int index)
    {
        return (TLV) children.elementAt(index);
    }

    public int getNumChildren()
    {
        return children.size();
    }

    public Enumeration enumeration()
    {
        return children.elements();
    }

    public int getLength()
    {
        if (length == -1)
            calcLengths();
        return length;
    }

    public int getLengthOfLength()
    {
        if (lengthOfLength == -1)
            calcLengths();
        return lengthOfLength;
    }

    protected void calcLengths()
    {
        if (getValueRef() != null)
        {
            lengthOfLength = ASN1.getLengthOfLength(getValueRef(), getLengthOffset());
            length = ASN1.getLength(getValueRef(), getLengthOffset());
        }
    }

    public String toString()
    {
        return "TLV [[0x" + Integer.toHexString(getTag()) + "][" + getLength() + "][..]@" + instanceID + "]";

    }

    public int hashCode()
    {
        return getValueRef().hashCode() ^ getTagOffset();
    }

    public boolean equals(Object obj)
    {
        TLV rhs = (TLV) obj;
        return Util.cmpByteArrays(getValueRef(), getTagOffset(), rhs.getValueRef(), rhs.getTagOffset(), 1
            + rhs.getLengthOfLength()
            + rhs.getLength());
    }

    public int getInstanceID()
    {
        return instanceID;
    }

    public int getTagOffset()
    {
        return tagOffset;
    }

    public void setTagOffset(int tagOffset)
    {
        this.tagOffset = tagOffset;
    }

    public int getLengthOffset()
    {
        return lengthOffset;
    }

    public void setLengthOffset(int lengthOffset)
    {
        this.lengthOffset = lengthOffset;
        invalidateLengths();
    }

    public int getValueOffset()
    {
        return valueOffset;
    }

    public void setValueOffset(int valueOffset)
    {
        this.valueOffset = valueOffset;
        invalidateLengths();
    }

    public byte[] getValueRef()
    {
        return valueRef;
    }

    public void setValueRef(byte[] valueRef)
    {
        this.valueRef = valueRef;
        invalidateLengths();
    }

    public void accept(Visitor v)
    {
        v.visit(this);
    }

    public byte getTag()
    {
        return valueRef[tagOffset];
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    protected void invalidateLengths()
    {
        length = -1;
        lengthOfLength = -1;
        if (parent != null)
            parent.invalidateLengths();
    }

    void setParent(TLV parent)
    {
        this.parent = parent;
    }

}
