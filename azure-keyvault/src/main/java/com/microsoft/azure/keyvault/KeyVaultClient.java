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
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
import com.microsoft.azure.RestClient;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Func1;

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
        Observable<Response<ResponseBody>> getPendingCertificateSigningRequest(@Path("certificate-name") String certificateName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);
    }
        
    /**
     * Creates a new, named, key in the specified vault.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     * 
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the KeyBundle if successful.
     */
    public KeyBundle createKey(CreateKeyRequest createKeyRequest) 
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle importKey(ImportKeyRequest importKeyRequest) 
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle deleteKey(String vaultBaseUrl, String keyName) 
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle updateKey(UpdateKeyRequest updateKeyRequest) 
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle getKey(String keyIdentifier)
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle getKey(String vaultBaseUrl, String keyName) 
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle getKey(String vaultBaseUrl, String keyName, String keyVersion) 
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
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeyVersions(final String vaultBaseUrl, final String keyName) 
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
    public ServiceCall<List<KeyItem>> listKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeyVersionsAsync(vaultBaseUrl, keyName, serviceCallback);
    }
    /**
     * List the versions of the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeyVersions(final String vaultBaseUrl, final String keyName, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeyVersions(vaultBaseUrl, keyName, maxresults);
    }

    /**
     * List the versions of the specified key.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> listKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeyVersionsAsync(vaultBaseUrl, keyName, maxresults, serviceCallback);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeys(final String vaultBaseUrl) 
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
    public ServiceCall<List<KeyItem>> listKeysAsync(final String vaultBaseUrl, final ListOperationCallback<KeyItem> serviceCallback) {
        return innerKeyVaultClient.getKeysAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    public PagedList<KeyItem> listKeys(final String vaultBaseUrl, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getKeys(vaultBaseUrl, maxresults);
    }

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<KeyItem>> listKeysAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback) {
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
     * @return the BackupKeyResult if successful.
     */
    public BackupKeyResult backupKey(String vaultBaseUrl, String keyName) 
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
     * @return the KeyBundle if successful.
     */
    public KeyBundle restoreKey(String vaultBaseUrl, byte[] keyBundleBackup) 
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
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult encrypt(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) 
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
    public ServiceCall<KeyOperationResult> encryptAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
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
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult decrypt(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) 
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
    public ServiceCall<KeyOperationResult> decryptAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
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
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult sign(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] value) 
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
    public ServiceCall<KeyOperationResult> signAsync(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
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
     * @return the KeyVerifyResult if successful.
     */
    public KeyVerifyResult verify(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] digest, byte[] signature) 
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
    public ServiceCall<KeyVerifyResult> verifyAsync(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] digest, byte[] signature, final ServiceCallback<KeyVerifyResult> serviceCallback) {
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
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult wrapKey(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) 
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
    public ServiceCall<KeyOperationResult> wrapKeyAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
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
     * @return the KeyOperationResult if successful.
     */
    public KeyOperationResult unwrapKey(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value) 
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
    public ServiceCall<KeyOperationResult> unwrapKeyAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback) {
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
     * @return the SecretBundle if successful.
     */
    public SecretBundle setSecret(SetSecretRequest setSecretRequest) 
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
     * @return the SecretBundle if successful.
     */
    public SecretBundle deleteSecret(String vaultBaseUrl, String secretName) 
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
     * @return the SecretBundle if successful.
     */
    public SecretBundle updateSecret(UpdateSecretRequest updateSecretRequest) 
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
     * @return the SecretBundle if successful.
     */
    public SecretBundle getSecret(String secretIdentifier) 
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
     * @return the SecretBundle if successful.
     */
    public SecretBundle getSecret(String vaultBaseUrl, String secretName) 
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
     * @return the SecretBundle if successful.
     */
    public SecretBundle getSecret(String vaultBaseUrl, String secretName, String secretVersion) 
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
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecrets(final String vaultBaseUrl) 
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
    public ServiceCall<List<SecretItem>> listSecretsAsync(final String vaultBaseUrl, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretsAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecrets(final String vaultBaseUrl, final Integer maxresults)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecrets(vaultBaseUrl, maxresults);
    }

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> listSecretsAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback) {
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
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecretVersions(final String vaultBaseUrl, final String secretName) 
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
    public ServiceCall<List<SecretItem>> listSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretVersionsAsync(vaultBaseUrl, secretName, serviceCallback);
    }
    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    public PagedList<SecretItem> listSecretVersions(final String vaultBaseUrl, final String secretName, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getSecretVersions(vaultBaseUrl, secretName, maxresults);
    }

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<SecretItem>> listSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback) {
        return innerKeyVaultClient.getSecretVersionsAsync(vaultBaseUrl, secretName, maxresults, serviceCallback);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificates(final String vaultBaseUrl)
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
    public ServiceCall<List<CertificateItem>> listCertificatesAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificatesAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificates(final String vaultBaseUrl, final Integer maxresults)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificates(vaultBaseUrl, maxresults);
    }

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> listCertificatesAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback) {
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle deleteCertificate(String vaultBaseUrl, String certificateName) 
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
     * @return the Contacts if successful.
     */
    public Contacts setCertificateContacts(String vaultBaseUrl, Contacts contacts) 
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
     * @return the Contacts if successful.
     */
    public Contacts getCertificateContacts(String vaultBaseUrl) 
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
     * @return the Contacts if successful.
     */
    public Contacts deleteCertificateContacts(String vaultBaseUrl) 
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
     * @return the PagedList&lt;CertificateIssuerItem&gt; if successful.
     */
    public PagedList<CertificateIssuerItem> listCertificateIssuers(final String vaultBaseUrl) 
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
    public ServiceCall<List<CertificateIssuerItem>> listCertificateIssuersAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateIssuersAsync(vaultBaseUrl, serviceCallback);
    }
    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;CertificateIssuerItem&gt; if successful.
     */
    public PagedList<CertificateIssuerItem> listCertificateIssuers(final String vaultBaseUrl, final Integer maxresults) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateIssuers(vaultBaseUrl, maxresults);
    }

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateIssuerItem>> listCertificateIssuersAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateIssuerItem> serviceCallback) {
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
     * @return the IssuerBundle if successful.
     */
    public IssuerBundle setCertificateIssuer(SetCertificateIssuerRequest setCertificateIssuerRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.setCertificateIssuer(
                setCertificateIssuerRequest.vaultBaseUrl(), 
                setCertificateIssuerRequest.issuerName(),
                setCertificateIssuerRequest.provider(),
                setCertificateIssuerRequest.credentials(),
                setCertificateIssuerRequest.organizationDetails(),
                setCertificateIssuerRequest.attributes());
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
                setCertificateIssuerRequest.provider(),
                setCertificateIssuerRequest.credentials(),
                setCertificateIssuerRequest.organizationDetails(),
                setCertificateIssuerRequest.attributes(),
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
     * @return the IssuerBundle if successful.
     */
    public IssuerBundle updateCertificateIssuer(UpdateCertificateIssuerRequest updateCertificateIssuerRequest)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificateIssuer(
                updateCertificateIssuerRequest.vaultBaseUrl(), 
                updateCertificateIssuerRequest.issuerName(),
                updateCertificateIssuerRequest.provider(),
                updateCertificateIssuerRequest.credentials(),
                updateCertificateIssuerRequest.organizationDetails(),
                updateCertificateIssuerRequest.attributes());
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
                updateCertificateIssuerRequest.provider(),
                updateCertificateIssuerRequest.credentials(),
                updateCertificateIssuerRequest.organizationDetails(),
                updateCertificateIssuerRequest.attributes(),
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
     * @return the IssuerBundle if successful.
     */
    public IssuerBundle getCertificateIssuer(String vaultBaseUrl, String issuerName) 
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
     * @return the IssuerBundle if successful.
     */
    public IssuerBundle deleteCertificateIssuer(String vaultBaseUrl, String issuerName) 
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
     * @return the CertificateOperation if successful.
     */
    public CertificateOperation createCertificate(CreateCertificateRequest createCertificateRequest) 
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle importCertificate(ImportCertificateRequest importCertificateRequest) 
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
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificateVersions(final String vaultBaseUrl, final String certificateName) 
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
    public ServiceCall<List<CertificateItem>> listCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final ListOperationCallback<CertificateItem> serviceCallback) {
        return innerKeyVaultClient.getCertificateVersionsAsync(vaultBaseUrl, certificateName, serviceCallback);        
    }
    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws KeyVaultErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    public PagedList<CertificateItem> listCertificateVersions(final String vaultBaseUrl, final String certificateName, final Integer maxresults)
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.getCertificateVersions(vaultBaseUrl, certificateName, maxresults);
    }

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceCall} object
     */
    public ServiceCall<List<CertificateItem>> listCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback) {
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
     * @return the CertificatePolicy if successful.
     */
    public CertificatePolicy getCertificatePolicy(String vaultBaseUrl, String certificateName) 
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
     * @return the CertificatePolicy if successful.
     */
    public CertificatePolicy updateCertificatePolicy(UpdateCertificatePolicyRequest updateCertificatePolicyRequest) 
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
    public ServiceCall<CertificatePolicy> updateCertificatePolicyAsync(UpdateCertificatePolicyRequest updateCertificatePolicyRequest, final ServiceCallback<CertificatePolicy> serviceCallback) {
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle updateCertificate(UpdateCertificateRequest updateCertificateRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificate(
                updateCertificateRequest.vaultBaseUrl(), 
                updateCertificateRequest.certificateName(),
                updateCertificateRequest.certificateVersion(),
                updateCertificateRequest.certificatePolicy(), 
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
                updateCertificateRequest.certificatePolicy(), 
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle getCertificate(String certificateIdentifier) 
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle getCertificate(String vaultBaseUrl, String certificateName) 
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle getCertificate(String vaultBaseUrl, String certificateName, String certificateVersion) 
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
     * @return the CertificateOperation if successful.
     */
    public CertificateOperation updateCertificateOperation(UpdateCertificateOperationRequest updateCertificateOperationRequest) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return innerKeyVaultClient.updateCertificateOperation(
                updateCertificateOperationRequest.vaultBaseUrl(), 
                updateCertificateOperationRequest.certificateName(), 
                updateCertificateOperationRequest.cancellationRequested());
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
                updateCertificateOperationRequest.cancellationRequested(),
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
     * @return the CertificateOperation if successful.
     */
    public CertificateOperation getCertificateOperation(String vaultBaseUrl, String certificateName) 
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
     * @return the CertificateOperation if successful.
     */
    public CertificateOperation deleteCertificateOperation(String vaultBaseUrl, String certificateName) 
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
    public ServiceCall<CertificateOperation> deleteCertificateOperationAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<CertificateOperation> serviceCallback) {
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
     * @return the CertificateBundle if successful.
     */
    public CertificateBundle mergeCertificate(MergeCertificateRequest mergeCertificateRequest) 
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
     * @return the String if successful.
     */
    public String getPendingCertificateSigningRequest(String vaultBaseUrl, String certificateName) 
            throws KeyVaultErrorException, IOException, IllegalArgumentException {
        return getPendingCertificateSigningRequestWithServiceResponseAsync(vaultBaseUrl, certificateName).toBlocking().single().getBody();
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
        return ServiceCall.create(getPendingCertificateSigningRequestWithServiceResponseAsync(vaultBaseUrl, certificateName), serviceCallback);
    }
    
    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @return the observable to the String object
     */
    private Observable<ServiceResponse<String>> getPendingCertificateSigningRequestWithServiceResponseAsync(String vaultBaseUrl, String certificateName) {
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
        return service.getPendingCertificateSigningRequest(certificateName, this.apiVersion(), this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                @Override
                public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<String> clientResponse = new ServiceResponse<String>(response.body().string(), response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }
}
