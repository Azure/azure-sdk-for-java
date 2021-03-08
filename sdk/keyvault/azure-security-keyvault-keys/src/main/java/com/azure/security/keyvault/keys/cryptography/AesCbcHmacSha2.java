// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.logging.ClientLogger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

abstract class AesCbcHmacSha2 extends SymmetricEncryptionAlgorithm {
    private static final long BYTE_TO_BITS = 8L;
    private final ClientLogger logger = new ClientLogger(AesCbcHmacSha2.class);

    abstract static class AbstractAesCbcHmacSha2CryptoTransform implements IAuthenticatedCryptoTransform {
        byte[] tag;
        final byte[] aadLength;
        final Mac hmac;
        final byte[] hmacKey;
        final ICryptoTransform inner;

        AbstractAesCbcHmacSha2CryptoTransform(String name, byte[] keyMaterial, byte[] initializationVector, byte[] authenticationData, ICryptoTransform.Factory<byte[]> factory)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException {
            Triplet<byte[], byte[], Mac> parameters = getAlgorithmParameters(name, keyMaterial);
            inner = factory.create(parameters.getLeft());
            hmacKey = parameters.getMiddle();
            hmac = parameters.getRight();
            aadLength = toBigEndian(authenticationData.length * BYTE_TO_BITS);
            hmac.update(authenticationData);
            hmac.update(initializationVector);
        }

        @Override
        public byte[] getTag() {
            return tag;
        }

        private byte[] toBigEndian(long i) {
            byte[] shortRepresentation = BigInteger.valueOf(i).toByteArray();
            byte[] longRepresentation = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

            System.arraycopy(shortRepresentation, 0, longRepresentation,
                longRepresentation.length - shortRepresentation.length, shortRepresentation.length);

            return longRepresentation;
        }

        private Triplet<byte[], byte[], Mac> getAlgorithmParameters(String algorithm, byte[] key)
            throws InvalidKeyException, NoSuchAlgorithmException {

            byte[] aesKey;
            byte[] hmacKey;
            Mac hmac;

            if (algorithm.equalsIgnoreCase(Aes128CbcHmacSha256.ALGORITHM_NAME)) {
                if ((key.length << 3) < 256) {
                    throw new IllegalArgumentException(String.format("%s key length in bits %d < 256", algorithm,
                        key.length << 3));
                }

                hmacKey = new byte[128 >> 3];
                aesKey = new byte[128 >> 3];

                // The HMAC key precedes the AES key
                System.arraycopy(key, 0, hmacKey, 0, 128 >> 3);
                System.arraycopy(key, 128 >> 3, aesKey, 0, 128 >> 3);

                hmac = Mac.getInstance("HmacSHA256");
                hmac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));

            } else if (algorithm.equalsIgnoreCase(Aes192CbcHmacSha384.ALGORITHM_NAME)) {

                if ((key.length << 3) < 384) {
                    throw new IllegalArgumentException(String.format("%s key length in bits %d < 384", algorithm,
                        key.length << 3));
                }

                hmacKey = new byte[192 >> 3];
                aesKey = new byte[192 >> 3];

                // The HMAC key precedes the AES key
                System.arraycopy(key, 0, hmacKey, 0, 192 >> 3);
                System.arraycopy(key, 192 >> 3, aesKey, 0, 192 >> 3);

                hmac = Mac.getInstance("HmacSHA384");
                hmac.init(new SecretKeySpec(hmacKey, "HmacSHA384"));
            } else if (algorithm.equalsIgnoreCase(Aes256CbcHmacSha512.ALGORITHM_NAME)) {

                if ((key.length << 3) < 512) {
                    throw new IllegalArgumentException(String.format("%s key length in bits %d < 512", algorithm,
                        key.length << 3));
                }

                hmacKey = new byte[256 >> 3];
                aesKey = new byte[256 >> 3];

                // The HMAC key precedes the AES key
                System.arraycopy(key, 0, hmacKey, 0, 256 >> 3);
                System.arraycopy(key, 256 >> 3, aesKey, 0, 256 >> 3);

                hmac = Mac.getInstance("HmacSHA512");
                hmac.init(new SecretKeySpec(hmacKey, "HmacSHA512"));
            } else {
                throw new IllegalArgumentException(String.format("Unsupported algorithm: %s", algorithm));
            }

            return new Triplet<>(aesKey, hmacKey, hmac);
        }
    }

    static class AesCbcHmacSha2Decryptor extends AbstractAesCbcHmacSha2CryptoTransform {
        final ClientLogger logger = new ClientLogger(AesCbcHmacSha2Decryptor.class);

        AesCbcHmacSha2Decryptor(String name, byte[] key, byte[] iv, byte[] authenticationData, byte[] authenticationTag,
                                Provider provider)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
            super(name, key, iv, authenticationData, aesKey -> new AesCbc.AesCbcDecryptor(aesKey, iv, provider));
            // Save the tag
            tag = authenticationTag;
        }


        @Override
        public byte[] doFinal(byte[] input)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            // Add the cipher text to the running hash
            hmac.update(input);

            // Add the associated_data_length bytes to the hash
            byte[] hash = hmac.doFinal(aadLength);

            // Compute the new tag
            byte[] tag = new byte[hmacKey.length];
            System.arraycopy(hash, 0, tag, 0, hmacKey.length);

            // Check the tag before performing the final decrypt
            if (!ByteExtensions.sequenceEqualConstantTime(tag, tag)) {
                throw logger.logExceptionAsWarning(new IllegalArgumentException("Data is not authentic"));
            }

            return inner.doFinal(input);
        }

    }

    static class AesCbcHmacSha2Encryptor extends AbstractAesCbcHmacSha2CryptoTransform {

        AesCbcHmacSha2Encryptor(String name, byte[] key, byte[] iv, byte[] authenticationData, Provider provider)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {
            super(name, key, iv, authenticationData, aesKey -> new AesCbc.AesCbcEncryptor(aesKey, iv, provider));
        }


        @Override
        public byte[] doFinal(byte[] input)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            // Encrypt the block
            byte[] output = inner.doFinal(input);

            // Add the cipher text to the running hash
            hmac.update(output);

            // Add the associated_data_length bytes to the hash
            byte[] hash = hmac.doFinal(aadLength);

            // Compute the tag
            tag = new byte[hmacKey.length];
            System.arraycopy(hash, 0, tag, 0, tag.length);

            return output;
        }
    }

    protected AesCbcHmacSha2(String name) {
        super(name);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        return createDecryptor(key, iv, additionalAuthenticatedData, authenticationTag, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {
        if (key == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("No key material"));
        }

        if (iv == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("No initialization vector"));
        }

        if (additionalAuthenticatedData == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("No authentication data"));
        }

        if (authenticationTag == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("No authentication tag"));
        }

        // Create the Decryptor
        return new AesCbcHmacSha2Decryptor(getName(), key, iv, additionalAuthenticatedData, authenticationTag, provider);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        return createEncryptor(key, iv, additionalAuthenticatedData, null, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
                                            byte[] authenticationTag, Provider provider)
        throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("No key material"));
        }

        if (iv == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("No initialization vector"));
        }

        if (additionalAuthenticatedData == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("No authentication data"));
        }

        // Create the Encryptor
        return new AesCbcHmacSha2Encryptor(getName(), key, iv, additionalAuthenticatedData, provider);
    }
}
