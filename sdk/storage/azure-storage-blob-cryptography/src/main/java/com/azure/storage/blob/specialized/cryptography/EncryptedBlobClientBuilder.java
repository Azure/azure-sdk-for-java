// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.models.EncryptionScope;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.connectionstring.StorageAuthenticationSettings;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.common.implementation.credentials.CredentialValidator;
import com.azure.storage.common.policy.MetadataValidationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.USER_AGENT_PROPERTIES;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of Storage Blob clients.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>Endpoint set through {@link #endpoint(String)}, including the container name and blob name, in the format of
 * {@code https://{accountName}.blob.core.windows.net/{containerName}/{blobName}}.
 * <li>Container and blob name if not specified in the {@link #endpoint(String)}, set through
 * {@link #containerName(String)} and {@link #blobName(String)} respectively.
 * <li>Credential set through {@link #credential(StorageSharedKeyCredential)} , {@link #sasToken(String)}, or
 * {@link #connectionString(String)} if the container is not publicly accessible.
 * <li>Key and key wrapping algorithm (for encryption) and/or key resolver (for decryption) must be specified
 * through {@link #key(AsyncKeyEncryptionKey, String)} and {@link #keyResolver(AsyncKeyEncryptionKeyResolver)}
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder use the following mapping to construct the given client:
 * <ul>
 * <li>{@link EncryptedBlobClientBuilder#buildEncryptedBlobClient()} - {@link EncryptedBlobClient}</li>
 * <li>{@link EncryptedBlobClientBuilder#buildEncryptedBlobAsyncClient()} -
 * {@link EncryptedBlobAsyncClient}</li>
 * </ul>
 */
@ServiceClientBuilder(serviceClients = {EncryptedBlobAsyncClient.class, EncryptedBlobClient.class})
public final class EncryptedBlobClientBuilder implements
    TokenCredentialTrait<EncryptedBlobClientBuilder>,
    ConnectionStringTrait<EncryptedBlobClientBuilder>,
    AzureNamedKeyCredentialTrait<EncryptedBlobClientBuilder>,
    AzureSasCredentialTrait<EncryptedBlobClientBuilder>,
    HttpTrait<EncryptedBlobClientBuilder>,
    ConfigurationTrait<EncryptedBlobClientBuilder>,
    EndpointTrait<EncryptedBlobClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptedBlobClientBuilder.class);
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-blob-cryptography.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");
    private static final String BLOB_CLIENT_NAME = USER_AGENT_PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static final String BLOB_CLIENT_VERSION = USER_AGENT_PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");

    private String endpoint;
    private String accountName;
    private String containerName;
    private String blobName;
    private String snapshot;
    private String versionId;
    private boolean requiresEncryption;
    private EncryptionVersion encryptionVersion;

    private StorageSharedKeyCredential storageSharedKeyCredential;
    private TokenCredential tokenCredential;
    private AzureSasCredential azureSasCredential;
    private String sasToken;

    private HttpClient httpClient;
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private HttpLogOptions logOptions;
    private RequestRetryOptions retryOptions;
    private RetryOptions coreRetryOptions;
    private HttpPipeline httpPipeline;

    private ClientOptions clientOptions = new ClientOptions();
    private Configuration configuration;

    private AsyncKeyEncryptionKey keyWrapper;
    private AsyncKeyEncryptionKeyResolver keyResolver;
    private String keyWrapAlgorithm;
    private BlobServiceVersion version;
    private CpkInfo customerProvidedKey;
    private EncryptionScope encryptionScope;

    /**
     * Creates a new instance of the EncryptedBlobClientBuilder
     * @deprecated Use {@link EncryptedBlobClientBuilder#EncryptedBlobClientBuilder(EncryptionVersion)}.
     */
    @Deprecated
    public EncryptedBlobClientBuilder() {
        logOptions = getDefaultHttpLogOptions();
    }

    /**
     *
     * @param version The version of the client side encryption protocol to use. It is highly recommended that v2 be
     * preferred for security reasons, though v1 continues to be supported for compatibility reasons. Note that even a
     * client configured to encrypt using v2 can decrypt blobs that use the v1 protocol.
     */
    public EncryptedBlobClientBuilder(EncryptionVersion version) {
        Objects.requireNonNull(version);
        this.encryptionVersion = version;
    }

    /**
     * Creates a {@link EncryptedBlobClient} based on options set in the Builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobAsyncClient -->
     * <pre>
     * EncryptedBlobAsyncClient client = new EncryptedBlobClientBuilder&#40;&#41;
     *     .key&#40;key, keyWrapAlgorithm&#41;
     *     .keyResolver&#40;keyResolver&#41;
     *     .connectionString&#40;connectionString&#41;
     *     .buildEncryptedBlobAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobAsyncClient -->
     *
     * @return a {@link EncryptedBlobClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public EncryptedBlobClient buildEncryptedBlobClient() {
        return new EncryptedBlobClient(buildEncryptedBlobAsyncClient());
    }

    /**
     * Creates a {@link EncryptedBlobAsyncClient} based on options set in the Builder.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobClient -->
     * <pre>
     * EncryptedBlobClient client = new EncryptedBlobClientBuilder&#40;&#41;
     *     .key&#40;key, keyWrapAlgorithm&#41;
     *     .keyResolver&#40;keyResolver&#41;
     *     .connectionString&#40;connectionString&#41;
     *     .buildEncryptedBlobClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder.buildEncryptedBlobClient -->
     *
     * @return a {@link EncryptedBlobAsyncClient} created from the configurations in this builder.
     * @throws NullPointerException If {@code endpoint}, {@code containerName}, or {@code blobName} is {@code null}.
     * @throws IllegalStateException If multiple credentials have been specified.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryOptions(RequestRetryOptions)} have been set.
     */
    public EncryptedBlobAsyncClient buildEncryptedBlobAsyncClient() {
        Objects.requireNonNull(blobName, "'blobName' cannot be null.");
        checkValidEncryptionParameters();

        this.encryptionVersion = encryptionVersion == null ? EncryptionVersion.V1 : encryptionVersion;
        if (EncryptionVersion.V1.equals(this.encryptionVersion)) {
            LOGGER.warning("Client is being configured to use v1 of client side encryption, which is no longer "
                + "considered secure. The default is v1 for compatibility reasons, but it is highly recommended "
                + "the version be set to v2 using the constructor");
        }

        /*
        Implicit and explicit root container access are functionally equivalent, but explicit references are easier
        to read and debug.
         */
        if (CoreUtils.isNullOrEmpty(containerName)) {
            containerName = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;
        }
        BlobServiceVersion serviceVersion = version != null ? version : BlobServiceVersion.getLatest();

        return new EncryptedBlobAsyncClient(addBlobUserAgentModificationPolicy(getHttpPipeline()), endpoint,
            serviceVersion, accountName, containerName, blobName, snapshot, customerProvidedKey, encryptionScope,
            keyWrapper, keyWrapAlgorithm, versionId, encryptionVersion);
    }


    private HttpPipeline addBlobUserAgentModificationPolicy(HttpPipeline pipeline) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy currPolicy = pipeline.getPolicy(i);
            policies.add(currPolicy);
            if (currPolicy instanceof UserAgentPolicy) {
                policies.add(new BlobUserAgentModificationPolicy(CLIENT_NAME, CLIENT_VERSION));
            }
        }

        return new HttpPipelineBuilder()
            .httpClient(pipeline.getHttpClient())
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    private BlobAsyncClient getUnencryptedBlobClient() {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .containerName(containerName)
            .blobName(blobName)
            .snapshot(snapshot)
            .customerProvidedKey(
                customerProvidedKey == null ? null : new CustomerProvidedKey(customerProvidedKey.getEncryptionKey()))
            .encryptionScope(encryptionScope == null ? null : encryptionScope.getEncryptionScope())
            .versionId(versionId)
            .serviceVersion(version)
            .httpClient(httpClient);
        // Is this missing some things? Refactor to use the pipeline builder code below?

        if (storageSharedKeyCredential != null) {
            builder.credential(storageSharedKeyCredential);
        } else if (tokenCredential != null) {
            builder.credential(tokenCredential);
        } else if (azureSasCredential != null) {
            builder.credential(azureSasCredential);
        } else if (sasToken != null) {
            builder.credential(new AzureSasCredential(sasToken));
        }

        return builder.buildAsyncClient();
    }

    private HttpPipeline getHttpPipeline() {
        CredentialValidator.validateSingleCredentialIsPresent(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken, LOGGER);

        // Prefer the pipeline set by the customer.
        if (httpPipeline != null) {
            List<HttpPipelinePolicy> policies = new ArrayList<>();
            // Check that BlobDecryptionPolicy not already present while copying them over to a new policy list.
            boolean decryptionPolicyPresent = false;
            for (int i = 0; i < httpPipeline.getPolicyCount(); i++) {
                HttpPipelinePolicy currPolicy = httpPipeline.getPolicy(i);
                if (currPolicy instanceof BlobDecryptionPolicy || currPolicy instanceof FetchEncryptionVersionPolicy) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException("The passed pipeline was already"
                        + " configured for encryption/decryption in a way that might conflict with the passed key "
                        + "information. Please ensure that the passed pipeline is not already configured for "
                        + "encryption/decryption"));
                }
                policies.add(currPolicy);
            }
            // There is guaranteed not to be a decryption policy in the provided pipeline. Add one to the front.
            policies.add(0, new BlobDecryptionPolicy(keyWrapper, keyResolver, requiresEncryption));
            policies.add(0, new FetchEncryptionVersionPolicy(getUnencryptedBlobClient(), requiresEncryption));

            return new HttpPipelineBuilder()
                .httpClient(httpPipeline.getHttpClient())
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .build();
        }

        Configuration userAgentConfiguration = (configuration == null) ? Configuration.NONE : configuration;

            // Closest to API goes first, closest to wire goes last.
            List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new FetchEncryptionVersionPolicy(getUnencryptedBlobClient(), requiresEncryption));
            policies.add(new BlobDecryptionPolicy(keyWrapper, keyResolver, requiresEncryption));
            String applicationId = clientOptions.getApplicationId() != null ? clientOptions.getApplicationId()
                : logOptions.getApplicationId();
            policies.add(new UserAgentPolicy(applicationId, BLOB_CLIENT_NAME, BLOB_CLIENT_VERSION, userAgentConfiguration));
            policies.add(new RequestIdPolicy());

            policies.addAll(perCallPolicies);
            HttpPolicyProviders.addBeforeRetryPolicies(policies);
            policies.add(BuilderUtils.createRetryPolicy(retryOptions, coreRetryOptions, LOGGER));

            policies.add(new AddDatePolicy());

            // We need to place this policy right before the credential policy since headers may affect the string to sign
            // of the request.
            HttpHeaders headers = new HttpHeaders();
            clientOptions.getHeaders().forEach(header -> headers.put(header.getName(), header.getValue()));
            if (headers.getSize() > 0) {
                policies.add(new AddHeadersPolicy(headers));
            }
            policies.add(new MetadataValidationPolicy());

            if (storageSharedKeyCredential != null) {
                policies.add(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential));
            } else if (tokenCredential != null) {
                BuilderHelper.httpsValidation(tokenCredential, "bearer token", endpoint, LOGGER);
                policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, Constants.STORAGE_SCOPE));
            } else if (azureSasCredential != null) {
                policies.add(new AzureSasCredentialPolicy(azureSasCredential, false));
            } else if (sasToken != null) {
                policies.add(new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false));
            }

            policies.addAll(perRetryPolicies);

            HttpPolicyProviders.addAfterRetryPolicies(policies);

            policies.add(new ResponseValidationPolicyBuilder()
                .addOptionalEcho(Constants.HeaderConstants.CLIENT_REQUEST_ID)
                .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256)
                .build());

            policies.add(new HttpLoggingPolicy(logOptions));

            policies.add(new ScrubEtagPolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Sets the encryption key parameters for the client
     *
     * @param key An object of type {@link AsyncKeyEncryptionKey} that is used to wrap/unwrap the content encryption key
     * @param keyWrapAlgorithm The {@link String} used to wrap the key.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder key(AsyncKeyEncryptionKey key, String keyWrapAlgorithm) {
        this.keyWrapper = key;
        this.keyWrapAlgorithm = keyWrapAlgorithm;
        return this;
    }

    /**
     * Sets the encryption parameters for this client
     *
     * @param keyResolver The key resolver used to select the correct key for decrypting existing blobs.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder keyResolver(AsyncKeyEncryptionKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
        return this;
    }

    private void checkValidEncryptionParameters() {
        // Check that key and key wrapper are not both null.
        if (this.keyWrapper == null && this.keyResolver == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Key and KeyResolver cannot both be null"));
        }

        // If the key is provided, ensure the key wrap algorithm is also provided.
        if (this.keyWrapper != null && this.keyWrapAlgorithm == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Key Wrap Algorithm must be specified with a Key."));
        }
    }

    /**
     * Sets the {@link StorageSharedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link StorageSharedKeyCredential}.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public EncryptedBlobClientBuilder credential(StorageSharedKeyCredential credential) {
        this.storageSharedKeyCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.tokenCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the {@link AzureNamedKeyCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureNamedKeyCredential}.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public EncryptedBlobClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        return credential(StorageSharedKeyCredential.fromAzureNamedKeyCredential(credential));
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public EncryptedBlobClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the SAS token used to authorize requests sent to the service.
     *
     * @param sasToken The SAS token to use for authenticating requests. This string should only be the query parameters
     * (with or without a leading '?') and not a full url.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code sasToken} is {@code null}.
     */
    public EncryptedBlobClientBuilder sasToken(String sasToken) {
        this.sasToken = Objects.requireNonNull(sasToken,
            "'sasToken' cannot be null.");
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return the updated EncryptedBlobClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public EncryptedBlobClientBuilder credential(AzureSasCredential credential) {
        this.azureSasCredential = Objects.requireNonNull(credential,
            "'credential' cannot be null.");
        return this;
    }

    /**
     * Clears the credential used to authorize the request.
     *
     * <p>This is for blobs that are publicly accessible.</p>
     *
     * @return the updated EncryptedBlobClientBuilder
     */
    public EncryptedBlobClientBuilder setAnonymousAccess() {
        this.storageSharedKeyCredential = null;
        this.tokenCredential = null;
        this.azureSasCredential = null;
        this.sasToken = null;
        return this;
    }

    /**
     * Sets the connection string to connect to the service.
     *
     * @param connectionString Connection string of the storage account.
     * @return the updated EncryptedBlobClientBuilder
     * @throws IllegalArgumentException If {@code connectionString} is invalid.
     */
    @Override
    public EncryptedBlobClientBuilder connectionString(String connectionString) {
        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, LOGGER);
        StorageEndpoint endpoint = storageConnectionString.getBlobEndpoint();
        if (endpoint == null || endpoint.getPrimaryUri() == null) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException(
                    "connectionString missing required settings to derive blob service endpoint."));
        }
        this.endpoint(endpoint.getPrimaryUri());
        if (storageConnectionString.getAccountName() != null) {
            this.accountName = storageConnectionString.getAccountName();
        }
        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        if (authSettings.getType() == StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY) {
            this.credential(new StorageSharedKeyCredential(authSettings.getAccount().getName(),
                authSettings.getAccount().getAccessKey()));
        } else if (authSettings.getType() == StorageAuthenticationSettings.Type.SAS_TOKEN) {
            this.sasToken(authSettings.getSasToken());
        }
        return this;
    }

    /**
     * Sets the service endpoint, additionally parses it for information (SAS token, container name, blob name)
     *
     * <p>If the blob name contains special characters, pass in the url encoded version of the blob name. </p>
     *
     * <p>If the endpoint is to a blob in the root container, this method will fail as it will interpret the blob name
     * as the container name. With only one path element, it is impossible to distinguish between a container name and a
     * blob in the root container, so it is assumed to be the container name as this is much more common. When working
     * with blobs in the root container, it is best to set the endpoint to the account url and specify the blob name
     * separately using the {@link EncryptedBlobClientBuilder#blobName(String) blobName} method.</p>
     *
     * @param endpoint URL of the service
     * @return the updated EncryptedBlobClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    @Override
    public EncryptedBlobClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            BlobUrlParts parts = BlobUrlParts.parse(url);

            this.accountName = parts.getAccountName();
            this.endpoint = BuilderHelper.getEndpoint(parts);
            this.containerName = parts.getBlobContainerName() == null ? this.containerName
                : parts.getBlobContainerName();
            this.blobName = parts.getBlobName() == null ? this.blobName : Utility.urlEncode(parts.getBlobName());
            this.snapshot = parts.getSnapshot();
            this.versionId = parts.getVersionId();

            String sasToken = parts.getCommonSasQueryParameters().encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                this.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Blob endpoint url is malformed.", ex));
        }
        return this;
    }

    /**
     * Sets the name of the container that contains the blob.
     *
     * @param containerName Name of the container. If the value {@code null} or empty the root container, {@code $root},
     * will be used.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder containerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Sets the name of the blob.
     *
     * @param blobName Name of the blob. If the blob name contains special characters, pass in the url encoded version
     * of the blob name.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code blobName} is {@code null}
     */
    public EncryptedBlobClientBuilder blobName(String blobName) {
        this.blobName = Utility.urlEncode(Utility.urlDecode(Objects.requireNonNull(blobName,
            "'blobName' cannot be null.")));
        return this;
    }

    /**
     * Sets the snapshot identifier of the blob.
     *
     * @param snapshot Snapshot identifier for the blob.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Sets the version identifier of the blob.
     *
     * @param versionId Version identifier for the blob, pass {@code null} to interact with the latest blob version.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return the updated EncryptedBlobClientBuilder object
     */
    @Override
    public EncryptedBlobClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("'httpClient' is being set to 'null' when it was previously configured.");
        }

        this.httpClient = httpClient;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public EncryptedBlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null");
        if (pipelinePolicy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(pipelinePolicy);
        } else {
            perRetryPolicies.add(pipelinePolicy);
        }
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    @Override
    public EncryptedBlobClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Gets the default Storage allowlist log headers and query parameters.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        return BuilderHelper.getDefaultHttpLogOptions();
    }

    /**
     * Sets the configuration object used to retrieve environment configuration values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment configurations.
     * @return the updated EncryptedBlobClientBuilder object
     */
    @Override
    public EncryptedBlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryOptions {@link RequestRetryOptions}.
     * @return the updated EncryptedBlobClientBuilder object.
     */
    public EncryptedBlobClientBuilder retryOptions(RequestRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RequestRetryOptions)}.
     * Consider using {@link #retryOptions(RequestRetryOptions)} to also set storage specific options.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return the updated EncryptedBlobClientBuilder object
     */
    @Override
    public EncryptedBlobClientBuilder retryOptions(RetryOptions retryOptions) {
        this.coreRetryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * The {@link #endpoint(String) endpoint} and
     * {@link #customerProvidedKey(CustomerProvidedKey) customer provided key} are
     * not ignored when {@code pipeline} is set.
     *
     * @return the updated EncryptedBlobClientBuilder object
     */
    @Override
    public EncryptedBlobClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @see HttpClientOptions
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code clientOptions} is {@code null}.
     */
    @Override
    public EncryptedBlobClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link BlobServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder serviceVersion(BlobServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt blob contents on the server.
     *
     * @param customerProvidedKey {@link CustomerProvidedKey}
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey) {
        if (customerProvidedKey == null) {
            this.customerProvidedKey = null;
        } else {
            this.customerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }

        return this;
    }

    /**
     * Sets the {@code encryption scope} that is used to encrypt blob contents on the server.
     *
     * @param encryptionScope Encryption scope containing the encryption key information.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder encryptionScope(String encryptionScope) {
        if (encryptionScope == null) {
            this.encryptionScope = null;
        } else {
            this.encryptionScope = new EncryptionScope().setEncryptionScope(encryptionScope);
        }

        return this;
    }

    /**
     * Configures the builder based on the passed {@link BlobClient}. This will set the {@link HttpPipeline},
     * {@link URL} and {@link BlobServiceVersion} that are used to interact with the service. Note that the underlying
     * pipeline should not already be configured for encryption/decryption.
     *
     * <p>If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint} and
     * {@link #serviceVersion(BlobServiceVersion) serviceVersion}.</p>
     *
     * <p>Note that for security reasons, this method does not copy over the {@link CustomerProvidedKey} and
     * encryption scope properties from the provided client. To set CPK, please use
     * {@link #customerProvidedKey(CustomerProvidedKey)}.</p>
     *
     * @param blobClient BlobClient used to configure the builder.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code containerClient} is {@code null}.
     */
    public EncryptedBlobClientBuilder blobClient(BlobClient blobClient) {
        Objects.requireNonNull(blobClient);
        return client(blobClient.getHttpPipeline(), blobClient.getBlobUrl(), blobClient.getServiceVersion());
    }

    /**
     * Configures the builder based on the passed {@link BlobAsyncClient}. This will set the {@link HttpPipeline},
     * {@link URL} and {@link BlobServiceVersion} that are used to interact with the service. Note that the underlying
     * pipeline should not already be configured for encryption/decryption.
     *
     * <p>If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint} and
     * {@link #serviceVersion(BlobServiceVersion) serviceVersion}.</p>
     *
     * <p>Note that for security reasons, this method does not copy over the {@link CustomerProvidedKey} and
     * encryption scope properties from the provided client. To set CPK, please use
     * {@link #customerProvidedKey(CustomerProvidedKey)}.</p>
     *
     * @param blobAsyncClient BlobAsyncClient used to configure the builder.
     * @return the updated EncryptedBlobClientBuilder object
     * @throws NullPointerException If {@code containerClient} is {@code null}.
     */
    public EncryptedBlobClientBuilder blobAsyncClient(BlobAsyncClient blobAsyncClient) {
        Objects.requireNonNull(blobAsyncClient);
        return client(blobAsyncClient.getHttpPipeline(), blobAsyncClient.getBlobUrl(),
            blobAsyncClient.getServiceVersion());
    }

    /**
     * Helper method to transform a regular client into an encrypted client
     *
     * @param httpPipeline {@link HttpPipeline}
     * @param endpoint The endpoint.
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return the updated EncryptedBlobClientBuilder object
     */
    private EncryptedBlobClientBuilder client(HttpPipeline httpPipeline, String endpoint, BlobServiceVersion version) {
        this.endpoint(endpoint);
        this.serviceVersion(version);
        return this.pipeline(httpPipeline);
    }

    /**
     * Sets the requires encryption option.
     *
     * @param requiresEncryption Whether encryption is enforced by this client. Client will throw if data is
     * downloaded and it is not encrypted.
     * @return the updated EncryptedBlobClientBuilder object
     */
    public EncryptedBlobClientBuilder requiresEncryption(boolean requiresEncryption) {
        this.requiresEncryption = requiresEncryption;
        return this;
    }
}
