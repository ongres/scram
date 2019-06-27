package com.ongres.scram.common.pbkdf2;

/**
 * super class for all Password Based Encryption (PBE) parameter generator classes.
 */
public abstract class PBEParametersGenerator
{
    protected byte[]  password;
    protected byte[]  salt;
    protected int     iterationCount;

    /**
     * base constructor.
     */
    protected PBEParametersGenerator()
    {
    }

    /**
     * initialise the PBE generator.
     *
     * @param password the password converted into bytes (see below).
     * @param salt the salt to be mixed with the password.
     * @param iterationCount the number of iterations the "mixing" function
     * is to be applied for.
     */
    public void init(
        byte[]  password,
        byte[]  salt,
        int     iterationCount)
    {
        this.password = password;
        this.salt = salt;
        this.iterationCount = iterationCount;
    }

    /**
     * return the password byte array.
     *
     * @return the password byte array.
     */
    public byte[] getPassword()
    {
        return password;
    }

    /**
     * return the salt byte array.
     *
     * @return the salt byte array.
     */
    public byte[] getSalt()
    {
        return salt;
    }

    /**
     * return the iteration count.
     *
     * @return the iteration count.
     */
    public int getIterationCount()
    {
        return iterationCount;
    }

    /**
     * generate derived parameters for a key of length keySize.
     *
     * @param keySize the length, in bits, of the key required.
     * @return a parameters object representing a key.
     */
    public abstract CipherParameters generateDerivedParameters(int keySize);

    /**
     * converts a password to a byte array according to the scheme in
     * PKCS5 (UTF-8, no padding)
     *
     * @param password a character array representing the password.
     * @return a byte array representing the password.
     */
    public static byte[] PKCS5PasswordToUTF8Bytes(
        char[]  password)
    {
        if (password != null)
        {
            return Strings.toUTF8ByteArray(password);
        }
        else
        {
            return new byte[0];
        }
    }
}
