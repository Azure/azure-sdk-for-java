// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.security.keyvault.keys.cryptography.implementation.CryptographyClientImpl;
import com.azure.security.keyvault.keys.cryptography.implementation.LocalKeyCryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.createLocalClient;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.isThrowableRetryable;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.retrieveJwkAndCreateLocalAsyncClient;

/**
 * The {@link CryptographyAsyncClient} provides asynchronous methods to perform cryptographic operations using
 * asymmetric and symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify
 * operations using the configured key.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link CryptographyAsyncClient} class, a vault url and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally,
 * we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">
 * Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Asynchronous Cryptography Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link CryptographyAsyncClient}, using the
 * {@link CryptographyClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation -->
 * <pre>
 * CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder&#40;&#41;
 *     .keyIdentifier&#40;&quot;&lt;your-key-id&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation -->
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation -->
 * <pre>
 * JsonWebKey jsonWebKey = new JsonWebKey&#40;&#41;.setId&#40;&quot;SampleJsonWebKey&quot;&#41;;
 * CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder&#40;&#41;
 *     .jsonWebKey&#40;jsonWebKey&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation -->
 * <br>
 *
 * <p>When a {@link CryptographyAsyncClient} gets created using a {@code Azure Key Vault key identifier}, the first
 * time a cryptographic operation is attempted, the client will attempt to retrieve the key material from the service,
 * cache it, and perform all future cryptographic operations locally, deferring to the service when that's not possible.
 * If key retrieval and caching fails because of a non-retryable error, the client will not make any further attempts
 * and will fall back to performing all cryptographic operations on the service side. Conversely, when a
 * {@link CryptographyAsyncClient} created using a {@link JsonWebKey JSON Web Key}, all cryptographic operations will be
 * performed locally.</p>
 *
 * <hr>
 *
 * <h2>Encrypt Data</h2>
 * The {@link CryptographyAsyncClient} can be used to encrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously encrypt data using the
 * {@link CryptographyAsyncClient#encrypt(EncryptionAlgorithm, byte[])} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte -->
 * <pre>
 * byte[] plaintext = new byte[100];
 * new Random&#40;0x1234567L&#41;.nextBytes&#40;plaintext&#41;;
 *
 * cryptographyAsyncClient.encrypt&#40;EncryptionAlgorithm.RSA_OAEP, plaintext&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;encryptResult -&gt;
 *         System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
 *             encryptResult.getCipherText&#40;&#41;.length, encryptResult.getAlgorithm&#40;&#41;.toString&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link CryptographyClient}.</p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Decrypt Data</h2>
 * The {@link CryptographyAsyncClient} can be used to decrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 *
 * <p>The following code sample demonstrates how to asynchronously decrypt data using the
 * {@link CryptographyAsyncClient#decrypt(EncryptionAlgorithm, byte[])} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte -->
 * <pre>
 * byte[] ciphertext = new byte[100];
 * new Random&#40;0x1234567L&#41;.nextBytes&#40;ciphertext&#41;;
 *
 * cryptographyAsyncClient.decrypt&#40;EncryptionAlgorithm.RSA_OAEP, ciphertext&#41;
 *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
 *     .subscribe&#40;decryptResult -&gt;
 *         System.out.printf&#40;&quot;Received decrypted content of length: %d%n&quot;, decryptResult.getPlainText&#40;&#41;.length&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link CryptographyClient}.</p>
 *
 * @see com.azure.security.keyvault.keys.cryptography
 * @see CryptographyClientBuilder
 */
@ServiceClient(builder = CryptographyClientBuilder.class, isAsync = true,
               serviceInterfaces = {KeyClientImpl.KeyClientService.class,
                   SecretMinClientImpl.SecretMinClientService.class})
public class CryptographyAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyAsyncClient.class);

    private final HttpPipeline pipeline;

    private volatile boolean skipLocalClientCreation;
    private volatile LocalKeyCryptographyClient localKeyCryptographyClient;

    final CryptographyClientImpl implClient;
    final String keyId;

    /**
     * Creates a {@link CryptographyAsyncClient} that uses a given {@link HttpPipeline pipeline} to service requests.
     *
     * @param keyId The Azure Key Vault key identifier to use for cryptography operations.
     * @param pipeline {@link HttpPipeline} that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     * @param disableKeyCaching Indicates if local key caching should be disabled and all cryptographic operations
     * deferred to the service.
     */
    CryptographyAsyncClient(String keyId, HttpPipeline pipeline, CryptographyServiceVersion version,
                            boolean disableKeyCaching) {
        this.implClient = new CryptographyClientImpl(keyId, pipeline, version);
        this.keyId = keyId;
        this.pipeline = pipeline;
        this.skipLocalClientCreation = disableKeyCaching;
    }

    /**
     * Creates a {@link CryptographyAsyncClient} that uses a {@link JsonWebKey} to perform local cryptography
     * operations.
     *
     * @param jsonWebKey The {@link JsonWebKey} to use for local cryptography operations.
     */
    CryptographyAsyncClient(JsonWebKey jsonWebKey) {
        Objects.requireNonNull(jsonWebKey, "The JSON Web Key is required.");

        if (!jsonWebKey.isValid()) {
            throw new IllegalArgumentException("The JSON Web Key is not valid.");
        }

        if (jsonWebKey.getKeyOps() == null) {
            throw new IllegalArgumentException("The JSON Web Key's key operations property is not configured.");
        }

        if (jsonWebKey.getKeyType() == null) {
            throw new IllegalArgumentException("The JSON Web Key's key type property is not configured.");
        }

        this.implClient = null;
        this.keyId = jsonWebKey.getId();
        this.pipeline = null;

        try {
            this.localKeyCryptographyClient = createLocalClient(jsonWebKey, null);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Could not initialize local cryptography client.", e));
        }
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission for non-local operations.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKey -->
     * <pre>
     * cryptographyAsyncClient.getKey&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;key -&gt;
     *         System.out.printf&#40;&quot;Key returned with name: %s, and id: %s.%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKey -->
     *
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}.
     *
     * @throws ResourceNotFoundException When the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> getKey() {
        return getKeyWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission for non-local operations.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKeyWithResponse -->
     * <pre>
     * cryptographyAsyncClient.getKeyWithResponse&#40;&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;keyResponse -&gt;
     *         System.out.printf&#40;&quot;Key returned with name: %s, and id: %s.%n&quot;, keyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *             keyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKeyWithResponse -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * requested {@link KeyVaultKey key}.
     *
     * @throws ResourceNotFoundException When the configured key doesn't exist in the key vault.
     * @throws UnsupportedOperationException When operating in local-only mode (using a client created using a
     * JsonWebKey instance).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> getKeyWithResponse() {
        if (implClient != null) {
            try {
                return implClient.getKeyAsync();
            } catch (RuntimeException e) {
                return monoError(LOGGER, e);
            }
        } else {
            return monoError(LOGGER,
                new UnsupportedOperationException("Operation not supported when operating in local-only mode."));
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports
     * a single block of data, the size of which is dependent on the target key and the encryption algorithm to be
     * used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting
     * the
     * specified {@code plaintext}. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * <p>
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte -->
     * <pre>
     * byte[] plaintext = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;plaintext&#41;;
     *
     * cryptographyAsyncClient.encrypt&#40;EncryptionAlgorithm.RSA_OAEP, plaintext&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;encryptResult -&gt;
     *         System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
     *             encryptResult.getCipherText&#40;&#41;.length, encryptResult.getAlgorithm&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte -->
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     *
     * @return A {@link Mono} containing a {@link EncryptResult} whose
     * {@link EncryptResult#getCipherText() cipher text} contains the encrypted content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.encryptAsync(algorithm, plaintext, context);
                } else {
                    return implClient.encryptAsync(algorithm, plaintext, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports
     * a single block of data, the size of which is dependent on the target key and the encryption algorithm to be
     * used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting
     * the
     * specified {@code plaintext}. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * <p>
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptParameters -->
     * <pre>
     * byte[] plaintextBytes = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;plaintextBytes&#41;;
     * byte[] iv = &#123;
     *     &#40;byte&#41; 0x1a, &#40;byte&#41; 0xf3, &#40;byte&#41; 0x8c, &#40;byte&#41; 0x2d, &#40;byte&#41; 0xc2, &#40;byte&#41; 0xb9, &#40;byte&#41; 0x6f, &#40;byte&#41; 0xfd,
     *     &#40;byte&#41; 0xd8, &#40;byte&#41; 0x66, &#40;byte&#41; 0x94, &#40;byte&#41; 0x09, &#40;byte&#41; 0x23, &#40;byte&#41; 0x41, &#40;byte&#41; 0xbc, &#40;byte&#41; 0x04
     * &#125;;
     *
     * EncryptParameters encryptParameters = EncryptParameters.createA128CbcParameters&#40;plaintextBytes, iv&#41;;
     *
     * cryptographyAsyncClient.encrypt&#40;encryptParameters&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;encryptResult -&gt;
     *         System.out.printf&#40;&quot;Received encrypted content of length: %d, with algorithm: %s.%n&quot;,
     *             encryptResult.getCipherText&#40;&#41;.length, encryptResult.getAlgorithm&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptParameters -->
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     *
     * @return A {@link Mono} containing a {@link EncryptResult} whose
     * {@link EncryptResult#getCipherText() cipher text} contains the encrypted content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EncryptResult> encrypt(EncryptParameters encryptParameters) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.encryptAsync(encryptParameters, context);
                } else {
                    return implClient.encryptAsync(encryptParameters, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm
     * to be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
     * the {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting
     * the specified encrypted content. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * <p>
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte -->
     * <pre>
     * byte[] ciphertext = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;ciphertext&#41;;
     *
     * cryptographyAsyncClient.decrypt&#40;EncryptionAlgorithm.RSA_OAEP, ciphertext&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;decryptResult -&gt;
     *         System.out.printf&#40;&quot;Received decrypted content of length: %d%n&quot;, decryptResult.getPlainText&#40;&#41;.length&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte -->
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted. Microsoft recommends you not use CBC without first ensuring the
     * integrity of the ciphertext using an HMAC, for example.
     * See <a href="https://docs.microsoft.com/dotnet/standard/security/vulnerabilities-cbc-mode">Timing
     * vulnerabilities with CBC-mode symmetric decryption using padding</a> for more information.
     *
     * @return A {@link Mono} containing the decrypted blob.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.decryptAsync(algorithm, ciphertext, context);
                } else {
                    return implClient.decryptAsync(algorithm, ciphertext, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm
     * to be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
     * the {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting
     * the specified encrypted content. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * <p>
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#DecryptParameters -->
     * <pre>
     * byte[] ciphertextBytes = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;ciphertextBytes&#41;;
     * byte[] iv = &#123;
     *     &#40;byte&#41; 0x1a, &#40;byte&#41; 0xf3, &#40;byte&#41; 0x8c, &#40;byte&#41; 0x2d, &#40;byte&#41; 0xc2, &#40;byte&#41; 0xb9, &#40;byte&#41; 0x6f, &#40;byte&#41; 0xfd,
     *     &#40;byte&#41; 0xd8, &#40;byte&#41; 0x66, &#40;byte&#41; 0x94, &#40;byte&#41; 0x09, &#40;byte&#41; 0x23, &#40;byte&#41; 0x41, &#40;byte&#41; 0xbc, &#40;byte&#41; 0x04
     * &#125;;
     *
     * DecryptParameters decryptParameters = DecryptParameters.createA128CbcParameters&#40;ciphertextBytes, iv&#41;;
     *
     * cryptographyAsyncClient.decrypt&#40;decryptParameters&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;decryptResult -&gt;
     *         System.out.printf&#40;&quot;Received decrypted content of length: %d.%n&quot;, decryptResult.getPlainText&#40;&#41;.length&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#DecryptParameters -->
     *
     * @param decryptParameters The parameters to use in the decryption operation. Microsoft recommends you not use CBC
     * without first ensuring the integrity of the ciphertext using an HMAC, for example.
     * See <a href="https://docs.microsoft.com/dotnet/standard/security/vulnerabilities-cbc-mode">Timing vulnerabilities
     * with CBC-mode symmetric decryption using padding</a> for more information.
     *
     * @return A {@link Mono} containing the decrypted blob.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DecryptResult> decrypt(DecryptParameters decryptParameters) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.decryptAsync(decryptParameters, context);
                } else {
                    return implClient.decryptAsync(decryptParameters, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 ES384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES256K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384}, and {@link SignatureAlgorithm#RS512 RS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sings the digest. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte -->
     * <pre>
     * byte[] data = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;data&#41;;
     * MessageDigest md = MessageDigest.getInstance&#40;&quot;SHA-256&quot;&#41;;
     * md.update&#40;data&#41;;
     * byte[] digest = md.digest&#40;&#41;;
     *
     * cryptographyAsyncClient.sign&#40;SignatureAlgorithm.ES256, digest&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;signResult -&gt;
     *         System.out.printf&#40;&quot;Received signature of length: %d, with algorithm: %s.%n&quot;,
     *             signResult.getSignature&#40;&#41;.length, signResult.getAlgorithm&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     *
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature}
     * contains the created signature.
     *
     * @throws NullPointerException If {@code algorithm} or {@code digest} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for signing.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.signAsync(algorithm, digest, context);
                } else {
                    return implClient.signAsync(algorithm, digest, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature. This operation
     * requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 ES384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES256K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384}, and {@link SignatureAlgorithm#RS512 RS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte -->
     * <pre>
     * byte[] myData = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;myData&#41;;
     * MessageDigest messageDigest = MessageDigest.getInstance&#40;&quot;SHA-256&quot;&#41;;
     * messageDigest.update&#40;myData&#41;;
     * byte[] myDigest = messageDigest.digest&#40;&#41;;
     *
     * &#47;&#47; A signature can be obtained from the SignResult returned by the CryptographyAsyncClient.sign&#40;&#41; operation.
     * cryptographyAsyncClient.verify&#40;SignatureAlgorithm.ES256, myDigest, signature&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;verifyResult -&gt;
     *         System.out.printf&#40;&quot;Verification status: %s.%n&quot;, verifyResult.isValid&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     *
     * @return A {@link Mono} containing a {@link VerifyResult}
     * {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws NullPointerException If {@code algorithm}, {@code digest} or {@code signature} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for verifying.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context);
                } else {
                    return implClient.verifyAsync(algorithm, digest, signature, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the {@code keys/wrapKey} permission for non-local
     * operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * <p>
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128KW A128KW},
     * {@link EncryptionAlgorithm#A192KW A192KW} and {@link EncryptionAlgorithm#A256KW A256KW}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte -->
     * <pre>
     * byte[] key = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;key&#41;;
     *
     * cryptographyAsyncClient.wrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, key&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;wrapResult -&gt;
     *         System.out.printf&#40;&quot;Received encrypted key of length: %d, with algorithm: %s.%n&quot;,
     *             wrapResult.getEncryptedKey&#40;&#41;.length, wrapResult.getAlgorithm&#40;&#41;.toString&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     *
     * @return A {@link Mono} containing a {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted key}
     * contains the wrapped key result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code key} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.wrapKeyAsync(algorithm, key, context);
                } else {
                    return implClient.wrapKeyAsync(algorithm, key, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation
     * is the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap.
     * This
     * operation requires the {@code keys/unwrapKey} permission for non-local operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for unwrapping the
     * specified encrypted key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * <p>
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW},
     * {@link KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Unwraps the key content. Subscribes to the call asynchronously and prints out the unwrapped key details when
     * a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte -->
     * <pre>
     * byte[] keyToWrap = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;key&#41;;
     *
     * cryptographyAsyncClient.wrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, keyToWrap&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;wrapResult -&gt;
     *         cryptographyAsyncClient.unwrapKey&#40;KeyWrapAlgorithm.RSA_OAEP, wrapResult.getEncryptedKey&#40;&#41;&#41;
     *             .subscribe&#40;keyUnwrapResult -&gt;
     *                 System.out.printf&#40;&quot;Received key of length: %d.%n&quot;, keyUnwrapResult.getKey&#40;&#41;.length&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     *
     * @return A {@link Mono} containing an {@link UnwrapResult} whose {@link UnwrapResult#getKey() decrypted
     * key} contains the unwrapped key result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code encryptedKey} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context);
                } else {
                    return implClient.unwrapKeyAsync(algorithm, encryptedKey, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to sign the digest.
     * Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 ES384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES256K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384}, and {@link SignatureAlgorithm#RS512 RS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a
     * response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte -->
     * <pre>
     * byte[] data = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;data&#41;;
     *
     * cryptographyAsyncClient.sign&#40;SignatureAlgorithm.ES256, data&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;signResult -&gt;
     *         System.out.printf&#40;&quot;Received signature of length: %d, with algorithm: %s.%n&quot;,
     *             signResult.getSignature&#40;&#41;.length, signResult.getAlgorithm&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     *
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature}
     * contains the created signature.
     *
     * @throws NullPointerException If {@code algorithm} or {@code data} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for signing.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.signDataAsync(algorithm, data, context);
                } else {
                    return implClient.signDataAsync(algorithm, data, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys. In case of asymmetric keys public portion of the key is used to verify the signature.
     * This operation requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 ES384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES256K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384}, and {@link SignatureAlgorithm#RS512 RS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     *
     * <!-- src_embed com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte -->
     * <pre>
     * byte[] myData = new byte[100];
     * new Random&#40;0x1234567L&#41;.nextBytes&#40;myData&#41;;
     *
     * &#47;&#47; A signature can be obtained from the SignResult returned by the CryptographyAsyncClient.sign&#40;&#41; operation.
     * cryptographyAsyncClient.verify&#40;SignatureAlgorithm.ES256, myData, signature&#41;
     *     .contextWrite&#40;Context.of&#40;&quot;key1&quot;, &quot;value1&quot;, &quot;key2&quot;, &quot;value2&quot;&#41;&#41;
     *     .subscribe&#40;verifyResult -&gt;
     *         System.out.printf&#40;&quot;Verification status: %s.%n&quot;, verifyResult.isValid&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     *
     * @return A {@link Mono} containing a {@link VerifyResult}
     * {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws NullPointerException If {@code algorithm}, {@code data} or {@code signature} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for verifying.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        try {
            return withContext(context -> isLocalClientAvailable().flatMap(available -> {
                if (available) {
                    return localKeyCryptographyClient.verifyDataAsync(algorithm, data, signature, context);
                } else {
                    return implClient.verifyDataAsync(algorithm, data, signature, context);
                }
            }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    private Mono<Boolean> isLocalClientAvailable() {
        if (!skipLocalClientCreation && localKeyCryptographyClient == null) {
            return retrieveJwkAndCreateLocalAsyncClient(implClient)
                .map(localClient -> {
                    localKeyCryptographyClient = localClient;

                    return true;
                })
                .onErrorResume(t -> {
                    if (isThrowableRetryable(t)) {
                        LOGGER.log(LogLevel.VERBOSE, () -> "Could not set up local cryptography for this operation. "
                            + "Defaulting to service-side cryptography.", t);
                    } else {
                        skipLocalClientCreation = true;

                        LOGGER.log(LogLevel.VERBOSE, () -> "Could not set up local cryptography. Defaulting to"
                            + "service-side cryptography for all operations.", t);
                    }

                    return Mono.just(false);
                });
        }

        return Mono.just(localKeyCryptographyClient != null);
    }
}
