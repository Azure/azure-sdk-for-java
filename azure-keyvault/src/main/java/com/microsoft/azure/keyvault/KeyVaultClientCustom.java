package com.microsoft.azure.keyvault;

import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.models.*;
import com.microsoft.azure.keyvault.requests.*;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;
import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;

import java.util.List;
import java.util.Map;

public interface KeyVaultClientCustom extends KeyVaultClientBase {

    /**
     * @return the Retrofit instance.e
     */
    Retrofit retrofit();

    /**
     * @return the HTTP client.
     */
    OkHttpClient httpClient();

    /**
     * @return the adapter to a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper}.
     */
    SerializerAdapter<?> serializerAdapter();

    void initializeService();

    /**
     * @return the {@link RestClient} instance.
     */
    RestClient restClient();

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client. The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: Requires the keys/create permission.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     * @return the KeyBundle if successful.
     */
    KeyBundle createKey(CreateKeyRequest createKeyRequest);
    
    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client.
     * The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param keyName The name for the new key. The system will generate the version name for the new key.
     * @param kty The type of key to create. For valid key types, see JsonWebKeyType. Supported JsonWebKey key types (kty) for Elliptic Curve, RSA, HSM, Octet. Possible values include: 'EC', 'RSA', 'RSA-HSM', 'oct'
     * @param keySize The key size in bytes. For example, 1024 or 2048.
     * @param keyOps the List&lt;JsonWebKeyOperation&gt; value
     * @param keyAttributes the KeyAttributes value
     * @param tags Application specific metadata in the form of key-value pairs.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws KeyVaultErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the KeyBundle object if successful.
     */
    KeyBundle createKey(String vaultBaseUrl, String keyName, JsonWebKeyType kty, Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags);

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client. The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: Requires the keys/create permission.
     *
     * @param createKeyRequest the grouped properties for creating a key request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyBundle> createKeyAsync(CreateKeyRequest createKeyRequest, ServiceCallback<KeyBundle> serviceCallback);

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client.
     * The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param keyName The name for the new key. The system will generate the version name for the new key.
     * @param kty The type of key to create. For valid key types, see JsonWebKeyType. Supported JsonWebKey key types (kty) for Elliptic Curve, RSA, HSM, Octet. Possible values include: 'EC', 'RSA', 'RSA-HSM', 'oct'
     * @param keySize The key size in bytes. For example, 1024 or 2048.
     * @param keyOps the List&lt;JsonWebKeyOperation&gt; value
     * @param keyAttributes the KeyAttributes value
     * @param tags Application specific metadata in the form of key-value pairs.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyBundle> createKeyAsync(String vaultBaseUrl, String keyName, JsonWebKeyType kty, Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags, final ServiceCallback<KeyBundle> serviceCallback);

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client.
     * The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param keyName The name for the new key. The system will generate the version name for the new key.
     * @param kty The type of key to create. For valid key types, see JsonWebKeyType. Supported JsonWebKey key types (kty) for Elliptic Curve, RSA, HSM, Octet. Possible values include: 'EC', 'RSA', 'RSA-HSM', 'oct'
     * @param keySize The key size in bytes. For example, 1024 or 2048.
     * @param keyOps the List&lt;JsonWebKeyOperation&gt; value
     * @param keyAttributes the KeyAttributes value
     * @param tags Application specific metadata in the form of key-value pairs.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the KeyBundle object
     */
    Observable<KeyBundle> createKeyAsync(String vaultBaseUrl, String keyName, JsonWebKeyType kty, Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags);

    /**
     * Creates a new key, stores it, then returns key parameters and attributes to the client.
     * The create key operation can be used to create any key type in Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param keyName The name for the new key. The system will generate the version name for the new key.
     * @param kty The type of key to create. For valid key types, see JsonWebKeyType. Supported JsonWebKey key types (kty) for Elliptic Curve, RSA, HSM, Octet. Possible values include: 'EC', 'RSA', 'RSA-HSM', 'oct'
     * @param keySize The key size in bytes. For example, 1024 or 2048.
     * @param keyOps the List&lt;JsonWebKeyOperation&gt; value
     * @param keyAttributes the KeyAttributes value
     * @param tags Application specific metadata in the form of key-value pairs.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the KeyBundle object
     */
    Observable<ServiceResponse<KeyBundle>> createKeyWithServiceResponseAsync(String vaultBaseUrl, String keyName, JsonWebKeyType kty, Integer keySize, List<JsonWebKeyOperation> keyOps, KeyAttributes keyAttributes, Map<String, String> tags);

    /**
     * Imports an externally created key, stores it, and returns key parameters and attributes to the client. The import key operation may be used to import any key type into an Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: requires the keys/import permission.
     *
     * @param importKeyRequest the grouped properties for importing a key request
     *
     * @return the KeyBundle if successful.
     */
    KeyBundle importKey(ImportKeyRequest importKeyRequest);

    /**
     * Imports an externally created key, stores it, and returns key parameters and attributes to the client. The import key operation may be used to import any key type into an Azure Key Vault. If the named key already exists, Azure Key Vault creates a new version of the key. Authorization: requires the keys/import permission.
     *
     * @param importKeyRequest the grouped properties for importing a key request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyBundle> importKeyAsync(ImportKeyRequest importKeyRequest, final ServiceCallback<KeyBundle> serviceCallback);

    /**
     * The update key operation changes specified attributes of a stored key and can be applied to any key type and key version stored in Azure Key Vault. The cryptographic material of a key itself cannot be changed. In order to perform this operation, the key must already exist in the Key Vault. Authorization: requires the keys/update permission.
     *
     * @param updateKeyRequest the grouped properties for updating a key request
     *
     * @return the KeyBundle if successful.
     */
    KeyBundle updateKey(UpdateKeyRequest updateKeyRequest);

    /**
     * The update key operation changes specified attributes of a stored key and can be applied to any key type and key version stored in Azure Key Vault. The cryptographic material of a key itself cannot be changed. In order to perform this operation, the key must already exist in the Key Vault. Authorization: requires the keys/update permission.
     *
     * @param updateKeyRequest the grouped properties for updating a key request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyBundle> updateKeyAsync(UpdateKeyRequest updateKeyRequest, final ServiceCallback<KeyBundle> serviceCallback);

    /**
     * Gets the  part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param keyIdentifier The full key identifier
     *
     * @return the KeyBundle if successful.
     */
    KeyBundle getKey(String keyIdentifier);

    /**
     * Gets the  part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param keyIdentifier The full key identifier
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyBundle> getKeyAsync(String keyIdentifier, final ServiceCallback<KeyBundle> serviceCallback);

    /**
     * Gets the  part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @return the KeyBundle if successful.
     */
    KeyBundle getKey(String vaultBaseUrl, String keyName);

    /**
     * Gets the  part of a stored key. The get key operation is applicable to all key types. If the requested key is symmetric, then no key material is released in the response. Authorization: Requires the keys/get permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyBundle> getKeyAsync(String vaultBaseUrl, String keyName, final ServiceCallback<KeyBundle> serviceCallback) ;

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     *
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    PagedList<KeyItem> listKeyVersions(final String vaultBaseUrl, final String keyName);

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<KeyItem>> listKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final ListOperationCallback<KeyItem> serviceCallback);

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    PagedList<KeyItem> listKeyVersions(final String vaultBaseUrl, final String keyName, final Integer maxresults);

    /**
     * Retrieves a list of individual key versions with the same key name. The full key identifier, attributes, and tags are provided in the response. Authorization: Requires the keys/list permission.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param keyName The name of the key
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<KeyItem>> listKeyVersionsAsync(final String vaultBaseUrl, final String keyName, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback);

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     *
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    PagedList<KeyItem> listKeys(final String vaultBaseUrl);

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<KeyItem>> listKeysAsync(final String vaultBaseUrl, final ListOperationCallback<KeyItem> serviceCallback);
    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;KeyItem&gt; if successful.
     */
    PagedList<KeyItem> listKeys(final String vaultBaseUrl, final Integer maxresults);

    /**
     * List keys in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<KeyItem>> listKeysAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<KeyItem> serviceCallback);

    /**
     * Wraps a symmetric key using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be wrapped
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyOperationResult> wrapKeyAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback);

    /**
     * Unwraps a symmetric key using the specified key in the vault that has initially been used for wrapping the key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be unwrapped
     *
     * @return the KeyOperationResult if successful.
     */
    KeyOperationResult unwrapKey(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value);

    /**
     * Unwraps a symmetric key using the specified key in the vault that has initially been used for wrapping the key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be unwrapped
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyOperationResult> unwrapKeyAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback);

    /**
     * Wraps a symmetric key using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the key to be wrapped
     *
     * @return the KeyOperationResult if successful.
     */
    KeyOperationResult wrapKey(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value);

    /**
     * Encrypts an arbitrary sequence of bytes using an encryption key that is stored in a key vault.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be encrypted
     *
     * @return the KeyOperationResult if successful.
     */
    KeyOperationResult encrypt(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value);

    /**
     * Encrypts an arbitrary sequence of bytes using an encryption key that is stored in a key vault.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be encrypted
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyOperationResult> encryptAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback);

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be decrypted
     *
     * @return the KeyOperationResult if successful.
     */
    KeyOperationResult decrypt(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value);

    /**
     * Decrypts a single block of encrypted data.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be decrypted
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyOperationResult> decryptAsync(String keyIdentifier, JsonWebKeyEncryptionAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback);

    /**
     * Creates a signature from a digest using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be signed
     *
     * @return the KeyOperationResult if successful.
     */
    KeyOperationResult sign(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] value);

    /**
     * Creates a signature from a digest using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm algorithm identifier
     * @param value the content to be signed
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyOperationResult> signAsync(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] value, final ServiceCallback<KeyOperationResult> serviceCallback);

    /**
     * Verifies a signature using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm The signing/verification algorithm. For more information on possible algorithm types, see JsonWebKeySignatureAlgorithm.
     * @param digest The digest used for signing
     * @param signature The signature to be verified
     *
     * @return the KeyVerifyResult if successful.
     */
    KeyVerifyResult verify(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] digest, byte[] signature);

    /**
     * Verifies a signature using the specified key.
     *
     * @param keyIdentifier The full key identifier
     * @param algorithm The signing/verification algorithm. For more information on possible algorithm types, see JsonWebKeySignatureAlgorithm.
     * @param digest The digest used for signing
     * @param signature The signature to be verified
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyVerifyResult> verifyAsync(String keyIdentifier, JsonWebKeySignatureAlgorithm algorithm, byte[] digest, byte[] signature, final ServiceCallback<KeyVerifyResult> serviceCallback);

    /**
     * Sets a secret in the specified vault.
     *
     * @param setSecretRequest the grouped properties for setting a secret request
     *
     * @return the SecretBundle if successful.
     */
    SecretBundle setSecret(SetSecretRequest setSecretRequest);

    /**
     * Sets a secret in the specified vault.
     *
     * @param setSecretRequest the grouped properties for setting a secret request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<SecretBundle> setSecretAsync(SetSecretRequest setSecretRequest, final ServiceCallback<SecretBundle> serviceCallback);


    /**
     * Updates the attributes associated with a specified secret in a given key vault.
     *
     * @param updateSecretRequest the grouped properties for updating a secret request
     *
     * @return the SecretBundle if successful.
     */
    SecretBundle updateSecret(UpdateSecretRequest updateSecretRequest);

    /**
     * Updates the attributes associated with a specified secret in a given key vault.
     *
     * @param updateSecretRequest the grouped properties for updating a secret request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<SecretBundle> updateSecretAsync(UpdateSecretRequest updateSecretRequest, final ServiceCallback<SecretBundle> serviceCallback);
    /**
     * Get a specified secret from a given key vault.
     *
     * @param secretIdentifier The URL for the secret.
     *
     * @return the SecretBundle if successful.
     */
    SecretBundle getSecret(String secretIdentifier);
    /**
     * Get a specified secret from a given key vault.
     *
     * @param secretIdentifier The URL for the secret.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<SecretBundle> getSecretAsync(String secretIdentifier, final ServiceCallback<SecretBundle> serviceCallback);

    /**
     * Get a specified secret from a given key vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     *
     * @return the SecretBundle if successful.
     */
    SecretBundle getSecret(String vaultBaseUrl, String secretName);

    /**
     * Get a specified secret from a given key vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */

    ServiceFuture<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<SecretBundle> serviceCallback);

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     *
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    PagedList<SecretItem> listSecrets(final String vaultBaseUrl);
    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<SecretItem>> listSecretsAsync(final String vaultBaseUrl, final ListOperationCallback<SecretItem> serviceCallback) ;
    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    PagedList<SecretItem> listSecrets(final String vaultBaseUrl, final Integer maxresults);

    /**
     * List secrets in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<SecretItem>> listSecretsAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback);

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     *
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    PagedList<SecretItem> listSecretVersions(final String vaultBaseUrl, final String secretName);

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<SecretItem>> listSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final ListOperationCallback<SecretItem> serviceCallback);
    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;SecretItem&gt; if successful.
     */
    PagedList<SecretItem> listSecretVersions(final String vaultBaseUrl, final String secretName, final Integer maxresults);

    /**
     * List the versions of the specified secret.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param secretName The name of the secret in the given vault
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<SecretItem>> listSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback);

    /**
     * List certificates in a specified key vault.
     * The GetCertificates operation returns the set of certificates resources in the specified key vault. This operation requires the certificates/list permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws KeyVaultErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PagedList&lt;CertificateItem&gt; object if successful.
     */
    PagedList<CertificateItem> getCertificates(final String vaultBaseUrl, final Integer maxresults);
    
    /**
     * List certificates in a specified key vault.
     * The GetCertificates operation returns the set of certificates resources in the specified key vault. This operation requires the certificates/list permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateItem>> getCertificatesAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback);
    
    /**
     * List certificates in a specified key vault.
     * The GetCertificates operation returns the set of certificates resources in the specified key vault. This operation requires the certificates/list permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;CertificateItem&gt; object
     */
    Observable<Page<CertificateItem>> getCertificatesAsync(final String vaultBaseUrl, final Integer maxresults);
    
    /**
     * List certificates in a specified key vault.
     * The GetCertificates operation returns the set of certificates resources in the specified key vault. This operation requires the certificates/list permission.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;CertificateItem&gt; object
     */
    Observable<ServiceResponse<Page<CertificateItem>>> getCertificatesWithServiceResponseAsync(final String vaultBaseUrl, final Integer maxresults);

    
    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     *
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    PagedList<CertificateItem> listCertificates(final String vaultBaseUrl) ;

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateItem>> listCertificatesAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateItem> serviceCallback);
    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    PagedList<CertificateItem> listCertificates(final String vaultBaseUrl, final Integer maxresults);

    /**
     * List certificates in the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateItem>> listCertificatesAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback);


    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     *
     * @return the PagedList&lt;CertificateIssuerItem&gt; if successful.
     */
    PagedList<CertificateIssuerItem> listCertificateIssuers(final String vaultBaseUrl);

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateIssuerItem>> listCertificateIssuersAsync(final String vaultBaseUrl, final ListOperationCallback<CertificateIssuerItem> serviceCallback);
    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;CertificateIssuerItem&gt; if successful.
     */
    PagedList<CertificateIssuerItem> listCertificateIssuers(final String vaultBaseUrl, final Integer maxresults);

    /**
     * List certificate issuers for the specified vault.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateIssuerItem>> listCertificateIssuersAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<CertificateIssuerItem> serviceCallback);

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param setCertificateIssuerRequest the grouped properties for setting a certificate issuer request
     *
     * @return the IssuerBundle if successful.
     */
    IssuerBundle setCertificateIssuer(SetCertificateIssuerRequest setCertificateIssuerRequest);

    /**
     * Sets the certificate contacts for the specified vault.
     *
     * @param setCertificateIssuerRequest the grouped properties for setting a certificate issuer request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<IssuerBundle> setCertificateIssuerAsync(SetCertificateIssuerRequest setCertificateIssuerRequest, final ServiceCallback<IssuerBundle> serviceCallback);

    /**
     * Updates the specified certificate issuer.
     *
     * @param updateCertificateIssuerRequest the grouped properties for updating a certificate issuer request
     *
     * @return the IssuerBundle if successful.
     */
    IssuerBundle updateCertificateIssuer(UpdateCertificateIssuerRequest updateCertificateIssuerRequest);

    /**
     * Updates the specified certificate issuer.
     *
     * @param updateCertificateIssuerRequest the grouped properties for updating a certificate issuer request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<IssuerBundle> updateCertificateIssuerAsync(UpdateCertificateIssuerRequest updateCertificateIssuerRequest, final ServiceCallback<IssuerBundle> serviceCallback);

    /**
     * Creates a new certificate version. If this is the first version, the certificate resource is created.
     *
     * @param createCertificateRequest the grouped properties for creating a certificate request
     *
     * @return the CertificateOperation if successful.
     */
    CertificateOperation createCertificate(CreateCertificateRequest createCertificateRequest);

    /**
     * Creates a new certificate version. If this is the first version, the certificate resource is created.
     *
     * @param createCertificateRequest the grouped properties for creating a certificate request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificateOperation> createCertificateAsync(CreateCertificateRequest createCertificateRequest, final ServiceCallback<CertificateOperation> serviceCallback);

    /**
     * Imports a certificate into the specified vault.
     *
     * @param importCertificateRequest the grouped properties for importing a certificate request
     *
     * @return the CertificateBundle if successful.
     */
    CertificateBundle importCertificate(ImportCertificateRequest importCertificateRequest);

    /**
     * Imports a certificate into the specified vault.
     *
     * @param importCertificateRequest the grouped properties for importing a certificate request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificateBundle> importCertificateAsync(ImportCertificateRequest importCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback);

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     *
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    PagedList<CertificateItem> listCertificateVersions(final String vaultBaseUrl, final String certificateName);

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateItem>> listCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final ListOperationCallback<CertificateItem> serviceCallback);
    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     *
     * @return the PagedList&lt;CertificateItem&gt; if successful.
     */
    PagedList<CertificateItem> listCertificateVersions(final String vaultBaseUrl, final String certificateName, final Integer maxresults);

    /**
     * List the versions of a certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<CertificateItem>> listCertificateVersionsAsync(final String vaultBaseUrl, final String certificateName, final Integer maxresults, final ListOperationCallback<CertificateItem> serviceCallback);

    /**
     * Updates the policy for a certificate. Set appropriate members in the certificatePolicy that must be updated. Leave others as null.
     *
     * @param updateCertificatePolicyRequest the grouped properties for updating a certificate policy request
     *
     * @return the CertificatePolicy if successful.
     */
    CertificatePolicy updateCertificatePolicy(UpdateCertificatePolicyRequest updateCertificatePolicyRequest);

    /**
     * Updates the policy for a certificate. Set appropriate members in the certificatePolicy that must be updated. Leave others as null.
     *
     * @param updateCertificatePolicyRequest the grouped properties for updating a certificate policy request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificatePolicy> updateCertificatePolicyAsync(UpdateCertificatePolicyRequest updateCertificatePolicyRequest, final ServiceCallback<CertificatePolicy> serviceCallback);

    /**
     * Updates the attributes associated with the specified certificate.
     *
     * @param updateCertificateRequest the grouped properties for updating a certificate request
     *
     * @return the CertificateBundle if successful.
     */
    CertificateBundle updateCertificate(UpdateCertificateRequest updateCertificateRequest);
    
    /**
     * Updates the attributes associated with the specified certificate.
     *
     * @param updateCertificateRequest the grouped properties for updating a certificate request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificateBundle> updateCertificateAsync(UpdateCertificateRequest updateCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback);

    /**
     * Gets information about a specified certificate.
     *
     * @param certificateIdentifier The certificate identifier
     *
     * @return the CertificateBundle if successful.
     */
    CertificateBundle getCertificate(String certificateIdentifier);

    /**
     * Gets information about a specified certificate.
     *
     * @param certificateIdentifier The certificate identifier
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificateBundle> getCertificateAsync(String certificateIdentifier, final ServiceCallback<CertificateBundle> serviceCallback);

    /**
     * Gets information about a specified certificate.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate in the given vault
     *
     * @return the CertificateBundle if successful.
     */
    CertificateBundle getCertificate(String vaultBaseUrl, String certificateName);

    /**
     * Updates a certificate operation.
     *
     * @param updateCertificateOperationRequest the grouped properties for updating a certificate operation request
     *
     * @return the CertificateOperation if successful.
     */
    CertificateOperation updateCertificateOperation(UpdateCertificateOperationRequest updateCertificateOperationRequest);

    /**
     * Updates a certificate operation.
     *
     * @param updateCertificateOperationRequest the grouped properties for updating a certificate operation request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificateOperation> updateCertificateOperationAsync(UpdateCertificateOperationRequest updateCertificateOperationRequest, final ServiceCallback<CertificateOperation> serviceCallback);



    /**
     * Merges a certificate or a certificate chain with a key pair existing on the server.
     *
     * @param mergeCertificateRequest the grouped properties for merging a certificate request
     *
     * @return the CertificateBundle if successful.
     */
    CertificateBundle mergeCertificate(MergeCertificateRequest mergeCertificateRequest);

    /**
     * Merges a certificate or a certificate chain with a key pair existing on the server.
     *
     * @param mergeCertificateRequest the grouped properties for merging a certificate request
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<CertificateBundle> mergeCertificateAsync(MergeCertificateRequest mergeCertificateRequest, final ServiceCallback<CertificateBundle> serviceCallback);


    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     *
     * @return the String if successful.
     */
    String getPendingCertificateSigningRequest(String vaultBaseUrl, String certificateName);
    /**
     * Gets the pending certificate signing request response.
     *
     * @param vaultBaseUrl The vault name, e.g. https://myvault.vault.azure.net
     * @param certificateName The name of the certificate
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<String> getPendingCertificateSigningRequestAsync(String vaultBaseUrl, String certificateName, final ServiceCallback<String> serviceCallback);

    /**
     * Lists the deleted certificates in the specified vault currently available for recovery.
     * The GetDeletedCertificates operation retrieves the certificates in the current vault which are in a deleted state and ready for recovery or purging. This operation includes deletion-specific information. This operation requires the certificates/get/list permission. This operation can only be enabled on soft-delete enabled vaults.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws KeyVaultErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PagedList&lt;DeletedCertificateItem&gt; object if successful.
     */
    PagedList<DeletedCertificateItem> getDeletedCertificates(final String vaultBaseUrl, final Integer maxresults);
    
    /**
     * Lists the deleted certificates in the specified vault currently available for recovery.
     * The GetDeletedCertificates operation retrieves the certificates in the current vault which are in a deleted state and ready for recovery or purging. This operation includes deletion-specific information. This operation requires the certificates/get/list permission. This operation can only be enabled on soft-delete enabled vaults.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<List<DeletedCertificateItem>> getDeletedCertificatesAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<DeletedCertificateItem> serviceCallback);

    /**
     * Lists the deleted certificates in the specified vault currently available for recovery.
     * The GetDeletedCertificates operation retrieves the certificates in the current vault which are in a deleted state and ready for recovery or purging. This operation includes deletion-specific information. This operation requires the certificates/get/list permission. This operation can only be enabled on soft-delete enabled vaults.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;DeletedCertificateItem&gt; object
     */
    Observable<Page<DeletedCertificateItem>> getDeletedCertificatesAsync(final String vaultBaseUrl, final Integer maxresults);
    
    /**
     * Lists the deleted certificates in the specified vault currently available for recovery.
     * The GetDeletedCertificates operation retrieves the certificates in the current vault which are in a deleted state and ready for recovery or purging. This operation includes deletion-specific information. This operation requires the certificates/get/list permission. This operation can only be enabled on soft-delete enabled vaults.
     *
     * @param vaultBaseUrl The vault name, for example https://myvault.vault.azure.net.
     * @param maxresults Maximum number of results to return in a page. If not specified the service will return up to 25 results.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;DeletedCertificateItem&gt; object
     */
    Observable<ServiceResponse<Page<DeletedCertificateItem>>> getDeletedCertificatesWithServiceResponseAsync(final String vaultBaseUrl, final Integer maxresults);

}