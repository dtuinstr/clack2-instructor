package clack.cipher;

import java.util.Arrays;

/**
 * Enumeration of all available ciphers.
 */
public enum CipherNameEnum {
    CAESAR_CIPHER,
    NULL_CIPHER,
    PLAYFAIR_CIPHER,
    PSEUDO_ONE_TIME_PAD,
    VIGNERE_CIPHER;

    public static String[] asStringArray() {
        CipherNameEnum[] cne = values();
        String[] names = new String[cne.length];
        for (int i = 0; i < cne.length; ++i) {
            names[i] = cne[i].toString();
        }
        return names;
    }
}
