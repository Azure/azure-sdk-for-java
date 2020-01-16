// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyAsyncClient}
 */
public final class CryptographyAsyncClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public CryptographyAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.withhttpclient.instantiation
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier("<Your-Key-ID>")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.withhttpclient.instantiation
        return cryptographyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public CryptographyAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.instantiation
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier("<YOUR-KEY-ID>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.instantiation
        return cryptographyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}
     * @return An instance of {@link KeyAsyncClient}
     */
    public CryptographyAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()), new RetryPolicy())
            .build();
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .keyIdentifier("<YOUR-KEY-ID")
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.pipeline.instantiation
        return cryptographyAsyncClient;
    }


    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#getKeyWithResponse()}
     */
    public void getKeyWithResponseSnippets() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.getKeyWithResponse
        cryptographyAsyncClient.getKeyWithResponse()
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(keyResponse ->  System.out.printf("Key is returned with name %s and id %s \n",
                keyResponse.getValue().getName(), keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.getKeyWithResponse
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#getKey()}
     */
    public void getKeySnippets() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.getKey
        cryptographyAsyncClient.getKey()
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(key ->  System.out.printf("Key is returned with name %s and id %s \n", key.getName(), key.getId()));
        // END: com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.getKey
    }


    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#encrypt(EncryptionAlgorithm, byte[])}
     */
    public void encrypt() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73
        };
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        cryptographyAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(encryptResult ->
                System.out.printf("Received encrypted content of length %d with algorithm %s \n",
                    encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#decrypt(EncryptionAlgorithm, byte[])}
     */
    public void decrypt() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        cryptographyAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, plainText)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(decryptResult ->
                System.out.printf("Received decrypted content of length %d\n", decryptResult.getPlainText().length));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#sign(SignatureAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#verify(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signVerify() throws NoSuchAlgorithmException {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();
        cryptographyAsyncClient.sign(SignatureAlgorithm.ES256, digest)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(signResult ->
                System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte
        cryptographyAsyncClient.verify(SignatureAlgorithm.ES256, digest, signature)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(verifyResult ->
                System.out.printf("Verification status %s", verifyResult.isValid()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte
    }


    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#signData(SignatureAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#verifyData(SignatureAlgorithm, byte[], byte[])}
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] signature = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        cryptographyAsyncClient.sign(SignatureAlgorithm.ES256, data)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(signResult ->
                System.out.printf("Received signature of length %d with algorithm %s", signResult.getSignature().length));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte
        cryptographyAsyncClient.verify(SignatureAlgorithm.ES256, data, signature)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(verifyResult ->
                System.out.printf("Verification status %s", verifyResult.isValid()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#wrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#unwrapKey(KeyWrapAlgorithm, byte[])}
     */
    public void wrapKeyUnwrapKey() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();
        byte[] encryptedKey = new byte[100];
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);
        cryptographyAsyncClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(keyWrapResult ->
                System.out.printf("Received encypted key of length %d with algorithm %s",
                    keyWrapResult.getEncryptedKey().length, keyWrapResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte
        cryptographyAsyncClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, encryptedKey)
            .subscriberContext(reactor.util.context.Context.of(key1, value1, key2, value2))
            .subscribe(keyUnwrapResult ->
                System.out.printf("Received key of length %d", keyUnwrapResult.getKey().length));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private TokenCredential getKeyVaultCredential() {
        return null;
    }
}
