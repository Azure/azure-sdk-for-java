// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class SignVerifyOperations {
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
        /* Instantiate a CryptographyClient that will be used to call the service. Notice that the client is using
        default Azure credentials. To make default credentials work, ensure that the environment variable
        'AZURE_CLIENT_ID' is set with the principal ID of a managed identity that has been given access to your vault.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("<your-key-id-from-keyvault")
            .buildClient();

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plaintext);
        byte[] digest = md.digest();

        // Let's create a signature from a simple digest.
        SignResult signResult = cryptoClient.sign(SignatureAlgorithm.RS256, digest);

        System.out.printf("Returned signature size is %d bytes with algorithm %s\n", signResult.getSignature().length,
            signResult.getAlgorithm().toString());

        // Let's verify the signature against the digest.
        VerifyResult verifyResult = cryptoClient.verify(SignatureAlgorithm.RS256, digest, signResult.getSignature());

        System.out.printf("Signature verified : %s \n", verifyResult.isValid());

        // We can sign the raw plain text data without having to create a digest.
        SignResult signingDataResult = cryptoClient.signData(SignatureAlgorithm.RS256, plaintext);

        System.out.printf("Returned signature size is %d bytes with algorithm %s\n",
            signingDataResult.getSignature().length, signingDataResult.getAlgorithm().toString());

        // Let's verify the signature against the raw plain text data.
        VerifyResult verifyDataResult =
            cryptoClient.verifyData(SignatureAlgorithm.RS256, plaintext, signResult.getSignature());

        System.out.printf("Signature verified : %s \n", verifyDataResult.isValid());

    }
}

