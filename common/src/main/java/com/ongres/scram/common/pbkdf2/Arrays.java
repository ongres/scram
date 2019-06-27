package com.ongres.scram.common.pbkdf2;

/**
 * General array utilities.
 */
public final class Arrays
{
    private Arrays()
    {
        // static class, hide constructor
    }

    /**
     * Make a copy of a range of bytes from the passed in data array. The range can
     * extend beyond the end of the input array, in which case the return array will
     * be padded with zeroes.
     *
     * @param data the array from which the data is to be copied.
     * @param from the start index at which the copying should take place.
     * @param to the final index of the range (exclusive).
     *
     * @return a new byte array containing the range given.
     */
    public static byte[] copyOfRange(byte[] data, int from, int to)
    {
        int newLength = getLength(from, to);

        byte[] tmp = new byte[newLength];

        if (data.length - from < newLength)
        {
            System.arraycopy(data, from, tmp, 0, data.length - from);
        }
        else
        {
            System.arraycopy(data, from, tmp, 0, newLength);
        }

        return tmp;
    }

    private static int getLength(int from, int to)
    {
        int newLength = to - from;
        if (newLength < 0)
        {
            StringBuffer sb = new StringBuffer(from);
            sb.append(" > ").append(to);
            throw new IllegalArgumentException(sb.toString());
        }
        return newLength;
    }
}
