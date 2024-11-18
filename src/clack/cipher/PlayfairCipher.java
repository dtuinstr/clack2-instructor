package clack.cipher;

/**
 * This class implements the classical Playfair cipher.
 */
public class PlayfairCipher extends CharacterCipher {

    private final char[][] matrix = new char[5][5];
    private final String cleanKey;
    private final boolean ENCRYPT = true;
    private final boolean DECRYPT = false;

    /**
     * Constructs a PlayfairCipher object. The key is cleaned
     * before using, so it may contain characters other than
     * those in ALPHABET. In this implementation, the letter
     * 'J' is changed to 'I' to get exactly 25 characters.
     * <p>
     * The keyword, after cleaning, is placed in a 5x5 array
     * by filling up the first row from left to right, then
     * the second from left to right, and so on until the key
     * is used up. Repeated letters are skipped when the repeat
     * is encountered. After the key is used up, the remaining
     * letters of ALPHABET are entered, left to right in each
     * row, and rows top to bottom.
     * <p>
     * An empty key (or one that reduces to an empty string
     * when cleaned) is permitted, but a null key throws an
     * IllegalArgumentException.
     *
     * @param key The string from which to extract the
     *            encryption/decryption array.
     * @throws IllegalArgumentException if key is null.
     */
    public PlayfairCipher(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        // Put the key letters in front of the alphabet, then remove
        // duplicates. What's left is ready for entry into the matrix.
        String allLetters = (clean(key) + ALPHABET).replaceAll("J", "I");
        // Removing duplicates.
        StringBuilder sb = new StringBuilder(allLetters);
        for (int i = 0; i < sb.length(); /* increment done in body */) {
            char c = sb.charAt(i);
            if (sb.indexOf(String.valueOf(c)) == i) {
                // this is first occurrence of c in sb
                ++i;
            } else {
                sb.deleteCharAt(i);
                // no ++i, a new char has moved into position i.
            }
        }
        // Store cleaned key and matrix.
        cleanKey = new String(sb);
        for (int i = 0; i < 25; ++i) {
            matrix[i / 5][i % 5] = sb.charAt(i);
        }
    }

    /**
     * Prepare cleartext for encrypting. Upper-case all letters
     * and remove all non-ALPHABET characters, then insert padding
     * characters ('X') to prevent any same-letter digraphs, and
     * finally pad the end ('Z') if needed to make it an even length.
     * If cleartext is null or empty, returns it as it is.
     *
     * @param cleartext the text to prep.
     * @return a version of the cleartext ready for encrypting.
     */
    @Override
    public String prep(String cleartext) {
        if (cleartext == null || cleartext.isEmpty()) {
            return cleartext;
        }
        StringBuilder sb = new StringBuilder(clean(cleartext).
                replaceAll("J", "I"));
        int i = 0;
        while (i < sb.length() - 1) {
            if (sb.charAt(i) == sb.charAt(i + 1)) {
                sb.insert(i + 1, 'X');
            }
            i += 2;
        }
        if (sb.length() % 2 == 1) {
            sb.append('Z');
        }
        return new String(sb);
    }

    /**
     * Encrypt a string that's been prepared for encryption. If
     * the string is null or empty, return it as it is.
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
     * Decrypts an encrypted string. The decrypted text should
     * match the preptext that was encrypted, not the original
     * cleartext. If the string is null or empty, return it as it is.
     *
     * @param ciphertext the encrypted string to decrypt.
     * @return the decryption of the ciphertext.
     */
    @Override
    public String decrypt(String ciphertext) {
        return transform(ciphertext, DECRYPT);
    }

    /**
     * Transforms a string, either encrypting or decrypting it.
     * If the string is null or empty, return it as it is.
     *
     * @param str the string to encrypt/decrypt
     * @param direction either ENCRYPT or DECRYPT (manifest constants).
     * @return the encrypted/decrypted string
     */
    private String transform(String str, boolean direction) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // delta == 1 for encryption, -1 for decryption.
        int delta = (direction ? 1 : -1);

        // encrypt/decrypt in place in a char array filled from str.
        char[] strChars = str.toCharArray();

        // encrypt/decrypt by digrams, replacing chars in strChars.
        for (int i = 0; i < str.length(); i += 2) {
            char c0 = strChars[i];
            char c1 = strChars[i + 1];
            if (c0 == c1) {
                throw new IllegalArgumentException(
                        "Same-letter digraph, cannot encrypt/decrypt");
            }
            int c0pos = cleanKey.indexOf(c0);
            int c1pos = cleanKey.indexOf(c1);
            int c0row = c0pos / 5;
            int c0col = c0pos % 5;
            int c1row = c1pos / 5;
            int c1col = c1pos % 5;
            if (c0row == c1row) {
                c0 = matrix[c0row][mod(c0col + delta, 5)];
                c1 = matrix[c1row][mod(c1col + delta, 5)];
            } else if (c0col == c1col) {
                c0 = matrix[mod(c0row + delta, 5)][c0col];
                c1 = matrix[mod(c1row + delta, 5)][c1col];
            } else {
                c0 = matrix[c0row][c1col];
                c1 = matrix[c1row][c0col];
            }
            strChars[i] = c0;
            strChars[i + 1] = c1;
        }
        return new String(strChars);
    }
}
