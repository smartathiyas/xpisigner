package org.oregan.xpi;

import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Defines some utility methods for loading files, saving byte arrays, converting
 * strings to bytes, etc.
 */
public class Utils
{
    private static final String hexString = "0123456789abcdef";

    /**
     * Loads a file from disk and returns a byte array.
     *
     * @param            filename The text file to be loaded.
     * @return           The equivalent byte array.
     * @exception        IOException File I/O error.
     */
    public static byte[] loadMessage(String filename) throws IOException
    {
        File f = new File(filename);

        if(!f.exists())
            throw new IOException("File : " + filename + " does not exist.");

        // open the file for reading
        RandomAccessFile file = new RandomAccessFile(filename,"r");

        // alloc enough space for entire file
        byte[] array = new byte[(int)file.length()];

        try
        {
            file.read(array);
        }
        catch(IOException e)
        {
            if(file != null)
                file.close();

            throw e;
        }

        file.close();

         return array;
    }

  /**
     * Loads a file from disk and returns a byte array.
     *
     * @param            dirname The path of the file to be loaded, of the form
     *                   "dir1/dir2".
     * @param            filename The text file to be loaded.
     * @return           The equivalent byte array.
     * @exception        IOException File I/O error.
     */
    public static byte[] loadMessage(String dirname, String filename) throws IOException
    {
        File d = new File(dirname);

        if(!d.exists())
            throw new IOException("Dir : " + dirname + " does not exist.");
        if(!d.isDirectory())
            throw new IOException(dirname + " is not a directory.");

        File pathandname = new File(dirname, filename);
        if(!pathandname.exists())
            throw new IOException("File : " + filename + " does not exist.");


        // open the file for reading
        RandomAccessFile file = new RandomAccessFile(pathandname,"r");

        // alloc enough space for entire file
        byte[] array = new byte[(int)file.length()];

        try
        {
            file.read(array);
        }
        catch(IOException e)
        {
            if(file != null)
                file.close();

            throw e;
        }

        file.close();

        return array;
    }

    /**
     * Saves a file to disk, given a byte array and a filename.
     *
     * @param            message The byte array.
     * @param            filename The file to save to.
     * @exception        IOException File I/O error.
     */
    public static void saveMessage(byte[] message, String filename) throws IOException
    {
        File f = new File(filename);
        saveMessage(message, f);
    }

    public static void saveMessage(byte[] message, File filename)
            throws IOException
    {

        // if the file already exists then delete it
        if(filename.exists())
            filename.delete();

        filename.createNewFile();

        RandomAccessFile file = new RandomAccessFile(filename,"rw");

        try
        {
            file.write(message,0,message.length);
        }
        catch(IOException e)
        {
            if(file != null)
                file.close();

            throw e;
        }

        file.close();
    }

    /**
     * Saves a file to disk, given a byte array and a filename.
     *
     * @param            message The byte array.
     * @param            dirname The path to save to, of the form "dir1/dir2".
     * @param            filename The file to save to.
     * @exception        IOException File I/O error.
     */
    public static void saveMessage(byte[] message, String dirname, String filename) throws IOException
    {
        File d = new File(dirname);

        // if the file already exists then delete it
        if(!d.exists())
            throw new IOException("Dir : " + dirname + " does not exist.");
        if(!d.isDirectory())
            throw new IOException(dirname + " is not a directory.");
        File pathandname = new File(dirname, filename);

        // if the file already exists then delete it
        if(pathandname.exists())
            pathandname.delete();

        RandomAccessFile file = new RandomAccessFile(pathandname,"rw");

        try
        {
            file.write(message,0,message.length);
        }
        catch(IOException e)
        {
            if(file != null)
                file.close();

            throw e;
        }

        file.close();
    }

    /**
     * Destroy a byte array. Instead of assigning it null, this will fill it with
     * zeros. For instance, if you read some sensitive information into a byte array
     * and obfuscate that in a buffer, then you can use clear() to remove any trace of
     * the original byte array. That byte array is then set to null for the garbage
     * collector.
     *
     * @param            b the byte array
     */
    public static final void clear(byte[] b)
    {
        // check to see if the byte array is null
        // if it is already null - simply return
        if (b == null) {
            return;
        }

        for(int i = b.length - 1; i >= 0; i--)
            b[i] = 0;

        b = null;
    }

    /**
     * Gets the long value of a byte, the byte is assumed to represent an unsigned
     * byte.
     * To convert to a long, we must also make the long unsigned.
     *
     * @param            b The byte array
     * @return           long containing value of byte
     */
    public static long tolong(byte b)
	{
        if(b < 0)
		{
			long tmplong = 1;
            tmplong <<= 7;
            return((long)((b & 0x7F) + tmplong));
        }

        return (long)b;
	}

    /**
     * Gets the int value of a byte. The byte is assumed to represent an unsigned
     * byte.
     * To convert to an int we must also make the int unsigned.
     *
     * @param            b The byte
     * @return           int containing value of byte
     */
    public static int toInt(byte b)
	{
        if(b < 0)
            return ((int)(b & 0x7F)) + 128;
        return
            (int)b;
	}

    /**
     * Gets the byte array value of an int. This array may be up to a maximum of 4
     * bytes long and a minimum of 1 byte long.
     *
     * @param            i the int
     * @return           byte[] a byte array representation of the integer.
     */
    public static byte[] toByteArray(int i)
	{
        byte[] inttobyte = new byte[4];     // an int can be at most 4 bytes
        int j = 3;
        for(; i != 0; j--)
        {
            inttobyte [j] = (byte)(i & 0xff);
            i >>>= 8;
        }
        byte[] value = new byte[3-j];
        System.arraycopy(inttobyte, j+1, value, 0, 3-j);
        return value;
    }

    /**
     * Gets the int from a byte array. This will only take the 4 least significant
     * bytes to form an integer.
     *
     * @param            bytes a byte array
     * @return           int an integer formed from the byte array.
     */
    public static int toInt(byte[] bytes)
	{
	    int value = 0;

	    if (bytes == null) {
	        return value;
	    }

	    int len = bytes.length;
        for (int i = 0; i < len; i++) {
            value = value << 8;
            value += (bytes[i] & 0xff);
        }
        return value;
    }

    /**
     * Checks to see if two byte arrays are equal.
     *
     * @param            byte1 first byte array.
     * @param            byte2 second byte array.
     * @return           boolean whether equal or not.
     */
    public static boolean cmpByteArrays(byte[] byte1, byte[] byte2)
    {
        boolean result = false;

        // DR: took out the check for the array's length == 0 as it was
        // returning false if both arrays had zero length even though
        // in that case they would be equal. This would get picked up
        // in any case in the loop below.

        // if either array is null, they are not equal
    	if ( byte1 == null || byte2 == null ) {
			return result;
		}

        // if both arrays are zero in length, they are equal
        // if only one array is zero in length, they are not equal

        int i = 0;
        if (byte1.length == byte2.length)
        {
            result = true;
            while((i < byte1.length) & result)
            {
                result = ((byte1[i] == byte2[i]));
                i++;
            }
        }

        return result;
    }

    /**
     * Checks to see if two int arrays are equal.
     *
     * @param            int1 first int array.
     * @param            int2 second int array.
     * @return           boolean whether equal or not.
     */
    public static boolean cmpIntArrays(int[] int1, int[] int2)
    {
        boolean result = false;

        // DR: took out the check for the array's length == 0 as it was
        // returning false if both arrays had zero length even though
        // in that case they would be equal. This would get picked up
        // in any case in the loop below.

        // if either array is null, they are not equal
    	if ( int1 == null || int2 == null ) {
			return result;
		}

        // if both arrays are zero in length, they are equal
        // if only one array is zero in length, they are not equal

        if (int1.length == int2.length)
        {
            result = true;
            int i = 0;
            while((i < int1.length) & result)
            {
                result = ((int1[i] == int2[i]));
                i++;
            }
        }

        return result;
    }


 /**
     * Checks to see if two boolean arrays are equal.
     *
     * @param            bool1 first boolean array.
     * @param            int2 second boolean array.
     * @return           boolean whether equal or not.
     */
    public static boolean cmpBooleanArrays(boolean[] bool1, boolean[] bool2)
    {
        boolean result = false;
    	if ( bool1 == null || bool2 == null || bool1.length == 0 || bool2.length == 0)
			return result;



        if (bool1.length == bool1.length)
        {
            int i = 0;
            result = true;
            while((i < bool1.length) & result)
            {
                result = ((bool1[i] == bool2[i]));
                i++;
            }

        }
        return result;
    }



    /**
     * Compares array1 with array2 and return an integer accordingly. We compare byte
     * by byte along both arrays until the bytes differ. If one array is a proper
     * subset of a longer array (ie all their bytes compare for the length of the
     * shorter array) then the shorter array is assumed to be less than the longer
     * array.
     * NOTE: If both arrays are null, then they are considered equal.
     * If only the first array is null, then it is less than the second.
     * If only the second array is null, then it is less than the first.
     *
     * @param            array1 byte array.
     * @param            array2 byte array.
     * @return           int -1 if less than, 0 if equal, 1 if greater than.
     */
    public static int compareByteArrays(byte[] array1, byte[] array2)
    {

        // check both arrrays passed in for null.
        // if both array1 and array2 are null then not less than
    	if (array1 == null && array2 == null) {
    	    // special case
			return 0;
		}
        // if array1 is null then it is less than array2
    	if (array1 == null) {
			return -1;
		}
		// array 1 is not null, so if array 2 is null, then it is not less than
        if (array2 == null) {
			return 1;
        }

        int result = 0;
        int index = 0;
        byte[] shortByteArray = null;

        /* choose the shorter of the arrays */
        int len = (array1.length < array2.length ? array1.length : array2.length);

        if(array1.length < array2.length)
              shortByteArray = array1;
        else
              shortByteArray = array2;


        while ((index < len) && (result == 0))
        {
            if ((int)(array1[index]) != (int)(array2[index]))
                result = ((int)(array1[index] & 0xff) < (int)(array2[index] & 0xff) ? -1 : 1);
            index++;
        }
        if ((index == len) && (result == 0))
        {
            if (array1.length == array2 .length)
                result = 0;
            else if(shortByteArray == array1)
                result = -1 ;
            else if(shortByteArray == array2)
                result = 1;


        }
        return result;
    }

    /**
     * Compare to see if array1 is less than array2 and return a boolean accordingly.
     * NOTE: If both arrays are null, then they are considered equal.
     * If only the first array is null, then it is less than the second.
     * If only the second array is null, then it is less than the first.
     *
     * @param            array1 byte array.
     * @param            array2 byte array.
     * @return           boolean true if array1 is less than array2, false otherwise.
     */
    public static boolean cmpByteArraysLess(byte[] array1, byte[] array2)
    {
        // check both arrays passed in for null.
        // if both array1 and array2 are null then not less than
    	if (array1 == null && array2 == null) {
			return false;
		}
        // if array1 is null then it is less than array2
    	if (array1 == null) {
			return true;
		}
		// array 1 is not null, so if array 2 is null, then it is not less than
        if (array2 == null) {
			return false;
        }

        return ((Utils.compareByteArrays(array1, array2) == -1) ? true : false);
    }

    /**
     * Compare to see if array1 is greater than array2 and return a boolean
     * accordingly.
     * NOTE: If both arrays are null, then they are considered equal.
     * If only the first array is null, then it is less than the second.
     * If only the second array is null, then it is less than the first.
     *
     * @param            array1 byte array.
     * @param            array2 byte array.
     * @return           boolean true if array1 is greater than array2, false
     *                   otherwise.
     */
    public static boolean cmpByteArraysGreater(byte[] array1, byte[] array2)
    {
        // check both arrrays passed in for null.
        // if array1 is null then it is not greater
    	if (array1 == null) {
			return false;
		}
		// array 1 is not null, so if array 2 is null, then it is greater
        if (array2 == null) {
			return true;
        }

        return ((Utils.compareByteArrays(array1, array2) == 1) ? true : false);
    }

    /**
      * Implementation of Hoare's Quick Sort algorithm. This sorts two-dimensional
      * arrays of bytes.
      *
      * @param            array two-dimensional array of bytes.
      * @param            start left boundary of array.
      * @param            end right boundary of array.
      */
     public static void QuickSort(byte[][] array, int start, int end)
     {
        int left = start;
        int right = end;
        byte[] mid;
        byte[] tmp;

        mid = array[(start+end)/2];

        if( start <= end)
        {
            /* find first element >= mid element */
            while(cmpByteArraysLess(array[left], mid) && (left < end))
                ++left;

            /* find last element <= mid element */
            while(cmpByteArraysGreater(array[right], mid) && (right > start))
                --right;

            /* if we haven't crossed our path then swop */
            if( left <= right )
            {
                tmp = array[left];
                array[left] = array[right];
                array[right] = tmp;
                ++left;
                --right;
            }

            /* if the right index has not reached the start we sort the left partition */
            if (start < right)
                QuickSort(array, start, right);
            /* if the left index has not reached the end we sort the right partition */
            if (left < end)
                QuickSort(array, left, end);
         }
     }

    /**
     * Appends two byte arrays. Appends byte array byte2 to the end byte array byte1.
     * Note that this is not a synchronized method.
     *
     * @param            byte1 first byte array.
     * @param            byte2 second byte array.
     * @return           byte[] byte array that is byte2 appended to the end of byte1.
     */
    public static byte[] appendArrays(byte[] byte1, byte[] byte2)
    {
        // must check to see if both parameters are null
        // if they are null - return empty byte array
        if ((byte1 == null) && (byte2 == null)) {
            return new byte[0];
        }
    	if (byte1 == null || byte1.length == 0)
			return byte2;
	    else if (byte2 == null || byte2.length == 0)
	        return byte1;
		else
		{
		    byte[] tmp = new byte[byte1.length + byte2.length];
		    System.arraycopy(byte1, 0, tmp, 0, byte1.length);
		    System.arraycopy(byte2, 0, tmp, byte1.length, byte2.length);
		    return tmp;
		}
    }

    /**
     * Converts a 4-byte length value that is held in a TCP/IP network format into a
     * native Java format.
     * Equivalent to ntohl() in Win32.
     *
     * @param            byte[] lengthArray, The 4-byte length value in network format.
     * @return           long, The length in native Java format.
     */
    public static long fromNetworkBytes( byte[] lengthArray ) {

        // XXX: what do we do if length array is null??

        // Make a byte array the same size as a long in Java.
        byte[] tempArray = null;

        if (lengthArray!=null) {

            tempArray = new byte[8];

            // Copy the network byte format value.  Note that the
            // network format is only in 4 bytes so we have to
            // pad the start.   The network format is MSB first
            // as is Java so we just add from the back of the
            // lengthArray array into the end of the Java long.
            for(int i=tempArray.length-1, j=lengthArray.length-1; i>=0; i--, j--) {
                // Ensure the byte is initialised.
                tempArray[i] = 0;
                if ( j >= 0 )
                    tempArray[i] = lengthArray[j];
            }

        }

        // Convert into Java's long value.
        long lLength = Utils.byteArrayToLong( tempArray, 0 );

        return lLength;

    }

    /**
     * Converts a long to a network ordered byte array (MSByte first).
     * Array will contain no leading zeros, and thus may be between 1 and 8 bytes long.
     *
     * @param            long l
     */
    public static byte[] toBytes(long l)
    {
        byte[] bytes = new byte[8];

        for (int i = 7; i >= 0 ; i--) {
            bytes[i] = (byte)(l & 0xff);
            l >>>= 8;
        }
        // get rid of any leading zero bytes
        int index = 7;
        boolean found = false;
        for (int i = 0; ((i < 7) && (found == false)) ; i++) {
            if (bytes[i] != 0) {
                index = i;
                found = true;
            }
        }

        byte[] realBytes = new byte[8 - index];
        System.arraycopy(bytes, index, realBytes, 0, realBytes.length);
        return realBytes;
    }

    /**
     * Converts a long to a network ordered byte array[4] (MSByte first).
     *
     * @param            long l
     */
    public static byte[] toNetworkBytes(long l)
    {
        byte[] bytes = new byte[4];

        bytes[3] = (byte)(l & 0xff);
        l >>>= 8;
        bytes[2] = (byte)(l & 0xff);
        l >>>= 8;
        bytes[1] = (byte)(l & 0xff);
        l >>>= 8;
        bytes[0] = (byte)(l & 0xff);

        return bytes;
    }

    /**
     * Converts an array of ints to bytes.
     * Note that each int must be of byte size, it just casts them.
     *
     * @param            int[] array of integers.
     * @return           byte[] an array of bytes.
     */
    public static byte[] toBytes(int[] ints)
    {
        // check the array for null
        if (ints == null) {
            return null;
        }

        byte[] bytes = new byte[ints.length];

        for(int i = 0; i < ints.length; i++)
            bytes[i] = (byte)(ints[i] & 0xff);

        return bytes;
    }

    /**
     * Encodes an array of booleans into an array of bytes.
     *
     * @param            bools[] array of booleans.
     * @return           byte[] an array of bytes.
     */
    public static byte[] toBytes(boolean[] bools)
    {
        // check the array for null
        if (bools == null) {
            return null;
        }

        int size = bools.length;

        byte[] bytes = new byte[(size >>> 3) + (size % 8 > 0 ? 1 : 0)];

        int count = 0;
        for(int i = 0; i < size; i++)
        {
            bytes[count] |= bools[i] ? 0x1 << (7 - (i % 8)) : 0x0;

            if(i % 8 == 7)
                count++;
        }

        return bytes;
    }

    /**
     * Encodes an array of bytes into an array of booleans.
     *
     * @param            byte[] array of bytes.
     * @param            int number of bits in last byte that are unused.
     * @return           boolean[] an array of booleans.
     * @exception        Exception if numUnusedBits parameter must be in the
     *                   range 0-7.
     */
    public static boolean[] toBooleans(byte[] bytes, int numUnusedBits)
            throws Exception
    {
        // XXX: now throws a more refined exception - like a JCRYPTO Exception
        if(numUnusedBits < 0 || numUnusedBits > 7) {
            throw new Exception("Utils::BytesToBooleans - number " +
                    "of unused bits in last byte must me in the range 0-7, " +
                    "we have " + numUnusedBits);
        }

        // DR: code to fix null pointer exception thrown if bytes is null
        // now returns zero length array of booleans
        if (bytes == null) {
            return new boolean[0];
        }

        boolean[] bools = new boolean[(bytes.length << 3) - numUnusedBits];

        int size = bools.length;
        int count = 0;

        for(int i = 0; i < size; i++)
        {
            bools[i] = (bytes[count] & (0x1 << (7- (i % 8)))) == 0 ? false : true;

            if(i % 8 == 7)
                count++;
        }

        return bools;
    }

    /**
     * Converts a byte array to a string of bits. The second parameter indicates how
     * many of the bits in the last byte are not significant.
     * That is, if numUnusedBits = 0, all of the last byte is significant.
     *
     * @param            bytes the byte array
     * @param            numUnusedBits the number of unused bits in the last byte.
     * @exception        Exception if numUnusedBits parameter must be in the
     *                   range 0-7.
     */
    public static String toBitString(byte[] bytes, int numUnusedBits)
            throws Exception
    {
        // XXX: now throws a more refined exception  - like a JCRYPTO Exception
        if(numUnusedBits < 0 || numUnusedBits > 7) {
            throw new Exception("Utils::BytesToBitString - " +
                    "number of unused bits in last byte must be in " +
                    "the range 0-7, we have " + numUnusedBits);
        }

        String result = new String();

        // if the array is null then a zero bit string is returned
        if(bytes == null) {
            // change this as this should not return a string "null"
            return result;
        }

        // if the array is zero lengthnull then a zero bit string is returned
        if (bytes.length == 0) {
            return result;
        }

        int length = bytes.length;
        byte[] localBytes = bytes;

        // do the first byte, and take into account the numUnusedBits
        for(int j = (7 - numUnusedBits); j >= 0; j--) {
            result += ((localBytes[0] >>> j) & 0x1) != 0 ? "1" : "0";
        }

        // do the remaining bytes
        for(int i = 1; i < length; i++)
        {
            byte b = localBytes[i];

            for(int j = 7; j >= 0; j--)
                result += ((b >>> j) & 0x1) != 0 ? "1" : "0";
        }

        return result;
    }

    /**
     * Convert a string to an array of low bytes. This method is prefered to using the
     * instance method getBytes() on a string because it will always get the low bytes
     * of the Unicode encoding of the string. It is independent of the default
     * encoding system.
     * <p>
     * Just the <em>low byte</em> of each character is extracted.
     * <p>
     *
     * @param            string The string to dissect.
     * @return           An array of the low bytes.
     */
    public static byte[] toBytes (String string) {
        if (string == null)
            return null;
        return toBytes (string, 0, string.length ());
    }

    /**
     * Convert a substring to an array of low bytes. This method is prefered to using
     * the instance method getBytes() on a string because it will always get the low
     * bytes of the Unicode encoding of the string. It is independent of the default
     * encoding system.
     * <p>
     * Just the <em>low byte</em> of each character is extracted.
     * <p>
     *
     * @param            string The string from which to dissect.
     * @param            offset The substring offset.
     * @param            length The substring length.
     * @return           An array of the low bytes.
     */
    public static byte[] toBytes (String string, int offset, int length) {
        // XXX: returns null string is null,
        if (string == null) {
            return null;
        }

        try {
            byte[] data = new byte[length];
            for (int i = 0; i < length; ++ i) {
                data[i] = (byte) string.charAt (offset + i);
            }
            return data;
        // or offset/length are out of bounds of string length
        } catch(ArrayIndexOutOfBoundsException ex) {
            return null;
        } catch(NegativeArraySizeException ex) {
            return null;
        } catch(StringIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Convert a string to an array of Unicode bytes.
     * <p>
     * Both bytes of each character are extracted in big-endian format. For example,
     * the string "Pavement"  has ASCII encoding - 50 61 76 65 6D 65 6E 74. The
     * Unicode encoding (big endian) of this is 00 50 00 61 00 76 00 65 00 6D 00 65 00
     * 6E 00 74 (the most significant byte (MSB) is first in each pair encoding; in
     * this example, the ACII byte is the least significant byte (LSB)). Different
     * operating systems use different endianess - most Unix systems uses big
     * endianess while Microsoft uses little endianess. BMPString uses Unicode
     * encoding (without marking it) of strings. It requires that the bytes be ordered
     * in network byte order (that is, big endian). As a result all of the Unicode
     * related methods in J/CRYPTO encode strings (and expect string encodings) to be
     * big endian.
     * <p>
     *
     * @param            string The string to dissect.
     * @return           An array of the Unicode bytes.
     */
    public static byte[] toUnicodeBytes (String string) {
        if (string == null)
            return null;
        return toUnicodeBytes (string, 0, string.length ());
    }

    /**
     * Convert a substring to an array of Unicode bytes.
     * <p>
     * Both bytes of each character are extracted in big-endian format. For example
     * the string "Pavement"  has ascii encoding - 50 61 76 65 6D 65 6E 74. The
     * Unicode encoding (big endian) of this is 00 50 00 61 00 76 00 65 00 6D 00 65 00
     * 6E 00 74 (the Most Significant Byte (MSB) is first in each pair encoding, in
     * this example, the ascii byte is the Least Significant Byte (LSB)). Different
     * operating systems use different endianess - most Unix systems uses big
     * endianess while Microsoft uses little endianess. BMPString uses Unicode
     * encoding (without marking it) of strings. It requires that the bytes be ordered
     * in network byte order (that is, big endian). As a result all of the Unicode
     * related methods in J/CRYPTO encode strings (and expect string encodings) to be
     * big endian.
     * <p>
     *
     * @param            string The string from which to dissect.
     * @param            offset The substring offset.
     * @param            length The substring length.
     * @return           An array of the Unicode bytes.
     */
    public static byte[] toUnicodeBytes (String string, int offset, int length) {
        if (string == null) {
            return null;
        }
        try {
            byte[] data = new byte[length * 2];
            for (int i = 0; i < length; ++ i) {
                char c = string.charAt (offset + i);
                data[i * 2] = (byte) (c >> 8);
                data[i * 2 + 1] = (byte) c;
            }
            return data;
        // or offset/length are out of bounds of string length
        } catch(ArrayIndexOutOfBoundsException ex) {
            return null;
        } catch(NegativeArraySizeException ex) {
            return null;
        } catch(StringIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Converts a byte array a string. Assumes that the byte array is made up of the
     * low bytes from a string; that is, if it is the Unicode encoding of a string
     * then use the getUnicodeString method. This method is preferred to using the
     * string constructor, which takes a byte array since it is independent of the
     * default encoding system.
     *
     * @param            string The byte array from which to dissect.
     * @return           A string.
     */
    public static String toString (byte[] bytes) {
        if (bytes == null)
            return new String();
        return toString(bytes, 0, bytes.length);
    }

    /**
     * Converts a byte array to a string. Assumes that the byte array is made up of
     * the low bytes from a string; that is, if it is the Unicode encoding of a string
     * then use the getUnicodeString method. This method is preferred to using the
     * string constructor, which takes a byte array since it is independent of the
     * default encoding system.
     *
     * @param            string The byte array from which to dissect.
     * @param            offset The byte subarray offset.
     * @param            length The byte subarray length.
     * @return           A string.
     */
    public static String toString (byte[] bytes, int offset, int length) {
        // check to ensure that the byte array is not null
        // if it is null return empty string.
        if (bytes == null)
            return new String();

        try {
            char[] chars = new char[length];
            for (int i = 0; i < length; ++ i) {
                chars[i] = (char) bytes[offset + i];
            }
            return new String (chars);
        } catch(ArrayIndexOutOfBoundsException ex) {
            return new String();
        } catch(NegativeArraySizeException ex) {
            return new String();
        }

    }

    /**
     * Converts the byte array to a HEX string.
     *
     * @param            The byte array to be converted.
     * @return           A string.
     */
    public static String toByteString(byte[] bytes)
    {

        if (bytes == null || bytes.length==0) {
            return new String();
        }

        String tmpStr = "";
        String xStr = "";
        for (int index=0;index<bytes.length-1;index++) {
            tmpStr =
            Integer.toHexString(Utils.toInt(bytes[index])).toUpperCase()+":" ;
            if (tmpStr.length() == 9)//length seems to be 1 more than it actually is so len(1F) == 3 e.g.
                tmpStr = tmpStr.substring(6,8) +":";
            if (tmpStr.length() == 2) //length seems to be 1 more than it actually is so len(1F) == 3 e.g.
                tmpStr = "0" + tmpStr;
            xStr += tmpStr;
        }
        tmpStr =  Integer.toHexString(Utils.toInt(bytes[bytes.length-1])).toUpperCase();
        if (tmpStr.length() == 8)//length is ok here - go figure!
            tmpStr = tmpStr.substring(6,8);
        if (tmpStr.length() == 1) //length is ok here - go figure!
                tmpStr = "0" + tmpStr;
        xStr += tmpStr;
        return xStr;
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
     * @param            string The byte array from which to dissect.
     * @param            offset The byte subarray offset.
     * @param            length The byte subarray length.
     * @return           A string.
     */
    public static String toUnicodeString (byte[] bytes) {
        // if byte array null return empty string
        if (bytes == null) {
            return new String();
        }
        return toUnicodeString(bytes, 0, bytes.length);
    }

    /**
     *  This is the method to convert unicode into ASCII bits
     *
     * @param unicodeBytes
     * @return
     */
    public static byte[] toASCIIBytes(byte[] unicodeBytes)
    {
        //:to do need to modify this method.
         byte[] bytes; byte[] asciiBytes;
         String str = Utils.toString(unicodeBytes);
         bytes = Utils.toBytes(str);
         {
            asciiBytes = new byte[(bytes.length/2)];
            int j=0;
            for(int i =0;i < bytes.length ;i++)
             {
                if(bytes[i] == 0 )
                {}
                else
                {
                    asciiBytes[j] = bytes[i];
                    j++;
                }
             }
        }

        return asciiBytes;
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
     * @param            string The byte array from which to dissect.
     * @param            offset The byte subarray offset.
     * @param            length The byte subarray length.
     * @return           A string.
     */
    public static String toUnicodeString (byte[] bytes, int offset, int length) {
        // if byte array null return empty string
        if (bytes == null || length > bytes.length) {
            return new String();
        }

        try {

            int len = length / 2;
            char[] chars = new char[len];
            len += offset;
            for (int i = offset; i < len; ++ i) {
                chars[i] = (char) (((bytes[i * 2] & 0xff) << 8) + (bytes[i * 2 + 1] & 0xff));
            }
            return new String (chars);

        // or offset/length are out of bounds of string length
        } catch(ArrayIndexOutOfBoundsException ex) {
            return new String();
        } catch(NegativeArraySizeException ex) {
            return new String();
        }
    }

    /**
	 * Print out an array of bytes to the system.
	 * If b is null then "Null" is printed, if the b.length = 0 then "[]" is printed.
	 *
	 * @param            b The byte array to be printed.
	 * @exception        Exception Output error.
	 */
	public static final void printBytes( byte[] b)
	{
	    if(b==null)
	        System.out.println("Null");
	    else if(b.length==0)
	        System.out.println("[]");
	    else
            System.out.print( Utils.toHexString(b));
	}

   /**
	 * This method checks the parity of each byte in the byte array and sets the
	 * parity of each byte in the byte array to be odd by setting the last bit.
	 *
	 * @param            byte[] testArray
	 */
	public static void setParity(byte[] testArray)
	{
        // DR: need to check if the array is null
        // if the array is null simply return
        if (testArray == null) {
            return;
        }

	    byte X, Y;
	    byte[] local = testArray;
	    int len = local.length;

	    for(int i = 0; i < len; i ++)
	    {
            Y = local[i];
            X = local[i];

	        for(int j = 0; j < 7; j++)
	        {
	            X >>= 1;
	            Y ^= X;
	        }

	        Y &= 1;

	        if(Y == 0)
	            local[i] ^= 1;
	    }
	}

    /**
     * Int formatter. Turns int into string that is length long with the int right
     * alligned. For example, format(1234, 7) returns "   1234". If the number is
     * actually longer than the buffer then the entire number is returned (that is,
     * the returned string will be longer than the length parameter).
     */
    public static String formatInt(int num, int length) {
        // Can this be done with NumberFormat?
        int len =   new Integer(num).toString().length();
        StringBuffer strBuf = new StringBuffer();
        int numSpaces = length - len;
        for (int i = 0; i < numSpaces; i++)
            strBuf.append(' ');
        strBuf.append(num);
        return strBuf.toString();
    }

    /**
     * Output Date in specific format wrt to default timezone (which is GMT unless the
     * java.properties are changed), and wrt to default locale (which is US).
     *
     * @param            date the date.
     * @return           String
     */
    public static String dateToString(Date date)
    {
        return dateToString(date, TimeZone.getDefault().getID(), Locale.getDefault());
    }

    /**
     * Output Date in specific format wrt to the parameter timezone and wrt to default
     * locale (which is US).
     *
     * @param            date the date.
     * @param            timeZone String that represents, for example, "PST", "SST".
     * @return           String
     */
    public static String dateToString(Date date, String timeZone)
    {
        return dateToString(date, timeZone, Locale.getDefault());
    }

    /**
     * Output Date in specific format wrt to the parameter timezone and wrt to locale
     * parameter.
     * <EM>NOTE:</EM> If the VM does not specify a default TimeZone, then GMT will be
     * used.
     *
     * @param            date the date.
     * @param            timeZone String that represents, for example, "PST", "SST".
     * @param            locale Actual Locale, for example, Locale.UK
     * @return           String
     */
    public static String dateToString(Date date, String timeZone, Locale locale)
    {
        // if the date object is null - return null
        if (date==null) {
            return null;
        }

        //TimeZone timeZone1 = TimeZone.getTimeZone(timeZone);
        TimeZone timeZone1 = TimeZone.getTimeZone((timeZone.equals("???") ? "GMT" : timeZone));
        // the above is a fix for interesting little bug post in the java users group....
		if(timeZone1==null)
		{
			String[] zones = TimeZone.getAvailableIDs();
			timeZone = zones[0];
			timeZone1 = TimeZone.getTimeZone(timeZone);
		}
        DateFormat formatter = new SimpleDateFormat (
                        "E d MMM yyyy HH:mm:ss '" + timeZone + "'", locale);
        formatter.setTimeZone(timeZone1);
        return(formatter.format(date));
    }

    /**
     * Output Date in calendar in specific format wrt to default timezone (which is
     * GMT unless the java.properties are changed), and wrt to default locale (which
     * is US).
     *
     * @param            Calendar the calendar
     * @return           String
     */
    public static String calendarToString(Calendar cal)
    {
        return calendarToString(cal, TimeZone.getDefault().getID(), Locale.getDefault());
    }

    /**
     * Output Date in calendar in specific format wrt to the parameter timezone and
     * wrt to default locale (which is US).
     *
     * @param            Calendar the calendar
     * @param            timeZone String that represents, for example, "PST", "SST".
     * @return           String
     */
    public static String calendarToString(Calendar cal, String timeZone)
    {
        return calendarToString(cal, timeZone, Locale.getDefault());
    }

    /**
     * Output Date in calendar in specific format wrt to the parameter timezone and
     * wrt to locale parameter.
     *
     * @param            Calendar the calendar
     * @param            timeZone String that represents, for example, "PST", "SST".
     * @param            locale Actual Locale e.g. Locale.UK
     * @return           String
     */
    public static String calendarToString(Calendar cal, String timeZone, Locale locale)
    {
        // if the calendar object is null - return null
        if (cal==null) {
            return null;
        }

        TimeZone timeZone1 = TimeZone.getTimeZone(timeZone);
        DateFormat formatter = new SimpleDateFormat (
                        "E d MMM yyyy HH:mm:ss '" + timeZone + "'", locale);
        formatter.setTimeZone(timeZone1);
        return(formatter.format(cal.getTime()));

    }

    /**
     * Compares two calendars to see if they are within <code>range</code>
     * milliseconds of each other.
     *
     * @param            lhs
     * @param            rhs
     * @param            range
     */
    public static boolean compareCalendars(Calendar lhs,Calendar rhs,long range)
    {
        if((Math.abs(lhs.getTime().getTime()-rhs.getTime().getTime()))<=range)
            return true;
        return false;
    }
    /**
     * Compares two calendars to see if they are within 1 second of each other.
     *
     * @param            lhs
     * @param            rhs
     */
    public static boolean compareCalendars(Calendar lhs,Calendar rhs)
    {
        return compareCalendars(lhs,rhs,1000);
    }


    /**
     * Takes a string and URL encodes it. CGI-BIN scripts do not understand certain
     * characters, for example, '+' or Carriage-Returns. These must be represented by
     * their hex encodings (in uppercase). This is especially useful when (say)
     * sending the base-64 encoding of a certificate to a Web server for processing by
     * a cgi-bin script.
     *
     * @param            String string the string to be processed.
     * @return           String
     */
    public static String urlEncode(String string)
    {
        if (string == null) {
            return new String();
        }
        return URLEncoder.encode(string);
    }

    /**
     * Takes a string and URL decodes it. CGI-BIN scripts do not understand certain
     * characters, for example, '+' or Carriage-Returns. These must be represented by
     * their hex encodings (in uppercase). Thus if (say) the BASE64 coding of a
     * certificate is sent back from an HTTP server it will be URL encode and you can
     * use this method to URL decode it (then decode it using Base64Coder).
     *
     * @param            s the string to be processed.
     * @return           String decoded string.
     * @exception        UnsupportedEncodingException If the ASCII code is not
     *                   recognized.
     */
    public static String urlDecode(String s) throws UnsupportedEncodingException
    {
        if (s == null) {
            return new String();
        }

        StringBuffer result = new StringBuffer();
        String encoding    = "UTF-8";

        // First convert all '+' characters to spaces.
        String str = s.replace('+', ' ');

        // Then go through the whole string looking for byte encoded characters
        int i;
        int start = 0;
        byte [] bytes = null;
        while ((i = str.indexOf('%', start)) >= 0)
        {

            // Add all non-encoded characters to the result buffer
            result.append(str.substring(start, i));
            start = i;

            // Get all consecutive encoded bytes
            int length = str.length();
            while ((i+2 < length) && (str.charAt(i) == '%'))
                i += 3;

            // Decode all these bytes
            if ((bytes == null) || (bytes.length < ((i-start)/3)))
                bytes = new byte[((i-start)/3)];

            int index = 0;
            while (start < i)
            {
                String sub = str.substring(start + 1, start + 3);
                try
                {

                    bytes[index] = (byte)Integer.parseInt(sub, 16);
                    index++;
                }
                catch (NumberFormatException nfe)
                {
                    // Ignore badly encoded char
                }
                start += 3;
            }

            // Add the bytes as characters according to the given encoding
            result.append(new String(bytes, 0, index, encoding));

            // Make sure we skip to just after a % sign
            // start = i+1;
        }

        // Add any character left
        if (start < str.length())
            result.append(str.substring(start));

        return result.toString();


    }

    /**
     * @param            long byteArray is to be converted to a longValue.
     * @param            byte[] buffer may/maynot be one block ( 8 bytes) in length.
     */
    public static long byteArrayToLong(byte[] buffer, int beginIndex)
    {

        // XXX: what do we do if buffer is null or begin index is negative??

        // if the buffer is null - return 0L
        // ensure this is the correct behaviour
        if (buffer == null || beginIndex < 0) {
            return 0L;
        }

        long one,two,three,four,five,six,seven,eight;

        if (beginIndex < buffer.length)
            one = (((long) buffer[beginIndex+0] & 0x0ffL) << 56);
        else
            one = 0L;
        if (beginIndex+1 < buffer.length)
            two = (((long) buffer[beginIndex+1] & 0x0ffL) << 48);
        else
            two = 0L;
        if (beginIndex+2 < buffer.length)
            three = (((long) buffer[beginIndex+2] & 0x0ffL) << 40);
        else
            three = 0L;
        if (beginIndex+3 < buffer.length)
            four = (((long) buffer[beginIndex+3] & 0x0ffL) << 32);
        else
            four = 0L;
        if (beginIndex+4 < buffer.length)
            five = (((long) buffer[beginIndex+4] & 0x0ffL) << 24);
        else
            five = 0L;
        if (beginIndex+5 < buffer.length)
            six = (((long) buffer[beginIndex+5] & 0x0ffL) << 16);
        else
            six = 0L;
        if (beginIndex+6 < buffer.length)
            seven = (((long) buffer[beginIndex+6] & 0x0ffL) << 8);
        else
            seven = 0L;
        if (beginIndex+7 < buffer.length)
            eight = ((long)  buffer[beginIndex+7] & 0x0ff);
        else
            eight = 0L;

        long value = one|two|three|four|five|six|seven|eight;
        return value;
    }

    /**
     * @param            long lValue is to be converted to a byteArray.
     * @param            byte[] buffer may/may not be one block in length.
     */
    public static void longToByteArray(long lValue, byte[] buffer , int beginIndex)
    {
        // XXX: what do we do if buffer is null or begin index is negative??

        // DR: need to check if the buffer is null
        // if the buffer is null simply return
        if (buffer == null || beginIndex < 0) {
            return;
        }

        if ((beginIndex+0) < buffer.length)
            buffer[beginIndex+0] = (byte) ((lValue >> 56) & 0x0ff);
        if ((beginIndex+1) < buffer.length)
            buffer[beginIndex+1] = (byte) ((lValue >> 48) & 0x0ff);
        if ((beginIndex+2) < buffer.length)
            buffer[beginIndex+2] = (byte) ((lValue >> 40) & 0x0ff);
        if ((beginIndex+3) < buffer.length)
            buffer[beginIndex+3] = (byte) ((lValue >> 32) & 0x0ff);
        if ((beginIndex+4) < buffer.length)
            buffer[beginIndex+4] = (byte) ((lValue >> 24) & 0x0ff);
        if ((beginIndex+5) < buffer.length)
            buffer[beginIndex+5] = (byte) ((lValue >> 16) & 0x0ff);
        if ((beginIndex+6) < buffer.length)
            buffer[beginIndex+6] = (byte) ((lValue >> 8) & 0x0ff);
        if ((beginIndex+7) < buffer.length)
            buffer[beginIndex+7] = (byte) (lValue & 0x0ff);

    }

    /**
     * Takes a string and replace each string "\n\n" by "\n" and add "\t" after each
     * "\n" found.
     * That method is called to print properly an ASN.1 message, in the same way as a
     * BER viewer.
     * Returns the modified string.
     *
     * @param            str the string to be processed.
     */

    public static String trimString(String str)
    {
        String interStr = str;
        int i = 0, index = 0, addedIndex = 0;
        boolean flag = true;

        while ( i < str.length()-1 && flag )
        {
            index  = str.indexOf("\n\n", i);
            if ( index != -1 && index < str.length()-1 )
            {
                interStr = interStr.substring(0, index+addedIndex+1)+interStr.substring(index+addedIndex+2, interStr.length());
                addedIndex--;
            }
            else flag = false;
            i = index+2;
        }

        String interStr2 = interStr;
        flag = true;
        i = index = addedIndex = 0;
        while ( i < interStr.length()-1 && flag )
        {
            index  = interStr.indexOf("\n", i);
            if ( index != -1 && index < interStr.length()-1 )
            {
                interStr2 = interStr2.substring(0, index+addedIndex+1)+"\t"+interStr2.substring(index+addedIndex+1, interStr2.length());
                addedIndex++;
            }
            else flag = false;
            i = index+1;
        }

        return interStr2;
    }

    private static final String hex = "0123456789abcdef";
    private static final char[] chex = hex.toCharArray ();

    /**
     * Hex encode some binary data.
     *
     * @param            data The data to encode.
     * @return           The hex encoded data.
     */
    public static String toHexString (byte[] data) {
        if (data == null || data.length == 0) {
          return new String();
        }
        return toHexString (data, 0, data.length);
    }

    /**
     * Hex encode some binary data.
     *
     * @param            data The data to encode.
     * @param            offset The offset from which to start encoding.
     * @param            length The length to encode.
     * @return           The hex encoded data.
     */
    public static String toHexString (byte[] data, int offset, int length) {
        // prevents NullPointerException, ArrayOutOfBoundsException
        // and NegativeIndexException.
        if (data == null || data.length == 0 || offset>=data.length ||
                length>data.length || offset+length>data.length ||
                offset<0 || length<0) {
          return new String();
        }
        char[] chars = new char[length * 2];
        for (int i = 0; i < length; ++ i) {
        chars[i * 2] = chex[(data[offset + i] >> 4) & 0xf];
        chars[i * 2 + 1] = chex[data[offset + i] & 0xf];
        }
        return new String (chars);
    }

    /**
     * Binary encode a bitstring.
     *
     * @param            data The data to encode.
     * @return           The binary encoded data.
     */
    public static String toBinaryString (boolean[] data) {
        return toBinaryString (data, 0, data.length);
    }

    /**
     * Binary encode a bitstring.
     *
     * @param            data The data to encode.
     * @param            offset The offset from which to start encoding.
     * @param            length The length to encode.
     * @return           The binary encoded data.
     */
    public static String toBinaryString (boolean[] data, int offset, int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; ++ i)
        chars[i] = data[offset + i] ? '1' : '0';
        return new String (chars);
    }

    private static final String base64 =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final char[] cbase64 = base64.toCharArray ();
    /* private static final int LINE = 3 * 19; */

    /**
     * Base-64 encode some binary data.
     *
     * @param            data The data to encode.
     * @return           The base-64 encoded data.
     */
    public static String toBase64String (byte[] data) {
        return toBase64String (data, 0, data.length);
    }

    /**
     * Base-64 encode some binary data.
     *
     * @param            data The data to encode.
     * @param            offset The offset from which to start encoding.
     * @param            length The length to encode.
     * @return           The base-64 encoded data.
     */
    public static String toBase64String (byte[] data, int offset, int length) {
        int i = offset, j = 0, end = offset + length;
        char[] chars = new char[((length + 2) / 3) * 4];
        while (i < end) {
        int x0 = data[i] & 0xff;
        int x1 = (i + 1 < end) ? (data[i + 1] & 0xff) : 0;
        int x2 = (i + 2 < end) ? (data[i + 2] & 0xff) : 0;
        char c0 = cbase64[x0 >> 2];
        char c1 = cbase64[((x0 << 4) | (x1 >> 4)) & 0x3f];
        char c2 = cbase64[((x1 << 2) | (x2 >> 6)) & 0x3f];
        char c3 = cbase64[x2 & 0x3f];
        chars[j] = c0;
        chars[j + 1] = c1;
        chars[j + 2] = (i + 1 < end) ? c2 : '=';
        chars[j + 3] = (i + 2 < end) ? c3 : '=';
        i += 3;
        j += 4;
        }
        return new String (chars, 0, j);
    }

    public static Map<String,Properties> slurpINI(File ffINI) throws IOException
    {
        HashMap<String,Properties> map = new HashMap<String,Properties>();

        LineNumberReader in = new LineNumberReader(new FileReader(ffINI));

        String line = null;
        String sectionName = null;
        Properties p = new Properties();
        while((line = in.readLine())!=null)
        {
            String trimmed = line.trim();
            if(trimmed.length()==0)
                continue;
            if(",;\'".indexOf(trimmed.charAt(0))>=0)
                continue;
            if(line.startsWith("[") && line.endsWith("]"))
            {
                sectionName = line.substring(1,line.length()-1);
                p = new Properties();
                map.put(sectionName,p);
            }else
            {
                String[] nameVal = line.split("=");
                if(nameVal.length==2)
                    p.setProperty(nameVal[0],nameVal[1]);
                else
                    System.err.println(in.getLineNumber() + " - " + line);
            }
        }

      return map;


    }

    public static String toB64(byte[] md5)
    {
        return new BASE64Encoder().encode(md5);
    }

    public static void slurp(InputStream in, OutputStream out) throws IOException
    {
        int byteCount;
        byte[] data = new byte[1024*2];
        while ((byteCount = in.read(data)) > -1)
        {
            out.write(data, 0, byteCount);
        }
    }

    public static void drain(InputStream in) throws IOException
    {
        byte[] data = new byte[1024*2];
        while (in.read(data) > -1);
    }

    public static String toUTCTime(Date time)
    {
        SimpleDateFormat dateF = new SimpleDateFormat("yyMMddHHmmss'Z'");

               dateF.setTimeZone(new SimpleTimeZone(0,"Z"));

               return dateF.format(time);

    }

}
