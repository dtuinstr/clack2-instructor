package clack.cipher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CipherManagerTest
{
    static CipherManager cm;

    @BeforeEach
    void setup()
    {
        cm = new CipherManager();
    }

    @Test
    void testDefaultConstructor()
    {
        CipherManager cm = new CipherManager();
        assertFalse(cm.isEnabled());
        assertEquals(CipherNameEnum.NULL_CIPHER, cm.getCipherName());
        assertEquals("KEY", cm.getKey());
    }

    /**
     * Runs the constructor for the named cipher with all combinations of
     * 'enabled' flag and the given keys. The 'goodKeys' should NOT raise an
     * exception, and the 'badKeys' should raise IllegalArgumentException.
     *
     * @param cipher   the cipher to use with the CipherManager constructor.
     * @param goodKeys the keys that should not raise an exception.
     * @param badKeys  the keys that SHOULD raise an exception.
     */
    private void testConstructor(CipherNameEnum cipher, String[] goodKeys, String[] badKeys)
    {
        boolean[] bools = {true, false};
        for (boolean b : bools) {
            for (String k : goodKeys) {
                new CipherManager(b, cipher, k);
                new CipherManager(b, cipher.toString(), k);
            }
        }
        for (boolean b : bools) {
            for (String k : badKeys) {
                assertThrows(IllegalArgumentException.class, () -> new CipherManager(b, cipher, k));
                assertThrows(IllegalArgumentException.class, () -> new CipherManager(b, cipher.toString(), k));
            }
        }
    }

    @Test
    void testConstructorCaesar()
    {
        String[] goodKeys = {"B", "ABCDEF", "XYZ"};
        String[] badKeys = {null, "", " ", "a", "aB", "#A"};
        testConstructor(CipherNameEnum.CAESAR_CIPHER, goodKeys, badKeys);
    }

    @Test
    void testConstructorPlayfair()
    {
        String[] goodKeys = {"", " ", "  ABC", "# James T. Kirk"};
        String[] badKeys = {null};
        testConstructor(CipherNameEnum.PLAYFAIR_CIPHER, goodKeys, badKeys);
    }

    @Test
    void testConstructorPseudoOneTimePad()
    {
        String[] goodKeys = {
                "", " ", "a", "A", " A", " qwertyuio pasdfgh jklzx cvbnm"
        };
        String[] badKeys = {null};
        testConstructor(CipherNameEnum.PSEUDO_ONE_TIME_PAD, goodKeys, badKeys);
    }

    @Test
    void testConstructorVignere()
    {
        String[] goodKeys = {"A", "ABCD", "ZYXWVUTSR"};
        String[] badKeys = {null, "", " ", " ABCD", "ABCd", "aBCD"};
        testConstructor(CipherNameEnum.VIGNERE_CIPHER, goodKeys, badKeys);
    }

//    @Test
//    void isEnabled()
//    {
//
//    }
//
//    @Test
//    void getCipherName()
//    {
//    }
//
//    @Test
//    void getKey()
//    {
//    }

    @Test
    void setCipherOptionsName()
    {
    }

    @Test
    void setCipherOptionsNameStr()
    {
    }

    @Test
    void setCipherOptionsNameKeyEnabled()
    {
    }

    @Test
    void setEnabledBoolean()
    {
    }

    @Test
    void setEnabledString()
    {
    }

//    @Test
//    void process()
//    {
//    }

//    @Test
//    void prep()
//    {
//    }
//
//    @Test
//    void encrypt()
//    {
//    }
//
//    @Test
//    void decrypt()
//    {
//    }
}