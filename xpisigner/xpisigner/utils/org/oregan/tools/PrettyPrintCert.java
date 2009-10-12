package org.oregan.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: koregan
 * Date: Feb 1, 2008
 * Time: 10:50:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrettyPrintCert {
    public static void main(String[] args) throws IOException {

        FileInputStream fis = new FileInputStream(args[0]);
        int read = 0;
        byte[] bytes = new byte[8];

        System.out.println("byte[] certifiate = new byte[] {");

        while ((read = fis.read(bytes)) >= 0) {
            for (int i = 0; i < read; i++) {
                if (i > 0)
                    System.out.print(", ");
                System.out.print("(byte)0x" + toHexString(bytes,i,1));

            }
            System.out.println(",");


        }
        System.out.println("};");


    }

    public static final String hex = "0123456789abcdef";
    public static final char[] chex = hex.toCharArray();


    public static String toHexString(byte[] data, int offset, int length)
    {
        // prevents NullPointerException, ArrayOutOfBoundsException
        // and NegativeIndexException.
        if (data == null || data.length == 0 || offset >= data.length || length > data.length || offset + length
                > data.length || offset < 0 || length < 0)
        {
            return new String();
        }
        char[] chars = new char[length * 2];
        for (int i = 0; i < length; ++i)
        {
            chars[i * 2] = chex[(data[offset + i] >> 4) & 0xf];
            chars[i * 2 + 1] = chex[data[offset + i] & 0xf];
        }
        return new String(chars);
    }
}
