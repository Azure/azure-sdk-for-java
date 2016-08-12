/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information. 
 */

package com.microsoft.azure.keyvault;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Joiner;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.BackupKeyResult;
import com.microsoft.azure.keyvault.models.CertificateBundle;
import com.microsoft.azure.keyvault.models.CertificateIssuerItem;
import com.microsoft.azure.keyvault.models.CertificateItem;
import com.microsoft.azure.keyvault.models.CertificateOperation;
import com.microsoft.azure.keyvault.models.CertificatePolicy;
import com.microsoft.azure.keyvault.models.Contacts;
import com.microsoft.azure.keyvault.models.IssuerBundle;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyItem;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.KeyVerifyResult;
import com.microsoft.azure.keyvault.models.PageImpl;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.azure.keyvault.requests.CreateCertificateRequest;
import com.microsoft.azure.keyvault.requests.CreateKeyRequest;
import com.microsoft.azure.keyvault.requests.ImportCertificateRequest;
import com.microsoft.azure.keyvault.requests.ImportKeyRequest;
import com.microsoft.azure.keyvault.requests.MergeCertificateRequest;
import com.microsoft.azure.keyvault.requests.SetCertificateIssuerRequest;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateIssuerRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateOperationRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificatePolicyRequest;
import com.microsoft.azure.keyvault.requests.UpdateCertificateRequest;
import com.microsoft.azure.keyvault.requests.UpdateKeyRequest;
import com.microsoft.azure.keyvault.requests.UpdateSecretRequest;
import com.microsoft.azure.RestClient;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Initializes a new instance of the KeyVaultClient class.
 */
public final class KeyVaultClient {

    private KeyVaultClientImpl innerKeyVaultClient;

    /** The Retrofit service to perform REST calls. */
    private KeyVaultClientService service;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * 
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return innerKeyVaultClient.getAzureClient();
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String apiVersion() {
        return innerKeyVaultClient.apiVersion();
    }

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return innerKeyVaultClient.acceptLanguage();
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     */
    public void withAcceptLanguage(String acceptLanguage) {
        innerKeyVaultClient.withAcceptLanguage(acceptLanguage);
    }

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return innerKeyVaultClient.longRunningOperationRetryTimeout();
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     */
    public void withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        innerKeyVaultClient.withLongRunningOperationRetryTimeout(longRunningOperationRetryTimeout);
    }
    
    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean getGenerateClientRequestId() {
        return innerKeyVaultClient.generateClientRequestId();
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     */
    public void withGenerateClientRequestId(boolean generateClientRequestId) {
        innerKeyVaultClient.withGenerateClientRequestId(generateClientRequestId);
    }
    
    /**
     * Initializes an instance of KeyVaultClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public KeyVaultClient(ServiceClientCredentials credentials) {
        innerKeyVaultClient = new KeyVaultClientImpl(credentials);
        initializeService();
    }

    /**
     * Initializes an instance of KeyVaultClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public KeyVaultClient(RestClient restClient) {
        innerKeyVaultClient = new KeyVaultClientImpl(restClient);
        initializeService();
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    public String userAgent() {
        return innerKeyVaultClient.userAgent();
    }

    private void initializeService() {
        service = innerKeyVaultClient.restClient().retrofit().create(KeyVaultClientService.class);
    }

    /**
     * The interface defining all the services for KeyVaultClient to be
     * used by Retrofit to perform actually REST calls.
     */
    interface KeyVaultClientService {
        @Headers({"Content-Type: application/json; charset=utf-8", "Accept: application/pkcs10"})
        @GET("certificates/{certificate-name}/pending")
        Call<ResponseBody> getPendingCertificateSigningRequest(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);
    }
        
    /**
     * Creates a new, named, key in the specified vault.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> createKey(CreateKeyRequest createKeyRequest) 
            throws KeyVaultErrorException, IllegalArgumentException, IOException {
        return innerKeyVaultClient.createKey(
                                    createKeyRequest.vaultBaseUrl(), 
                                    createKeyRequest.keyName(), 
                                    createKeyRequest.keyType(), 
                                    createKeyRequest.keySize(), 
                                    createKeyRequest.keyOperations(), 
                                    createKeyRequest.keyAttributes(), 
                                    createKeyRequest.tags());
    }
    
    /**
     * Creates a new, named, key in the specified vault.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> createKeyAsync(CreateKeyRequest createKeyRequest, ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.createKeyAsync(
                createKeyRequest.vaultBaseUrl(), 
                createKeyRequest.keyName(), 
                createKeyRequest.keyType(), 
                createKeyRequest.keySize(), 
                createKeyRequest.keyOperations(), 
                createKeyRequest.keyAttributes(), 
                createKeyRequest.tags(), 
                serviceCallback);
    }
    
    /**
     * Imports a key into the specified vault.
     *
     * @param importKeyRequest the grouped properties for importing a key request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> importKey(ImportKeyRequest importKeyRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.importKey(
                importKeyRequest.vaultBaseUrl(), 
                importKeyRequest.keyName(), 
                importKeyRequest.key(), 
                importKeyRequest.isHsm(), 
                importKeyRequest.keyAttributes(), 
                importKeyRequest.tags());
    }

    /**
     * Imports a key into the specified vault.
     *
     * @param importKeyRequest the grouped properties for importing a key request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> importKeyAsync(ImportKeyRequest importKeyRequest, final ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.importKeyAsync(
                importKeyRequest.vaultBaseUrl(), 
                importKeyRequest.keyName(), 
                importKeyRequest.key(), 
                importKeyRequest.isHsm(), 
                importKeyRequest.keyAttributes(), 
                importKeyRequest.tags(), 
                serviceCallback);
    }

    /**
     * Deletes the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> deleteKey(String vaultBaseUrl, String keyName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.deleteKey(vaultBaseUrl, keyName);
    }

    /**
     * Deletes the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> deleteKeyAsync(String vaultBaseUrl, String keyName, final ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.deleteKeyAsync(vaultBaseUrl, keyName, serviceCallback);
    }

    /**
     * Updates the Key Attributes associated with the specified key.
     *
     * @param updateKeyRequest the grouped properties for updating a key request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> updateKey(UpdateKeyRequest updateKeyRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateKey(
                updateKeyRequest.vaultBaseUrl(), 
                updateKeyRequest.keyName(), 
                updateKeyRequest.keyVersion(), 
                updateKeyRequest.keyOperations(), 
                updateKeyRequest.keyAttributes(), 
                updateKeyRequest.tags());
    }

    /**
     * Updates the Key Attributes associated with the specified key.
     *
     * @param updateKeyRequest the grouped properties for updating a key request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> updateKeyAsync(UpdateKeyRequest updateKeyRequest, final ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.updateKeyAsync(
                updateKeyRequest.vaultBaseUrl(), 
                updateKeyRequest.keyName(), 
                updateKeyRequest.keyVersion(), 
                updateKeyRequest.keyOperations(), 
                updateKeyRequest.keyAttributes(), 
                updateKeyRequest.tags(),
                serviceCallback);
    }
    
    /**
     * Retrieves the public portion of a key plus its attributes.
     *
     * @param keyIdentifier The full key identifier
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> getKey(String keyIdentifier)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.getKey(id.vault, id.name, id.version == null ? "" : id.version);
    }

    /**
     * Retrieves the public portion of a key plus its attributes.
     *
     * @param keyIdentifier The full key identifier
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> getKeyAsync(String keyIdentifier, final ServiceCallback<KeyBundle> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.getKeyAsync(id.vault, id.name, id.version == null ? "" : id.version, serviceCallback);
    }
    
    /**
     * Retrieves the public portion of a key plus its attributes.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> getKey(String vaultBaseUrl, String keyName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKey(vaultBaseUrl, keyName, "");
    }

    /**
     * Retrieves the public portion of a key plus its attributes.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> getKeyAsync(String vaultBaseUrl, String keyName, final ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.getKeyAsync(vaultBaseUrl, keyName, "", serviceCallback);
    }
    
    /**
     * Retrieves the public portion of a key plus its attributes.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param keyVersion The version of the key
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> getKey(String vaultBaseUrl, String keyName, String keyVersion) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKey(vaultBaseUrl, keyName, keyVersion);
    }

    /**
     * Retrieves the public portion of a key plus its attributes.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param keyVersion The version of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> getKeyAsync(String vaultBaseUrl, String keyName, String keyVersion, final ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.getKeyAsync(vaultBaseUrl, keyName, keyVersion, serviceCallback);
    }

    /**
     * List the versions of the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;KeyItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<KeyItem>> getKeyVersions(final String vaultBaseUrl, final String keyName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeyVersions(vaultBaseUrl, keyName);
    }

    /**
     * List the versions of the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> getKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeyVersionsAsync(vaultBaseUrl, keyName, serviceCallback);
    }
    /**
     * List the versions of the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param maxresults Maximum number of results to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;KeyItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<KeyItem>> getKeyVersions(final String vaultBaseUrl, final String keyName, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeyVersions(vaultBaseUrl, keyName, maxresults);
    }

    /**
     * List the versions of the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param maxresults Maximum number of results to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> getKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeyVersionsAsync(vaultBaseUrl, keyName, maxresults, serviceCallback);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;KeyItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<KeyItem>> getKeys(final String vaultBaseUrl) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeys(vaultBaseUrl);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> getKeysAsync(final String vaultBaseUrl, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeysAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;KeyItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<KeyItem>> getKeys(final String vaultBaseUrl, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeys(vaultBaseUrl, maxresults);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> getKeysAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeysAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * Requests that a backup of the specified key be downloaded to the client.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the BackupKeyResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<BackupKeyResult> backupKey(String vaultBaseUrl, String keyName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.backupKey(vaultBaseUrl, keyName);
    }

    /**
     * Requests that a backup of the specified key be downloaded to the client.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<BackupKeyResult> backupKeyAsync(String vaultBaseUrl, String keyName, final ServiceCallback<BackupKeyResult> serviceCallback) {
        return innerKeyVaultClient.backupKeyAsync(vaultBaseUrl, keyName, serviceCallback);
    }

    /**
     * Restores the backup key in to a vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyBundleBackup the backup blob associated with a key bundle
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyBundle> restoreKey(String vaultBaseUrl, byte[] keyBundleBackup) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.restoreKey(vaultBaseUrl, keyBundleBackup);
    }

    /**
     * Restores the backup key in to a vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyBundleBackup the backup blob associated with a key bundle
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyBundle> restoreKeyAsync(String vaultBaseUrl, byte[] keyBundleBackup, final ServiceCallback<KeyBundle> serviceCallback) {
        return innerKeyVaultClient.restoreKeyAsync(vaultBaseUrl, keyBundleBackup, serviceCallback);
    }

    /**
     * Encrypts a single block of data. The amount of data that may be encrypted is determined.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be encrypted
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyOperationResult> encrypt(String keyIdentifier, String algorithm, byte[] value) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.encrypt(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value);
    }

    /**
     * Encrypts a single block of data. The amount of data that may be encrypted is determined.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be encrypted
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyOperationResult> encryptAsync(String keyIdentifier, String algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.encryptAsync(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value, serviceCallback);
    }

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be decrypted
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyOperationResult> decrypt(String keyIdentifier, String algorithm, byte[] value) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.decrypt(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value);
    }

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be decrypted
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses. 
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyOperationResult> decryptAsync(String keyIdentifier, String algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.decryptAsync(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value, serviceCallback);
    }

    /**
     * Creates a signature from a digest using the specified key in the vault.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be signed
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyOperationResult> sign(String keyIdentifier, String algorithm, byte[] value) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.sign(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value);
    }

    /**
     * Creates a signature from a digest using the specified key in the vault.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be signed
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyOperationResult> signAsync(String keyIdentifier, String algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.signAsync(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value, serviceCallback);        
    }

    /**
     * Verifies a signature using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm The signing/verification algorithm. For more information on possible algorithm types, see JsonWebKeySignatureAlgorithm.
     * @param digest The digest used for signing
     * @param signature The signature to be verified
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyVerifyResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyVerifyResult> verify(String keyIdentifier, String algorithm, byte[] digest, byte[] signature) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.verify(id.vault, id.name, id.version == null ? "" : id.version, algorithm, digest, signature);
    }

    /**
     * Verifies a signature using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm The signing/verification algorithm. For more information on possible algorithm types, see JsonWebKeySignatureAlgorithm.
     * @param digest The digest used for signing
     * @param signature The signature to be verified
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyVerifyResult> verifyAsync(String keyIdentifier, String algorithm, byte[] digest, byte[] signature, final ServiceCallback<KeyVerifyResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.verifyAsync(id.vault, id.name, id.version == null ? "" : id.version, algorithm, digest, signature, serviceCallback);
    }

    /**
     * Wraps a symmetric key using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be wrapped
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyOperationResult> wrapKey(String keyIdentifier, String algorithm, byte[] value) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.wrapKey(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value);
    }

    /**
     * Wraps a symmetric key using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be wrapped
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyOperationResult> wrapKeyAsync(String keyIdentifier, String algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.wrapKeyAsync(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value, serviceCallback);
    }

    /**
     * Unwraps a symmetric key using the specified key in the vault that has initially been used for wrapping the key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be unwrapped
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<KeyOperationResult> unwrapKey(String keyIdentifier, String algorithm, byte[] value) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.unwrapKey(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value);
    }

    /**
     * Unwraps a symmetric key using the specified key in the vault that has initially been used for wrapping the key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be unwrapped
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<KeyOperationResult> unwrapKeyAsync(String keyIdentifier, String algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
        KeyIdentifier id = new KeyIdentifier(keyIdentifier);
        return innerKeyVaultClient.unwrapKeyAsync(id.vault, id.name, id.version == null ? "" : id.version, algorithm, value, serviceCallback);
    }

    /**
     * Sets a secret in the specified vault.
     *
     * @param setSecretRequest the grouped properties for setting a secret request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SecretBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SecretBundle> setSecret(SetSecretRequest setSecretRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.setSecret(
                setSecretRequest.vaultBaseUrl(), 
                setSecretRequest.secretName(), 
                setSecretRequest.value(), 
                setSecretRequest.tags(), 
                setSecretRequest.contentType(), 
                setSecretRequest.secretAttributes());
    }

    /**
     * Sets a secret in the specified vault.
     *
     * @param setSecretRequest the grouped properties for setting a secret request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<SecretBundle> setSecretAsync(SetSecretRequest setSecretRequest, final ServiceCallback<SecretBundle> serviceCallback) {
        return innerKeyVaultClient.setSecretAsync(
                setSecretRequest.vaultBaseUrl(), 
                setSecretRequest.secretName(), 
                setSecretRequest.value(), 
                setSecretRequest.tags(), 
                setSecretRequest.contentType(), 
                setSecretRequest.secretAttributes(),
                serviceCallback);
    }

    /**
     * Deletes a secret from the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SecretBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SecretBundle> deleteSecret(String vaultBaseUrl, String secretName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.deleteSecret(vaultBaseUrl, secretName);
    }

    /**
     * Deletes a secret from the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<SecretBundle> deleteSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<SecretBundle> serviceCallback) {
        return innerKeyVaultClient.deleteSecretAsync(vaultBaseUrl, secretName, serviceCallback);
    }

    /**
     * Updates the attributes associated with the specified secret.
     *
     * @param updateSecretRequest the grouped properties for updating a secret request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SecretBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SecretBundle> updateSecret(UpdateSecretRequest updateSecretRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateSecret(
                updateSecretRequest.vaultBaseUrl(), 
                updateSecretRequest.secretName(),  
                updateSecretRequest.secretVersion(),
                updateSecretRequest.contentType(), 
                updateSecretRequest.secretAttributes(),
                updateSecretRequest.tags());
    }

    /**
     * Updates the attributes associated with the specified secret.
     *
     * @param updateSecretRequest the grouped properties for updating a secret request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<SecretBundle> updateSecretAsync(UpdateSecretRequest updateSecretRequest, final ServiceCallback<SecretBundle> serviceCallback) {
        return innerKeyVaultClient.updateSecretAsync(
                updateSecretRequest.vaultBaseUrl(), 
                updateSecretRequest.secretName(),  
                updateSecretRequest.secretVersion(),
                updateSecretRequest.contentType(), 
                updateSecretRequest.secretAttributes(),
                updateSecretRequest.tags(), 
                serviceCallback);
    }

    /**
     * Gets a secret.
     *
     * @param secretIdentifier The URL for the secret.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SecretBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SecretBundle> getSecret(String secretIdentifier) 
             throws KeyVaultErrorException, IOException, IllegalArgumentException {
        SecretIdentifier id = new SecretIdentifier(secretIdentifier);
        return innerKeyVaultClient.getSecret(id.vault, id.name, id.version == null ? "" : id.version);
    }

    /**
     * Gets a secret.
     *
     * @param secretIdentifier The URL for the secret.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<SecretBundle> getSecretAsync(String secretIdentifier, final ServiceCallback<SecretBundle> serviceCallback) {
        SecretIdentifier id = new SecretIdentifier(secretIdentifier);
        return innerKeyVaultClient.getSecretAsync(id.vault, id.name, id.version == null ? "" : id.version, serviceCallback);
    }

    /**
     * Gets a secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SecretBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SecretBundle> getSecret(String vaultBaseUrl, String secretName) 
             throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecret(vaultBaseUrl, secretName, "");
    }

    /**
     * Gets a secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<SecretBundle> serviceCallback) {
        return innerKeyVaultClient.getSecretAsync(vaultBaseUrl, secretName, "", serviceCallback);
    }
    
    /**
     * Gets a secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param secretVersion The version of the secret
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SecretBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SecretBundle> getSecret(String vaultBaseUrl, String secretName, String secretVersion) 
             throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecret(vaultBaseUrl, secretName, secretVersion == null ? "" : secretVersion);
    }

    /**
     * Gets a secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param secretVersion The version of the secret
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, String secretVersion, final ServiceCallback<SecretBundle> serviceCallback) {
        return innerKeyVaultClient.getSecretAsync(vaultBaseUrl, secretName, secretVersion == null ? "" : secretVersion, serviceCallback);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SecretItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SecretItem>> getSecrets(final String vaultBaseUrl) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecrets(vaultBaseUrl);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> getSecretsAsync(final String vaultBaseUrl, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretsAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of secrets to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SecretItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SecretItem>> getSecrets(final String vaultBaseUrl, final Integer maxresults)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecrets(vaultBaseUrl, maxresults);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of secrets to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> getSecretsAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretsAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SecretItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SecretItem>> getSecretVersions(final String vaultBaseUrl, final String secretName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecretVersions(vaultBaseUrl, secretName);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> getSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretVersionsAsync(vaultBaseUrl, secretName, serviceCallback);
    }
    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param maxresults Maximum number of results to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SecretItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SecretItem>> getSecretVersions(final String vaultBaseUrl, final String secretName, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecretVersions(vaultBaseUrl, secretName, maxresults);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param maxresults Maximum number of results to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> getSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretVersionsAsync(vaultBaseUrl, secretName, maxresults, serviceCallback);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<CertificateItem>> getCertificates(final String vaultBaseUrl)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificates(vaultBaseUrl);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> getCertificatesAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificatesAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<CertificateItem>> getCertificates(final String vaultBaseUrl, final Integer maxresults)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificates(vaultBaseUrl, maxresults);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> getCertificatesAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificatesAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * Deletes a certificate from the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> deleteCertificate(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.deleteCertificate(vaultBaseUrl, certificateName);
    }

    /**
     * Deletes a certificate from the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> deleteCertificateAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificateBundle> serviceCallback) {
        return innerKeyVaultClient.deleteCertificateAsync(vaultBaseUrl, certificateName, serviceCallback);
    }

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param contacts The contacts for the vault certificates.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Contacts object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Contacts> setCertificateContacts(String vaultBaseUrl, Contacts contacts) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.setCertificateContacts(vaultBaseUrl, contacts);
    }

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param contacts The contacts for the vault certificates.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<Contacts> setCertificateContactsAsync(String vaultBaseUrl, Contacts contacts, final ServiceCallback<Contacts> serviceCallback) {
        return innerKeyVaultClient.setCertificateContactsAsync(vaultBaseUrl, contacts, serviceCallback);
    }

    /**
     * Gets the certificate contacts for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Contacts object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Contacts> getCertificateContacts(String vaultBaseUrl) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateContacts(vaultBaseUrl);
    }

    /**
     * Gets the certificate contacts for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<Contacts> getCertificateContactsAsync(String vaultBaseUrl, final ServiceCallback<Contacts> serviceCallback) {
        return innerKeyVaultClient.getCertificateContactsAsync(vaultBaseUrl, serviceCallback);
    }

    /**
     * Deletes the certificate contacts for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Contacts object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Contacts> deleteCertificateContacts(String vaultBaseUrl) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.deleteCertificateContacts(vaultBaseUrl);
    }

    /**
     * Deletes the certificate contacts for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<Contacts> deleteCertificateContactsAsync(String vaultBaseUrl, final ServiceCallback<Contacts> serviceCallback) {
        return innerKeyVaultClient.deleteCertificateContactsAsync(vaultBaseUrl, serviceCallback);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateIssuerItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<CertificateIssuerItem>> getCertificateIssuers(final String vaultBaseUrl) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateIssuers(vaultBaseUrl);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateIssuerItem>> getCertificateIssuersAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateIssuersAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateIssuerItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<CertificateIssuerItem>> getCertificateIssuers(final String vaultBaseUrl, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateIssuers(vaultBaseUrl, maxresults);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateIssuerItem>> getCertificateIssuersAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateIssuersAsync(vaultBaseUrl, maxresults, serviceCallback);
    }

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param setCertificateIssuerRequest the grouped properties for setting a certificate issuer request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the IssuerBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<IssuerBundle> setCertificateIssuer(SetCertificateIssuerRequest setCertificateIssuerRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.setCertificateIssuer(
                setCertificateIssuerRequest.vaultBaseUrl(), 
                setCertificateIssuerRequest.issuerName(), 
                setCertificateIssuerRequest.issuer());
    }

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param setCertificateIssuerRequest the grouped properties for setting a certificate issuer request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<IssuerBundle> setCertificateIssuerAsync(SetCertificateIssuerRequest setCertificateIssuerRequest, final ServiceCallback<IssuerBundle> serviceCallback) {
        return innerKeyVaultClient.setCertificateIssuerAsync(
                setCertificateIssuerRequest.vaultBaseUrl(), 
                setCertificateIssuerRequest.issuerName(), 
                setCertificateIssuerRequest.issuer(), 
                serviceCallback);
    }

    /**
     * Updates the specified certificate issuer.
     *
     * @param updateCertificateIssuerRequest the grouped properties for updating a certificate issuer request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the IssuerBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<IssuerBundle> updateCertificateIssuer(UpdateCertificateIssuerRequest updateCertificateIssuerRequest)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificateIssuer(
                updateCertificateIssuerRequest.vaultBaseUrl(), 
                updateCertificateIssuerRequest.issuerName(), 
                updateCertificateIssuerRequest.issuer());
    }

    /**
     * Updates the specified certificate issuer.
     *
     * @param updateCertificateIssuerRequest the grouped properties for updating a certificate issuer request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<IssuerBundle> updateCertificateIssuerAsync(UpdateCertificateIssuerRequest updateCertificateIssuerRequest, final ServiceCallback<IssuerBundle> serviceCallback) {
        return innerKeyVaultClient.updateCertificateIssuerAsync(
                updateCertificateIssuerRequest.vaultBaseUrl(), 
                updateCertificateIssuerRequest.issuerName(), 
                updateCertificateIssuerRequest.issuer(),
                serviceCallback);
    }

    /**
     * Gets the specified certificate issuer.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param issuerName The name of the issuer.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the IssuerBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<IssuerBundle> getCertificateIssuer(String vaultBaseUrl, String issuerName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateIssuer(vaultBaseUrl, issuerName);
    }

    /**
     * Gets the specified certificate issuer.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param issuerName The name of the issuer.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<IssuerBundle> getCertificateIssuerAsync(String vaultBaseUrl, String issuerName, final ServiceCallback<IssuerBundle> serviceCallback) {
        return innerKeyVaultClient.getCertificateIssuerAsync(vaultBaseUrl, issuerName, serviceCallback);    
    }

    /**
     * Deletes the specified certificate issuer.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param issuerName The name of the issuer.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the IssuerBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<IssuerBundle> deleteCertificateIssuer(String vaultBaseUrl, String issuerName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.deleteCertificateIssuer(vaultBaseUrl, issuerName);
    }

    /**
     * Deletes the specified certificate issuer.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param issuerName The name of the issuer.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<IssuerBundle> deleteCertificateIssuerAsync(String vaultBaseUrl, String issuerName, final ServiceCallback<IssuerBundle> serviceCallback) {
        return innerKeyVaultClient.deleteCertificateIssuerAsync(vaultBaseUrl, issuerName, serviceCallback);
    }

    /**
     * Creates a new certificate version. If this is the first version, the certificate resource is created.
     *
     * @param createCertificateRequest the grouped properties for creating a certificate request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateOperation object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateOperation> createCertificate(CreateCertificateRequest createCertificateRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.createCertificate(
                createCertificateRequest.vaultBaseUrl(), 
                createCertificateRequest.certificateName(), 
                createCertificateRequest.certificatePolicy(), 
                createCertificateRequest.certificateAttributes(), 
                createCertificateRequest.tags());
    }

    /**
     * Creates a new certificate version. If this is the first version, the certificate resource is created.
     *
     * @param createCertificateRequest the grouped properties for creating a certificate request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateOperation> createCertificateAsync(CreateCertificateRequest createCertificateRequest, final ServiceCallback<CertificateOperation> serviceCallback) {
        return innerKeyVaultClient.createCertificateAsync(
                createCertificateRequest.vaultBaseUrl(), 
                createCertificateRequest.certificateName(), 
                createCertificateRequest.certificatePolicy(), 
                createCertificateRequest.certificateAttributes(), 
                createCertificateRequest.tags(), 
                serviceCallback);
    }

    /**
     * Imports a certificate into the specified vault.
     *
     * @param importCertificateRequest the grouped properties for importing a certificate request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> importCertificate(ImportCertificateRequest importCertificateRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.importCertificate(
                importCertificateRequest.vaultBaseUrl(), 
                importCertificateRequest.certificateName(), 
                importCertificateRequest.base64EncodedCertificate(),
                importCertificateRequest.password(), 
                importCertificateRequest.certificatePolicy(), 
                importCertificateRequest.certificateAttributes(), 
                importCertificateRequest.tags());
    }

    /**
     * Imports a certificate into the specified vault.
     *
     * @param importCertificateRequest the grouped properties for importing a certificate request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> importCertificateAsync(ImportCertificateRequest importCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback) {
        return innerKeyVaultClient.importCertificateAsync(
                importCertificateRequest.vaultBaseUrl(), 
                importCertificateRequest.certificateName(), 
                importCertificateRequest.base64EncodedCertificate(), 
                importCertificateRequest.password(), 
                importCertificateRequest.certificatePolicy(), 
                importCertificateRequest.certificateAttributes(), 
                importCertificateRequest.tags(), 
                serviceCallback);
    }
    
    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<CertificateItem>> getCertificateVersions(final String vaultBaseUrl, final String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateVersions(vaultBaseUrl, certificateName);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> getCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateVersionsAsync(vaultBaseUrl, certificateName, serviceCallback);        
    }
    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults Maximum number of results to return.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<CertificateItem>> getCertificateVersions(final String vaultBaseUrl, final String certificateName, final Integer maxresults)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateVersions(vaultBaseUrl, certificateName, maxresults);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults Maximum number of results to return.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> getCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateVersionsAsync(vaultBaseUrl, certificateName, maxresults, serviceCallback);
    }

    /**
     * Gets the policy for a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificatePolicy object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificatePolicy> getCertificatePolicy(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificatePolicy(vaultBaseUrl, certificateName);
    }

    /**
     * Gets the policy for a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificatePolicy> getCertificatePolicyAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificatePolicy> serviceCallback) {
        return innerKeyVaultClient.getCertificatePolicyAsync(vaultBaseUrl, certificateName, serviceCallback);
    }

    /**
     * Updates the policy for a certificate. Set appropriate members in the certificatePolicy that must be updated. Leave others as null.
     *
     * @param updateCertificatePolicyRequest the grouped properties for updating a certificate policy request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificatePolicy object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificatePolicy> updateCertificatePolicy(UpdateCertificatePolicyRequest updateCertificatePolicyRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificatePolicy(
                updateCertificatePolicyRequest.vaultBaseUrl(), 
                updateCertificatePolicyRequest.certificateName(), 
                updateCertificatePolicyRequest.certificatePolicy());
    }

    /**
     * Updates the policy for a certificate. Set appropriate members in the certificatePolicy that must be updated. Leave others as null.
     *
     * @param updateCertificatePolicyRequest the grouped properties for updating a certificate policy request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificatePolicy> updateCertificatePolicy(UpdateCertificatePolicyRequest updateCertificatePolicyRequest, final ServiceCallback<CertificatePolicy> serviceCallback) {
        return innerKeyVaultClient.updateCertificatePolicyAsync(
                updateCertificatePolicyRequest.vaultBaseUrl(), 
                updateCertificatePolicyRequest.certificateName(), 
                updateCertificatePolicyRequest.certificatePolicy(), 
                serviceCallback);
    }

    /**
     * Updates the attributes associated with the specified certificate.
     *
     * @param updateCertificateRequest the grouped properties for updating a certificate request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> updateCertificate(UpdateCertificateRequest updateCertificateRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificate(
                updateCertificateRequest.vaultBaseUrl(), 
                updateCertificateRequest.certificateName(), 
                updateCertificateRequest.certificateVersion(), 
                updateCertificateRequest.certificateAttributes(), 
                updateCertificateRequest.tags());
    }

    /**
     * Updates the attributes associated with the specified certificate.
     *
     * @param updateCertificateRequest the grouped properties for updating a certificate request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> updateCertificateAsync(UpdateCertificateRequest updateCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback) {
        return innerKeyVaultClient.updateCertificateAsync(
                updateCertificateRequest.vaultBaseUrl(), 
                updateCertificateRequest.certificateName(), 
                updateCertificateRequest.certificateVersion(), 
                updateCertificateRequest.certificateAttributes(), 
                updateCertificateRequest.tags(), 
                serviceCallback);
    }
    
    /**
     * Gets a Certificate.
     *
     * @param certificateIdentifier The certificate identifier
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> getCertificate(String certificateIdentifier) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        CertificateIdentifier id = new CertificateIdentifier(certificateIdentifier); 
        return innerKeyVaultClient.getCertificate(id.vault, id.name, id.version == null ? "" : id.version);
    }

    /**
     * Gets a Certificate.
     *
     * @param certificateIdentifier The certificate identifier
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> getCertificateAsync(String certificateIdentifier, final ServiceCallback<CertificateBundle> serviceCallback) {
        CertificateIdentifier id = new CertificateIdentifier(certificateIdentifier);
        return innerKeyVaultClient.getCertificateAsync(id.vault, id.name, id.version == null ? "" : id.version, serviceCallback);
    }
    
    /**
     * Gets a Certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> getCertificate(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificate(vaultBaseUrl, certificateName, "");
    }

    /**
     * Gets a Certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> getCertificateAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificateBundle> serviceCallback) {
        return innerKeyVaultClient.getCertificateAsync(vaultBaseUrl, certificateName, "", serviceCallback);
    }
    
    /**
     * Gets a Certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @param certificateVersion The version of the certificate
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> getCertificate(String vaultBaseUrl, String certificateName, String certificateVersion) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificate(vaultBaseUrl, certificateName, certificateVersion);
    }

    /**
     * Gets a Certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     * @param certificateVersion The version of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> getCertificateAsync(String vaultBaseUrl, String certificateName, String certificateVersion, final ServiceCallback<CertificateBundle> serviceCallback) {
        return innerKeyVaultClient.getCertificateAsync(vaultBaseUrl, certificateName, certificateVersion, serviceCallback);
    }

    /**
     * Updates a certificate operation.
     * 
     * @param updateCertificateOperationRequest the grouped properties for updating a certificate operation request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateOperation object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateOperation> updateCertificateOperation(UpdateCertificateOperationRequest updateCertificateOperationRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificateOperation(
                updateCertificateOperationRequest.vaultBaseUrl(), 
                updateCertificateOperationRequest.certificateName(), 
                updateCertificateOperationRequest.certificateOperation());
    }

    /**
     * Updates a certificate operation.
     *
     * @param updateCertificateOperationRequest the grouped properties for updating a certificate operation request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateOperation> updateCertificateOperationAsync(UpdateCertificateOperationRequest updateCertificateOperationRequest, final ServiceCallback<CertificateOperation> serviceCallback) {
        return innerKeyVaultClient.updateCertificateOperationAsync(
                updateCertificateOperationRequest.vaultBaseUrl(), 
                updateCertificateOperationRequest.certificateName(), 
                updateCertificateOperationRequest.certificateOperation(),
                serviceCallback);
    }

    /**
     * Gets the certificate operation response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateOperation object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateOperation> getCertificateOperation(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateOperation(vaultBaseUrl, certificateName);
    }

    /**
     * Gets the certificate operation response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateOperation> getCertificateOperationAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificateOperation> serviceCallback) {
        return innerKeyVaultClient.getCertificateOperationAsync(vaultBaseUrl, certificateName, serviceCallback);
    }

    /**
     * Deletes the certificate operation.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateOperation object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateOperation> deleteCertificateOperation(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.deleteCertificateOperation(vaultBaseUrl, certificateName);
    }

    /**
     * Deletes the certificate operation.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    ServiceCall<CertificateOperation> deleteCertificateOperationAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificateOperation> serviceCallback) {
        return innerKeyVaultClient.deleteCertificateOperationAsync(vaultBaseUrl, certificateName, serviceCallback);
    }

    /**
     * Merges a certificate or a certificate chain with a key pair existing on the server.
     *
     * @param mergeCertificateRequest the grouped properties for merging a certificate request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CertificateBundle object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CertificateBundle> mergeCertificate(MergeCertificateRequest mergeCertificateRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.mergeCertificate(
                mergeCertificateRequest.vaultBaseUrl(), 
                mergeCertificateRequest.certificateName(), 
                mergeCertificateRequest.x509Certificates(), 
                mergeCertificateRequest.certificateAttributes(), 
                mergeCertificateRequest.tags());
    }

    /**
     * Merges a certificate or a certificate chain with a key pair existing on the server.
     *
     * @param mergeCertificateRequest the grouped properties for merging a certificate request
     * 
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<CertificateBundle> mergeCertificateAsync(MergeCertificateRequest mergeCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback) {
        return innerKeyVaultClient.mergeCertificateAsync(
                mergeCertificateRequest.vaultBaseUrl(), 
                mergeCertificateRequest.certificateName(), 
                mergeCertificateRequest.x509Certificates(), 
                mergeCertificateRequest.certificateAttributes(), 
                mergeCertificateRequest.tags(), 
                serviceCallback);
    }


    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the String object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<String> getPendingCertificateSigningRequest(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        if (vaultBaseUrl == null) {
            throw new IllegalArgumentException("Parameter vaultBaseUrl is required and cannot be null.");
        }
        if (certificateName == null) {
            throw new IllegalArgumentException("Parameter certificateName is required and cannot be null.");
        }
        if (this.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{vaultBaseUrl}", vaultBaseUrl);
        Call<ResponseBody> call = service.getPendingCertificateSigningRequest(certificateName, this.apiVersion(), this.acceptLanguage(), parameterizedHost, this.userAgent());
        Response<ResponseBody> response = call.execute();
        return new ServiceResponse<String>(response.body().string(), response);
    }

    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<String> getPendingCertificateSigningRequestAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<String> serviceCallback) {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (vaultBaseUrl == null) {
            throw new IllegalArgumentException("Parameter vaultBaseUrl is required and cannot be null.");
        }
        if (certificateName == null) {
            throw new IllegalArgumentException("Parameter certificateName is required and cannot be null.");
        }
        if (this.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{vaultBaseUrl}", vaultBaseUrl);
        Call<ResponseBody> call = service.getPendingCertificateSigningRequest(certificateName, this.apiVersion(), this.acceptLanguage(), parameterizedHost, this.userAgent());
        final ServiceCall<String> serviceCall = new ServiceCall<String>(call);
        call.enqueue(new ServiceResponseCallback<String>(serviceCall, serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (serviceCallback != null) {
                        serviceCallback.success(new ServiceResponse<String>(response.body().string(), response));
                    }
                    serviceCall.success(new ServiceResponse<String>(response.body().string(), response));
                } catch (IOException exception) {
                    if (serviceCallback != null) {
                        serviceCallback.failure(exception);
                    }
                    serviceCall.failure(exception);
                }
            }
        });
        return serviceCall;
    }
    
    /**
     * List the versions of the specified key.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;KeyItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<KeyItem>> getKeyVersionsNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeyVersionsNext(nextPageLink);
    }

    /**
     * List the versions of the specified key.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> getKeyVersionsNextAsync(final String nextPageLink, final ServiceCall<List<KeyItem>> serviceCall, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeyVersionsNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

    /**
     * List keys in the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;KeyItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<KeyItem>> getKeysNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeysNext(nextPageLink);
    }

    /**
     * List keys in the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> getKeysNextAsync(final String nextPageLink, final ServiceCall<List<KeyItem>> serviceCall, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeysNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SecretItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<SecretItem>> getSecretsNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecretsNext(nextPageLink);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> getSecretsNextAsync(final String nextPageLink, final ServiceCall<List<SecretItem>> serviceCall, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretsNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SecretItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<SecretItem>> getSecretVersionsNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecretVersionsNext(nextPageLink);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> getSecretVersionsNextAsync(final String nextPageLink, final ServiceCall<List<SecretItem>> serviceCall, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretVersionsNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<CertificateItem>> getCertificatesNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificatesNext(nextPageLink);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> getCertificatesNextAsync(final String nextPageLink, final ServiceCall<List<CertificateItem>> serviceCall, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificatesNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateIssuerItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<CertificateIssuerItem>> getCertificateIssuersNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateIssuersNext(nextPageLink);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateIssuerItem>> getCertificateIssuersNextAsync(final String nextPageLink, final ServiceCall<List<CertificateIssuerItem>> serviceCall, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateIssuersNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

    /**
     * List the versions of a certificate.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CertificateItem&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<CertificateItem>> getCertificateVersionsNext(final String nextPageLink) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateVersionsNext(nextPageLink);
    }

    /**
     * List the versions of a certificate.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> getCertificateVersionsNextAsync(final String nextPageLink, final ServiceCall<List<CertificateItem>> serviceCall, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateVersionsNextAsync(nextPageLink, serviceCall, serviceCallback);
    }

}
