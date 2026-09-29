// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.cryptography;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyEncryptionKeyContractsTests {
    @Test
    public void resolverProvidesKeyWrappingContract() {
        KeyEncryptionKey key = new KeyEncryptionKey() {
            @Override
            public String getKeyId() {
                return "key-id";
            }

            @Override
            public byte[] wrapKey(String algorithm, byte[] value) {
                return reverse(value);
            }

            @Override
            public byte[] unwrapKey(String algorithm, byte[] encryptedKey) {
                return reverse(encryptedKey);
            }
        };
        KeyEncryptionKeyResolver resolver = keyId -> key;
        byte[] plaintext = new byte[] { 1, 2, 3 };

        KeyEncryptionKey resolved = resolver.buildKeyEncryptionKey("key-id");
        byte[] encrypted = resolved.wrapKey("test", plaintext);

        assertEquals("key-id", resolved.getKeyId());
        assertArrayEquals(new byte[] { 3, 2, 1 }, encrypted);
        assertArrayEquals(plaintext, resolved.unwrapKey("test", encrypted));
    }

    private static byte[] reverse(byte[] value) {
        byte[] result = value.clone();
        for (int left = 0, right = result.length - 1; left < right; left++, right--) {
            byte current = result[left];
            result[left] = result[right];
            result[right] = current;
        }
        return result;
    }
}
