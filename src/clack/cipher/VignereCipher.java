package clack.cipher;

/**
 * This class implements the classical Vignere cipher.
 */
public class VignereCipher extends CharacterCipher {

    private final int FORWARD = 1;      // shift forward
    private final int BACKWARD = -1;    // shift backward

    private final String key;
    private final int[] shifts;     // As given by key.

    /**
     * Constructs a VignereCipher with the given key. Throws
     * IllegalArgumentException if key is null or empty.
     *
     * @param key the encryption/decryption key.
     * @throws IllegalArgumentException if key contains any
     *                                  non-ALPHABET characters,
     *                                  or is null or empty,
     */
    public VignereCipher(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key is null or empty");
        }
        this.key = clean(key);
        if (!this.key.equals(key)) {
            throw new IllegalArgumentException(
                    "Key contains a non-ALPHABET character");
        }
        shifts = new int[key.length()];
        for (int i = 0; i < key.length(); ++i) {
            shifts[i] = ALPHABET.indexOf(key.charAt(i));
        }
    }

    /**
     * Prepares cleartext for encrypting. For this cipher, it
     * simply calls CharacterCipher.clean(cleartext). If cleartext
     * is null or empty, returns it as it is.
     *
     * @param cleartext the text to prep.
     * @return a version of the cleartext ready for encrypting.
     */
    @Override
    public String prep(String cleartext) {
        return clean(cleartext);
    }

    /**
     * Encrypt a string that's been prepared for encryption. If
     * preptext is null or empty, returns it as it is.
     *
     * @param preptext a version of a cleartext string, prepared
     *                 for encryption.
     * @return the encryption of the preptext.
     */
    @Override
    public String encrypt(String preptext) {
        return vignereShift(preptext, FORWARD);
    }

    /**
     * Decrypts an encrypted string. The decrypted text should match
     * the preptext that was encrypted. If ciphertext is null or
     * empty, returns it as it is.
     *
     * @param ciphertext the encrypted string to decrypt.
     * @return the decryption of the ciphertext.
     */
    @Override
    public String decrypt(String ciphertext) {
        return vignereShift(ciphertext, BACKWARD);
    }

    /**
     * Shifts characters of a string based on the key, either
     * forward or backward. If the string is null or empty,
     * returns it as it is.
     *
     * @param str       the string to shift.
     * @param direction the direction to shift (1 or -1).
     * @return the Vignere-shifted string.
     */
    private String vignereShift(String str, int direction) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char[] argChars = str.toCharArray();
        int len = argChars.length;
        char[] resultChars = new char[len];
        for (int i = 0; i < len; ++i) {
            resultChars[i] = shift(argChars[i],
                    direction * (shifts[i % shifts.length]));
        }
        return new String(resultChars);
    }
}
