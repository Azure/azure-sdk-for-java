// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;

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
import java.util.Objects;

abstract class AesCbcHmacSha2 extends SymmetricEncryptionAlgorithm {
    private static final long BYTE_TO_BITS = 8L;
    private static final ClientLogger LOGGER = new ClientLogger(AesCbcHmacSha2.class);

    abstract static class AbstractAesCbcHmacSha2CryptoTransform implements IAuthenticatedCryptoTransform {
        byte[] tag;
        final byte[] aadLength;
        final Mac hmac;
        final byte[] hmacKey;
        final ICryptoTransform inner;

        AbstractAesCbcHmacSha2CryptoTransform(String name, byte[] keyMaterial, byte[] initializationVector,
            byte[] authenticationData, ICryptoTransform.Factory<byte[]> factory) throws InvalidKeyException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException {

            Triplet<byte[], byte[], Mac> parameters = getAlgorithmParameters(name, keyMaterial);
            inner = factory.create(parameters.getFirst());
            hmacKey = parameters.getSecond();
            hmac = parameters.getThird();
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
            byte[] longRepresentation = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };

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
                    throw LOGGER.throwableAtError()
                        .addKeyValue("algorithm", algorithm)
                        .addKeyValue("keyLengthInBits", key.length << 3)
                        .log("Key is too short, must be at least 256 bits long.", InvalidKeyException::new);
                }

                hmacKey = new byte[128 >> 3];
                aesKey = new byte[128 >> 3];

                // The HMAC key precedes the AES key.
                System.arraycopy(key, 0, hmacKey, 0, 128 >> 3);
                System.arraycopy(key, 128 >> 3, aesKey, 0, 128 >> 3);

                hmac = Mac.getInstance("HmacSHA256");
                hmac.init(new SecretKeySpec(hmacKey, "HmacSHA256"));
            } else if (algorithm.equalsIgnoreCase(Aes192CbcHmacSha384.ALGORITHM_NAME)) {
                if ((key.length << 3) < 384) {
                    throw LOGGER.throwableAtError()
                        .addKeyValue("algorithm", algorithm)
                        .addKeyValue("keyLengthInBits", key.length << 3)
                        .log("Key is too short, must be at least 384 bits long.", InvalidKeyException::new);
                }

                hmacKey = new byte[192 >> 3];
                aesKey = new byte[192 >> 3];

                // The HMAC key precedes the AES key.
                System.arraycopy(key, 0, hmacKey, 0, 192 >> 3);
                System.arraycopy(key, 192 >> 3, aesKey, 0, 192 >> 3);

                hmac = Mac.getInstance("HmacSHA384");
                hmac.init(new SecretKeySpec(hmacKey, "HmacSHA384"));
            } else if (algorithm.equalsIgnoreCase(Aes256CbcHmacSha512.ALGORITHM_NAME)) {
                if ((key.length << 3) < 512) {
                    throw LOGGER.throwableAtError()
                        .addKeyValue("algorithm", algorithm)
                        .addKeyValue("keyLengthInBits", key.length << 3)
                        .log("Key is too short, must be at least 512 bits long.", InvalidKeyException::new);
                }

                hmacKey = new byte[256 >> 3];
                aesKey = new byte[256 >> 3];

                // The HMAC key precedes the AES key.
                System.arraycopy(key, 0, hmacKey, 0, 256 >> 3);
                System.arraycopy(key, 256 >> 3, aesKey, 0, 256 >> 3);

                hmac = Mac.getInstance("HmacSHA512");
                hmac.init(new SecretKeySpec(hmacKey, "HmacSHA512"));
            } else {
                throw LOGGER.throwableAtError()
                    .addKeyValue("algorithm", algorithm)
                    .log("Unsupported algorithm.", IllegalArgumentException::new);
            }

            return new Triplet<>(aesKey, hmacKey, hmac);
        }
    }

    static class AesCbcHmacSha2Decryptor extends AbstractAesCbcHmacSha2CryptoTransform {
        private static final ClientLogger LOGGER = new ClientLogger(AesCbcHmacSha2Decryptor.class);

        AesCbcHmacSha2Decryptor(String name, byte[] key, byte[] iv, byte[] authenticationData, byte[] authenticationTag,
            Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException {

            super(name, key, iv, authenticationData, aesKey -> new AesCbc.AesCbcDecryptor(aesKey, iv, provider));

            // Save the tag.
            tag = authenticationTag;
        }

        @Override
        public byte[] doFinal(byte[] input)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            // Add the cipher text to the running hash.
            hmac.update(input);

            // Add the associated_data_length bytes to the hash.
            byte[] hash = hmac.doFinal(aadLength);

            // Compute the new tag.
            byte[] tag = new byte[hmacKey.length];

            System.arraycopy(hash, 0, tag, 0, hmacKey.length);

            // Check the tag before performing the final decrypt
            if (!sequenceEqualConstantTime(tag, tag)) {
                throw LOGGER.throwableAtError().log("Data is not authentic", IllegalArgumentException::new);
            }

            return inner.doFinal(input);
        }

        /**
         * Compares two byte arrays in constant time.
         *
         * @param self The first byte array to compare.
         * @param other The second byte array to compare.
         * @return True if the two byte arrays are equal.
         */
        static boolean sequenceEqualConstantTime(byte[] self, byte[] other) {
            // Constant time comparison of two byte arrays
            long difference = (self.length & 0xffffffffL) ^ (other.length & 0xffffffffL);

            for (int i = 0; i < self.length && i < other.length; i++) {
                difference |= (self[i] ^ other[i]) & 0xffffffffL;
            }

            return difference == 0;
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

            // Encrypt the block.
            byte[] output = inner.doFinal(input);

            // Add the cipher text to the running hash.
            hmac.update(output);

            // Add the associated_data_length bytes to the hash.
            byte[] hash = hmac.doFinal(aadLength);

            // Compute the tag.
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
        byte[] authenticationTag) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        return createDecryptor(key, iv, additionalAuthenticatedData, authenticationTag, null);
    }

    @Override
    public ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
        byte[] authenticationTag, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidAlgorithmParameterException {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(iv, "'iv' cannot be null.");
        Objects.requireNonNull(additionalAuthenticatedData, "'additionalAuthenticatedData' cannot be null.");
        Objects.requireNonNull(authenticationTag, "'authenticationTag' cannot be null.");

        // Create the Decryptor.
        return new AesCbcHmacSha2Decryptor(getName(), key, iv, additionalAuthenticatedData, authenticationTag,
            provider);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
        byte[] authenticationTag) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidAlgorithmParameterException {

        return createEncryptor(key, iv, additionalAuthenticatedData, null, null);
    }

    @Override
    public ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] additionalAuthenticatedData,
        byte[] authenticationTag, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidAlgorithmParameterException {

        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(iv, "'iv' cannot be null.");
        Objects.requireNonNull(additionalAuthenticatedData, "'additionalAuthenticatedData' cannot be null.");

        // Create the Encryptor.
        return new AesCbcHmacSha2Encryptor(getName(), key, iv, additionalAuthenticatedData, provider);
    }

    private static class Triplet<T, U, V> {
        private final T first;
        private final U second;
        private final V third;

        Triplet(T first, U second, V third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        T getFirst() {
            return first;
        }

        U getSecond() {
            return second;
        }

        V getThird() {
            return third;
        }
    }
}
