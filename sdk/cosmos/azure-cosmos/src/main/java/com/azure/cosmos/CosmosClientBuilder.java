// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.WriteRetryPolicy;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.routing.LocationHelper;
import com.azure.cosmos.models.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosPermissionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper;

/**
 * Helper class to build CosmosAsyncClient {@link CosmosAsyncClient} and CosmosClient {@link CosmosClient}
 * instances as logical representation of the Azure Cosmos database service.
 * <p>
 * When building client, endpoint() and key() are mandatory APIs, without these the initialization will fail.
 * <p>
 * Though consistencyLevel is not mandatory, but we strongly suggest to pay attention to this API when building client.
 * By default, account consistency level is used if none is provided.
 * <p>
 * By default, direct connection mode is used if none specified.
 * <pre>
 *     Building Cosmos Async Client minimal APIs (without any customized configurations)
 * {@code
 * CosmosAsyncClient client = new CosmosClientBuilder()
 *         .endpoint(serviceEndpoint)
 *         .key(key)
 *         .buildAsyncClient();
 * }
 * </pre>
 *
 * <pre>
 *     Building Cosmos Async Client with customizations
 * {@code
 * CosmosAsyncClient client = new CosmosClientBuilder()
 *         .endpoint(serviceEndpoint)
 *         .key(key)
 *         .directMode(directConnectionConfig, gatewayConnectionConfig)
 *         .consistencyLevel(ConsistencyLevel.SESSION)
 *         .connectionSharingAcrossClientsEnabled(true)
 *         .contentResponseOnWriteEnabled(true)
 *         .userAgentSuffix("my-application1-client")
 *         .preferredRegions(Collections.singletonList("West US", "East US"))
 *         .buildAsyncClient();
 * }
 * </pre>
 *
 * <pre>
 *     Building Cosmos Sync Client minimal APIs (without any customized configurations)
 * {@code
 * CosmosClient client = new CosmosClientBuilder()
 *         .endpoint(serviceEndpoint)
 *         .key(key)
 *         .buildClient();
 * }
 * </pre>
 *
 * <pre>
 *     Building Cosmos Sync Client with customizations
 * {@code
 * CosmosClient client = new CosmosClientBuilder()
 *         .endpoint(serviceEndpoint)
 *         .key(key)
 *         .directMode(directConnectionConfig, gatewayConnectionConfig)
 *         .consistencyLevel(ConsistencyLevel.SESSION)
 *         .connectionSharingAcrossClientsEnabled(true)
 *         .contentResponseOnWriteEnabled(true)
 *         .userAgentSuffix("my-application1-client")
 *         .preferredRegions(Collections.singletonList("West US", "East US"))
 *         .buildClient();
 * }
 * </pre>
 */
@ServiceClientBuilder(serviceClients = {CosmosClient.class, CosmosAsyncClient.class})
public class CosmosClientBuilder implements
    TokenCredentialTrait<CosmosClientBuilder>,
    AzureKeyCredentialTrait<CosmosClientBuilder>,
    EndpointTrait<CosmosClientBuilder> {

    private final static Logger logger = LoggerFactory.getLogger(CosmosClientBuilder.class);
    private Configs configs = new Configs();
    private String serviceEndpoint;
    private String keyOrResourceToken;
    private CosmosClientMetadataCachesSnapshot state;
    private TokenCredential tokenCredential;
    private ConnectionPolicy connectionPolicy;
    private GatewayConnectionConfig gatewayConnectionConfig;
    private DirectConnectionConfig directConnectionConfig;
    private ConsistencyLevel desiredConsistencyLevel;
    private List<CosmosPermissionProperties> permissions;
    private CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver;
    private AzureKeyCredential credential;
    private boolean sessionCapturingOverrideEnabled;
    private boolean connectionSharingAcrossClientsEnabled;
    private boolean contentResponseOnWriteEnabled;
    private String userAgentSuffix;
    private ThrottlingRetryOptions throttlingRetryOptions;
    private List<String> preferredRegions;
    private boolean endpointDiscoveryEnabled = true;
    private boolean multipleWriteRegionsEnabled = true;
    private boolean readRequestsFallbackEnabled = true;

    private WriteRetryPolicy writeRetryPolicy = WriteRetryPolicy.DISABLED;
    private CosmosClientTelemetryConfig clientTelemetryConfig;
    private ApiType apiType = null;
    private Boolean clientTelemetryEnabledOverride = null;
    private CosmosContainerProactiveInitConfig proactiveContainerInitConfig;

    /**
     * Instantiates a new Cosmos client builder.
     */
    public CosmosClientBuilder() {
        //  Build default connection policy with direct default connection config
        this.connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        //  Some default values
        this.userAgentSuffix = "";
        this.throttlingRetryOptions = new ThrottlingRetryOptions();
        this.clientTelemetryConfig = new CosmosClientTelemetryConfig();
        this.resetNonIdempotentWriteRetryPolicy();
    }

    CosmosClientBuilder metadataCaches(CosmosClientMetadataCachesSnapshot metadataCachesSnapshot) {
        this.state = metadataCachesSnapshot;
        return this;
    }

    CosmosClientMetadataCachesSnapshot metadataCaches() {
        return this.state;
    }

    /**
     * Sets an apiType for the builder.
     * @param apiType
     * @return current cosmosClientBuilder
     */
    CosmosClientBuilder setApiType(ApiType apiType){
        this.apiType = apiType;
        return this;
    }

    /**
     * Returns apiType for the Builder.
     * @return
     */
    ApiType apiType(){ return this.apiType; }

    /**
     * Session capturing is enabled by default for {@link ConsistencyLevel#SESSION}.
     * For other consistency levels, it is not needed, unless if you need occasionally send requests with Session
     * Consistency while the client is not configured in session.
     * <p>
     * enabling Session capturing for Session mode has no effect.
     * @param sessionCapturingOverrideEnabled session capturing override
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder sessionCapturingOverrideEnabled(boolean sessionCapturingOverrideEnabled) {
        this.sessionCapturingOverrideEnabled = sessionCapturingOverrideEnabled;
        return this;
    }

    /**
     * Indicates if Session capturing is enabled for non Session modes.
     * The default is false.
     *
     * @return the session capturing override
     */
    boolean isSessionCapturingOverrideEnabled() {
        return this.sessionCapturingOverrideEnabled;
    }

    /**
     * Enables connections sharing across multiple Cosmos Clients. The default is false.
     * <br/>
     * <br/>
     * <pre>
     * {@code
     * CosmosAsyncClient client1 = new CosmosClientBuilder()
     *         .endpoint(serviceEndpoint1)
     *         .key(key1)
     *         .consistencyLevel(ConsistencyLevel.SESSION)
     *         .connectionSharingAcrossClientsEnabled(true)
     *         .buildAsyncClient();
     *
     * CosmosAsyncClient client2 = new CosmosClientBuilder()
     *         .endpoint(serviceEndpoint2)
     *         .key(key2)
     *         .consistencyLevel(ConsistencyLevel.SESSION)
     *         .connectionSharingAcrossClientsEnabled(true)
     *         .buildAsyncClient();
     *
     * // when configured this way client1 and client2 will share connections when possible.
     * }
     * </pre>
     * <br/>
     * When you have multiple instances of Cosmos Client in the same JVM interacting to multiple Cosmos accounts,
     * enabling this allows connection sharing in Direct mode if possible between instances of Cosmos Client.
     * <br/>
     * Please note, when setting this option, the connection configuration (e.g., socket timeout config, idle timeout
     * config) of the first instantiated client will be used for all other client instances.
     * <br/>
     * @param connectionSharingAcrossClientsEnabled connection sharing
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder connectionSharingAcrossClientsEnabled(boolean connectionSharingAcrossClientsEnabled) {
        this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
        return this;
    }

    /**
     * Indicates whether connection sharing is enabled. The default is false.
     * <br/>
     * When you have multiple instances of Cosmos Client in the same JVM interacting to multiple Cosmos accounts,
     * enabling this allows connection sharing in Direct mode if possible between instances of Cosmos Client.
     * <br/>
     * @return the connection sharing across multiple clients
     */
    boolean isConnectionSharingAcrossClientsEnabled() {
        return this.connectionSharingAcrossClientsEnabled;
    }

    /**
     * Gets the token resolver
     * <br/>
     * @return the token resolver
     */
    CosmosAuthorizationTokenResolver getAuthorizationTokenResolver() {
        return cosmosAuthorizationTokenResolver;
    }

    /**
     * Sets the token resolver
     *
     * @param cosmosAuthorizationTokenResolver the token resolver
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder authorizationTokenResolver(
        CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver) {
        this.cosmosAuthorizationTokenResolver = Objects.requireNonNull(cosmosAuthorizationTokenResolver,
            "'cosmosAuthorizationTokenResolver' cannot be null.");
        this.keyOrResourceToken = null;
        this.credential = null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets the Azure Cosmos DB endpoint the SDK will connect to
     *
     * @return the endpoint
     */
    String getEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Sets the Azure Cosmos DB endpoint the SDK will connect to
     *
     * @param endpoint the service endpoint
     * @return current Builder
     */
    @Override
    public CosmosClientBuilder endpoint(String endpoint) {
        this.serviceEndpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Gets either a master or readonly key used to perform authentication
     * for accessing resource.
     *
     * @return the key
     */
    String getKey() {
        return keyOrResourceToken;
    }

    /**
     * Sets either a master or readonly key used to perform authentication
     * for accessing resource.
     *
     * @param key master or readonly key
     * @return current Builder.
     */
    public CosmosClientBuilder key(String key) {
        this.keyOrResourceToken = Objects.requireNonNull(key, "'key' cannot be null.");
        this.cosmosAuthorizationTokenResolver = null;
        this.credential = null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets a resource token used to perform authentication
     * for accessing resource.
     *
     * @return the resourceToken
     */
    String getResourceToken() {
        return keyOrResourceToken;
    }

    /**
     * Sets a resource token used to perform authentication
     * for accessing resource.
     *
     * @param resourceToken resourceToken for authentication
     * @return current Builder.
     */
    public CosmosClientBuilder resourceToken(String resourceToken) {
        this.keyOrResourceToken = Objects.requireNonNull(resourceToken, "'resourceToken' cannot be null.");
        this.cosmosAuthorizationTokenResolver = null;
        this.credential = null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets a token credential instance used to perform authentication
     * for accessing resource.
     *
     * @return the token credential.
     */
    TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the updated CosmosClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public CosmosClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosAuthorizationTokenResolver = null;
        this.credential = null;
        this.permissions = null;
        return this;
    }

    /**
     * Gets the permission list, which contains the
     * resource tokens needed to access resources.
     *
     * @return the permission list
     */
    List<CosmosPermissionProperties> getPermissions() {
        return permissions;
    }

    /**
     * Sets the permission list, which contains the
     * resource tokens needed to access resources.
     *
     * @param permissions Permission list for authentication.
     * @return current Builder.
     */
    public CosmosClientBuilder permissions(List<CosmosPermissionProperties> permissions) {
        this.permissions = Objects.requireNonNull(permissions, "'permissions' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosAuthorizationTokenResolver = null;
        this.credential = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets the {@link ConsistencyLevel} to be used
     * <br/>
     * By default, {@link ConsistencyLevel#SESSION} consistency will be used.
     * <br/>
     * @return the consistency level
     */
    ConsistencyLevel getConsistencyLevel() {
        return this.desiredConsistencyLevel;
    }

    /**
     * Sets the {@link ConsistencyLevel} to be used
     * <br/>
     * By default, {@link ConsistencyLevel#SESSION} consistency will be used.
     *
     * @param desiredConsistencyLevel {@link ConsistencyLevel}
     * @return current Builder
     */
    public CosmosClientBuilder consistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
        this.desiredConsistencyLevel = desiredConsistencyLevel;
        return this;
    }

    /**
     * Gets the (@link ConnectionPolicy) to be used
     *
     * @return the connection policy
     */
    ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Gets the {@link AzureKeyCredential} to be used
     *
     * @return {@link AzureKeyCredential}
     */
    AzureKeyCredential getCredential() {
        return credential;
    }

    /**
     * Gets the {@link CosmosContainerProactiveInitConfig} to be used
     *
     * @return {@link CosmosContainerProactiveInitConfig}
     * */
    CosmosContainerProactiveInitConfig getProactiveContainerInitConfig() {
        return proactiveContainerInitConfig;
    }

    /**
     * Sets the {@link AzureKeyCredential} to be used
     *
     * @param credential {@link AzureKeyCredential}
     * @return current cosmosClientBuilder
     */
    @Override
    public CosmosClientBuilder credential(AzureKeyCredential credential) {
        this.credential = Objects.requireNonNull(credential, "'cosmosKeyCredential' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosAuthorizationTokenResolver = null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets the boolean which indicates whether to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     * <br/>
     * If set to false (which is by default), service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it
     * on the client.
     * <br/>
     * By-default, this is false.
     *
     * @return a boolean indicating whether payload will be included in the response or not
     */
    boolean isContentResponseOnWriteEnabled() {
        return contentResponseOnWriteEnabled;
    }

    /**
     * Sets the boolean to only return the headers and status code in Cosmos DB response
     * in case of Create, Update and Delete operations on CosmosItem.
     * <br/>
     * If set to false (which is by default), service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     * <br/>
     * This feature does not impact RU usage for read or write operations.
     * <br/>
     * By-default, this is false.
     *
     * @param contentResponseOnWriteEnabled a boolean indicating whether payload will be included in the response or not
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder contentResponseOnWriteEnabled(boolean contentResponseOnWriteEnabled) {
        this.contentResponseOnWriteEnabled = contentResponseOnWriteEnabled;
        return this;
    }

    /**
     * Sets the default GATEWAY connection configuration to be used.
     *
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder gatewayMode() {
        this.gatewayConnectionConfig = GatewayConnectionConfig.getDefaultConfig();
        return this;
    }

    /**
     * Sets the GATEWAY connection configuration to be used.
     *
     * @param gatewayConnectionConfig gateway connection configuration
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder gatewayMode(GatewayConnectionConfig gatewayConnectionConfig) {
        this.gatewayConnectionConfig = gatewayConnectionConfig;
        return this;
    }

    /**
     * Sets the default DIRECT connection configuration to be used.
     * <br/>
     * By default, the builder is initialized with directMode()
     *
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder directMode() {
        this.directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        return this;
    }

    /**
     * Sets the DIRECT connection configuration to be used.
     * <br/>
     * By default, the builder is initialized with directMode()
     *
     * @param directConnectionConfig direct connection configuration
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder directMode(DirectConnectionConfig directConnectionConfig) {
        this.directConnectionConfig = directConnectionConfig;
        return this;
    }

    /**
     * Sets the DIRECT connection configuration to be used.
     * gatewayConnectionConfig - represents basic configuration to be used for gateway client.
     * <br/>
     * Even in direct connection mode, some of the meta data operations go through gateway client,
     * <br/>
     * Setting gateway connection config in this API doesn't affect the connection mode,
     * which will be Direct in this case.
     *
     * @param directConnectionConfig direct connection configuration to be used
     * @param gatewayConnectionConfig gateway connection configuration to be used
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder directMode(DirectConnectionConfig directConnectionConfig, GatewayConnectionConfig gatewayConnectionConfig) {
        this.directConnectionConfig = directConnectionConfig;
        this.gatewayConnectionConfig = gatewayConnectionConfig;
        return this;
    }

    /**
     * sets the value of the user-agent suffix.
     *
     * @param userAgentSuffix The value to be appended to the user-agent header, this is
     * used for monitoring purposes.
     *
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder userAgentSuffix(String userAgentSuffix) {
        this.userAgentSuffix = userAgentSuffix;
        return this;
    }

    /**
     * Sets the retry policy options associated with the DocumentClient instance.
     * <p>
     * Properties in the RetryOptions class allow application to customize the built-in
     * retry policies. This property is optional. When it's not set, the SDK uses the
     * default values for configuring the retry policies.  See RetryOptions class for
     * more details.
     *
     * @param throttlingRetryOptions the RetryOptions instance.
     * @return current CosmosClientBuilder
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public CosmosClientBuilder throttlingRetryOptions(ThrottlingRetryOptions throttlingRetryOptions) {
        this.throttlingRetryOptions = throttlingRetryOptions;
        return this;
    }

    /**
     * Sets the preferred regions for geo-replicated database accounts. For example,
     * "East US" as the preferred region.
     * <p>
     * When EnableEndpointDiscovery is true and PreferredRegions is non-empty,
     * the SDK will prefer to use the regions in the container in the order
     * they are specified to perform operations.
     * <p>
     * If EnableEndpointDiscovery is set to false, this property is ignored.
     *
     * @param preferredRegions the list of preferred regions.
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder preferredRegions(List<String> preferredRegions) {
        this.preferredRegions = preferredRegions;
        return this;
    }

    /**
     * Sets the flag to enable endpoint discovery for geo-replicated database accounts.
     * <p>
     * When EnableEndpointDiscovery is true, the SDK will automatically discover the
     * current write and read regions to ensure requests are sent to the correct region
     * based on the capability of the region and the user's preference.
     * <p>
     * The default value for this property is true indicating endpoint discovery is enabled.
     *
     * @param endpointDiscoveryEnabled true if EndpointDiscovery is enabled.
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder endpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
        this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
        return this;
    }

    /**
     * Sets the flag to enable writes on any regions for geo-replicated database accounts in the Azure
     * Cosmos DB service.
     * <p>
     * When the value of this property is true, the SDK will direct write operations to
     * available writable regions of geo-replicated database account. Writable regions
     * are ordered by PreferredRegions property. Setting the property value
     * to true has no effect until EnableMultipleWriteRegions in DatabaseAccount
     * is also set to true.
     * <p>
     * DEFAULT value is true indicating that writes are directed to
     * available writable regions of geo-replicated database account.
     *
     * @param multipleWriteRegionsEnabled flag to enable writes on any regions for geo-replicated
     * database accounts.
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder multipleWriteRegionsEnabled(boolean multipleWriteRegionsEnabled) {
        this.multipleWriteRegionsEnabled = multipleWriteRegionsEnabled;
        return this;
    }

    /**
     * Sets the flag to enable client telemetry which will periodically collect
     * database operations aggregation statistics, system information like cpu/memory
     * and send it to cosmos monitoring service, which will be helpful during debugging.
     *<p>
     * DEFAULT value is false indicating this is opt in feature, by default no telemetry collection.
     *
     * @param clientTelemetryEnabled flag to enable client telemetry.
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder clientTelemetryEnabled(boolean clientTelemetryEnabled) {
        this.clientTelemetryEnabledOverride = clientTelemetryEnabled;
        return this;
    }

    /**
     * Sets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * <p>
     * DEFAULT value is true.
     * <p>
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #endpointDiscoveryEnabled} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @param readRequestsFallbackEnabled flag to enable reads to go to multiple regions configured on an account of
     * Azure Cosmos DB service.
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder readRequestsFallbackEnabled(boolean readRequestsFallbackEnabled) {
        this.readRequestsFallbackEnabled = readRequestsFallbackEnabled;
        return this;
    }

    /**
     * Sets a flag indicating whether to allow retries for write operations when the SDK can't guarantee that
     * the retry won't impact idempotency. This is for example the case, when retrying a createItem operation
     * after the initial attempt timed-out after writing the request payload on the network connection. It is
     * unclear whether the initial request ever reached the service and was processed there or not. The retry
     * could return a 409-Conflict response simply because the initial attempt was processed.
     * When enabling write retries even when idempotency is not guaranteed the SDK will apply a few extra
     * steps to minimize the risk for the caller of facing these idempotency issues due to retries. If enabled.
     * the SDK will use a system property "_trackingId" which will be stored in the documents to help
     * filter out failure conditions caused simply by retries for requests that have actually been processed already
     * by the service. For example a 409 on a retry would be mapped back to a 201 if the document has the same
     * _trackingId value the initial attempt to create the document used.
     * <p>
     * DEFAULT value is false.
     * <p>
     * NOTE: the setting on the CosmosClientBuilder will determine the default behavior for Create, Replace,
     * Upsert and Delete operations. It can be overridden on per-request base in the request options. For patch
     * operations by default (unless overridden in the request options) retries are always disabled by default
     * when the retry can't be guaranteed to be idempotent. The exception for patch is used because whether
     * a retry is "safe" for a patch operation really depends on the set of patch instructions. The documentation
     * for the patch operation has more details.
     *
     * @param useTrackingIdPropertyForCreateAndReplace a flag indicating whether write operations can use the
     * trackingId system property '/_trackingId' to allow identification of conflicts and pre-condition failures due to
     * retries. If enabled, each document being created or replaced will have an additional '/_trackingId' property
     * for which the value will be updated by the SDK. If it is not desired to add this new json property (for example
     * due to the RU-increase based on the payload size or because it causes documents to exceed the 2 MB upper limit),
     * the usage of this system property can be disabled by setting this parameter to false. This means there could be
     * a higher level of 409/312 due to retries - and applications would need to handle them gracefully on their own.
     * @return current CosmosClientBuilder
     */
    CosmosClientBuilder enableNonIdempotentWriteRetries(boolean useTrackingIdPropertyForCreateAndReplace) {
        if (useTrackingIdPropertyForCreateAndReplace) {
            this.writeRetryPolicy = WriteRetryPolicy.WITH_TRACKING_ID;
        } else {
            this.writeRetryPolicy = WriteRetryPolicy.WITH_RETRIES;
        }
        return this;
    }

    WriteRetryPolicy getNonIdempotentWriteRetryPolicy()
    {
        return this.writeRetryPolicy;
    }

    void resetNonIdempotentWriteRetryPolicy()
    {
        String writePolicyName = Configs.getNonIdempotentWriteRetryPolicy();
        if (writePolicyName != null) {
            if (writePolicyName.equalsIgnoreCase("NO_RETRIES")) {
                this.writeRetryPolicy = WriteRetryPolicy.DISABLED;
                return;
            } else if (writePolicyName.equalsIgnoreCase("WITH_TRACKING_ID")) {
                this.writeRetryPolicy = WriteRetryPolicy.WITH_TRACKING_ID;
                return;
            } else if (writePolicyName.equalsIgnoreCase("WITH_RETRIES")) {
                this.writeRetryPolicy = WriteRetryPolicy.WITH_RETRIES;
                return;
            }
        }
        this.writeRetryPolicy = WriteRetryPolicy.DISABLED;
    }

    /**
     * Sets the {@link CosmosContainerProactiveInitConfig} which enable warming up of caches and connections
     * associated with containers obtained from {@link CosmosContainerProactiveInitConfig#getCosmosContainerIdentities()} to replicas
     * obtained from the first <em>k</em> preferred regions where <em>k</em> evaluates to {@link CosmosContainerProactiveInitConfig#getProactiveConnectionRegionsCount()}.
     *
     * <p>
     *     Use the {@link CosmosContainerProactiveInitConfigBuilder} class to instantiate {@link CosmosContainerProactiveInitConfig} class
     * </p>
     * @param proactiveContainerInitConfig which encapsulates a list of container identities and no of
     *                                     proactive connection regions
     * @return current CosmosClientBuilder
     * */
    public CosmosClientBuilder openConnectionsAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        this.proactiveContainerInitConfig = proactiveContainerInitConfig;
        return this;
    }

    /**
     * Gets the GATEWAY connection configuration to be used.
     *
     * @return gateway connection config
     */
    GatewayConnectionConfig getGatewayConnectionConfig() {
        return gatewayConnectionConfig;
    }

    /**
     * Gets the DIRECT connection configuration to be used.
     *
     * @return direct connection config
     */
    DirectConnectionConfig getDirectConnectionConfig() {
        return directConnectionConfig;
    }

    /**
     * Gets the value of user-agent suffix.
     *
     * @return the value of user-agent suffix.
     */
    String getUserAgentSuffix() {
        return userAgentSuffix;
    }

    /**
     * Gets the retry policy options associated with the DocumentClient instance.
     *
     * @return the RetryOptions instance.
     */
    ThrottlingRetryOptions getThrottlingRetryOptions() {
        return throttlingRetryOptions;
    }

    /**
     * Gets the preferred regions for geo-replicated database accounts
     *
     * @return the list of preferred region.
     */
    List<String> getPreferredRegions() {
        return preferredRegions != null ? preferredRegions : Collections.emptyList();
    }

    /**
     * Gets the flag to enable endpoint discovery for geo-replicated database accounts.
     *
     * @return whether endpoint discovery is enabled.
     */
    boolean isEndpointDiscoveryEnabled() {
        return endpointDiscoveryEnabled;
    }

    /**
     * Gets the flag to enable writes on any regions for geo-replicated database accounts in the Azure
     * Cosmos DB service.
     * <p>
     * When the value of this property is true, the SDK will direct write operations to
     * available writable regions of geo-replicated database account. Writable regions
     * are ordered by PreferredRegions property. Setting the property value
     * to true has no effect until EnableMultipleWriteRegions in DatabaseAccount
     * is also set to true.
     * <p>
     * DEFAULT value is true indicating that writes are directed to
     * available writable regions of geo-replicated database account.
     *
     * @return flag to enable writes on any regions for geo-replicated database accounts.
     */
    boolean isMultipleWriteRegionsEnabled() {
        return multipleWriteRegionsEnabled;
    }

    /**
     * Gets the flag to enabled client telemetry.
     *
     * @return flag to enable client telemetry.
     */
    boolean isClientTelemetryEnabled() {
        Boolean explicitlySetInConfig = ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .isSendClientTelemetryToServiceEnabled(this.clientTelemetryConfig);

        if (this.clientTelemetryEnabledOverride != null) {
            return this.clientTelemetryEnabledOverride;
        }

        if (explicitlySetInConfig != null) {
            return explicitlySetInConfig;
        }

        return ClientTelemetry.DEFAULT_CLIENT_TELEMETRY_ENABLED;
    }

    /**
     * Gets whether to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     * <p>
     * DEFAULT value is true.
     * <p>
     * If this property is not set, the default is true for all Consistency Levels other than Bounded Staleness,
     * The default is false for Bounded Staleness.
     * 1. {@link #endpointDiscoveryEnabled} is true
     * 2. the Azure Cosmos DB account has more than one region
     *
     * @return flag to allow for reads to go to multiple regions configured on an account of Azure Cosmos DB service.
     */
    boolean isReadRequestsFallbackEnabled() {
        return readRequestsFallbackEnabled;
    }

    /**
     * Returns the client telemetry config instance for this builder
     * @return the client telemetry config instance for this builder
     */
    CosmosClientTelemetryConfig getClientTelemetryConfig() {
        return this.clientTelemetryConfig;
    }

    /**
     * Returns the client telemetry config instance for this builder
     * @param telemetryConfig the client telemetry configuration to be used
     * @return current CosmosClientBuilder
     */
    public CosmosClientBuilder clientTelemetryConfig(CosmosClientTelemetryConfig telemetryConfig) {
        ifThrowIllegalArgException(telemetryConfig == null,
            "Parameter 'telemetryConfig' must not be null.");

        Boolean explicitValueFromConfig = ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .isSendClientTelemetryToServiceEnabled(telemetryConfig);
        if (explicitValueFromConfig != null) {
            this.clientTelemetryEnabledOverride = null;
        }

        this.clientTelemetryConfig = telemetryConfig;

        return this;
    }

    /**
     * Builds a cosmos async client with the provided properties
     *
     * @return CosmosAsyncClient
     */
    public CosmosAsyncClient buildAsyncClient() {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        validateConfig();
        buildConnectionPolicy();
        CosmosAsyncClient cosmosAsyncClient = new CosmosAsyncClient(this);
        cosmosAsyncClient.openConnectionsAndInitCaches();
        logStartupInfo(stopwatch, cosmosAsyncClient);
        return cosmosAsyncClient;
    }

    /**
     * Builds a cosmos sync client with the provided properties
     *
     * @return CosmosClient
     */
    public CosmosClient buildClient() {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        validateConfig();
        buildConnectionPolicy();
        CosmosClient cosmosClient = new CosmosClient(this);
        cosmosClient.openConnectionsAndInitCaches();
        logStartupInfo(stopwatch, cosmosClient.asyncClient());
        return cosmosClient;
    }

    //  Connection policy has to be built before it can be used by this builder
    ConnectionPolicy buildConnectionPolicy() {
        if (this.directConnectionConfig != null) {
            //  Check if the user passed additional gateway connection configuration
            //  If this is null, initialize with default values
            if (this.gatewayConnectionConfig == null) {
                this.gatewayConnectionConfig = GatewayConnectionConfig.getDefaultConfig();
            }
            this.connectionPolicy = new ConnectionPolicy(directConnectionConfig, gatewayConnectionConfig);
        } else if (gatewayConnectionConfig != null) {
            this.connectionPolicy = new ConnectionPolicy(gatewayConnectionConfig);
        }
        this.connectionPolicy.setPreferredRegions(this.preferredRegions);
        this.connectionPolicy.setUserAgentSuffix(this.userAgentSuffix);
        this.connectionPolicy.setThrottlingRetryOptions(this.throttlingRetryOptions);
        this.connectionPolicy.setEndpointDiscoveryEnabled(this.endpointDiscoveryEnabled);
        this.connectionPolicy.setMultipleWriteRegionsEnabled(this.multipleWriteRegionsEnabled);
        this.connectionPolicy.setReadRequestsFallbackEnabled(this.readRequestsFallbackEnabled);
        return this.connectionPolicy;
    }

    private void validateConfig() {
        URI uri;
        try {
            uri = new URI(serviceEndpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid serviceEndpoint", e);
        }

        if (preferredRegions != null) {
            // validate preferredRegions
            preferredRegions.forEach(
                preferredRegion -> {
                    Preconditions.checkArgument(StringUtils.trimToNull(preferredRegion) != null, "preferredRegion can't be empty");
                    String trimmedPreferredRegion = preferredRegion.toLowerCase(Locale.ROOT).replace(" ", "");
                    LocationHelper.getLocationEndpoint(uri, trimmedPreferredRegion);
                }
            );
        }

        if (proactiveContainerInitConfig != null) {
            Preconditions.checkArgument(preferredRegions != null, "preferredRegions cannot be null when proactiveContainerInitConfig has been set");
            Preconditions.checkArgument(this.proactiveContainerInitConfig.getProactiveConnectionRegionsCount() <= this.preferredRegions.size(), "no. of regions to proactively connect to " +
                    "cannot be greater than the no.of preferred regions");
        }

        ifThrowIllegalArgException(this.serviceEndpoint == null,
            "cannot buildAsyncClient client without service endpoint");
        ifThrowIllegalArgException(
            this.keyOrResourceToken == null && (permissions == null || permissions.isEmpty())
                && this.credential == null && this.tokenCredential == null && this.cosmosAuthorizationTokenResolver == null,
            "cannot buildAsyncClient client without any one of key, resource token, permissions, and "
                + "azure key credential");
        ifThrowIllegalArgException(credential != null && StringUtils.isEmpty(credential.getKey()),
            "cannot buildAsyncClient client without key credential");
    }

    Configs configs() {
        return configs;
    }

    /**
     * Configs
     *
     * @return current cosmosClientBuilder
     */
    CosmosClientBuilder configs(Configs configs) {
        this.configs = configs;
        return this;
    }

    private void ifThrowIllegalArgException(boolean value, String error) {
        if (value) {
            throw new IllegalArgumentException(error);
        }
    }

    private void logStartupInfo(StopWatch stopwatch, CosmosAsyncClient client) {
        stopwatch.stop();

        if (logger.isInfoEnabled()) {
            long time = stopwatch.getTime();
            // NOTE: if changing the logging below - do not log any confidential info like master key credentials etc.
            logger.info("Cosmos Client with (Correlation) ID [{}] started up in [{}] ms with the following " +
                    "configuration: serviceEndpoint [{}], preferredRegions [{}], connectionPolicy [{}], " +
                    "consistencyLevel [{}], contentResponseOnWriteEnabled [{}], sessionCapturingOverride [{}], " +
                    "connectionSharingAcrossClients [{}], clientTelemetryEnabled [{}], proactiveContainerInit [{}].",
                client.getContextClient().getClientCorrelationId(), time, getEndpoint(), getPreferredRegions(),
                getConnectionPolicy(), getConsistencyLevel(), isContentResponseOnWriteEnabled(),
                isSessionCapturingOverrideEnabled(), isConnectionSharingAcrossClientsEnabled(),
                isClientTelemetryEnabled(), getProactiveContainerInitConfig());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        CosmosClientBuilderHelper.setCosmosClientBuilderAccessor(
            new CosmosClientBuilderHelper.CosmosClientBuilderAccessor() {

                @Override
                public void setCosmosClientMetadataCachesSnapshot(CosmosClientBuilder builder,
                                                                  CosmosClientMetadataCachesSnapshot metadataCache) {
                    builder.metadataCaches(metadataCache);
                }

                @Override
                public CosmosClientMetadataCachesSnapshot getCosmosClientMetadataCachesSnapshot(CosmosClientBuilder builder) {
                    return builder.metadataCaches();
                }

                @Override
                public void setCosmosClientApiType(CosmosClientBuilder builder, ApiType apiType) {
                    builder.setApiType(apiType);
                }

                @Override
                public ApiType getCosmosClientApiType(CosmosClientBuilder builder) {
                    return builder.apiType();
                }

                @Override
                public ConnectionPolicy getConnectionPolicy(CosmosClientBuilder builder) {
                    return builder.getConnectionPolicy();
                }

                @Override
                public ConnectionPolicy buildConnectionPolicy(CosmosClientBuilder builder) {
                    return builder.buildConnectionPolicy();
                }

                @Override
                public Configs getConfigs(CosmosClientBuilder builder) {
                    return builder.configs();
                }

                @Override
                public ConsistencyLevel getConsistencyLevel(CosmosClientBuilder builder) {
                    return builder.getConsistencyLevel();
                }

                @Override
                public String getEndpoint(CosmosClientBuilder builder) {
                    return builder.getEndpoint();
                }
            });
    }

    static { initialize(); }
}
