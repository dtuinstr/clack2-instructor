package clack.cipher;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This class implements a classical One Time Pad cipher, using
 * a pseudo-random number generator (prng) to produce the pad.
 * <p>
 * At present, the class lacks a means of synchronizing the
 * pads used by a sender and receiver, but for simple applications
 * where every encrypted message is decrypted before either side
 * creates a new message, it should do.
 * <p>
 * The underlying prng is the "SHA1PRNG" algorithm, chosen because
 * it is deterministic if a seed is given just after it is created
 * and before any output is requested from it. If SHA1PRNG changes
 * to that it is no longer deterministic, this class will require
 * reworking.
 * <p>
 * SHA1PRNG is a SecureRandom prng, greatly improving on the
 * linear congruential generator used in Java's Random class.
 * <p>
 * For more information on understanding and using SecureRandom,
 * see the <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/SecureRandom.html">
 * SecureRandom JavaDoc page</a>, and the references below.
 *
 * @see <a hrer="https://www.baeldung.com/java-secure-random">
 * The Java SecureRandom Class</a>
 * @see <a href="https://metebalci.com/blog/everything-about-javas-securerandom">
 * Everything about Java's SecureRandom</a>
 * @see <a href="https://www.blackduck.com/blog/proper-use-of-javas-securerandom.html">
 * Proper use of Java SecureRandom</a>
 * @see <a href="https://www.geeksforgeeks.org/random-vs-secure-random-numbers-java/">
 * Random vs Secure Random numbers in Java</a>
 */
public class PseudoOneTimePad extends CharacterCipher {

    private final Random prng;
    private final boolean ENCRYPT = true;
    private final boolean DECRYPT = false;

    /**
     * Constructs a pseudo one time pad cipher, using the
     * given key to seed the internal pseudo random number
     * generator. The key can be any long integer. Negative
     * values are allowed. Java's long integers are in the
     * range [-9223372036854775808, 9223372036854775807].
     * <p>
     * Note that a long integer constant appearing in program
     * code must have an 'L' appended to its digits (otherwise
     * Java will think it's an int). Alternatively it
     * can be put in a string and given to Long.parseLong().
     *
     * @param key The seed for the underlying PRNG.
     * @throws NoSuchAlgorithmException if the underlying
     *                                  PRNG has no implementation on the platform running
     *                                  this code.
     */
    public PseudoOneTimePad(long key)
            throws NoSuchAlgorithmException {
        prng = SecureRandom.getInstance("SHA1PRNG");
        prng.setSeed(key);
    }

    /**
     * Constructs a pseudo one time pad cipher, using the
     * given key to seed the internal pseudo random number
     * generator. The key should be a phrase, memorable to
     * the user but not guessable to others, of at least 32
     * characters. The first 32 characters of the phrase are
     * hashed into the low-order bits of a long integer,
     * while any remaining characters are hashed into the
     * high-order bits. This long is then used to seed the
     * internal pseudo-random number generator.
     *
     * @param key the seed phrase for the underlying PRNG.
     * @throws IllegalArgumentException if key == null.
     */
    public PseudoOneTimePad(String key) throws NoSuchAlgorithmException {
        if (key == null) {
            throw new IllegalArgumentException("null not allowed for key");
        }
        /*
         * hashCode() returns an int, of 32 bits. Assuming there's
         * about 1 bit of entropy per character in English (estimates
         * vary), hashing any more than 32 characters doesn't get you
         * more entropy -- hash them and put them in the low-order
         * bits of the key. However, if you have more characters, put
         * their hash into the high-order bits.
         */
        long hash0;
        long hash1;
        if (key.length() < 32) {
            hash0 = key.hashCode();
            hash1 = 0;
        } else {
            hash0 = key.substring(0, 32).hashCode();
            hash1 = key.substring(32).hashCode();
        }
        long hash = hash0 ^ Long.reverse(hash1);
        prng = SecureRandom.getInstance("SHA1PRNG");
        prng.setSeed(hash);
    }

    /**
     * Prepares cleartext for encrypting, by calling
     * CharacterCipher.clean(cleartext).
     *
     * @param cleartext the text to prep.
     * @return a version of the cleartext ready for encrypting.
     */
    @Override
    public String prep(String cleartext) {
        return clean(cleartext);
    }

    /**
     * Encrypt a string that's been prepared for encryption.
     * If preptext is null or empty, returns it as it is.
     *
     * @param preptext a version of a cleartext string, prepared
     *                 for encryption.
     * @return the encryption of the preptext.
     */
    @Override
    public String encrypt(String preptext) {
        return transform(preptext, ENCRYPT);
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
        return transform(ciphertext, DECRYPT);
    }

    /**
     * Encrypts/Decrypts the string 'str', based on value
     * of 'operation' (use manifest constants, either
     * ENCRYPT or DECRYPT). If str is null or empty, returns
     * it as it is.
     *
     * @param str the string to encrypt/decrypt.
     * @return the encryption/decryption of str.
     */
    private String transform(String str, boolean operation) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char[] chars = new char[str.length()];
        int direction = (operation ? 1 : -1);
        for (int i = 0; i < str.length(); ++i) {
            chars[i] = shift(str.charAt(i),
                    direction * prng.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}
