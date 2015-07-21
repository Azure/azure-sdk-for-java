package com.microsoft.windowsazure.services.media;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class EncryptionUtils {

    public final String keyIdentifierPrefix = "nb:kid:UUID:";

    /// The key size for AEI 128.
    public final int keySizeInBytesForAes128 = 16;

    /// The key size for AEI 256.
    public final int keySizeInBytesForAes256 = 32;

    /// The key size for AEI 128 in bits.
    public final int keySizeInBitsForAes128 = 128;

    /// The key size for AEI 256 in bits.
    public final int keySizeInBitsForAes256 = 256;

    /// The IV size for AEI Cbc.
    public final int iVSizeInBytesForAesCbc = 16;

    // Enforce noninstantiability with a private constructor
    private EncryptionUtils() {
        // not called
    }

    /**
     * Overwrites the supplied byte array with RNG generated data which destroys
     * the original contents.
     * 
     * @param keyToErase
     *            The content key to erase.
     */
    public static void eraseKey(byte[] keyToErase) {
        if (keyToErase != null) {
            SecureRandom random;
            try {
                random = SecureRandom.getInstance("SHA1PRNG");
                random.nextBytes(keyToErase);
            } catch (NoSuchAlgorithmException e) {
                // never reached
            }
        }
    }
}
