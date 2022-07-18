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
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws NoSuchAlgorithmException when the specified algorithm doesn't exist.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException, NoSuchAlgorithmException {
        /* Instantiate a CryptographyAsyncClient that will be used to call the service. Notice that the client is using
        default Azure credentials. To make default credentials work, ensure that the environment variable
        'AZURE_CLIENT_ID' is set with the principal ID of a managed identity that has been given access to your vault.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("<your-key-id-from-keyvault>")
            .buildAsyncClient();

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plaintext);
        byte[] digest = md.digest();

        // Let's create a signature from a simple digest.
        cryptoAsyncClient.sign(SignatureAlgorithm.RS256, digest)
            .subscribe(signResult -> {
                System.out.printf("Returned signature size is %d bytes with algorithm %s\n",
                    signResult.getSignature().length, signResult.getAlgorithm().toString());
                // Let's verify the signature against the digest.
                cryptoAsyncClient.verify(SignatureAlgorithm.RS256, digest, signResult.getSignature())
                    .subscribe(verifyResult ->
                        System.out.printf("Signature verified : %s\n", verifyResult.isValid()));
            });

        Thread.sleep(4000);

        // We can sign the raw plain text data without having to create a digest.
        cryptoAsyncClient.sign(SignatureAlgorithm.RS256, digest)
            .subscribe(signResult -> {
                System.out.printf("Returned signature size is %d bytes with algorithm %s\n",
                    signResult.getSignature().length, signResult.getAlgorithm().toString());
                // Let's verify the signature against the raw plain text data.
                cryptoAsyncClient.verify(SignatureAlgorithm.RS256, digest, signResult.getSignature())
                    .subscribe(verifyDataResult ->
                        System.out.printf("Signature verified : %s\n", verifyDataResult.isValid()));
            });

        // Block main thread to let async operations finish.
        Thread.sleep(4000);
    }
}

