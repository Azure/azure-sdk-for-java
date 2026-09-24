// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class KeyVaultEncodeTest {

    private static final byte TEST_TAG = '&';

    @Test
    public void buildLengthBytesTest() {
        Random random = new Random();
        int a = random.nextInt(1 << 7);
        byte[] result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(2, result.length);
        Assertions.assertEquals(TEST_TAG, result[0]);
        Assertions.assertEquals((byte) a, result[1]);

        a = random.nextInt((1 << 8) - (1 << 7)) + (1 << 7);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals(TEST_TAG, result[0]);
        Assertions.assertEquals((byte) 0x081, result[1]);
        Assertions.assertEquals((byte) a, result[2]);

        a = random.nextInt((1 << 16) - (1 << 8)) + (1 << 8);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(4, result.length);
        Assertions.assertEquals(TEST_TAG, result[0]);
        Assertions.assertEquals((byte) 0x082, result[1]);
        Assertions.assertEquals((byte) (a >> 8), result[2]);
        Assertions.assertEquals((byte) a, result[3]);

        a = random.nextInt((1 << 24) - (1 << 16)) + (1 << 16);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(5, result.length);
        Assertions.assertEquals(TEST_TAG, result[0]);
        Assertions.assertEquals((byte) 0x083, result[1]);
        Assertions.assertEquals((byte) (a >> 16), result[2]);
        Assertions.assertEquals((byte) (a >> 8), result[3]);
        Assertions.assertEquals((byte) a, result[4]);

        a = random.nextInt((1 << 30) - (1 << 24)) + (1 << 24);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(6, result.length);
        Assertions.assertEquals(TEST_TAG, result[0]);
        Assertions.assertEquals((byte) 0x084, result[1]);
        Assertions.assertEquals((byte) (a >> 24), result[2]);
        Assertions.assertEquals((byte) (a >> 16), result[3]);
        Assertions.assertEquals((byte) (a >> 8), result[4]);
        Assertions.assertEquals((byte) a, result[5]);
    }

    @Test
    public void concatBytesWithThreeBytes() {
        SecureRandom random = new SecureRandom();
        byte[] byte1 = random.generateSeed(32);
        byte[] byte2 = random.generateSeed(32);
        byte[] byte3 = random.generateSeed(32);
        byte[] result = KeyVaultEncode.concatBytes(byte1, byte2, byte3);
        Assertions.assertArrayEquals(byte1, Arrays.copyOfRange(result, 0, byte1.length));
        Assertions.assertArrayEquals(byte2, Arrays.copyOfRange(result, byte1.length, byte1.length + byte2.length));
        Assertions.assertArrayEquals(byte3, Arrays.copyOfRange(result, byte1.length + byte2.length, result.length));
    }

    @Test
    public void concatBytesWithTwoBytes() {
        SecureRandom random = new SecureRandom();
        byte[] byte1 = random.generateSeed(32);
        byte[] byte2 = random.generateSeed(32);
        byte[] result = KeyVaultEncode.concatBytes(byte1, byte2);
        Assertions.assertArrayEquals(byte1, Arrays.copyOfRange(result, 0, byte1.length));
        Assertions.assertArrayEquals(byte2, Arrays.copyOfRange(result, byte1.length, result.length));
    }

    @Test
    public void toBigIntegerBytesWithLengthPrefixTest() {
        SecureRandom random = new SecureRandom();
        byte[] testByte = random.generateSeed(32);
        int offset = random.nextInt(testByte.length);
        int length = random.nextInt(testByte.length - offset);
        byte[] result = KeyVaultEncode.toBigIntegerBytesWithLengthPrefix(testByte, offset, length);
        byte[] value;
        switch (result[1]) {
            case (byte) 0x081:
                value = Arrays.copyOfRange(result, 3, 3 + result[2]);
                break;

            case (byte) 0x082:
                value = Arrays.copyOfRange(result, 4, 4 + result[3]);
                break;

            case (byte) 0x083:
                value = Arrays.copyOfRange(result, 5, 5 + result[4]);
                break;

            case (byte) 0x084:
                value = Arrays.copyOfRange(result, 6, 6 + result[5]);
                break;

            default:
                value = Arrays.copyOfRange(result, 2, 2 + result[1]);
        }
        BigInteger bigInteger = new BigInteger(value);
        Assertions.assertEquals(bigInteger, new BigInteger(1, Arrays.copyOfRange(testByte, offset, offset + length)));
    }

    @Test
    public void encodeByteTest() {
        String encryptedString
            = "3GZWGlDZdFl+VYcngv/qEvbWsuxXud+EMP1Od19DTBQHXfSnOdrG8DKEByztiWbEIQIp45rO7uoTAae4T6+GUDtjtotfejKeT2En3/Cekm9ZQPs45Hx4rhSLIH40ZTdw";
        String encodedString
            = "MGUCMQDcZlYaUNl0WX5VhyeC/+oS9tay7Fe534Qw/U53X0NMFAdd9Kc52sbwMoQHLO2JZsQCMCECKeOazu7qEwGnuE+vhlA7Y7aLX3oynk9hJ9/wnpJvWUD7OOR8eK4UiyB+NGU3cA==";
        //Digital signature generated during EC handshake, when using Key Less, which is the result from azure service,
        //otherwise, which is generated by ECDSASignature in server
        byte[] encrypted = Base64.getDecoder().decode(encryptedString);
        // The encoded digital signature, which is obtained when the client verifies the digital signature
        byte[] encoded = Base64.getDecoder().decode(encodedString);
        Assertions.assertArrayEquals(encoded, KeyVaultEncode.encodeByte(encrypted));
    }

}
