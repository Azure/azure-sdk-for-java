// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}
 */
public final class LocalCryptographyClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link LocalCryptographyClient}
     * @return An instance of {@link LocalCryptographyClient}
     */
    public LocalCryptographyClient createClient() {
        JsonWebKey jsonWebKey = null;
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.instantiation
        LocalCryptographyClient cryptographyClient = new LocalCryptographyClientBuilder()
            .key(jsonWebKey)
            .buildClient();
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.instantiation
        return cryptographyClient;
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link LocalCryptographyClient#encrypt(EncryptionAlgorithm, byte[])}
     */
    public void encrypt() {
        LocalCryptographyClient cryptographyClient = createClient();
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73
        };
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.encrypt#EncryptionAlgorithm-byte
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        EncryptResult encryptResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText);
        System.out.printf("Received encrypted content of length %d with algorithm %s \n",
            encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.encrypt#EncryptionAlgorithm-byte
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link LocalCryptographyClient#decrypt(EncryptionAlgorithm, byte[])}
     */
    public void decrypt() {
        LocalCryptographyClient cryptographyClient = createClient();
        byte[] encryptedData = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.decrypt#EncryptionAlgorithm-byte
        DecryptResult decryptResult = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptedData);
        System.out.printf("Received decrypted content of length %d\n", decryptResult.getPlainText().length);
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.decrypt#EncryptionAlgorithm-byte
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyClient#sign(SignatureAlgorithm, byte[])} and
     * {@link LocalCryptographyClient#verify(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signVerify() throws NoSuchAlgorithmException {
        LocalCryptographyClient cryptographyClient = createClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.sign#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();
        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, digest);
        System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length,
            signResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.sign#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.verify#SignatureAlgorithm-byte-byte
        VerifyResult verifyResult = cryptographyClient.verify(SignatureAlgorithm.ES256, digest, signature);
        System.out.printf("Verification status %s", verifyResult.isValid());
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.verify#SignatureAlgorithm-byte-byte
    }


    /**
     * Generates a code sample for using {@link LocalCryptographyClient#signData(SignatureAlgorithm, byte[])} and
     * {@link LocalCryptographyClient#verifyData(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        LocalCryptographyClient cryptographyClient = createClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.signData#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, data);
        System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length);
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.signData#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.verifyData#SignatureAlgorithm-byte-byte
        VerifyResult verifyResult =  cryptographyClient.verify(SignatureAlgorithm.ES256, data, signature);
        System.out.printf("Verification status %s", verifyResult.isValid());
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.verifyData#SignatureAlgorithm-byte-byte
    }

    /**
     * Generates a code sample for using {@link LocalCryptographyClient#wrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link LocalCryptographyClient#unwrapKey(KeyWrapAlgorithm, byte[])}
     */
    public void wrapKeyUnwrapKey() {
        LocalCryptographyClient cryptographyClient = createClient();
        byte[] encryptedKey = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.wrapKey#KeyWrapAlgorithm-byte
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);
        WrapResult wrapResult = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key);
        System.out.printf("Received encypted key of length %d with algorithm %s", wrapResult.getEncryptedKey().length,
            wrapResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.wrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.unwrapKey#KeyWrapAlgorithm-byte
        UnwrapResult unwrapResult = cryptographyClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, encryptedKey);
        System.out.printf("Received key of length %d", unwrapResult.getKey().length);
        // END: com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.unwrapKey#KeyWrapAlgorithm-byte
    }
}
