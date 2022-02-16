/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * This class implements authenticated encryption with the associated data (AEAD_AES_256_CBC_HMAC_SHA256) algorithm
 * specified at <a href="http://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05">
 * http://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05</a>
 *
 */
public class AeadAes256CbcHmac256EncryptionAlgorithm extends DataProtector {
    final static String algorithmName = "AEAD_AES_256_CBC_HMAC_SHA256";
    // Stores data encryption key which includes root key and derived keys
    private DataEncryptionKey columnEncryptionkey;
    private byte algorithmVersion = 0x1;
    // This variable indicate whether encryption type is deterministic (if true)
    // or random (if false)
    private boolean isDeterministic = false;
    // Each block in the AES is 128 bits
    private int blockSizeInBytes = 16;
    private int keySizeInBits = 256;
    private int keySizeInBytes = keySizeInBits / 8;
    private byte[] version = new byte[] {0x01};
    // Added so that java hashing algorithm is similar to c#
    private byte[] versionSize = new byte[] {1};

    /*
     * Minimum Length of cipherText without authentication tag. This value is 1 (version byte) + 16 (IV) + 16 (minimum
     * of 1 block of cipher Text)
     */
    private int minimumCipherTextLengthInBytesNoAuthenticationTag = 1 + blockSizeInBytes + blockSizeInBytes;

    /*
     * Minimum Length of cipherText. This value is 1 (version byte) + 32 (authentication tag) + 16 (IV) + 16 (minimum of
     * 1 block of cipher Text)
     */
    private int minimumCipherTextLengthInBytesWithAuthenticationTag = minimumCipherTextLengthInBytesNoAuthenticationTag
            + keySizeInBytes;

    private static Map<Tuple<DataEncryptionKey, EncryptionType>, AeadAes256CbcHmac256EncryptionAlgorithm> algorithmCache = new ConcurrentHashMap<Tuple<DataEncryptionKey, EncryptionType>, AeadAes256CbcHmac256EncryptionAlgorithm>();

    /**
     * Retrieves an existing AeadAes256CbcHmac256EncryptionAlgorithm or creates a new AeadAes256CbcHmac256EncryptionAlgorithm.
     * @param dataEncryptionKey data encryption key
     * @param encryptionType encryption type
     * @return AeadAes256CbcHmac256EncryptionAlgorithm instance
     * @throws MicrosoftDataEncryptionException if EncryptionKey is null
     */
    public static AeadAes256CbcHmac256EncryptionAlgorithm getOrCreate(DataEncryptionKey dataEncryptionKey,
            EncryptionType encryptionType) throws MicrosoftDataEncryptionException {
        if (null == dataEncryptionKey) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_NullColumnEncryptionKey"));
        }

        Tuple<DataEncryptionKey, EncryptionType> key = new Tuple<DataEncryptionKey, EncryptionType>(dataEncryptionKey,
                encryptionType);

        if (algorithmCache.containsKey(key)) {
            return algorithmCache.get(key);
        } else {
            AeadAes256CbcHmac256EncryptionAlgorithm e = new AeadAes256CbcHmac256EncryptionAlgorithm(dataEncryptionKey,
                    encryptionType);
            algorithmCache.put(key, e);
            return e;
        }
    }

    /**
     * Constructor for AeadAes256CbcHmac256EncryptionAlgorithm
     * @param dataEncryptionKey data encryption key
     * @param encryptionType encryption type
     * @throws MicrosoftDataEncryptionException
     */
    public AeadAes256CbcHmac256EncryptionAlgorithm(DataEncryptionKey dataEncryptionKey,
            EncryptionType encryptionType) throws MicrosoftDataEncryptionException {
        validateEncryptionKeySize(dataEncryptionKey.getRootKeyBytes().length);

        this.columnEncryptionkey = dataEncryptionKey;
        if (encryptionType == EncryptionType.Deterministic) {
            this.isDeterministic = true;
        }
    }

    private void validateEncryptionKeySize(int size) throws MicrosoftDataEncryptionException {
        if (size != keySizeInBytes) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataEncryptionKeySize"));
            Object[] msgArgs = {size};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    @Override
    public byte[] encrypt(byte[] plaintext) throws MicrosoftDataEncryptionException {
        return encryptData(plaintext, true);
    }

    /**
     * Performs encryption of plain text
     *
     * @param plainText
     *        text to be encrypted
     * @param hasAuthenticationTag
     *        specify if encryption needs authentication
     * @return cipher text
     * @throws MicrosoftDataEncryptionException if encryption fails
     */
    protected byte[] encryptData(byte[] plainText,
            boolean hasAuthenticationTag) throws MicrosoftDataEncryptionException {
        // we will generate this initialization vector based whether
        // this encryption type is deterministic
        assert (plainText != null);
        byte[] iv = new byte[blockSizeInBytes];
        // Secret/private key to be used in AES encryption
        SecretKeySpec skeySpec = new SecretKeySpec(columnEncryptionkey.getEncryptionKeyBytes(), "AES");

        if (isDeterministic) {
            // this method makes sure this is 16 bytes key
            try {
                iv = SecurityUtility.getHMACWithSHA256(plainText, columnEncryptionkey.getIvKeyBytes(),
                        blockSizeInBytes);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_EncryptionFailed"));
                byte[] slice = Arrays.copyOfRange(columnEncryptionkey.getEncryptionKeyBytes(),
                        columnEncryptionkey.getEncryptionKeyBytes().length - 10,
                        columnEncryptionkey.getEncryptionKeyBytes().length);
                Object[] msgArgs = {CryptographyExtensions.toHexStringWithDashes(slice)};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }
        } else {
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
        }

        int numBlocks = plainText.length / blockSizeInBytes + 1;

        int hmacStartIndex = 1;
        int authenticationTagLen = hasAuthenticationTag ? keySizeInBytes : 0;
        int ivStartIndex = hmacStartIndex + authenticationTagLen;
        int cipherStartIndex = ivStartIndex + blockSizeInBytes;

        // Output buffer size = size of VersionByte + Authentication Tag + IV + cipher Text blocks.
        int outputBufSize = 1 + authenticationTagLen + iv.length + (numBlocks * blockSizeInBytes);
        byte[] outBuffer = new byte[outputBufSize];

        // Copying the version to output buffer
        outBuffer[0] = algorithmVersion;
        // Coping IV to the output buffer
        System.arraycopy(iv, 0, outBuffer, ivStartIndex, iv.length);

        // Start the AES encryption

        try {
            // initialization vector
            IvParameterSpec ivector = new IvParameterSpec(iv);
            Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivector);

            int count = 0;
            int cipherIndex = cipherStartIndex; // this is where cipherText starts

            if (numBlocks > 1) {
                count = (numBlocks - 1) * blockSizeInBytes;
                cipherIndex += encryptCipher.update(plainText, 0, count, outBuffer, cipherIndex);
            }
            // doFinal will complete the encryption
            byte[] buffTmp = encryptCipher.doFinal(plainText, count, plainText.length - count);
            // Encryption completed
            System.arraycopy(buffTmp, 0, outBuffer, cipherIndex, buffTmp.length);

            if (hasAuthenticationTag) {

                Mac hmac = Mac.getInstance("HmacSHA256");
                SecretKeySpec initkey = new SecretKeySpec(columnEncryptionkey.getMacKeyBytes(), "HmacSHA256");
                hmac.init(initkey);
                hmac.update(version, 0, version.length);
                hmac.update(iv, 0, iv.length);
                hmac.update(outBuffer, cipherStartIndex, numBlocks * blockSizeInBytes);
                hmac.update(versionSize, 0, version.length);
                byte[] hash = hmac.doFinal();
                // coping the authentication tag in the output buffer which holds cipher text
                System.arraycopy(hash, 0, outBuffer, hmacStartIndex, authenticationTagLen);

            }
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_EncryptionFailed"));
            byte[] slice = Arrays.copyOfRange(columnEncryptionkey.getEncryptionKeyBytes(),
                    columnEncryptionkey.getEncryptionKeyBytes().length - 10,
                    columnEncryptionkey.getEncryptionKeyBytes().length);
            Object[] msgArgs = {CryptographyExtensions.toHexStringWithDashes(slice)};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        return outBuffer;

    }

    @Override
    public byte[] decrypt(byte[] ciphertext) throws MicrosoftDataEncryptionException {
        return decryptData(ciphertext, true);
    }

    /**
     * Decrypt the cipher text and return plain text
     *
     * @param cipherText
     *        data to be decrypted
     * @param hasAuthenticationTag
     *        tells whether cipher text contain authentication tag
     * @return plain text
     * @throws MicrosoftDataEncryptionException
     */
    private byte[] decryptData(byte[] cipherText,
            boolean hasAuthenticationTag) throws MicrosoftDataEncryptionException {
        assert (cipherText != null);

        byte[] iv = new byte[blockSizeInBytes];

        int minimumCipherTextLength = hasAuthenticationTag ? minimumCipherTextLengthInBytesWithAuthenticationTag
                                                           : minimumCipherTextLengthInBytesNoAuthenticationTag;

        // Here we check if length of cipher text is more than minimum value,
        // if not exception is thrown
        if (cipherText.length < minimumCipherTextLength) {
            throw new MicrosoftDataEncryptionException();
        }

        // Validate the version byte
        int startIndex = 0;
        if (cipherText[startIndex] != algorithmVersion) {
            throw new MicrosoftDataEncryptionException();

        }

        startIndex += 1;
        int authenticationTagOffset = 0;

        // Read authentication tag
        if (hasAuthenticationTag) {
            authenticationTagOffset = startIndex;
            // authentication tag size is keySizeInBytes
            startIndex += keySizeInBytes;
        }

        // Read IV from cipher text
        System.arraycopy(cipherText, startIndex, iv, 0, iv.length);
        startIndex += iv.length;

        // To read encrypted text from cipher
        int cipherTextOffset = startIndex;
        // All data after IV is encrypted data
        int cipherTextCount = cipherText.length - startIndex;

        if (hasAuthenticationTag) {
            byte[] authenticationTag;
            try {
                authenticationTag = prepareAuthenticationTag(iv, cipherText, cipherTextOffset, cipherTextCount);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidCipherTextSize"));
                Object[] msgArgs = {cipherText.length, minimumCipherTextLength};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }
            if (!(SecurityUtility.compareBytes(authenticationTag, cipherText, authenticationTagOffset,
                    cipherTextCount))) {
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidAlgorithmVersion"));
                // converting byte to Hexa Decimal
                Object[] msgArgs = {String.format("%02X ", cipherText[startIndex]),
                        String.format("%02X ")};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
            }

        }

        // Decrypt the text and return
        return decryptData(iv, cipherText, cipherTextOffset, cipherTextCount);
    }

    /**
     * Decrypt data with specified IV
     *
     * @param iv
     *        initialization vector
     * @param cipherText
     *        text to be decrypted
     * @param offset
     *        of cipher text
     * @param count
     *        length of cipher text
     * @return plain text
     * @throws MicrosoftDataEncryptionException
     */
    private byte[] decryptData(byte[] iv, byte[] cipherText, int offset,
            int count) throws MicrosoftDataEncryptionException {
        assert (cipherText != null);
        assert (iv != null);
        byte[] plainText = null;
        // key to be used for decryption
        SecretKeySpec skeySpec = new SecretKeySpec(columnEncryptionkey.getEncryptionKeyBytes(), "AES");
        IvParameterSpec ivector = new IvParameterSpec(iv);
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, ivector);
            plainText = decryptCipher.doFinal(cipherText, offset, count);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            byte[] sliceDEK = Arrays.copyOfRange(columnEncryptionkey.getEncryptionKeyBytes(),
                    columnEncryptionkey.getEncryptionKeyBytes().length - 10,
                    columnEncryptionkey.getEncryptionKeyBytes().length);
            byte[] sliceCipher = Arrays.copyOfRange(cipherText, cipherText.length - 10, cipherText.length);
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_DecryptionFailed"));
            Object[] msgArgs = {CryptographyExtensions.toHexStringWithDashes(sliceDEK),
                    CryptographyExtensions.toHexStringWithDashes(sliceCipher)};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        return plainText;

    }

    /**
     * Prepare the authentication tag
     *
     * @param iv
     *        initialization vector
     * @param cipherText
     * @param offset
     * @param length
     *        length of cipher text
     * @return authentication tag
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private byte[] prepareAuthenticationTag(byte[] iv, byte[] cipherText, int offset,
            int length) throws NoSuchAlgorithmException, InvalidKeyException {
        assert (cipherText != null);
        byte[] computedHash;
        byte[] authenticationTag = new byte[keySizeInBytes];

        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(columnEncryptionkey.getMacKeyBytes(), "HmacSHA256");
        hmac.init(key);
        hmac.update(version, 0, version.length);
        hmac.update(iv, 0, iv.length);
        hmac.update(cipherText, offset, length);
        hmac.update(versionSize, 0, version.length);
        computedHash = hmac.doFinal();
        System.arraycopy(computedHash, 0, authenticationTag, 0, authenticationTag.length);

        return authenticationTag;
    }
}
