/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.impl.client.HttpClientBuilder;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.KeyVaultClientImpl;
import com.microsoft.azure.keyvault.models.Secret;
import com.microsoft.windowsazure.exception.ServiceException;

/**
 * A utility class for interacting with KeyVault
 */
public class KeyVaultUtility {
    /**
     * Creates a secret in Azure Key Vault and returns its ID.
     * 
     * @param secretName
     *            The name of the secret to create
     * @return The ID of the created secret
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static String SetUpKeyVaultSecret(String secretName)
            throws InterruptedException, ExecutionException,
            NoSuchAlgorithmException, URISyntaxException, MalformedURLException {
        KeyVaultClient cloudVault = GetKeyVaultClient();

        if (Utility.vaultURL == null || Utility.vaultURL.isEmpty()) {
            throw new IllegalArgumentException("No Keyvault URL specified.");
        }

        try {
            // Delete the secret if it exists.
            cloudVault.deleteSecretAsync(Utility.vaultURL, secretName).get();
        } catch (ExecutionException ex) {
            boolean keyNotFound = false;
            if (ex.getCause().getClass() == ServiceException.class) {
                ServiceException serviceException = (ServiceException) ex
                        .getCause();
                if (serviceException.getHttpStatusCode() == 404) {
                    keyNotFound = true;
                }
            }

            if (!keyNotFound) {
                System.out
                        .println("Unable to access the specified vault. Please confirm the KVClientId, KVClientKey, and VaultUri are valid in the app.config file.");
                System.out
                        .println("Also ensure that the client ID has previously been granted full permissions for Key Vault secrets using the Set-AzureKeyVaultAccessPolicy command with the -PermissionsToSecrets parameter.");
                System.out.println("Press any key to exit");
                Scanner input = new Scanner(System.in);
                input.nextLine();
                input.close();
                throw ex;
            }
        }

        // Create a 256bit symmetric key and convert it to Base64.
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Note that we cannot use SymmetricKey.KeySize256,
                          // because this resolves to '0x20'.
        SecretKey wrapKey = keyGen.generateKey();

        // Store the Base64 of the key in the key vault. Note that the
        // content-type of the secret must
        // be application/octet-stream or the KeyVaultKeyResolver will not load
        // it as a key.
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/octet-stream");
        Secret cloudSecret = cloudVault.setSecretAsync(Utility.vaultURL,
                secretName, Base64.encodeBase64String(wrapKey.getEncoded()),
                "application/octet-stream", null, null).get();

        // Return the base identifier of the secret. This will be resolved to
        // the current version of the secret.
        return cloudSecret.getSecretIdentifier().getBaseIdentifier();
    }

    /**
     * Creates the KeyVaultClient using the credentials specified in the Utility
     * class.
     * 
     * @return
     * @throws URISyntaxException
     * @throws MalformedURLException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static KeyVaultClient GetKeyVaultClient() throws URISyntaxException,
            MalformedURLException, InterruptedException, ExecutionException {
        if (Utility.AuthClientId == null || Utility.AuthClientId.isEmpty()
                || Utility.AuthClientSecret == null
                || Utility.AuthClientSecret.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid AuthClientID or AuthClientSecret specified.");
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        ExecutorService executorService = Executors.newCachedThreadPool();
        KVCredentials creds = new KVCredentials(Utility.AuthClientId,
                Utility.AuthClientSecret);
        KeyVaultClient cloudVault = new KeyVaultClientImpl(httpClientBuilder,
                executorService, creds);
        return cloudVault;
    }

    /**
     * Helper method to create a new secret on the KeyVault service.
     * 
     * @param defaultKeyName
     *            The default key name to use if the user does not provide one
     * @return The keyID for the newly-created secret (or the existing secret,
     *         if one was passed in.)
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static String createSecret(String defaultKeyName)
            throws InterruptedException, ExecutionException,
            NoSuchAlgorithmException, URISyntaxException, MalformedURLException {
        System.out.println("No secret specified in Utility class.");
        System.out
                .println("Please enter the name of a new secret to create in Key Vault.");
        System.out
                .println("WARNING: This will delete any existing secret with the same name.");
        System.out.println("If nothing is entered, the value \""
                + defaultKeyName + "\" will be used.");

        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        String newSecretName = input.nextLine();

        if (newSecretName == null || newSecretName.isEmpty()) {
            newSecretName = defaultKeyName;
        }

        // Although it is possible to use keys (rather than secrets) stored in
        // Key Vault, this prevents caching.
        // Therefore it is recommended to use secrets along with a caching
        // resolver (see below).
        String keyID = KeyVaultUtility.SetUpKeyVaultSecret(newSecretName);

        System.out.println();
        System.out.println("Created a secret with ID: " + keyID);
        System.out.println("Copy the secret ID to App.config to reuse.");
        System.out.println();
        return keyID;
    }
}
