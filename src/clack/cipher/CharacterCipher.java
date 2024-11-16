package clack.cipher;

/**
 * Abstract class for ciphers that work on character data.
 */
public abstract class CharacterCipher {
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Removes all non-alphabet characters from a string, and
     * uppercases all remaining letters. This is a utility
     * method, useful in implementing prep(). If the argument
     * is null, returns null.
     *
     * @param str the string to clean
     * @return the cleaned string (which might be empty), or null.
     */
    public static String clean(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().toUpperCase().replaceAll("[^A-Z]", "");
    }

    /**
     * Return a copy of a string, but reformatted into groups of
     * n non-whitespace characters, with groups separated
     * by a space. The last group may have fewer than <em>n</em>
     * characters. If n < 1, throws IllegalArgumentException;
     * if str is null, returns null. Does not alter the string in
     * any other way.
     *
     * @param str the string to break into groups
     * @param n   how many characters in each group
     * @return the grouped version of the argument string, or null.
     * @throws IllegalArgumentException if n < 1
     */
    public static String group(String str, int n) {
        if (n < 1) {
            throw new IllegalArgumentException(
                    "groups must have 1 or more letters");
        }
        if (str == null || str.isEmpty()) {
            return str;
        }
        // We have a non-null, non-empty string. Group it.
        String regex = "(" + ".".repeat(n) + ")";
        String result = str.replaceAll(regex, "$1 ");
        if (str.length() % n == 0) {
            // ... we put an extra blank space at the end of result.
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Mathematical "mod" operator. Use instead of Java's "%"
     * operator when shifting leftward (a negative shift), as
     * this will always return a number in the range [0, modulus).
     *
     * @param n       the number to be "modded".
     * @param modulus the modulus.
     * @throws IllegalArgumentException if modulus < 1.
     */
    public static int mod(int n, int modulus) {
        if (modulus < 1) {
            throw new IllegalArgumentException("modulus cannot be < 1");
        }
        // n % modulus -> in range (-modulus, modulus)
        // (n % modulus) + modulus -> in (0, 2 * modulus)
        // ((n % modulus) + modulus) % modulus -> in [0, modulus)
        return ((n % modulus) + modulus) % modulus;
    }

    /**
     * Returns the character that is n letters further on in ALPHABET,
     * with wrap around at either end of ALPHABET. Negative values are
     * allowed and cause a shift to the left. A shift of 0 returns
     * the original character.
     *
     * @param c the character to shift.
     * @param n the number of places to shift the character.
     * @return the character at the location n places beyond c.
     * @throws IllegalArgumentException if c is not in ALPHABET.
     */
    public static char shift(char c, int n) {
        if (ALPHABET.indexOf(c) < 0) {
            throw new IllegalArgumentException(
                    "Argument ('" + c + "') not in ALPHABET");
        }
        int charPos = ALPHABET.indexOf(c);
        return ALPHABET.charAt(mod(charPos + n, ALPHABET.length()));
    }

    /**
     * Returns the string resulting from shifting each character of str
     * by n places, (positive to right, negative to left), with wrap
     * around at either end of ALPHABET. If the string argument is null,
     * returns null.
     *
     * @param str the string to shift.
     * @param n   the amount to shift each letter.
     * @return the shifted version of str, or null if str is null.
     * @throws IllegalArgumentException if any character in String
     *                                  is not in ALPHABET.
     */
    public static String shift(String str, int n) {
        if (str == null) {
            return null;
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < str.length(); ++i) {
            chars[i] = shift(chars[i], n);
        }
        return new String(chars);
    }

    /**
     * Prepare cleartext for encrypting. At minimum this requires
     * removing spaces, punctuation, and non-alphabetic characters,
     * then uppercasing what's left. Other ciphers, such as PLAYFAIR,
     * may have additional preparation that this method needs to do.
     *
     * @param cleartext the text to prep.
     * @return a version of the cleartext ready for encrypting.
     */
    public abstract String prep(String cleartext);

    /**
     * Encrypt a string that's been prepared for encryption.
     *
     * @param preptext a version of a cleartext string, prepared
     *                 for encryption.
     * @return the encryption of the preptext.
     */
    public abstract String encrypt(String preptext);

    /**
     * Decrypts an encrypted string. The decrypted text should match
     * the preptext that was encrypted.
     *
     * @param ciphertext the encrypted string to decrypt.
     * @return the decryption of the ciphertext.
     */
    public abstract String decrypt(String ciphertext);
}
