// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
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
                System.out.printf("Returned cipherText size is %d bytes with algorithm %s\n", encryptResult.cipherText().length, encryptResult.algorithm().toString());
                //Let's decrypt the encrypted response.
                cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptResult.cipherText())
                    .subscribe(decryptResult -> System.out.printf("Returned plainText size is %d bytes\n", decryptResult.plainText().length));
            });

        Thread.sleep(5000);

        // Let's do Encrypt and Decrypt operations with a symmetric key.
        byte[] keyContent = { 0x06, (byte) 0xa9, 0x21, 0x40, 0x36, (byte) 0xb8, (byte) 0xa1, 0x5b, 0x51, 0x2e, 0x03, (byte) 0xd5, 0x34, 0x12, 0x00, 0x06 };
        byte[] plaintext = "Single block msg".getBytes();
        byte[] initializationVector = { 0x3d, (byte) 0xaf, (byte) 0xba, 0x42, (byte) 0x9d, (byte) 0x9e, (byte) 0xb4, 0x30, (byte) 0xb4, 0x22, (byte) 0xda, (byte) 0x80, 0x2c, (byte) 0x9f, (byte) 0xac, 0x41 };

        // Convert the symmetric key encoded content to Json Web key.
        JsonWebKey symmetricKey = JsonWebKey.fromAes(new SecretKeySpec(keyContent, "AES"))
            .kty(KeyType.OCT)
            .keyOps(Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT));

        // Configure the symmetric key in a new crypto client.
        CryptographyAsyncClient symmetricKeyCryptoAsyncClient = new CryptographyClientBuilder()
            .jsonWebKey(symmetricKey)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Note the implementation of A128CBC in this library uses PKCS7 padding.
        symmetricKeyCryptoAsyncClient.encrypt(EncryptionAlgorithm.A128CBC, plaintext, initializationVector, null)
            .subscribe(encryptResult -> {
                System.out.printf("Returned cipherText size is %d bytes with algorithm %s\n", encryptResult.cipherText().length, encryptResult.algorithm().toString());
                //Let's decrypt the encrypted response.
                symmetricKeyCryptoAsyncClient.decrypt(EncryptionAlgorithm.A128CBC, encryptResult.cipherText(), initializationVector, null, null)
                    .subscribe(decryptResult -> System.out.printf("Returned plainText size is %d bytes\n", decryptResult.plainText().length));
            });

        //Block main thread to let async operations finish
        Thread.sleep(5000);
    }
}

