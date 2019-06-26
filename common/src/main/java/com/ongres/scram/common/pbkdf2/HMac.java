package com.ongres.scram.common.pbkdf2;

import java.util.Hashtable;

/**
 * HMAC implementation based on RFC2104
 *
 * H(K XOR opad, H(K XOR ipad, text))
 */
public class HMac
{
    private final static byte IPAD = (byte)0x36;
    private final static byte OPAD = (byte)0x5C;

    private SHA256Digest digest;
    private int digestSize;
    private int blockLength;
    private SHA256Digest ipadState;
    private SHA256Digest opadState;

    private byte[] inputPad;
    private byte[] outputBuf;

    private static Hashtable<String, Integer> blockLengths;
    
    static
    {
        blockLengths = new Hashtable<>();
        
        blockLengths.put("GOST3411", Integer.valueOf(32));
        
        blockLengths.put("MD2", Integer.valueOf(16));
        blockLengths.put("MD4", Integer.valueOf(64));
        blockLengths.put("MD5", Integer.valueOf(64));
        
        blockLengths.put("RIPEMD128", Integer.valueOf(64));
        blockLengths.put("RIPEMD160", Integer.valueOf(64));
        
        blockLengths.put("SHA-1", Integer.valueOf(64));
        blockLengths.put("SHA-224", Integer.valueOf(64));
        blockLengths.put("SHA-256", Integer.valueOf(64));
        blockLengths.put("SHA-384", Integer.valueOf(128));
        blockLengths.put("SHA-512", Integer.valueOf(128));
        
        blockLengths.put("Tiger", Integer.valueOf(64));
        blockLengths.put("Whirlpool", Integer.valueOf(64));
    }
    
    private static int getByteLength(
        SHA256Digest digest)
    {
      //TODO      return digest.getByteLength();
        
        Integer  b = (Integer)blockLengths.get(digest.getAlgorithmName());
        
        if (b == null)
        {       
            throw new IllegalArgumentException("unknown digest passed: " + digest.getAlgorithmName());
        }
        
        return b.intValue();
    }
    
    /**
     * Base constructor for one of the standard digest algorithms that the 
     * byteLength of the algorithm is know for.
     * 
     * @param digest the digest.
     */
    public HMac(
        SHA256Digest digest)
    {
        this(digest, getByteLength(digest));
    }

    private HMac(
            SHA256Digest digest,
        int    byteLength)
    {
        this.digest = digest;
        this.digestSize = digest.getDigestSize();
        this.blockLength = byteLength;
        this.inputPad = new byte[blockLength];
        this.outputBuf = new byte[blockLength + digestSize];
    }

    public String getAlgorithmName()
    {
        return digest.getAlgorithmName() + "/HMAC";
    }

    public SHA256Digest getUnderlyingDigest()
    {
        return digest;
    }

    public void init(
        byte[] key)
    {
        digest.reset();

        int keyLength = key.length;

        if (keyLength > blockLength)
        {
            digest.update(key, 0, keyLength);
            digest.doFinal(inputPad, 0);
            
            keyLength = digestSize;
        }
        else
        {
            System.arraycopy(key, 0, inputPad, 0, keyLength);
        }

        for (int i = keyLength; i < inputPad.length; i++)
        {
            inputPad[i] = 0;
        }

        System.arraycopy(inputPad, 0, outputBuf, 0, blockLength);

        xorPad(inputPad, blockLength, IPAD);
        xorPad(outputBuf, blockLength, OPAD);

        opadState = digest.copy();

        opadState.update(outputBuf, 0, blockLength);

        digest.update(inputPad, 0, inputPad.length);

        ipadState = digest.copy();
    }

    public int getMacSize()
    {
        return digestSize;
    }

    public void update(
        byte in)
    {
        digest.update(in);
    }

    public void update(
        byte[] in,
        int inOff,
        int len)
    {
        digest.update(in, inOff, len);
    }

    public int doFinal(
        byte[] out,
        int outOff)
    {
        digest.doFinal(outputBuf, blockLength);

        if (opadState != null)
        {
            digest.reset(opadState);
            digest.update(outputBuf, blockLength, digest.getDigestSize());
        }
        else
        {
            digest.update(outputBuf, 0, outputBuf.length);
        }

        int len = digest.doFinal(out, outOff);

        for (int i = blockLength; i < outputBuf.length; i++)
        {
            outputBuf[i] = 0;
        }

        if (ipadState != null)
        {
            digest.reset(ipadState);
        }
        else
        {
            digest.update(inputPad, 0, inputPad.length);
        }

        return len;
    }

    /**
     * Reset the mac generator.
     */
    public void reset()
    {
        /*
         * reset the underlying digest.
         */
        digest.reset();

        /*
         * reinitialize the digest.
         */
        digest.update(inputPad, 0, inputPad.length);
    }

    private static void xorPad(byte[] pad, int len, byte n)
    {
        for (int i = 0; i < len; ++i)
        {
            pad[i] ^= n;
        }
    }
}
