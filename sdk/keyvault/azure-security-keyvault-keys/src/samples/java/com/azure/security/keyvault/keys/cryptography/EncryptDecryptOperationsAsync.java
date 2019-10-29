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
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {

        // Instantiate a cryptography async client that will be used to perform crypto operations. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("<Your-Key-Id-From-Keyvault>")
            .buildAsyncClient();

        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);

        // Let's encrypt a simple plain text of size 100 bytes.
        cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText)
            .subscribe(encryptResult -> {
                System.out.printf("Returned cipherText size is %d bytes with algorithm %s\n", encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
                //Let's decrypt the encrypted response.
                cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptResult.getCipherText())
                    .subscribe(decryptResult -> System.out.printf("Returned plainText size is %d bytes\n", decryptResult.getPlainText().length));
            });

        Thread.sleep(5000);
    }
}

