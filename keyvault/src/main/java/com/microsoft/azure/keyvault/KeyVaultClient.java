/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.microsoft.azure.keyvault;

import java.io.Closeable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;

import com.microsoft.azure.keyvault.models.KeyAttributes;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.ListKeysResponseMessage;
import com.microsoft.azure.keyvault.models.ListSecretsResponseMessage;
import com.microsoft.azure.keyvault.models.Secret;
import com.microsoft.azure.keyvault.models.SecretAttributes;
import com.microsoft.windowsazure.core.FilterableService;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.credentials.CloudCredentials;

/**
 * A client for the Azure Key Vault service.
 * <p>
 * Applications typically call one of the {@link KeyVaultClientService}
 * <code>create</code> methods to obtain an instance of this class.
 * </p>
 * 
 * @author Fernando Colombo (Microsoft)
 *
 */
public interface KeyVaultClient extends Closeable, FilterableService<KeyVaultClient> {

    /**
     * Gets the API version.
     * 
     * @return The ApiVersion value.
     */
    String getApiVersion();

    /**
     * Gets the URI used as the base for all cloud service requests.
     * 
     * @return The BaseUri value.
     */
    URI getBaseUri();

    /**
     * Gets or sets the credential
     * 
     * @return The Credentials value.
     */
    CloudCredentials getCredentials();

    /**
     * Gets or sets the credential
     * 
     * @param credentialsValue
     *            The Credentials value.
     */
    void setCredentials(final CloudCredentials credentialsValue);

    /**
     * Gets or sets the initial timeout for Long Running Operations.
     * 
     * @return The LongRunningOperationInitialTimeout value.
     */
    int getLongRunningOperationInitialTimeout();

    /**
     * Gets or sets the initial timeout for Long Running Operations.
     * 
     * @param longRunningOperationInitialTimeoutValue
     *            The LongRunningOperationInitialTimeout value.
     */
    void setLongRunningOperationInitialTimeout(final int longRunningOperationInitialTimeoutValue);

    /**
     * Gets or sets the retry timeout for Long Running Operations.
     * 
     * @return The LongRunningOperationRetryTimeout value.
     */
    int getLongRunningOperationRetryTimeout();

    /**
     * Gets or sets the retry timeout for Long Running Operations.
     * 
     * @param longRunningOperationRetryTimeoutValue
     *            The LongRunningOperationRetryTimeout value.
     */
    void setLongRunningOperationRetryTimeout(final int longRunningOperationRetryTimeoutValue);

    ServiceClient<?> getServiceClient();

    Future<KeyOperationResult> encryptAsync(String vault, String keyName, String keyVersion, String algorithm, byte[] plainText);

    Future<KeyOperationResult> encryptAsync(String keyIdentifier, String algorithm, byte[] plainText);

    Future<KeyOperationResult> decryptAsync(String keyIdentifier, String algorithm, byte[] cipherText);

    Future<KeyOperationResult> signAsync(String vault, String keyName, String keyVersion, String algorithm, byte[] digest);

    Future<KeyOperationResult> signAsync(String keyIdentifier, String algorithm, byte[] digest);

    Future<Boolean> verifyAsync(String keyIdentifier, String algorithm, byte[] digest, byte[] signature);

    Future<KeyOperationResult> wrapKeyAsync(String vault, String keyName, String keyVersion, String algorithm, byte[] key);

    Future<KeyOperationResult> wrapKeyAsync(String keyIdentifier, String algorithm, byte[] key);

    Future<KeyOperationResult> unwrapKeyAsync(String keyIdentifier, String algorithm, byte[] wrappedKey);

    Future<KeyBundle> createKeyAsync(String vault, String keyName, String keyType, Integer keySize, String[] keyOps, KeyAttributes keyAttributes, Map<String, String> tags);

    Future<KeyBundle> getKeyAsync(String vault, String keyName, String keyVersion);

    Future<KeyBundle> getKeyAsync(String keyIdentifier);

    Future<ListKeysResponseMessage> getKeysAsync(String vault, Integer maxresults);

    Future<ListKeysResponseMessage> getKeysNextAsync(String nextLink);

    Future<ListKeysResponseMessage> getKeyVersionsAsync(String vault, String keyName, Integer maxresults);

    Future<ListKeysResponseMessage> getKeyVersionsNextAsync(String nextLink);

    Future<KeyBundle> deleteKeyAsync(String vault, String keyName);

    Future<KeyBundle> updateKeyAsync(String vault, String keyName, String[] keyOps, KeyAttributes attributes, Map<String, String> tags);

    Future<KeyBundle> updateKeyAsync(String keyIdentifier, String[] keyOps, KeyAttributes attributes, Map<String, String> tags);

    Future<KeyBundle> importKeyAsync(String vault, String keyName, KeyBundle keyBundle, Boolean importToHardware);

    Future<byte[]> backupKeyAsync(String vault, String keyName);

    Future<KeyBundle> restoreKeyAsync(String vault, byte[] keyBundleBackup);

    Future<Secret> getSecretAsync(String vault, String secretName, String secretVersion);

    Future<Secret> getSecretAsync(String secretIdentifier);

    Future<Secret> setSecretAsync(String vault, String secretName, String value, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);

    Future<Secret> updateSecretAsync(String vault, String secretName, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);

    Future<Secret> updateSecretAsync(String secretIdentifier, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);

    Future<Secret> deleteSecretAsync(String vault, String secretName);

    Future<ListSecretsResponseMessage> getSecretsAsync(String vault, Integer maxresults);

    Future<ListSecretsResponseMessage> getSecretsNextAsync(String nextLink);

    Future<ListSecretsResponseMessage> getSecretVersionsAsync(String vault, String secretName, Integer maxresults);

    Future<ListSecretsResponseMessage> getSecretVersionsNextAsync(String nextLink);

}
