// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
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
        Assertions.assertEquals(result.length, 2);
        Assertions.assertEquals(result[0], TEST_TAG);
        Assertions.assertEquals(result[1], (byte) a);

        a = random.nextInt((1 << 8) - (1 << 7)) + (1 << 7);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(result.length, 3);
        Assertions.assertEquals(result[0], TEST_TAG);
        Assertions.assertEquals(result[1], (byte) 0x081);
        Assertions.assertEquals(result[2], (byte) a);

        a = random.nextInt((1 << 16) - (1 << 8)) + (1 << 8);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(result.length, 4);
        Assertions.assertEquals(result[0], TEST_TAG);
        Assertions.assertEquals(result[1], (byte) 0x082);
        Assertions.assertEquals(result[2], (byte) (a >> 8));
        Assertions.assertEquals(result[3], (byte) a);

        a = random.nextInt((1 << 24) - (1 << 16)) + (1 << 16);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(result.length, 5);
        Assertions.assertEquals(result[0], TEST_TAG);
        Assertions.assertEquals(result[1], (byte) 0x083);
        Assertions.assertEquals(result[2], (byte) (a >> 16));
        Assertions.assertEquals(result[3], (byte) (a >> 8));
        Assertions.assertEquals(result[4], (byte) a);

        a = random.nextInt((1 << 30) - (1 << 24)) + (1 << 24);
        result = KeyVaultEncode.buildLengthBytes(TEST_TAG, a);
        Assertions.assertEquals(result.length, 6);
        Assertions.assertEquals(result[0], TEST_TAG);
        Assertions.assertEquals(result[1], (byte) 0x084);
        Assertions.assertEquals(result[2], (byte) (a >> 24));
        Assertions.assertEquals(result[3], (byte) (a >> 16));
        Assertions.assertEquals(result[4], (byte) (a >> 8));
        Assertions.assertEquals(result[5], (byte) a);
    }

    @Test
    public void concatBytesWithThreeBytes() {
        byte[] byte1 = RandomString.make(32).getBytes();
        byte[] byte2 = RandomString.make(32).getBytes();
        byte[] byte3 = RandomString.make(32).getBytes();
        byte[] result = KeyVaultEncode.concatBytes(byte1, byte2, byte3);
        Assertions.assertArrayEquals(byte1, Arrays.copyOfRange(result, 0, byte1.length));
        Assertions.assertArrayEquals(byte2, Arrays.copyOfRange(result, byte1.length, byte1.length + byte2.length));
        Assertions.assertArrayEquals(byte3, Arrays.copyOfRange(result, byte1.length + byte2.length, result.length));
    }


    @Test
    public void concatBytesWithTwoBytes() {
        byte[] byte1 = RandomString.make(32).getBytes();
        byte[] byte2 = RandomString.make(32).getBytes();
        byte[] result = KeyVaultEncode.concatBytes(byte1, byte2);
        Assertions.assertArrayEquals(byte1, Arrays.copyOfRange(result, 0, byte1.length));
        Assertions.assertArrayEquals(byte2, Arrays.copyOfRange(result, byte1.length, result.length));
    }

    @Test
    public void toBigIntegerBytesWithLengthPrefixTest() {
        byte[] testByte = RandomString.make(32).getBytes();
        Random random = new Random();
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
        String encryptedString = "3GZWGlDZdFl+VYcngv/qEvbWsuxXud+EMP1Od19DTBQHXfSnOdrG8DKEByztiWbEIQIp45rO7uoTAae4T6+GUDtjtotfejKeT2En3/Cekm9ZQPs45Hx4rhSLIH40ZTdw";
        String encodedString = "MGUCMQDcZlYaUNl0WX5VhyeC/+oS9tay7Fe534Qw/U53X0NMFAdd9Kc52sbwMoQHLO2JZsQCMCECKeOazu7qEwGnuE+vhlA7Y7aLX3oynk9hJ9/wnpJvWUD7OOR8eK4UiyB+NGU3cA==";
        //Digital signature generated during EC handshake, when using Key Less, which is the result from azure service,
        //otherwise, which is generated by ECDSASignature in server
        byte[] encrypted = Base64.getDecoder().decode(encryptedString);
        // The encoded digital signature, which is obtained when the client verifies the digital signature
        byte[] encoded = Base64.getDecoder().decode(encodedString);
        Assertions.assertArrayEquals(encoded, KeyVaultEncode.encodeByte(encrypted));
    }

}
