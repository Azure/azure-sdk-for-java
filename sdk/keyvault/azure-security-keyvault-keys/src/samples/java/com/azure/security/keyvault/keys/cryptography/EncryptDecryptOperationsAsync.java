// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

import java.util.Random;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class EncryptDecryptOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {
        /* Instantiate a CryptographyAsyncClient that will be used to call the service. Notice that the client is using
        default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("<your-key-id-from-keyvault>")
            .buildAsyncClient();

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);

        // Let's encrypt a simple plain text of size 100 bytes.
        cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
            .subscribe(encryptResult -> {
                System.out.printf("Returned ciphertext size is %d bytes with algorithm %s\n",
                    encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
                // Let's decrypt the encrypted response.
                cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptResult.getCipherText())
                    .subscribe(decryptResult -> System.out.printf("Returned plaintext size is %d bytes\n",
                        decryptResult.getPlainText().length));
            });

        Thread.sleep(5000);
    }
}

