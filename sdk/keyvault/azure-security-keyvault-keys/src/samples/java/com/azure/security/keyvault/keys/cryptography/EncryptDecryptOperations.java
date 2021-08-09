// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;

import java.util.Random;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class EncryptDecryptOperations {

    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {

        // Instantiate a key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .keyIdentifier("<Your-Key-Id-From-Keyvault")
            .buildClient();

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);

        // Let's encrypt a simple plain text of size 100 bytes.
        EncryptResult encryptResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);
        System.out.printf("Returned ciphertext size is %d bytes with algorithm %s\n",
            encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());

        //Let's decrypt the encrypted response.
        DecryptResult decryptResult = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptResult.getCipherText());
        System.out.printf("Returned plaintext size is %d bytes \n", decryptResult.getPlainText().length);
    }
}

