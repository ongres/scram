package com.ongres.scram.common.pbkdf2;

/**
 * Generate an algorithm PBKDF2WithHmacSHA256
 */
public class PBKDF2Generator {

    private HMac hMac;
    private byte[] state;
    private int dkLen = 32;
	
    public PBKDF2Generator() {
        hMac = new HMac(new SHA256Digest());
        state = new byte[hMac.getMacSize()];
    }
    
	public byte[] generatePBKDF(byte[] password, byte[] salt, int iterationCount) {
		
        int     hLen = hMac.getMacSize();
        int     l = (dkLen + hLen - 1) / hLen;
        byte[]  iBuf = new byte[4];
        byte[]  outBytes = new byte[l * hLen];
        int     outPos = 0;

        hMac.init(password);

        for (int i = 1; i <= l; i++)
        {
            // Increment the value in 'iBuf'
            int pos = 3;
            while (++iBuf[pos] == 0)
            {
                --pos;
            }

            F(salt, iterationCount, iBuf, outBytes, outPos);
            outPos += hLen;
        }

        return copyOfRange(outBytes, 0, 32);
	}
	
    private void F(
            byte[]  S,
            int     c,
            byte[]  iBuf,
            byte[]  out,
            int     outOff)
        {
            if (c == 0)
            {
                throw new IllegalArgumentException("iteration count must be at least 1.");
            }

            if (S != null)
            {
                hMac.update(S, 0, S.length);
            }

            hMac.update(iBuf, 0, iBuf.length);
            hMac.doFinal(state, 0);

            System.arraycopy(state, 0, out, outOff, state.length);

            for (int count = 1; count < c; count++)
            {
                hMac.update(state, 0, state.length);
                hMac.doFinal(state, 0);

                for (int j = 0; j != state.length; j++)
                {
                    out[outOff + j] ^= state[j];
                }
            }
        }
    
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
