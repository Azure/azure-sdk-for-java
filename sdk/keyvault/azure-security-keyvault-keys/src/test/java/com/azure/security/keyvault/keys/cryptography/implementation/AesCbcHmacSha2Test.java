// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AesCbcHmacSha2Test {

    private static Stream<Arguments> algorithmProvider() {
        return Stream.of(Arguments.of(new Aes128CbcHmacSha256(), 32), Arguments.of(new Aes192CbcHmacSha384(), 48),
            Arguments.of(new Aes256CbcHmacSha512(), 64));
    }

    @ParameterizedTest
    @MethodSource("algorithmProvider")
    public void decryptorDoFinalRoundTrip(AesCbcHmacSha2 algorithm, int keySizeBytes) throws Exception {
        SecureRandom random = new SecureRandom();

        byte[] key = new byte[keySizeBytes];
        random.nextBytes(key);

        byte[] iv = new byte[16];
        random.nextBytes(iv);

        byte[] plaintext = new byte[64];
        random.nextBytes(plaintext);

        byte[] aad = new byte[16];
        random.nextBytes(aad);

        // Encrypt
        ICryptoTransform encryptor = algorithm.createEncryptor(key, iv, aad, null);
        byte[] ciphertext = encryptor.doFinal(plaintext);
        byte[] tag = ((IAuthenticatedCryptoTransform) encryptor).getTag();

        // Decrypt with correct tag
        ICryptoTransform decryptor = algorithm.createDecryptor(key, iv, aad, tag);
        byte[] decrypted = decryptor.doFinal(ciphertext);

        assertArrayEquals(plaintext, decrypted);
    }

    @ParameterizedTest
    @MethodSource("algorithmProvider")
    public void decryptorDoFinalFailsWithWrongTag(AesCbcHmacSha2 algorithm, int keySizeBytes) throws Exception {
        SecureRandom random = new SecureRandom();

        byte[] key = new byte[keySizeBytes];
        random.nextBytes(key);

        byte[] iv = new byte[16];
        random.nextBytes(iv);

        byte[] plaintext = new byte[64];
        random.nextBytes(plaintext);

        byte[] aad = new byte[16];
        random.nextBytes(aad);

        // Encrypt
        ICryptoTransform encryptor = algorithm.createEncryptor(key, iv, aad, null);
        byte[] ciphertext = encryptor.doFinal(plaintext);
        byte[] tag = ((IAuthenticatedCryptoTransform) encryptor).getTag();

        // Tamper with the tag
        byte[] wrongTag = tag.clone();
        wrongTag[0] ^= 0xFF;

        // Decrypt with wrong tag should fail
        ICryptoTransform decryptor = algorithm.createDecryptor(key, iv, aad, wrongTag);
        assertThrows(IllegalArgumentException.class, () -> decryptor.doFinal(ciphertext));
    }
}
