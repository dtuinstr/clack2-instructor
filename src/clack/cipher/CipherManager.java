package clack.cipher;

import clack.message.OptionMessage;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * This class maintains cipher options, manages the current cipher object,
 * and preps/encrypts/decrypts strings using that cipher object. The current
 * cipher and key are checked for compatibility when: a CipherManager is
 * constructed; the enabled flag is set to true; or the cipher or key is set
 * while the enabled flag is true.
 * <p>
 * This class also provides a static list of synonyms for 'true' and
 * 'false' that users may enter when enabling/disabling encryption.
 */
public class CipherManager
{
    private static final String[] trues = {"TRUE", "YES", "ON", "1"};
    private static final String[] falses = {"FALSE", "NO", "OFF", "0"};

    public static List<String> trueSynonyms = List.of(trues);
    public static List<String> falseSynonyms = List.of(falses);

    private boolean enabled;
    private CipherNameEnum cipherName;
    private String key;

    private CharacterCipher cipher;

    /**
     * Creates a new CipherManager with the given option settings.
     *
     * @param enabled initial state: enabled or disabled.
     * @param cne     cipher name enum: what kind of cipher to use.
     * @param key     the key to use.
     * @throws IllegalArgumentException if cipher cannot be instantiated, or
     *                                  key is invalid for the cipher.
     */
    public CipherManager(boolean enabled, CipherNameEnum cne, String key)
            throws IllegalArgumentException
    {
        this.enabled = enabled;
        this.cipherName = cne;
        this.key = key;
        updateCipher();
    }

    /**
     * Creates a new CipherManager with the given option settings.
     *
     * @param enabled initial state: enabled or disabled.
     * @param cns     cipher name enum (as string): what kind of cipher to use.
     * @param key     the key to use.
     * @throws IllegalArgumentException if cipher cannot be instantiated, or
     *                                  key is invalid for the cipher,
     *                                  or string does not name a cipher.
     */
    public CipherManager(boolean enabled, String cns, String key)
            throws IllegalArgumentException
    {
        this(enabled, CipherNameEnum.valueOf(cns), key);
    }

    /**
     * Creates a new CipherManager with default settings (disabled,
     * NULL_CIPHER, "KEY").
     * @throws IllegalArgumentException if cipher cannot be instantiated, or
     *                                  key is invalid for the cipher.
    */
    public CipherManager()
    {
        this(false, CipherNameEnum.NULL_CIPHER, "KEY");
    }

    /**
     * Returns setting of the "enabled" option.
     *
     * @return setting of the "enabled" option.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Returns setting of the "cipherName" option.
     *
     * @return setting of the "cipherName" option.
     */
    public CipherNameEnum getCipherName()
    {
        return cipherName;
    }

    /**
     * Returns setting of the "key" option.
     *
     * @return setting of the "key" option.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Sets the argument cipher option(s). If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     * 
     * @param cipherName name of the cipher to use.
     * @param key the key to use.
     * @param enabled the setting for the 'enabled' flag.
     * @throws IllegalArgumentException if options cannot be set.
     */
    public void setCipherOptions(CipherNameEnum cipherName, String key, 
            boolean enabled)
            throws IllegalArgumentException
    {
        CipherNameEnum oldCipherName = this.cipherName;
        String oldKey = this.key;
        boolean oldEnabled = this.enabled;

        this.cipherName = cipherName;
        this.key = key;
        this.enabled = enabled;
        try {
            updateCipher();
        } catch (IllegalArgumentException e) {
            this.cipherName = oldCipherName;
            this.key = oldKey;
            this.enabled = oldEnabled;
            throw e;
        }
    }

    /**
     * Sets the argument cipher option(s). If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     *
     * @param cipherNameStr name of the cipher to use, as a string.
     * @param key the key to use.
     * @param enabled the setting for the 'enabled' flag.
     * @throws IllegalArgumentException if options cannot be set.
     */
    public void setCipherOptions(String cipherNameStr, String key,
            boolean enabled)
            throws IllegalArgumentException
    {
        setCipherOptions(CipherNameEnum.valueOf(cipherNameStr), key, enabled);
    }

    /**
     * Sets the argument cipher option(s). If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     * 
     * @param cipherName name of the cipher to use.
     * @param key        the key to use.
     * @throws IllegalArgumentException if options cannot be set.
     */
    public void setCipherOptions(CipherNameEnum cipherName, String key) 
            throws IllegalArgumentException
    {
        setCipherOptions(cipherName, key, this.enabled);
    }

    /**
     * Sets the argument cipher option(s). If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     *
     * @param cipherName name the cipher to use.
     * @throws IllegalArgumentException if options cannot be set.
     */
    public void setCipherOptions(CipherNameEnum cipherName)
            throws IllegalArgumentException
    {
        setCipherOptions(cipherName, this.key, this.enabled);
    }

    /**
     * Sets the argument cipher option(s). If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     *
     * @param key the key to use.
     * @throws IllegalArgumentException if options cannot be set.
     */
    public void setCipherOptions(String key)
            throws IllegalArgumentException
    {
        setCipherOptions(this.cipherName, key, this.enabled);
    }

    /**
     * Sets the 'enabled' option. If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     *
     * @param enabled the enabled setting to use.
     * @throws IllegalArgumentException if options cannot be set.
     */
    public void setEnabled(boolean enabled)
            throws IllegalArgumentException
    {
        setCipherOptions(this.cipherName, this.key, enabled);
    }

    /**
     * Sets the 'enabled' option. If the setting fails, an
     * IllegalArgumentException is thrown and no changes are made.
     *
     * @param str string found in either trueSynonyms or falseSynonyms.
     * @throws IllegalArgumentException if option cannot be set.
     */
    public void setEnabled(String str)
            throws IllegalArgumentException
    {
        if (str == null) {
            throw new IllegalArgumentException("string argument is null");
        }

        boolean enabled;
        str = str.toUpperCase();
        if (trueSynonyms.contains(str)) {
            enabled = true;
        } else if (falseSynonyms.contains(str)) {
            enabled = false;
        } else {
            throw new IllegalArgumentException(
                    "'" + str + "' not a boolean synonym");
        }

        setCipherOptions(this.cipherName, this.key, enabled);
    }
    
    /**
     * Creates a new cipher object based on the current values of the "key"
     * and "cipherName" options, and makes it the current cipher object.
     * If callers want to restore previous settings should this method throw,
     * the caller is responsible for saving the old settings, and restoring
     * them in a catch clause.
     *
     * @throws IllegalArgumentException if cipher cannot be instantiated, or
     *                                  key is invalid for the cipher.
     */
    private void updateCipher()
            throws IllegalArgumentException
    {
        try {
            cipher = switch (cipherName) {
                case CAESAR_CIPHER -> new CaesarCipher(key);
                case NULL_CIPHER -> new NullCipher(key);
                case PLAYFAIR_CIPHER -> new PlayfairCipher(key);
                case PSEUDO_ONE_TIME_PAD -> new PseudoOneTimePad(key);
                case VIGNERE_CIPHER -> new VignereCipher(key);
            };
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(
                    "Pseudo One Time Pad not available: "
                            + e.getMessage());
        }
    }

    /**
     * Process the OptionMessage, querying or updating the option it
     * mentions. If the OptionMessage's value is null, the message is a
     * query; otherwise the value is the new value for the option.
     * <p>
     * Queries return the string "option <em>option_name</em> = <em>value</em>".
     * This is also returned when an update succeeds, <em>value</em> being
     * the updated value. A failed update returns the string
     * "FAIL: <em>failure_description</em>.", followed by the above
     * option-value string. The option's value could have been changed before
     * the failure (an exception) occurred, so its value should be reported
     * to the user.
     *
     * @param om the OptionMessage to process.
     * @return a String with the current option value, or failure description.
     */
    public String process(OptionMessage om)
    {
        if (om == null) {
            return "FAIL: OptionMessage was null";
        }

        String reply = "";
        // If optionMessage has non-null value, set options.
        if (om.getValue() != null) {
            try {
                switch (om.getOption()) {
                    case CIPHER_KEY ->
                            setCipherOptions(om.getValue());
                    case CIPHER_NAME -> {
                        CipherNameEnum cne =
                                CipherNameEnum.valueOf(om.getValue());
                        setCipherOptions(cne);
                    }
                    case CIPHER_ENABLE -> setEnabled(om.getValue());
                }
            } catch (Exception e) {
                reply = "FAIL: " + e.getMessage() + ". ";
            }
        }
        // Whether message is query or setting, report back the
        // option and its setting.
        String val = switch (om.getOption()) {
            case CIPHER_KEY -> getKey();
            case CIPHER_NAME -> "" + getCipherName();
            case CIPHER_ENABLE -> "" + isEnabled();
        };
        return reply + "option " + om.getOption() + " = " + val;
    }

    /**
     * Prepare cleartext for encryption, using the current cipher object.
     *
     * @param cleartext the text to prepare.
     * @return the prepared text.
     */
    public String prep(String cleartext)
    {
        return cipher.prep(cleartext);
    }

    /**
     * Encrypt the preptext, using the current cipher object.
     *
     * @param preptext the text to encrypt.
     * @return the encrypted text.
     */
    public String encrypt(String preptext)
    {
        return cipher.encrypt(preptext);
    }

    /**
     * Decrypt the ciphertext, using the current cipher object.
     *
     * @param ciphertext the text to decrypt.
     * @return the decrypted text.
     */
    public String decrypt(String ciphertext)
    {
        return cipher.decrypt(ciphertext);
    }
}