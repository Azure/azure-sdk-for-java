// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.keys.KeyClient;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyClient}.
 */
public final class CryptographyClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link CryptographyClient}.
     *
     * @return An instance of {@link CryptographyClient}.
     */
    public CryptographyClient createClient() {
        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier("<your-key-id-from-keyvault>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation

        return cryptographyClient;
    }

    /**
     * Generates a code sample for creating a {@link CryptographyClient} with a given {@link JsonWebKey}.
     *
     * @return An instance of {@link CryptographyClient}.
     */
    public CryptographyClient createClientWithJsonWebKey() {
        JsonWebKey myJsonWebKey = new JsonWebKey();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withJsonWebKey
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .jsonWebKey(myJsonWebKey)
            .buildClient();
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withJsonWebKey

        return cryptographyClient;
    }

    /**
     * Generates a code sample for creating a {@link CryptographyClient} with a given {@link HttpClient}.
     *
     * @return An instance of {@link CryptographyClient}.
     */
    public CryptographyClient createClientWithHttpClient() {
        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withHttpClient
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier("<your-key-id-from-keyvault>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.getSharedInstance())
            .buildClient();
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withHttpClient

        return cryptographyClient;
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#getKey()}.
     */
    public void getKey() {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKey
        KeyVaultKey key = cryptographyClient.getKey();

        System.out.printf("Key returned with name: %s and id: %s.%n", key.getName(), key.getId());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKey
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#getKeyWithResponse(RequestContext)}.
     */
    public void getKeyWithResponse() {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        KeyVaultKey keyWithVersion = cryptographyClient.getKeyWithResponse(requestContext).getValue();

        System.out.printf("Key is returned with name: %s and id %s.%n", keyWithVersion.getName(),
            keyWithVersion.getId());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#RequestContext
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyClient#encrypt(EncryptParameters, RequestContext)}.
     */
    public void encrypt() {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte
        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);

        EncryptResult encryptResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);

        System.out.printf("Received encrypted content of length: %d, with algorithm: %s.%n",
            encryptResult.getCipherText().length, encryptResult.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-RequestContext
        byte[] myPlaintext = new byte[100];
        new Random(0x1234567L).nextBytes(myPlaintext);
        byte[] iv = {
            (byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd,
            (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04
        };

        EncryptParameters encryptParameters = EncryptParameters.createA128CbcParameters(myPlaintext, iv);

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        EncryptResult encryptedResult = cryptographyClient.encrypt(encryptParameters, requestContext);

        System.out.printf("Received encrypted content of length: %d, with algorithm: %s.%n",
            encryptedResult.getCipherText().length, encryptedResult.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-RequestContext
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[])} and
     * {@link CryptographyClient#decrypt(DecryptParameters, RequestContext)}.
     */
    public void decrypt() {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte
        byte[] ciphertext = new byte[100];
        new Random(0x1234567L).nextBytes(ciphertext);

        DecryptResult decryptResult = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP, ciphertext);

        System.out.printf("Received decrypted content of length: %d.%n", decryptResult.getPlainText().length);
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-RequestContext
        byte[] myCiphertext = new byte[100];
        new Random(0x1234567L).nextBytes(myCiphertext);
        byte[] iv = {
            (byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd,
            (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04
        };

        DecryptParameters decryptParameters = DecryptParameters.createA128CbcParameters(myCiphertext, iv);

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        DecryptResult decryptedResult = cryptographyClient.decrypt(decryptParameters, requestContext);

        System.out.printf("Received decrypted content of length: %d.%n", decryptedResult.getPlainText().length);
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-RequestContext
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#sign(SignatureAlgorithm, byte[])} and
     * {@link CryptographyClient#verify(SignatureAlgorithm, byte[], byte[])}.
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signVerify() throws NoSuchAlgorithmException {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte
        byte[] data = new byte[100];
        new Random(0x1234567L).nextBytes(data);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        byte[] digest = md.digest();

        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, digest);

        System.out.printf("Received signature of length: %d, with algorithm: %s.%n", signResult.getSignature().length,
            signResult.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-RequestContext
        byte[] dataToVerify = new byte[100];
        new Random(0x1234567L).nextBytes(dataToVerify);
        MessageDigest myMessageDigest = MessageDigest.getInstance("SHA-256");
        myMessageDigest.update(dataToVerify);
        byte[] digestContent = myMessageDigest.digest();

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        SignResult signResponse = cryptographyClient.sign(SignatureAlgorithm.ES256, digestContent, requestContext);

        System.out.printf("Received signature of length: %d, with algorithm: %s.%n", signResponse.getSignature().length,
            signResponse.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-RequestContext

        byte[] signature = signResult.getSignature();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte
        byte[] myData = new byte[100];
        new Random(0x1234567L).nextBytes(myData);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(myData);
        byte[] myDigest = messageDigest.digest();

        // A signature can be obtained from the SignResult returned by the CryptographyClient.sign() operation.
        VerifyResult verifyResult = cryptographyClient.verify(SignatureAlgorithm.ES256, myDigest, signature);

        System.out.printf("Verification status: %s.%n", verifyResult.isValid());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte

        byte[] signatureBytes = signResponse.getSignature();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-RequestContext
        byte[] dataBytes = new byte[100];
        new Random(0x1234567L).nextBytes(dataBytes);
        MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
        msgDigest.update(dataBytes);
        byte[] digestBytes = msgDigest.digest();

        // A signature can be obtained from the SignResult returned by the CryptographyClient.sign() operation.
        VerifyResult verifyResponse =
            cryptographyClient.verify(SignatureAlgorithm.ES256, digestBytes, signatureBytes, requestContext);

        System.out.printf("Verification status: %s.%n", verifyResponse.isValid());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-RequestContext
    }


    /**
     * Generates a code sample for using {@link CryptographyClient#wrapKey(KeyWrapAlgorithm, byte[])},
     * {@link CryptographyClient#wrapKey(KeyWrapAlgorithm, byte[], RequestContext)},
     * {@link CryptographyClient#unwrapKey(KeyWrapAlgorithm, byte[])} and
     * {@link CryptographyClient#unwrapKey(KeyWrapAlgorithm, byte[], RequestContext)}.
     */
    public void wrapKeyUnwrapKey() {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte
        byte[] key = new byte[100];
        new Random(0x1234567L).nextBytes(key);

        WrapResult wrapResult = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key);

        System.out.printf("Received encrypted key of length: %d, with algorithm: %s.%n",
            wrapResult.getEncryptedKey().length, wrapResult.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-RequestContext
        byte[] keyToWrap = new byte[100];
        new Random(0x1234567L).nextBytes(keyToWrap);

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        WrapResult keyWrapResult = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, keyToWrap, requestContext);

        System.out.printf("Received encrypted key of length: %d, with algorithm: %s.%n",
            keyWrapResult.getEncryptedKey().length, keyWrapResult.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte
        byte[] keyContent = new byte[100];
        new Random(0x1234567L).nextBytes(keyContent);

        WrapResult wrapKeyResult = cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, keyContent);
        UnwrapResult unwrapResult =
            cryptographyClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, wrapKeyResult.getEncryptedKey());

        System.out.printf("Received key of length %d", unwrapResult.getKey().length);
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-RequestContext
        byte[] keyContentToWrap = new byte[100];
        new Random(0x1234567L).nextBytes(keyContentToWrap);

        RequestContext reqContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        WrapResult wrapKeyContentResult =
            cryptographyClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, keyContentToWrap, reqContext);
        UnwrapResult unwrapKeyResponse =
            cryptographyClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, wrapKeyContentResult.getEncryptedKey(), reqContext);

        System.out.printf("Received key of length %d", unwrapKeyResponse.getKey().length);
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-RequestContext
    }

    /**
     * Generates a code sample for using {@link CryptographyClient#signData(SignatureAlgorithm, byte[])} and
     * {@link CryptographyClient#verifyData(SignatureAlgorithm, byte[], byte[])}.
     *
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public void signDataVerifyData() throws NoSuchAlgorithmException {
        CryptographyClient cryptographyClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte
        byte[] data = new byte[32];
        new Random(0x1234567L).nextBytes(data);

        SignResult signResult = cryptographyClient.sign(SignatureAlgorithm.ES256, data);

        System.out.printf("Received signature of length: %d, with algorithm: %s.%n", signResult.getSignature().length,
            signResult.getAlgorithm());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-RequestContext
        byte[] plainTextData = new byte[32];
        new Random(0x1234567L).nextBytes(plainTextData);

        SignResult signingResult = cryptographyClient.sign(SignatureAlgorithm.ES256, plainTextData);

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        System.out.printf("Received signature of length: %d, with algorithm: %s.%n",
            signingResult.getSignature().length, requestContext);
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-RequestContext

        byte[] signature = signResult.getSignature();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte
        byte[] myData = new byte[32];
        new Random(0x1234567L).nextBytes(myData);

        // A signature can be obtained from the SignResult returned by the CryptographyClient.sign() operation.
        VerifyResult verifyResult = cryptographyClient.verify(SignatureAlgorithm.ES256, myData, signature);

        System.out.printf("Verification status: %s.%n", verifyResult.isValid());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte

        byte[] mySignature = signingResult.getSignature();

        // BEGIN: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-RequestContext
        byte[] dataToVerify = new byte[32];
        new Random(0x1234567L).nextBytes(dataToVerify);

        // A signature can be obtained from the SignResult returned by the CryptographyClient.sign() operation.
        VerifyResult verificationResult =
            cryptographyClient.verify(SignatureAlgorithm.ES256, dataToVerify, mySignature, requestContext);

        System.out.printf("Verification status: %s.%n", verificationResult.isValid());
        // END: com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-RequestContext
    }
}
