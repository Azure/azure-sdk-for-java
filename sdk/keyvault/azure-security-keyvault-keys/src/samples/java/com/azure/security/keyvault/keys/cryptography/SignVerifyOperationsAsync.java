// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class SignVerifyOperationsAsync {

    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException, NoSuchAlgorithmException {

        // Instantiate a cryptography async client that will be used to perform crypto operations. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("<Your-Key-Id-From-Keyvault>")
            .buildAsyncClient();

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plaintext);
        byte[] digest = md.digest();

        // Let's create a signature from a simple digest.
        cryptoAsyncClient.sign(SignatureAlgorithm.RS256, digest)
            .subscribe(signResult -> {
                System.out.printf("Returned signature size is %d bytes with algorithm %s\n", signResult.getSignature().length, signResult.getAlgorithm().toString());
                // Let's verify the signature against the digest.
                cryptoAsyncClient.verify(SignatureAlgorithm.RS256, digest, signResult.getSignature())
                    .subscribe(verifyResult -> System.out.printf("Signature verified : %s\n", verifyResult.isValid()));
            });

        Thread.sleep(4000);

        // We can sign the raw plain text data without having to create a digest
        cryptoAsyncClient.sign(SignatureAlgorithm.RS256, digest)
            .subscribe(signResult -> {
                System.out.printf("Returned signature size is %d bytes with algorithm %s\n", signResult.getSignature().length, signResult.getAlgorithm().toString());
                // Let's verify the signature against the raw plain text data.
                cryptoAsyncClient.verify(SignatureAlgorithm.RS256, digest, signResult.getSignature())
                    .subscribe(verifyDataResult -> System.out.printf("Signature verified : %s\n", verifyDataResult.isValid()));
            });

        //Block main thread to let async operations finish
        Thread.sleep(4000);
    }
}

