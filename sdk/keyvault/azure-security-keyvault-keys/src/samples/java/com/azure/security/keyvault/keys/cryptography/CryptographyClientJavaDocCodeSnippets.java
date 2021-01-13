// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

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
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.instantiation
        return cryptographyClient;
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#getKeyWithResponse(Context)}
     */
    public void getKeyWithResponseSnippets() {
        CryptographyClient cryptographyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKeyWithResponse#Context
        KeyVaultKey keyWithVersion = cryptographyClient.getKeyWithResponse(new Context(key1, value1)).getValue();
        System.out.printf("Key is returned with name %s and id %s \n", keyWithVersion.getName(), keyWithVersion.getId());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKeyWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#getKey()}
     */
    public void getKeySnippets() {
        CryptographyClient cryptographyClient = createClient();
        // BEGIN: com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKey
        KeyVaultKey key = cryptographyClient.getKey();
        System.out.printf("Key is returned with name %s and id %s \n", key.getName(), key.getId());
        // END: com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKey
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[])}
     */
    public void encrypt() {
        CryptographyClient cryptographyClient = createClient();
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73
        };
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        EncryptResult encryptResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText);
        System.out.printf("Received encrypted content of length %d with algorithm %s \n",
            encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte-Context
        byte[] plainTextToEncrypt = new byte[100];
        new Random(0x1234567L).nextBytes(plainTextToEncrypt);
        EncryptResult encryptionResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainTextToEncrypt,
            new Context(key1, value1));
        System.out.printf("Received encrypted content of length %d with algorithm %s \n",
            encryptionResult.getCipherText().length, encryptionResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte-Context
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[])}
     */
    public void decrypt() {
        CryptographyClient cryptographyClient = createClient();
        byte[] encryptedData = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte
        DecryptResult decryptResult = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptedData);
        System.out.printf("Received decrypted content of length %d\n", decryptResult.getPlainText().length);
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte-Context
        DecryptResult decryptionResult = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptedData,
            new Context(key1, value1));
        System.out.printf("Received decrypted content of length %d\n", decryptionResult.getPlainText().length);
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte-Context
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#sign(SignatureAlgorithm, byte[])} and
     * {@link CryptographyClient#verify(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signVerify() throws NoSuchAlgorithmException {
        CryptographyClient cryptographyClient = createClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();
        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, digest);
        System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length,
            signResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-Context
        byte[] plainTextData = new byte[100];
        new Random(0x1234567L).nextBytes(plainTextData);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(data);
        byte[] digetContent = messageDigest.digest();
        SignResult signResponse = cryptographyClient.sign(SignatureAlgorithm.ES256, digetContent);
        System.out.printf("Received signature of length %d with algorithm %s", signResponse.getSignature().length,
            signResponse.getAlgorithm().toString(), new Context(key1, value1));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-Context

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte
        VerifyResult verifyResult = cryptographyClient.verify(SignatureAlgorithm.ES256, digest, signature);
        System.out.printf("Verification status %s", verifyResult.isValid());
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-Context
        VerifyResult verifyResponse = cryptographyClient.verify(SignatureAlgorithm.ES256, digest, signature);
        System.out.printf("Verification status %s", verifyResponse.isValid(), new Context(key2, value2));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-Context
    }


    /**
     * Generates a code sample for using {@link CryptographyClient#signData(SignatureAlgorithm, byte[])} and
     * {@link CryptographyClient#verifyData(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        CryptographyClient cryptographyClient = createClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, data);
        System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length);
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-Context
        byte[] plainTextData = new byte[100];
        new Random(0x1234567L).nextBytes(plainTextData);
        SignResult signReponse = cryptographyClient.sign(SignatureAlgorithm.ES256, plainTextData);
        System.out.printf("Received signature of length %d with algorithm %s", signReponse.getSignature().length,
            new Context(key1, value1));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-Context

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte
        VerifyResult verifyResult =  cryptographyClient.verify(SignatureAlgorithm.ES256, data, signature);
        System.out.printf("Verification status %s", verifyResult.isValid());
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-Context
        VerifyResult verifyResponse =  cryptographyClient.verify(SignatureAlgorithm.ES256, data, signature);
        System.out.printf("Verification status %s", verifyResponse.isValid(), new Context(key2, value2));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-Context
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#wrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link CryptographyClient#unwrapKey(KeyWrapAlgorithm, byte[])}
     */
    public void wrapKeyUnwrapKey() {
        CryptographyClient cryptographyClient = createClient();
        byte[] encryptedKey = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);
        WrapResult wrapResult = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key);
        System.out.printf("Received encypted key of length %d with algorithm %s", wrapResult.getEncryptedKey().length,
            wrapResult.getAlgorithm().toString());
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-Context
        byte[] keyContent = new byte[100];
        new Random(0x1234567L).nextBytes(keyContent);
        WrapResult keyWrapResponse = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, keyContent);
        System.out.printf("Received encypted key of length %d with algorithm %s", keyWrapResponse.getEncryptedKey().length,
            keyWrapResponse.getAlgorithm().toString(), new Context(key1, value1));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-Context

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte
        UnwrapResult unwrapResult = cryptographyClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, encryptedKey);
        System.out.printf("Received key of length %d", unwrapResult.getKey().length);
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-Context
        UnwrapResult keyUnwrapResponse = cryptographyClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, encryptedKey,
            new Context(key2, value2));
        System.out.printf("Received key of length %d", keyUnwrapResponse.getKey().length);
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-Context
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
