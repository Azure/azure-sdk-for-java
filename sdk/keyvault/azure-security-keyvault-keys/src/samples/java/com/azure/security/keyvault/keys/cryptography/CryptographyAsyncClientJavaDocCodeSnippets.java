// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.JsonWebKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyAsyncClient}.
 */
public final class CryptographyAsyncClientJavaDocCodeSnippets {
    private final String key1 = "key1";
    private final String key2 = "key2";
    private final String value1 = "val1";
    private final String value2 = "val2";

    /**
     * Generates code sample for creating a {@link KeyAsyncClient}.
     *
     * @return An instance of {@link KeyAsyncClient}.
     */
    public CryptographyAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier("<your-key-id>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation

        return cryptographyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient} with a given {@link JsonWebKey}.
     *
     * @return An instance of {@link KeyAsyncClient}.
     */
    public CryptographyAsyncClient createAsyncClientWithJsonWebKey() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation
        JsonWebKey jsonWebKey = new JsonWebKey().setId("SampleJsonWebKey");
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .jsonWebKey(jsonWebKey)
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation

        return cryptographyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient} with a given {@link HttpClient}.
     *
     * @return An instance of {@link KeyAsyncClient}.
     */
    public CryptographyAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withHttpClient.instantiation
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier("<your-key-id>")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withHttpClient.instantiation

        return cryptographyAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyAsyncClient} with a given {@link HttpPipeline}.
     *
     * @return An instance of {@link KeyAsyncClient}.
     */
    public CryptographyAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withPipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()), new RetryPolicy())
            .build();
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .keyIdentifier("<your-key-id>")
            .buildAsyncClient();
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withPipeline.instantiation

        return cryptographyAsyncClient;
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#getKey()}.
     */
    public void getKeySnippets() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKey
        cryptographyAsyncClient.getKey()
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(key -> System.out.printf("Key returned with name: %s, and id: %s.\n", key.getName(),
                key.getId()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKey
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#getKeyWithResponse()}.
     */
    public void getKeyWithResponseSnippets() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKeyWithResponse
        cryptographyAsyncClient.getKeyWithResponse()
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(keyResponse -> System.out.printf("Key returned with name: %s, and id: %s.\n",
                keyResponse.getValue().getName(), keyResponse.getValue().getId()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKeyWithResponse
    }

    /**
     * Generates code samples for using {@link CryptographyAsyncClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#encrypt(EncryptParameters)}.
     */
    public void encrypt() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte
        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);

        cryptographyAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(encryptResult ->
                System.out.printf("Received encrypted content of length: %d, with algorithm: %s.\n",
                    encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptParameters
        byte[] plaintextBytes = new byte[100];
        new Random(0x1234567L).nextBytes(plaintextBytes);
        byte[] iv = {
            (byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd,
            (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04
        };

        EncryptParameters encryptParameters = EncryptParameters.createA128CbcParameters(plaintextBytes, iv);

        cryptographyAsyncClient.encrypt(encryptParameters)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(encryptResult ->
                System.out.printf("Received encrypted content of length: %d, with algorithm: %s.\n",
                    encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptParameters
    }

    /**
     * Generates code samples for using {@link CryptographyAsyncClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#decrypt(DecryptParameters)}.
     */
    public void decrypt() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte
        byte[] ciphertext = new byte[100];
        new Random(0x1234567L).nextBytes(ciphertext);

        cryptographyAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, ciphertext)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(decryptResult ->
                System.out.printf("Received decrypted content of length: %d\n", decryptResult.getPlainText().length));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#DecryptParameters
        byte[] ciphertextBytes = new byte[100];
        new Random(0x1234567L).nextBytes(ciphertextBytes);
        byte[] iv = {
            (byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd,
            (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04
        };

        DecryptParameters decryptParameters = DecryptParameters.createA128CbcParameters(ciphertextBytes, iv);

        cryptographyAsyncClient.decrypt(decryptParameters)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(decryptResult ->
                System.out.printf("Received decrypted content of length: %d.\n", decryptResult.getPlainText().length));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#DecryptParameters
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#sign(SignatureAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#verify(SignatureAlgorithm, byte[], byte[])}.
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signVerify() throws NoSuchAlgorithmException {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();

        cryptographyAsyncClient.sign(SignatureAlgorithm.ES256, digest)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(signResult ->
                System.out.printf("Received signature of length: %d, with algorithm: %s.\n",
                    signResult.getSignature().length, signResult.getAlgorithm()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte

        byte[] signature = new byte[100];

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte
        byte[] myData = new byte[100];
        new Random(0x1234567L).nextBytes(myData);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(myData);
        byte[] myDigest = messageDigest.digest();

        // A signature can be obtained from the SignResult returned by the CryptographyAsyncClient.sign() operation.
        cryptographyAsyncClient.verify(SignatureAlgorithm.ES256, myDigest, signature)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(verifyResult ->
                System.out.printf("Verification status: %s.\n", verifyResult.isValid()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte
    }


    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#wrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#unwrapKey(KeyWrapAlgorithm, byte[])}.
     */
    public void wrapKeyUnwrapKey() {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);

        cryptographyAsyncClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(wrapResult ->
                System.out.printf("Received encrypted key of length: %d, with algorithm: %s.\n",
                    wrapResult.getEncryptedKey().length, wrapResult.getAlgorithm().toString()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte
        byte[] keyToWrap = new byte[100];
        new Random(0x1234567L).nextBytes(key);

        cryptographyAsyncClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, keyToWrap)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(wrapResult ->
                cryptographyAsyncClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, wrapResult.getEncryptedKey())
                    .subscribe(keyUnwrapResult ->
                        System.out.printf("Received key of length: %d.\n", keyUnwrapResult.getKey().length)));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte
    }

    /**
     * Generates a code sample for using {@link CryptographyAsyncClient#signData(SignatureAlgorithm, byte[])} and
     * {@link CryptographyAsyncClient#verifyData(SignatureAlgorithm, byte[], byte[])}.
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        CryptographyAsyncClient cryptographyAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);

        cryptographyAsyncClient.sign(SignatureAlgorithm.ES256, data)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(signResult ->
                System.out.printf("Received signature of length: %d, with algorithm: %s.\n",
                    signResult.getSignature().length, signResult.getAlgorithm()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte

        byte[] signature = new byte[100];

        // BEGIN: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte
        byte[] myData = new byte[100];
        new Random(0x1234567L).nextBytes(myData);

        // A signature can be obtained from the SignResult returned by the CryptographyAsyncClient.sign() operation.
        cryptographyAsyncClient.verify(SignatureAlgorithm.ES256, myData, signature)
            .contextWrite(context -> context.put(key1, value1).put(key2, value2))
            .subscribe(verifyResult ->
                System.out.printf("Verification status: %s.\n", verifyResult.isValid()));
        // END: com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte
    }
}
