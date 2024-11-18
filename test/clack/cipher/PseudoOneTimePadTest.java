package clack.cipher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PseudoOneTimePadTest {

    // Used as the key for constructor taking a String, and
    // also as the text to prep, encrypt, and decrypt.
    static String melville = """
            Call me Ishmael. Some years ago -- never mind how long 
            precisely -- having little or no money in my purse, and 
            nothing particular to interest me on shore, I thought I 
            would sail about a little and see the watery part of 
            the world.
            """;

    // POTPs taking a long for the key.
    static PseudoOneTimePad otp0;
    static PseudoOneTimePad otp1;
    // POTPs taking a String for the key.
    static PseudoOneTimePad otpStr0;
    static PseudoOneTimePad otpStr1;

    /**
     * Create a pair of PseudoOneTimePad objects with the
     * same key. Separate their creation times by at least
     * a second to verify that time of creation is not used
     * in initializing the objects' state.
     *
     * @throws InterruptedException     if sleep() is disturbed.
     * @throws NoSuchAlgorithmException if underlying PRNG
     *                                  is not implemented on this platform.
     */
    @BeforeAll
    static void beforeAll()
            throws InterruptedException,
            NoSuchAlgorithmException {
        long key = Long.parseLong("1315268640013699");
        otp0 = new PseudoOneTimePad(key);
        Thread.sleep(1000);
        otp1 = new PseudoOneTimePad(key);

        otpStr0 = new PseudoOneTimePad(melville);
        Thread.sleep(1000);
        otpStr1 = new PseudoOneTimePad(melville);
    }

    /**
     * Constructors taking a long are tested implicitly
     * in the other test methods.
     */
    @Test
    void testConstructorLong() {
    }

    /**
     * Constructors taking a String are tested implicitly
     * in the other test methods.
     */
    @Test
    void testConstructorString() {
    }

    /**
     * Test the prep() method.
     */
    @Test
    void testPrep() {
        // Two one time pads should prep the same.
        String preptext0 = otp0.prep(melville);
        String preptext1 = otp1.prep(melville);
        String preptext2 = otpStr0.prep(melville);
        String preptext3 = otpStr1.prep(melville);
        assertEquals(preptext0, preptext1);
        assertEquals(preptext1, preptext2);
        assertEquals(preptext2, preptext3);

        // Prep should be idempotent.
        assertEquals(preptext0, otp0.prep(preptext0));
        assertEquals(preptext3, otpStr0.prep(preptext3));
    }

    /**
     * Test encrypt and decrypt together by "round-tripping",
     * to maintain synchronization of pad position.
     */
    @Test
    void testEncryptDecrypt() {
        String[] testVals = {
                //null,
                "",
                "X",
                otp0.prep(melville),
        };

        for (String tv : testVals) {
            assertEquals(tv,
                    otp1.decrypt(otp0.encrypt(tv)));
            assertEquals(tv,
                    otp0.decrypt(otp1.encrypt(tv)));
            assertEquals(tv,
                    otpStr1.decrypt(otpStr0.encrypt(tv)));
            assertEquals(tv,
                    otpStr0.decrypt(otpStr1.encrypt(tv)));
        }
    }

}