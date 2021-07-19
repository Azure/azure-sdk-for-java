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

    @Test
    public void buildLengthBytesTest() {
        Random random = new Random();
        int a = random.nextInt(1 << 7);
        byte[] result = KeyVaultEncode.buildLengthBytes((byte) a, a);
        Assertions.assertEquals(result.length, 2);
        Assertions.assertEquals(result[0], (byte) a);
        Assertions.assertEquals(result[1], (byte) a);

        a = random.nextInt((1 << 8) - (1 << 7)) + (1 << 7);
        result = KeyVaultEncode.buildLengthBytes((byte) a, a);
        Assertions.assertEquals(result.length, 3);
        Assertions.assertEquals(result[0], (byte) a);
        Assertions.assertEquals(result[1], (byte) 0x081);
        Assertions.assertEquals(result[2], (byte) a);

        a = random.nextInt((1 << 16) - (1 << 8)) + (1 << 8);
        result = KeyVaultEncode.buildLengthBytes((byte) a, a);
        Assertions.assertEquals(result.length, 4);
        Assertions.assertEquals(result[0], (byte) a);
        Assertions.assertEquals(result[1], (byte) 0x082);
        Assertions.assertEquals(result[2], (byte) (a >> 8));
        Assertions.assertEquals(result[3], (byte) a);

        a = random.nextInt((1 << 24) - (1 << 16)) + (1 << 16);
        result = KeyVaultEncode.buildLengthBytes((byte) a, a);
        Assertions.assertEquals(result.length, 5);
        Assertions.assertEquals(result[0], (byte) a);
        Assertions.assertEquals(result[1], (byte) 0x083);
        Assertions.assertEquals(result[2], (byte) (a >> 16));
        Assertions.assertEquals(result[3], (byte) (a >> 8));
        Assertions.assertEquals(result[4], (byte) a);

        a = random.nextInt((1 << 30) - (1 << 24)) + (1 << 24);
        result = KeyVaultEncode.buildLengthBytes((byte) a, a);
        Assertions.assertEquals(result.length, 6);
        Assertions.assertEquals(result[0], (byte) a);
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
                value = Arrays.copyOfRange(result, 3, result.length);
                break;
            case (byte) 0x082:
                value = Arrays.copyOfRange(result, 4, result.length);
                break;
            case (byte) 0x083:
                value = Arrays.copyOfRange(result, 5, result.length);
                break;
            case (byte) 0x084:
                value = Arrays.copyOfRange(result, 6, result.length);
                break;
            default:
                value = Arrays.copyOfRange(result, 2, result.length);
        }
        BigInteger bigInteger = new BigInteger(value);
        Assertions.assertEquals(bigInteger, new BigInteger(1, testByte, offset, length));
    }

    @Test
    public void encodeByteTest() {
        String encryptedString = "ekdELYx8lzYKJQDR3ptCyGIt60WElKuRb563Y89nBfsTwLhpN4eCiisXci0LTAvi3wudGWHHAJ9ghT4k51zwMg==";
        String encodedString = "MEQCIHpHRC2MfJc2CiUA0d6bQshiLetFhJSrkW+et2PPZwX7AiATwLhpN4eCiisXci0LTAvi3wudGWHHAJ9ghT4k51zwMg==";
        byte[] encrypted = Base64.getDecoder().decode(encryptedString);
        byte[] encoded = Base64.getDecoder().decode(encodedString);
        Assertions.assertArrayEquals(encoded, KeyVaultEncode.encodeByte(encrypted));
    }

}
