// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import com.microsoft.azure.keyvault.cryptography.AsymmetricSignatureAlgorithm;
import com.microsoft.azure.keyvault.cryptography.ISignatureTransform;
import com.microsoft.azure.keyvault.cryptography.Strings;

public abstract class RsaSignature extends AsymmetricSignatureAlgorithm {

    private static final BigInteger TWO_FIVE_SIX = new BigInteger("256");
    private static final byte[] SHA_256_PREFIX = new byte[] { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };

    protected RsaSignature(String name) {
        super(name);
    }

    protected int getOctetLength(int bits) {
        return (bits % 8 > 0) ? bits >> 3 + 1 : bits >> 3;
    }

    /*
     * See https://tools.ietf.org/html/rfc3447#section-4.2
     */
    protected BigInteger OS2IP(byte[] x) {

        if (x == null || x.length == 0) {
            throw new IllegalArgumentException("x");
        }

        return new BigInteger(1, x);
    }

    /*
     * See https://tools.ietf.org/html/rfc3447#section-4.1
     */
    protected byte[] I2OSP(BigInteger x, int xLen) {

        if (x == null) {
            throw new IllegalArgumentException("x");
        }

        if (xLen <= 0) {
            throw new IllegalArgumentException("xLen");
        }

        if (x.compareTo(TWO_FIVE_SIX.pow(xLen)) == 1) {
            throw new IllegalArgumentException("integer too large");
        }

        // Even if x is less than 256^xLen, sometiems x.toByteArray() returns 257 bytes with leading zero
        byte[] bigEndianBytes = x.toByteArray();
        byte[] bytes;
        if (bigEndianBytes.length == 257 && bigEndianBytes[0] == 0) {
            bytes = Arrays.copyOfRange(bigEndianBytes, 1, 257);
        } else {
            bytes = bigEndianBytes;
        }

        if (bytes.length > xLen) {
            throw new IllegalArgumentException("integer too large");
        }

        byte[] result = new byte[xLen];

        System.arraycopy(bytes, 0, result, xLen - bytes.length, bytes.length);

        return result;
    }

    /*
     * See https://tools.ietf.org/html/rfc3447#section-5.2.1
     */
    protected BigInteger RSASP1(RSAPrivateKey key, BigInteger m) {

        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (m == null) {
            throw new IllegalArgumentException("m");
        }

        BigInteger n = key.getModulus();
        BigInteger d = key.getPrivateExponent();

        if (m.compareTo(BigInteger.ONE) == -1 || m.compareTo(n) != -1) {
            throw new IllegalArgumentException("message representative out of range");
        }

        return m.modPow(d, n);
    }

    /*
     * See https://tools.ietf.org/html/rfc3447#section-5.2.2
     */
    protected BigInteger RSAVP1(RSAPublicKey key, BigInteger s) {

        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (s == null) {
            throw new IllegalArgumentException("s");
        }
        BigInteger n = key.getModulus();
        BigInteger e = key.getPublicExponent();

        if (s.compareTo(BigInteger.ONE) == -1 || s.compareTo(n) != -1) {
            throw new IllegalArgumentException("message representative out of range");
        }

        return s.modPow(e, n);
    }

    /*
     * See https://tools.ietf.org/html/rfc3447#section-9.2
     */
    protected byte[] EMSA_PKCS1_V1_5_ENCODE(byte[] m, int emLen, String algorithm) throws NoSuchAlgorithmException {

        // Check m
        if (m == null || m.length == 0) {
            throw new IllegalArgumentException("m");
        }

        MessageDigest messageDigest   = null;

        // Check algorithm
        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        // Only supported algorithms
        if (algorithm.equals("SHA-256")) {

            // Initialize digest
            messageDigest   = MessageDigest.getInstance("SHA-256");
        } else {
            throw new IllegalArgumentException("algorithm");
        }

        // Hash the message
        byte[] digest = messageDigest.digest(m);

        // Construct T, the DER encoded DigestInfo structure
        return EMSA_PKCS1_V1_5_ENCODE_HASH(digest, emLen, algorithm);
    }

    /*
     * See https://tools.ietf.org/html/rfc3447#section-9.2
     */
    protected byte[] EMSA_PKCS1_V1_5_ENCODE_HASH(byte[] h, int emLen, String algorithm) throws NoSuchAlgorithmException {

        // Check m
        if (h == null || h.length == 0) {
            throw new IllegalArgumentException("m");
        }

        byte[] algorithmPrefix = null;

        // Check algorithm
        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        // Only supported algorithms
        if (algorithm.equals("SHA-256")) {

            // Initialize prefix and digest
            algorithmPrefix = SHA_256_PREFIX;

            if (h.length != 32) {
                throw new IllegalArgumentException("h is incorrect length for SHA-256");
            }
        } else {
            throw new IllegalArgumentException("algorithm");
        }


        // Construct t, the DER encoded DigestInfo structure
        byte[] t = new byte[algorithmPrefix.length + h.length];

        System.arraycopy(algorithmPrefix, 0, t, 0, algorithmPrefix.length);
        System.arraycopy(h, 0, t, algorithmPrefix.length, h.length);

        if (emLen < t.length + 11) {
            throw new IllegalArgumentException("intended encoded message length too short");
        }

        // Construct ps
        byte[] ps = new byte[emLen - t.length - 3];

        for (int i = 0; i < ps.length; i++) {
            ps[i] = (byte) 0xff;
        }

        // Construct em
        byte[] em = new byte[ps.length + t.length + 3];

        em[0] = 0x00; em[1] = 0x01; em[ps.length + 2] = 0x00;

        System.arraycopy(ps, 0, em, 2, ps.length);
        System.arraycopy(t, 0, em, ps.length + 3, t.length);

        return em;
    }

    public abstract ISignatureTransform createSignatureTransform(KeyPair keyPair);
}
