// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import com.azure.v2.security.keyvault.keys.KeyClient;
import com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyClientImpl;
import com.azure.v2.security.keyvault.keys.cryptography.implementation.LocalKeyCryptographyClient;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.v2.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.RetryUtils;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.createLocalClient;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.retrieveJwkAndCreateLocalClient;

/**
 * This class provides methods to perform cryptographic operations using asymmetric and symmetric keys. The client
 * supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the configured key.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault or Managed HSM service, you will need to create an instance of the
 * {@link KeyClient} class, an Azure Key Vault or Managed HSM endpoint and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named {@code DefaultAzureCredential} for
 * authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Cryptography Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link CryptographyClient}, using the
 * {@link CryptographyClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 * <pre>
 * CryptographyClient cryptographyClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id-from-keyvault&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withJsonWebKey -->
 * <pre>
 * CryptographyClient cryptographyClient = new CryptographyClientBuilder&#40;&#41;
 *     .jsonWebKey&#40;myJsonWebKey&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation.withJsonWebKey -->
 *
 * <p>When a {@link CryptographyClient} gets created using a {@code Azure Key Vault key identifier}, the first time a
 * cryptographic operation is attempted, the client will attempt to retrieve the key material from the service, cache
 * it, and perform all future cryptographic operations locally, deferring to the service when that's not possible. If
 * key retrieval and caching fails because of a non-retryable error, the client will not make any further attempts and
 * will fall back to performing all cryptographic operations on the service side. Conversely, when a
 * {@link CryptographyClient} created using a {@link JsonWebKey JSON Web Key}, all cryptographic operations will be
 * performed locally.</p>
 *
 * <br>
 * <hr>
 *
 * <h2>Encrypt Data</h2>
 * The {@link CryptographyClient} can be used to encrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to encrypt data using the
 * {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[])} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
 * <pre>
 * byte[] plaintext = new byte[100];
 * new Random&#40;0x1234567L&#41;.nextBytes&#40;plaintext&#41;;
 *
 * EncryptResult encryptResult = cryptographyClient.encrypt&#40;EncryptionAlgorithm.RSA_OAEP, plaintext&#41;;
 *
 * System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
 *     encryptResult.getCipherText&#40;&#41;.length, encryptResult.getAlgorithm&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
 *
 * <br>
 * <hr>
 *
 * <h2>Decrypt Data</h2>
 * The {@link CryptographyClient} can be used to decrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to decrypt data using the
 * {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[])} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
 * <pre>
 * byte[] ciphertext = new byte[100];
 * new Random&#40;0x1234567L&#41;.nextBytes&#40;ciphertext&#41;;
 *
 * DecryptResult decryptResult = cryptographyClient.decrypt&#40;EncryptionAlgorithm.RSA_OAEP, ciphertext&#41;;
 *
 * System.out.printf&#40;&quot;Received decrypted content of length: %d.%n&quot;, decryptResult.getPlainText&#40;&#41;.length&#41;;
 * </pre>
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
 *
 * @see com.azure.v2.security.keyvault.keys.cryptography
 * @see CryptographyClientBuilder
 */
@ServiceClient(
    builder = CryptographyClientBuilder.class,
    serviceInterfaces = { KeyClientImpl.KeyClientService.class, SecretMinClientImpl.SecretMinClientService.class })
public class CryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClient.class);
    private final CryptographyClientImpl clientImpl;

    private volatile boolean skipLocalClientCreation;
    private volatile LocalKeyCryptographyClient localKeyCryptographyClient;

    final String keyId;

    /**
     * Creates an instance of {@link CryptographyClient}.
     *
     * @param clientImpl The implementation client.
     * @param disableKeyCaching Indicates if local key caching should be disabled and all cryptographic operations
     * deferred to the service.
     */
    CryptographyClient(CryptographyClientImpl clientImpl, boolean disableKeyCaching) {
        this.clientImpl = clientImpl;
        this.keyId = clientImpl.getKeyId();
        this.skipLocalClientCreation = disableKeyCaching;
    }

    /**
     * Creates an instance of {@link CryptographyClient} that uses a {@link JsonWebKey} to perform local cryptography
     * operations.
     *
     * @param jsonWebKey The JSON Web Key to use for local cryptography operations.
     *
     * @throws NullPointerException If {@code jsonWebKey} is {@code null}.
     * @throws IllegalArgumentException If the provided {@code jsonWebKey} is not valid, or if either of the key
     * operations or key type properties is not configured.
     */
    CryptographyClient(JsonWebKey jsonWebKey) {
        Objects.requireNonNull(jsonWebKey, "The JSON Web Key is required.");

        if (!jsonWebKey.isValid()) {
            throw LOGGER.throwableAtError().log("The JSON Web Key is not valid.", IllegalArgumentException::new);
        }

        if (jsonWebKey.getKeyOps() == null) {
            throw LOGGER.throwableAtError()
                .log("The JSON Web Key's key operations property is not configured.", IllegalArgumentException::new);
        }

        if (jsonWebKey.getKeyType() == null) {
            throw LOGGER.throwableAtError()
                .log("The JSON Web Key's key type property is not configured.", IllegalArgumentException::new);
        }

        this.clientImpl = null;
        this.keyId = jsonWebKey.getId();
        this.localKeyCryptographyClient = createLocalClient(jsonWebKey, null, LOGGER);
    }

    /**
     * Gets the public part of a given key, as well as the key's properties. The get key operation is applicable to all
     * key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the key the client is configured with from the service and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKey -->
     * <pre>
     * KeyVaultKey key = cryptographyClient.getKey&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Key returned with name: %s and id: %s.%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKey -->
     *
     * @return The requested key.
     *
     * @throws HttpResponseException If the key this client is configured with doesn't exist in the key vault.
     * @throws UnsupportedOperationException If operating in local-only mode (using a client created using a
     * {@link JsonWebKey} instance).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey() {
        return getKeyWithResponse(RequestContext.none()).getValue();
    }

    /**
     * Gets the public part of a specific version of a given key, as well as the key's properties. The get key operation
     * is applicable to all key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the key the client is configured with from the service. Prints out details of the response returned by
     * the service and the requested key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * KeyVaultKey keyWithVersion = cryptographyClient.getKeyWithResponse&#40;requestContext&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Key is returned with name: %s and id %s.%n&quot;, keyWithVersion.getName&#40;&#41;,
     *     keyWithVersion.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#RequestContext -->
     *
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested key.
     *
     * @throws HttpResponseException If the key this client is configured with doesn't exist in the key vault.
     * @throws UnsupportedOperationException If operating in local-only mode (using a client created using a
     * {@link JsonWebKey} instance).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(RequestContext requestContext) {
        if (clientImpl != null) {
            return clientImpl.getKeyWithResponse(requestContext);
        } else {
            throw LOGGER.throwableAtError()
                .log("Operation not supported when operating in local-only mode.", UnsupportedOperationException::new);
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for encrypting the specified {@code plaintext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Encrypts the content and prints out the result's details'.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
     * <pre>
     * byte[] plaintext = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;plaintext&#41;;
     *
     * EncryptResult encryptResult = cryptographyClient.encrypt&#40;EncryptionAlgorithm.RSA_OAEP, plaintext&#41;;
     *
     * System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
     *     encryptResult.getCipherText&#40;&#41;.length, encryptResult.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @return A result object whose {@link EncryptResult#getCipherText() ciphertext} contains the encrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.encrypt(algorithm, plaintext, RequestContext.none());
        } else {
            return clientImpl.encrypt(algorithm, plaintext);
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for encrypting the specified {@code plaintext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Encrypts the content and prints out the result's details'.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-RequestContext -->
     * <pre>
     * byte[] myPlaintext = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;myPlaintext&#41;;
     * byte[] iv = &#123;
     *     &#40;byte&#41; 0x1a, &#40;byte&#41; 0xf3, &#40;byte&#41; 0x8c, &#40;byte&#41; 0x2d, &#40;byte&#41; 0xc2, &#40;byte&#41; 0xb9, &#40;byte&#41; 0x6f, &#40;byte&#41; 0xfd,
     *     &#40;byte&#41; 0xd8, &#40;byte&#41; 0x66, &#40;byte&#41; 0x94, &#40;byte&#41; 0x09, &#40;byte&#41; 0x23, &#40;byte&#41; 0x41, &#40;byte&#41; 0xbc, &#40;byte&#41; 0x04
     * &#125;;
     *
     * EncryptParameters encryptParameters = EncryptParameters.createA128CbcParameters&#40;myPlaintext, iv&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * EncryptResult encryptedResult = cryptographyClient.encrypt&#40;encryptParameters, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
     *     encryptedResult.getCipherText&#40;&#41;.length, encryptedResult.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-RequestContext -->
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object whose {@link EncryptResult#getCipherText() ciphertext} contains the encrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EncryptResult encrypt(EncryptParameters encryptParameters, RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.encrypt(encryptParameters, requestContext);
        } else {
            return clientImpl.encrypt(encryptParameters, requestContext);
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for decrypting the specified {@code ciphertext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Decrypts the encrypted content prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
     * <pre>
     * byte[] ciphertext = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;ciphertext&#41;;
     *
     * DecryptResult decryptResult = cryptographyClient.decrypt&#40;EncryptionAlgorithm.RSA_OAEP, ciphertext&#41;;
     *
     * System.out.printf&#40;&quot;Received decrypted content of length: %d.%n&quot;, decryptResult.getPlainText&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted. Microsoft recommends you not use CBC without first ensuring the
     * integrity of the ciphertext using an HMAC, for example.
     * See <a href="https://docs.microsoft.com/dotnet/standard/security/vulnerabilities-cbc-mode">Timing vulnerabilities
     * with CBC-mode symmetric decryption using padding</a> for more information.
     * @return A result object whose {@link DecryptResult#getPlainText() plaintext} contains the decrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.decrypt(algorithm, ciphertext, RequestContext.none());
        } else {
            return clientImpl.decrypt(algorithm, ciphertext, RequestContext.none());
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for decrypting the specified {@code ciphertext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Decrypts the encrypted content prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-RequestContext -->
     * <pre>
     * byte[] myCiphertext = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;myCiphertext&#41;;
     * byte[] iv = &#123;
     *     &#40;byte&#41; 0x1a, &#40;byte&#41; 0xf3, &#40;byte&#41; 0x8c, &#40;byte&#41; 0x2d, &#40;byte&#41; 0xc2, &#40;byte&#41; 0xb9, &#40;byte&#41; 0x6f, &#40;byte&#41; 0xfd,
     *     &#40;byte&#41; 0xd8, &#40;byte&#41; 0x66, &#40;byte&#41; 0x94, &#40;byte&#41; 0x09, &#40;byte&#41; 0x23, &#40;byte&#41; 0x41, &#40;byte&#41; 0xbc, &#40;byte&#41; 0x04
     * &#125;;
     *
     * DecryptParameters decryptParameters = DecryptParameters.createA128CbcParameters&#40;myCiphertext, iv&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * DecryptResult decryptedResult = cryptographyClient.decrypt&#40;decryptParameters, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received decrypted content of length: %d.%n&quot;, decryptedResult.getPlainText&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-RequestContext -->
     *
     * @param decryptParameters The parameters to use in the decryption operation. Microsoft recommends you not use CBC
     * without first ensuring the integrity of the ciphertext using an HMAC, for example.
     * See <a href="https://docs.microsoft.com/dotnet/standard/security/vulnerabilities-cbc-mode">Timing vulnerabilities
     * with CBC-mode symmetric decryption using padding</a> for more information.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object whose {@link DecryptResult#getPlainText() plaintext} contains the decrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DecryptResult decrypt(DecryptParameters decryptParameters, RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.decrypt(decryptParameters, requestContext);
        } else {
            return clientImpl.decrypt(decryptParameters, requestContext);
        }
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte -->
     * <pre>
     * byte[] data = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;data&#41;;
     * MessageDigest md = MessageDigest.getInstance&#40;&quot;SHA-256&quot;&#41;;
     * md.update&#40;data&#41;;
     * byte[] digest = md.digest&#40;&#41;;
     *
     * SignResult signResult = cryptographyClient.sign&#40;SignatureAlgorithm.ES256, digest&#41;;
     *
     * System.out.printf&#40;&quot;Received signature of length: %d, with algorithm: %s.%n&quot;, signResult.getSignature&#40;&#41;.length,
     *     signResult.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which the signature is to be created.
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code digest} is {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.sign(algorithm, digest, RequestContext.none());
        } else {
            return clientImpl.sign(algorithm, digest, RequestContext.none());
        }
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-RequestContext -->
     * <pre>
     * byte[] dataToVerify = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;dataToVerify&#41;;
     * MessageDigest myMessageDigest = MessageDigest.getInstance&#40;&quot;SHA-256&quot;&#41;;
     * myMessageDigest.update&#40;dataToVerify&#41;;
     * byte[] digestContent = myMessageDigest.digest&#40;&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * SignResult signResponse = cryptographyClient.sign&#40;SignatureAlgorithm.ES256, digestContent, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received signature of length: %d, with algorithm: %s.%n&quot;, signResponse.getSignature&#40;&#41;.length,
     *     signResponse.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-RequestContext -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If any of the provided {@code algorithm}, {@code digest}, or {@code signature} is
     * {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.sign(algorithm, digest, requestContext);
        } else {
            return clientImpl.sign(algorithm, digest, requestContext);
        }
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature. This operation
     * requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the specified digest prints out the verification details when a response has
     * been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte -->
     * <pre>
     * byte[] myData = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;myData&#41;;
     * MessageDigest messageDigest = MessageDigest.getInstance&#40;&quot;SHA-256&quot;&#41;;
     * messageDigest.update&#40;myData&#41;;
     * byte[] myDigest = messageDigest.digest&#40;&#41;;
     *
     * &#47;&#47; A signature can be obtained from the SignResult returned by the CryptographyClient.sign&#40;&#41; operation.
     * VerifyResult verifyResult = cryptographyClient.verify&#40;SignatureAlgorithm.ES256, myDigest, signature&#41;;
     *
     * System.out.printf&#40;&quot;Verification status: %s.%n&quot;, verifyResult.isValid&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If any of the provided {@code algorithm}, {@code digest}, or {@code signature} is
     * {@code null}.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return verify(algorithm, digest, signature, RequestContext.none());
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature. This operation
     * requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the specified digest prints out the verification details when a response has
     * been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-RequestContext -->
     * <pre>
     * byte[] dataBytes = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;dataBytes&#41;;
     * MessageDigest msgDigest = MessageDigest.getInstance&#40;&quot;SHA-256&quot;&#41;;
     * msgDigest.update&#40;dataBytes&#41;;
     * byte[] digestBytes = msgDigest.digest&#40;&#41;;
     *
     * &#47;&#47; A signature can be obtained from the SignResult returned by the CryptographyClient.sign&#40;&#41; operation.
     * VerifyResult verifyResponse =
     *     cryptographyClient.verify&#40;SignatureAlgorithm.ES256, digestBytes, signatureBytes, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Verification status: %s.%n&quot;, verifyResponse.isValid&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-RequestContext -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws HttpResponseException If the key cannot be found for verifying.
     * @throws NullPointerException If any of the provided {@code algorithm}, {@code digest}, or {@code signature} is
     * {@code null}.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.verify(algorithm, digest, signature, requestContext);
        } else {
            return clientImpl.verify(algorithm, digest, signature, requestContext);
        }
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the {@code keys/wrapKey} permission for non-local
     * operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Wraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte -->
     * <pre>
     * byte[] key = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;key&#41;;
     *
     * WrapResult wrapResult = cryptographyClient.wrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, key&#41;;
     *
     * System.out.printf&#40;&quot;Received encrypted key of length: %d, with algorithm: %s.%n&quot;,
     *     wrapResult.getEncryptedKey&#40;&#41;.length, wrapResult.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     * @return A result object whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped key result.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code key} is {@code null}.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKey(algorithm, key, RequestContext.none());
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the {@code keys/wrapKey} permission for non-local
     * operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Wraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-RequestContext -->
     * <pre>
     * byte[] keyToWrap = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;keyToWrap&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * WrapResult keyWrapResult = cryptographyClient.wrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, keyToWrap, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Received encrypted key of length: %d, with algorithm: %s.%n&quot;,
     *     keyWrapResult.getEncryptedKey&#40;&#41;.length, keyWrapResult.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-RequestContext -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped key result.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code key} is {@code null}.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.wrapKey(algorithm, key, requestContext);
        } else {
            return clientImpl.wrapKey(algorithm, key, requestContext);
        }
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation
     * is the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap.
     * This operation requires the {@code keys/unwrapKey} permission for non-local operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Unwraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte -->
     * <pre>
     * byte[] keyContent = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;keyContent&#41;;
     *
     * WrapResult wrapKeyResult = cryptographyClient.wrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, keyContent&#41;;
     * UnwrapResult unwrapResult =
     *     cryptographyClient.unwrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, wrapKeyResult.getEncryptedKey&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Received key of length %d&quot;, unwrapResult.getKey&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @return A result object whose {@link UnwrapResult#getKey() decrypted key} contains the unwrapped key result.
     *
     * @throws HttpResponseException If the key cannot be found for wrap operation.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code encryptedKey} is {@code null}.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return unwrapKey(algorithm, encryptedKey, RequestContext.none());
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation
     * is the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap.
     * This operation requires the {@code keys/unwrapKey} permission for non-local operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Unwraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-RequestContext -->
     * <pre>
     * byte[] keyContentToWrap = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;keyContentToWrap&#41;;
     *
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * WrapResult wrapKeyContentResult =
     *     cryptographyClient.wrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, keyContentToWrap, reqContext&#41;;
     * UnwrapResult unwrapKeyResponse =
     *     cryptographyClient.unwrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, wrapKeyContentResult.getEncryptedKey&#40;&#41;, reqContext&#41;;
     *
     * System.out.printf&#40;&quot;Received key of length %d&quot;, unwrapKeyResponse.getKey&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-RequestContext -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object whose {@link UnwrapResult#getKey() decrypted key} contains the unwrapped key result.
     *
     * @throws HttpResponseException If the key cannot be found for wrap operation.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code encryptedKey} is {@code null}.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.unwrapKey(algorithm, encryptedKey, requestContext);
        } else {
            return clientImpl.unwrapKey(algorithm, encryptedKey, requestContext);
        }
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte -->
     * <pre>
     * byte[] data = new byte[32];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;data&#41;;
     *
     * SignResult signResult = cryptographyClient.sign&#40;SignatureAlgorithm.ES256, data&#41;;
     *
     * System.out.printf&#40;&quot;Received signature of length: %d, with algorithm: %s.%n&quot;, signResult.getSignature&#40;&#41;.length,
     *     signResult.getAlgorithm&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code data} is {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data) {
        return signData(algorithm, data, RequestContext.none());
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-RequestContext -->
     * <pre>
     * byte[] plainTextData = new byte[32];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;plainTextData&#41;;
     *
     * SignResult signingResult = cryptographyClient.sign&#40;SignatureAlgorithm.ES256, plainTextData&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;someKey&quot;, &quot;someValue&quot;&#41;
     *     .build&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Received signature of length: %d, with algorithm: %s.%n&quot;,
     *     signingResult.getSignature&#40;&#41;.length, requestContext&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-RequestContext -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code data} is {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.signData(algorithm, data, requestContext);
        } else {
            return clientImpl.signData(algorithm, data, requestContext);
        }
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys. In case of asymmetric keys public portion of the key is used to verify the signature.
     * This operation requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the raw data prints out the verification details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte -->
     * <pre>
     * byte[] myData = new byte[32];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;myData&#41;;
     *
     * &#47;&#47; A signature can be obtained from the SignResult returned by the CryptographyClient.sign&#40;&#41; operation.
     * VerifyResult verifyResult = cryptographyClient.verify&#40;SignatureAlgorithm.ES256, myData, signature&#41;;
     *
     * System.out.printf&#40;&quot;Verification status: %s.%n&quot;, verifyResult.isValid&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws HttpResponseException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return verifyData(algorithm, data, signature, RequestContext.none());
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys. In case of asymmetric keys public portion of the key is used to verify the signature.
     * This operation requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the raw data prints out the verification details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-RequestContext -->
     * <pre>
     * byte[] dataToVerify = new byte[32];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;dataToVerify&#41;;
     *
     * &#47;&#47; A signature can be obtained from the SignResult returned by the CryptographyClient.sign&#40;&#41; operation.
     * VerifyResult verificationResult =
     *     cryptographyClient.verify&#40;SignatureAlgorithm.ES256, dataToVerify, mySignature, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Verification status: %s.%n&quot;, verificationResult.isValid&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-RequestContext -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @param requestContext Additional options that are passed through the HTTP pipeline during the service call.
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     * @throws HttpResponseException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestContext requestContext) {
        if (isLocalClientAvailable()) {
            return localKeyCryptographyClient.verifyData(algorithm, data, signature, requestContext);
        } else {
            return clientImpl.verifyData(algorithm, data, signature, requestContext);
        }
    }

    private boolean isLocalClientAvailable() {
        if (!skipLocalClientCreation && localKeyCryptographyClient == null) {
            try {
                localKeyCryptographyClient = retrieveJwkAndCreateLocalClient(clientImpl, LOGGER);
            } catch (RuntimeException t) {
                if (RetryUtils.isRetryable(t)) {
                    LOGGER.atVerbose()
                        .setThrowable(t)
                        .log("Could not set up local cryptography for this operation. Defaulting to service-side "
                            + "cryptography.");
                } else {
                    skipLocalClientCreation = true;

                    LOGGER.atVerbose()
                        .setThrowable(t)
                        .log("Could not set up local cryptography. Defaulting to service-side cryptography for all "
                            + "operations.");
                }
            }
        }

        return localKeyCryptographyClient != null;
    }
}
