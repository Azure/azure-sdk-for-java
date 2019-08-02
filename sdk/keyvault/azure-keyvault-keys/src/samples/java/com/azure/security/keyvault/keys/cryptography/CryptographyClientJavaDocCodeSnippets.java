// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credentials.TokenCredential;
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.models.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}
 */
public final class CryptographyClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link CryptographyClient}
     * @return An instance of {@link CryptographyClient}
     */
    public CryptographyClient createClient() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.instantiation
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier("<YOUR-KEY-IDENTIFIER>")
            .credential(new DefaultAzureCredential())
            .buildClient();
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.instantiation
        return cryptographyClient;
    }
    /**
     * Generates a code sample for using {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[], byte[], byte[])}
     */
    public void encrypt() {
        CryptographyClient cryptographyClient = createClient();
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73
        };
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#asymmetric-encrypt
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        EncryptResult encryptResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText);
        System.out.printf("Received encrypted content of length %d with algorithm %s \n",
            encryptResult.cipherText().length, encryptResult.algorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#asymmetric-encrypt

        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#symmetric-encrypt
        EncryptResult encryptionResult = cryptographyClient.encrypt(EncryptionAlgorithm.A192CBC_HS384, plainText,
            iv, authData);
        System.out.printf("Received encrypted content of length %d with algorithm %s \n",
            encryptResult.cipherText().length, encryptResult.algorithm().toString());

        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#symmetric-encrypt
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[], byte[], byte[], byte[])}
     */
    public void decrypt() {
        CryptographyClient cryptographyClient = createClient();
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73
        };
        byte[] authTag = {(byte) 0x65, (byte) 0x2c, (byte) 0x3f, (byte) 0xa3, (byte) 0x6b, (byte) 0x0a, (byte) 0x7c, (byte) 0x5b, (byte) 0x32, (byte) 0x19, (byte) 0xfa, (byte) 0xb3, (byte) 0xa3, (byte) 0x0b, (byte) 0xc1, (byte) 0xc4};
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#asymmetric-decrypt
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        DecryptResult decryptResult = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP, plainText);
        System.out.printf("Received decrypted content of length %d\n", decryptResult.plainText().length);
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#asymmetric-decrypt

        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#symmetric-decrypt
        DecryptResult decryptionResult = cryptographyClient.decrypt(EncryptionAlgorithm.A192CBC_HS384, plainText,
            iv, authData, authTag);
        System.out.printf("Received decrypted content of length %d with algorithm %s \n",
            decryptionResult.plainText().length);

        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#symmetric-decrypt
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#sign(SignatureAlgorithm, byte[])} and
     * {@link CryptographyClient#verify(SignatureAlgorithm, byte[], byte[])}
     */
    public void signVerify() throws NoSuchAlgorithmException {
        CryptographyClient cryptographyClient = createClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();
        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, digest);
        System.out.printf("Received signature of length %d with algorithm %s", signResult.signature().length,
            signResult.algorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign

        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify
        VerifyResult verifyResult = cryptographyClient.verify(SignatureAlgorithm.ES256, digest, signature);
        System.out.printf("Verification status %s", verifyResult.isValid());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify
    }


    /**
     * Generates a code sample for using {@link CryptographyClient#signData(SignatureAlgorithm, byte[])} and
     * {@link CryptographyClient#verifyData(SignatureAlgorithm, byte[], byte[])}
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        CryptographyClient cryptographyClient = createClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign-data
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, data);
        System.out.printf("Received signature of length %d with algorithm %s", signResult.signature().length);
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign-data

        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify-data
        VerifyResult verifyResult =  cryptographyClient.verify(SignatureAlgorithm.ES256, data, signature);
        System.out.printf("Verification status %s", verifyResult.isValid());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify-data
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#wrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link CryptographyClient#unwrapKey(KeyWrapAlgorithm, byte[])
     */
    public void wrapKeyUnwrapKey() {
        CryptographyClient cryptographyClient = createClient();
        byte[] encryptedKey = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.wrap-key
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);
        KeyWrapResult keyWrapResult = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key);
        System.out.printf("Received encypted key of length %d with algorithm %s", keyWrapResult.encryptedKey().length,
            keyWrapResult.algorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.wrap-key

        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.unwrap-key
        KeyUnwrapResult keyUnwrapResult = cryptographyClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, encryptedKey);
        System.out.printf("Received key of length %d", keyUnwrapResult.key().length);
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.unwrap-key
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
