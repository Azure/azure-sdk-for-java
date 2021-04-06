// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyAsyncClient}
 */
public final class LocalCryptographyAsyncClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link LocalCryptographyAsyncClient}
     * @return An instance of {@link LocalCryptographyAsyncClient}
     */
    public LocalCryptographyAsyncClient createAsyncClient() {
        JsonWebKey jsonWebKey = null;
        // BEGIN: com.azure.security.keyvault.keys.cryptography.async.LocalCryptographyAsyncClient.instantiation
        LocalCryptographyAsyncClient cryptographyAsyncClient = new LocalCryptographyClientBuilder()
            .key(jsonWebKey)
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.async.LocalCryptographyAsyncClient.instantiation
        return cryptographyAsyncClient;
    }


    /**
     * Generates a code sample for using {@link LocalCryptographyAsyncClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link LocalCryptographyAsyncClient#encrypt(EncryptionAlgorithm, byte[])}
     */
    public void encrypt() {
        LocalCryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte
        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);
        cryptographyAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(encryptResult ->
                System.out.printf("Received encrypted content of length %d with algorithm %s \n",
                    encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.encrypt#EncryptParameters
        byte[] plaintextBytes = new byte[100];
        new Random(0x1234567L).nextBytes(plaintextBytes);
        byte[] iv = {
            (byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd,
            (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04
        };
        EncryptParameters encryptParameters = EncryptParameters.createA128CbcParameters(plaintextBytes, iv);

        cryptographyAsyncClient.encrypt(encryptParameters)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(encryptResult ->
                System.out.printf("Received encrypted content of length %d with algorithm %s \n",
                    encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.encrypt#EncryptParameters
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyAsyncClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link LocalCryptographyAsyncClient#decrypt(EncryptionAlgorithm, byte[])}
     */
    public void decrypt() {
        LocalCryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte
        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);
        cryptographyAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(decryptResult ->
                System.out.printf("Received decrypted content of length %d\n", decryptResult.getPlainText().length));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.decrypt#DecryptParameters
        byte[] plaintextBytes = new byte[100];
        new Random(0x1234567L).nextBytes(plaintextBytes);
        byte[] iv = {
            (byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd,
            (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04
        };
        DecryptParameters decryptParameters = DecryptParameters.createA128CbcParameters(plaintextBytes, iv);

        cryptographyAsyncClient.decrypt(decryptParameters)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(decryptResult ->
                System.out.printf("Received decrypted content of length %d\n", decryptResult.getPlainText().length));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.decrypt#DecryptParameters
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyAsyncClient#sign(SignatureAlgorithm, byte[])} and
     * {@link LocalCryptographyAsyncClient#verify(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signVerify() throws NoSuchAlgorithmException {
        LocalCryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.sign#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();
        cryptographyAsyncClient.sign(SignatureAlgorithm.ES256, digest)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(signResult ->
                System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.sign#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte
        cryptographyAsyncClient.verify(SignatureAlgorithm.ES256, digest, signature)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(verifyResult ->
                System.out.printf("Verification status %s", verifyResult.isValid()));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte
    }


    /**
     * Generates a code sample for using {@link LocalCryptographyAsyncClient#signData(SignatureAlgorithm, byte[])} and
     * {@link LocalCryptographyAsyncClient#verifyData(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        LocalCryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.signData#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        cryptographyAsyncClient.sign(SignatureAlgorithm.ES256, data)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(signResult ->
                System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.signData#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte
        cryptographyAsyncClient.verify(SignatureAlgorithm.ES256, data, signature)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(verifyResult ->
                System.out.printf("Verification status %s", verifyResult.isValid()));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyAsyncClient#wrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link LocalCryptographyAsyncClient#unwrapKey(KeyWrapAlgorithm, byte[])}
     */
    public void wrapKeyUnwrapKey() {
        LocalCryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] encryptedKey = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);
        cryptographyAsyncClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(keyWrapResult ->
                System.out.printf("Received encypted key of length %d with algorithm %s",
                    keyWrapResult.getEncryptedKey().length, keyWrapResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte
        cryptographyAsyncClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, encryptedKey)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(keyUnwrapResult ->
                System.out.printf("Received key of length %d", keyUnwrapResult.getKey().length));
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte
    }
}
