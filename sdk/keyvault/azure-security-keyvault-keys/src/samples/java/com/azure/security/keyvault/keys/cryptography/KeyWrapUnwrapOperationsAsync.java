// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import java.util.Random;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class KeyWrapUnwrapOperationsAsync {

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

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);

        // Let's wrap a simple dummy key content.
        cryptoAsyncClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, plaintext)
            .subscribe(keyWrapResult -> {
                System.out.printf("Returned encrypted key size is %d bytes with algorithm %s\n", keyWrapResult.getEncryptedKey().length, keyWrapResult.getAlgorithm().toString());
                //Let's decrypt the encrypted response.
                cryptoAsyncClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, keyWrapResult.getEncryptedKey())
                    .subscribe(keyUnwrapResult -> System.out.printf("Returned unwrapped key size is %d bytes\n", keyUnwrapResult.getKey().length));
            });

        Thread.sleep(5000);

        // Let's do Key Wrap and Unwrap operations with a symmetric key.
        byte[] symmetrickeyContent = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
        byte[] keyContentToWrap = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
    }
}

