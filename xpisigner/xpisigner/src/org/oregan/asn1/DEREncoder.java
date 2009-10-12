package org.oregan.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Stack;

public class DEREncoder
{
    public static byte[] encode(TLV root) throws IOException
    {


        int length = root.getLength();
        int lol = root.getLengthOfLength();
        int totalLen = 1 + lol + length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(totalLen);

        Stack st = new Stack();

        buildTraversalList(root, st);

        traverseAndEncode(st, baos);

        return baos.toByteArray();
    }

    /**
     * Traverse the stack created by buildTraversalList() and encode each item.
     * @param st
     * @param baos
     * @throws IOException
     */
    private static void traverseAndEncode(Stack st, ByteArrayOutputStream baos) throws IOException
    {
        EncodableTLV tlv = (EncodableTLV) st.pop();
        baos.write(tlv.getTag());
        baos.write(ASN1.encodeLength(tlv.getLength()));

        if (tlv == st.peek())
        {
            //encode self
            baos.write(tlv.getValue());
        } else
        {
            while (tlv != ((TLV) st.peek()))
            {
                traverseAndEncode(st, baos);
            }
        }
        st.pop();
    }

    /**
     * Recursivly walk the node structure and populate a stack ordered from deepest to shallowest.
     * @param node
     * @param st
     * @return
     */
    private static int buildTraversalList(TLV node, Stack st)
    {
        st.push(node);
        int numChildren = node.getNumChildren();
        for (int i = numChildren - 1; i >= 0; i--)
        {
            buildTraversalList(node.getChild(i), st);
        }
        st.push(node);
        return numChildren;
    }


}
