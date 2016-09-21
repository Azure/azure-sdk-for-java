/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.tuple.Triple;

import com.microsoft.azure.keyvault.cryptography.IAuthenticatedCryptoTransform;
import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.SymmetricEncryptionAlgorithm;

public abstract class AesCbcHmacSha2 extends SymmetricEncryptionAlgorithm {

    static class AesCbcHmacSha2Decryptor implements IAuthenticatedCryptoTransform {

        final byte[]           _aad_length;
        final Mac              _hmac;
        final byte[]           _hmac_key;
        final ICryptoTransform _inner;

        byte[] _tag;

        AesCbcHmacSha2Decryptor(String name, byte[] key, byte[] iv, byte[] associatedData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
            // Split the key to get the AES key, the HMAC key and the HMAC
            // object
            Triple<byte[], byte[], Mac> parameters = GetAlgorithmParameters(name, key);

            // Save the MAC provider and key
            _hmac = parameters.getRight();
            _hmac_key = parameters.getMiddle();

            // Create the AES provider
            _inner = new AesCbc.AesCbcDecryptor(parameters.getLeft(), iv, provider);

            _aad_length = toBigEndian(associatedData.length * 8);

            // Prime the hash.
            _hmac.update(associatedData);
            _hmac.update(iv);
        }

        @Override
        public byte[] getTag() {
            return _tag;
        }

        // public int TransformBlock( byte[] inputBuffer, int inputOffset, int
        // inputCount, byte[] outputBuffer, int outputOffset )
        // {
        // // Add the cipher text to the running hash
        // _hmac.TransformBlock( inputBuffer, inputOffset, inputCount,
        // inputBuffer, inputOffset );
        //
        // // Decrypt the cipher text
        // return _inner.TransformBlock( inputBuffer, inputOffset, inputCount,
        // outputBuffer, outputOffset );
        // }

        @Override
        public byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            // Add the cipher text to the running hash
            _hmac.update(input);

            // Add the associated_data_length bytes to the hash
            byte[] hash = _hmac.doFinal(_aad_length);

            // Compute the tag
            _tag = new byte[_hmac_key.length];
            System.arraycopy(hash, 0, _tag, 0, _hmac_key.length);

            return _inner.doFinal(input);
        }
    }

    static class AesCbcHmacSha2Encryptor implements IAuthenticatedCryptoTransform {

        final byte[]           _aad_length;
        final Mac              _hmac;
        final byte[]           _hmac_key;
        final ICryptoTransform _inner;

        byte[] _tag;

        AesCbcHmacSha2Encryptor(String name, byte[] key, byte[] iv, byte[] associatedData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
            // Split the key to get the AES key, the HMAC key and the HMAC
            // object
            Triple<byte[], byte[], Mac> parameters = GetAlgorithmParameters(name, key);

            // Save the MAC provider and key
            _hmac = parameters.getRight();
            _hmac_key = parameters.getMiddle();

            // Create the AES encryptor
            _inner = new AesCbc.AesCbcEncryptor(parameters.getLeft(), iv, provider);

            _aad_length = toBigEndian(associatedData.length * 8);

            // Prime the hash.
            _hmac.update(associatedData);
            _hmac.update(iv);
        }

        @Override
        public byte[] getTag() {
            return _tag;
        }

        // public int TransformBlock( byte[] inputBuffer, int inputOffset, int
        // inputCount, byte[] outputBuffer, int outputOffset )
        // {
        // // Encrypt the block
        // var result = _inner.TransformBlock( inputBuffer, inputOffset,
        // inputCount, outputBuffer, outputOffset );
        //
        // // Add it to the running hash
        // _hmac.TransformBlock( outputBuffer, outputOffset, result,
        // outputBuffer, outputOffset );
        //
        // return result;
        // }

        @Override
        public byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            // Encrypt the block
            byte[] output = _inner.doFinal(input);

            // Add the cipher text to the running hash
            _hmac.update(output);

            // Add the associated_data_length bytes to the hash
            byte[] hash = _hmac.doFinal(_aad_length);

            // Compute the tag
            _tag = new byte[_hmac_key.length];
            System.arraycopy(hash, 0, _tag, 0, _tag.length);

            return output;
        }
    }

    protected AesCbcHmacSha2(String name) {
        super(name);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return CreateDecryptor(key, iv, authenticationData, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        if (key == null) {
            throw new IllegalArgumentException("No key material");
        }

        if (iv == null) {
            throw new IllegalArgumentException("No initialization vector");
        }

        if (authenticationData == null) {
            throw new IllegalArgumentException("No associated data");
        }

        // Create the Decryptor
        return new AesCbcHmacSha2Decryptor(getName(), key, iv, authenticationData, provider);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return CreateEncryptor(key, iv, authenticationData, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("No key material");
        }

        if (iv == null) {
            throw new IllegalArgumentException("No initialization vector");
        }

        if (authenticationData == null) {
            throw new IllegalArgumentException("No associated data");
        }

        // Create the Encryptor
        return new AesCbcHmacSha2Encryptor(getName(), key, iv, authenticationData, provider);
    }

    private static Triple<byte[], byte[], Mac> GetAlgorithmParameters(String algorithm, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {

        byte[] aes_key;
        byte[] hmac_key;
        Mac hmac;

        if (algorithm.equalsIgnoreCase(Aes128CbcHmacSha256.ALGORITHM_NAME)) {
            if ((key.length << 3) < 256) {
                throw new IllegalArgumentException(String.format("%s key length in bits %d < 256", algorithm, key.length << 3));
            }

            hmac_key = new byte[128 >> 3];
            aes_key = new byte[128 >> 3];

            // The HMAC key precedes the AES key
            System.arraycopy(key, 0, hmac_key, 0, 128 >> 3);
            System.arraycopy(key, 128 >> 3, aes_key, 0, 128 >> 3);

            hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(hmac_key, "HmacSHA256"));

        } else if (algorithm.equalsIgnoreCase(Aes192CbcHmacSha384.ALGORITHM_NAME)) {

            if ((key.length << 3) < 384) {
                throw new IllegalArgumentException(String.format("%s key length in bits %d < 384", algorithm, key.length << 3));
            }

            hmac_key = new byte[192 >> 3];
            aes_key = new byte[192 >> 3];

            // The HMAC key precedes the AES key
            System.arraycopy(key, 0, hmac_key, 0, 192 >> 3);
            System.arraycopy(key, 192 >> 3, aes_key, 0, 192 >> 3);

            hmac = Mac.getInstance("HmacSHA384");
            hmac.init(new SecretKeySpec(hmac_key, "HmacSHA384"));
        } else if (algorithm.equalsIgnoreCase(Aes256CbcHmacSha512.ALGORITHM_NAME)) {

            if ((key.length << 3) < 512) {
                throw new IllegalArgumentException(String.format("%s key length in bits %d < 512", algorithm, key.length << 3));
            }

            hmac_key = new byte[256 >> 3];
            aes_key = new byte[256 >> 3];

            // The HMAC key precedes the AES key
            System.arraycopy(key, 0, hmac_key, 0, 256 >> 3);
            System.arraycopy(key, 256 >> 3, aes_key, 0, 256 >> 3);

            hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(hmac_key, "HmacSHA512"));
        } else {
            throw new IllegalArgumentException(String.format("Unsupported algorithm: %s", algorithm));
        }

        return Triple.of(aes_key, hmac_key, hmac);
    }

    static byte[] toBigEndian(long i) {

        byte[] shortRepresentation = BigInteger.valueOf(i).toByteArray();
        byte[] longRepresentation = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };

        System.arraycopy(shortRepresentation, 0, longRepresentation, longRepresentation.length - shortRepresentation.length, shortRepresentation.length);

        return longRepresentation;
    }

}
