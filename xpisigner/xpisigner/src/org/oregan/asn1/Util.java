package org.oregan.asn1;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: koregan
 * Date: 20-Jun-2003
 * Time: 09:36:35
 * To change this template use Options | File Templates.
 */
public class Util
{
    public static final String hex = "0123456789abcdef";
    public static final char[] chex = hex.toCharArray();

    public static void showBytes(byte[] pfxBytes, int offset)
    {
        String hex = toHexString(pfxBytes, offset, 10);
        System.out.println(Integer.toHexString(offset) + offset + "\t\t" + hex);
    }

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

    /**
     * Converts a byte array to a string. Assumes that the byte array is made up of
     * the low bytes from a string; that is, if it is the Unicode encoding of a string
     * then use the getUnicodeString method. This method is preferred to using the
     * string constructor, which takes a byte array since it is independent of the
     * default encoding system.
     *
     * @param            bytes The byte array from which to dissect.
     * @param            offset The byte subarray offset.
     * @param            length The byte subarray length.
     * @return           A string.
     */
    public static String toString(byte[] bytes, int offset, int length)
    {
        // check to ensure that the byte array is not null
        // if it is null return empty string.
        if (bytes == null)
            return new String();

        try
        {
            char[] chars = new char[length];
            for (int i = 0; i < length; ++i)
            {
                chars[i] = (char) bytes[offset + i];
            }
            return new String(chars);
        } catch (ArrayIndexOutOfBoundsException ex)
        {
            return new String();
        } catch (NegativeArraySizeException ex)
        {
            return new String();
        }

    }

    /**
     * Converts a byte array a string. Assumes that the byte array is made up of
     * Unicode byte encoding from a string; that is, each pair of bytes make up a
     * character. We assume that the bytes are in big-endian format.
     * <p>
     * For example, the string "Pavement"  has ASCII encoding - 50 61 76 65 6D 65 6E
     * 74. The Unicode encoding (big endian) of this is 00 50 00 61 00 76 00 65 00 6D
     * 00 65 00 6E 00 74 (the most significant byte (MSB) is first in each pair
     * encoding, in this example, the ASCII byte is the least significant byte (LSB)).
     * Different operating systems use different endianess - most Unix system uses big
     * endianess while Microsoft uses little endianess. BMPString uses Unicode
     * encoding (without marking it) of strings. It requires that the bytes be ordered
     * in network byte order (that is, big endian). As a result all of the Unicode
     * related methods in J/CRYPTO encode strings (and expect string encodings) to be
     * big endian.
     *
     * @param            bytes The byte array from which to dissect.
     * @param            offset The byte subarray offset.
     * @param            length The byte subarray length.
     * @return           A string.
     */
    public static String toUnicodeString(byte[] bytes, int offset, int length)
    {
        // if byte array null return empty string
        if (bytes == null || length > bytes.length)
        {
            return new String();
        }

        char ac[] = new char[length / 2];
        for (int i = 0; i != ac.length; i++)
        {
            byte x = bytes[offset + (2 * i)];
            byte y = bytes[offset + (2 * i + 1)];
            ac[i] = (char) (x << 8 | y & 0xff);
        }

        return new String(ac);

    }

    public static void setParity(byte[] testArray)
    {
        // DR: need to check if the array is null
        // if the array is null simply return
        if (testArray == null)
        {
            return;
        }

        byte X, Y;
        byte[] local = testArray;
        int len = local.length;

        for (int i = 0; i < len; i++)
        {
            Y = local[i];
            X = local[i];

            for (int j = 0; j < 7; j++)
            {
                X >>= 1;
                Y ^= X;
            }

            Y &= 1;

            if (Y == 0)
                local[i] ^= 1;
        }
    }

    public static boolean cmpIntArrays(int[] int1, int[] int2)
    {
        boolean result = false;

        // DR: took out the check for the array's length == 0 as it was
        // returning false if both arrays had zero length even though
        // in that case they would be equal. This would get picked up
        // in any case in the loop below.

        // if either array is null, they are not equal
        if (int1 == null || int2 == null)
        {
            return result;
        }

        // if both arrays are zero in length, they are equal
        // if only one array is zero in length, they are not equal

        if (int1.length == int2.length)
        {
            result = true;
            int i = 0;
            while ((i < int1.length) & result)
            {
                result = ((int1[i] == int2[i]));
                i++;
            }
        }

        return result;
    }

    public static boolean cmpByteArrays(byte[] byte1, byte[] byte2)
    {
        boolean result = false;

        // DR: took out the check for the array's length == 0 as it was
        // returning false if both arrays had zero length even though
        // in that case they would be equal. This would get picked up
        // in any case in the loop below.

        // if either array is null, they are not equal
        if (byte1 == null || byte2 == null)
        {
            return result;
        }

        // if both arrays are zero in length, they are equal
        // if only one array is zero in length, they are not equal

        int i = 0;
        if (byte1.length == byte2.length)
        {
            result = true;
            while ((i < byte1.length) & result)
            {
                result = ((byte1[i] == byte2[i]));
                i++;
            }
        }

        return result;
    }


    public static long byteArrayToLong(byte[] buffer, int beginIndex)
    {

        // XXX: what do we do if buffer is null or begin index is negative??

        // if the buffer is null - return 0L
        // ensure this is the correct behaviour
        if (buffer == null || beginIndex < 0)
        {
            return 0L;
        }

        long one,two,three,four,five,six,seven,eight;

        if (beginIndex < buffer.length)
            one = (((long) buffer[beginIndex + 0] & 0x0ffL) << 56);
        else
            one = 0L;
        if (beginIndex + 1 < buffer.length)
            two = (((long) buffer[beginIndex + 1] & 0x0ffL) << 48);
        else
            two = 0L;
        if (beginIndex + 2 < buffer.length)
            three = (((long) buffer[beginIndex + 2] & 0x0ffL) << 40);
        else
            three = 0L;
        if (beginIndex + 3 < buffer.length)
            four = (((long) buffer[beginIndex + 3] & 0x0ffL) << 32);
        else
            four = 0L;
        if (beginIndex + 4 < buffer.length)
            five = (((long) buffer[beginIndex + 4] & 0x0ffL) << 24);
        else
            five = 0L;
        if (beginIndex + 5 < buffer.length)
            six = (((long) buffer[beginIndex + 5] & 0x0ffL) << 16);
        else
            six = 0L;
        if (beginIndex + 6 < buffer.length)
            seven = (((long) buffer[beginIndex + 6] & 0x0ffL) << 8);
        else
            seven = 0L;
        if (beginIndex + 7 < buffer.length)
            eight = ((long) buffer[beginIndex + 7] & 0x0ff);
        else
            eight = 0L;

        long value = one | two | three | four | five | six | seven | eight;
        return value;
    }


    public static void longToByteArray(long lValue, byte[] buffer, int beginIndex)
    {


        // DR: need to check if the buffer is null
        // if the buffer is null simply return
        if (buffer == null || beginIndex < 0)
        {
            return;
        }

        if ((beginIndex + 0) < buffer.length)
            buffer[beginIndex + 0] = (byte) ((lValue >> 56) & 0x0ff);
        if ((beginIndex + 1) < buffer.length)
            buffer[beginIndex + 1] = (byte) ((lValue >> 48) & 0x0ff);
        if ((beginIndex + 2) < buffer.length)
            buffer[beginIndex + 2] = (byte) ((lValue >> 40) & 0x0ff);
        if ((beginIndex + 3) < buffer.length)
            buffer[beginIndex + 3] = (byte) ((lValue >> 32) & 0x0ff);
        if ((beginIndex + 4) < buffer.length)
            buffer[beginIndex + 4] = (byte) ((lValue >> 24) & 0x0ff);
        if ((beginIndex + 5) < buffer.length)
            buffer[beginIndex + 5] = (byte) ((lValue >> 16) & 0x0ff);
        if ((beginIndex + 6) < buffer.length)
            buffer[beginIndex + 6] = (byte) ((lValue >> 8) & 0x0ff);
        if ((beginIndex + 7) < buffer.length)
            buffer[beginIndex + 7] = (byte) (lValue & 0x0ff);

    }

    public static boolean cmpByteArrays(byte[] lhsBytes, int lhsOffset, byte[] rhsBytes, int rhsOffset, int length)
    {
        if (lhsBytes.length < lhsOffset + length)
            return false;
        if (rhsBytes.length < rhsOffset + length)
            return false;

        for (int i = 0; i < length; i++)
        {
            if (rhsBytes[i + rhsOffset] != lhsBytes[i + lhsOffset])
                return false;
        }
        return true;
    }

    public static int toInt(byte b)
    {
        if (b < 0)
            return ((int) (b & 0x7F)) + 128;
        return (int) b;
    }

    public static void clearArray(byte[] passPhraseBytes)
    {
        for (int i = 0; i < passPhraseBytes.length; i++)
        {
            passPhraseBytes[i] = 0x00;
        }
    }
    public static void clearArray(char[] passPhraseBytes)
    {
        for (int i = 0; i < passPhraseBytes.length; i++)
        {
            passPhraseBytes[i] = 0x00;
        }
    }

    public static void saveMessage(String path2File, byte[] bytes) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(path2File);
        fos.write(bytes);
        fos.close();
        fos = null;
    }
}
