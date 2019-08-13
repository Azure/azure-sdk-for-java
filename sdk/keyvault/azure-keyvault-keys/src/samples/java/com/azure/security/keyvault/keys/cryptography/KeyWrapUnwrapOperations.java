// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyUnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Random;


/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class KeyWrapUnwrapOperations {

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

        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);

        // Let's wrap a simple dummy key content.
        KeyWrapResult keyWrapResult = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, plainText);
        System.out.printf("Returned encrypted key size is %d bytes with algorithm %s\n", keyWrapResult.encryptedKey().length, keyWrapResult.algorithm().toString());

        //Let's unwrap the encrypted key response.
        KeyUnwrapResult keyUnwrapResult = cryptoClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, keyWrapResult.encryptedKey());
        System.out.printf("Returned unwrapped key size is %d bytes\n", keyUnwrapResult.key().length);

        // Let's do Key Wrap and Unwrap operations with a symmetric key.
        byte[] symmetrickeyContent = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
        byte[] keyContentToWrap = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

        // Convert the symmetric key encoded content to Json Web key.
        JsonWebKey symmetricKey = JsonWebKey.fromAes(new SecretKeySpec(symmetrickeyContent, "AES"))
            .kty(KeyType.OCT)
            .keyOps(Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));

        // Configure the symmetric key in a new crypto client.
        CryptographyClient symmetricKeyCryptoClient = new CryptographyClientBuilder()
            .jsonWebKey(symmetricKey)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        KeyWrapResult symKeyWrapResult = symmetricKeyCryptoClient.wrapKey(KeyWrapAlgorithm.A128KW, keyContentToWrap);
        System.out.printf("Returned encrypted key size is %d bytes with algorithm %s\n", keyWrapResult.encryptedKey().length, keyWrapResult.algorithm().toString());

        KeyUnwrapResult symKeyUnwrapResult = symmetricKeyCryptoClient.unwrapKey(KeyWrapAlgorithm.A128KW, symKeyWrapResult.encryptedKey());
        System.out.printf("Returned unwrapped key size is %d bytes\n", symKeyUnwrapResult.key().length);
    }
}

