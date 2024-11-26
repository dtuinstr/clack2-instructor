package clack.cipher;

/**
 * The null cipher -- doesn't do anything to its input.
 */
public class NullCipher extends CharacterCipher
{
    /**
     * Constructs a null cipher. The key argument doesn't do anything and
     * is not stored; it's there so the constructor signature rhymes with
     * that of the other ciphers.
     * @param key
     */
    public NullCipher(String key) {
        return;
    }

    /**
     * Returns the cleartext string unchanged.
     *
     * @param cleartext the text to prep.
     * @return the cleartext string unchanged.
     */
    @Override
    public String prep(String cleartext)
    {
        return cleartext;
    }

    /**
     * Returns the preptext string unchanged.
     *
     * @param preptext a version of a cleartext string, prepared
     *                 for encryption.
     * @return the null-encryption of the preptext (i.e., the preptext).
     */
    @Override
    public String encrypt(String preptext)
    {
        return preptext;
    }

    /**
     * Returns the ciphertext string unchanged.
     *
     * @param ciphertext the encrypted string to decrypt.
     * @return the null-decryption of the ciphertext (i.e., the ciphertext).
     */
    @Override
    public String decrypt(String ciphertext)
    {
        return ciphertext;
    }
}
