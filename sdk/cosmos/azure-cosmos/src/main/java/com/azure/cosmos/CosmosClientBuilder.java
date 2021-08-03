// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CosmosAuthorizationTokenResolver;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.implementation.routing.LocationHelper;
import com.azure.cosmos.models.CosmosPermissionProperties;
import static com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientBuilderHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
public class CosmosClientBuilder {
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
    private boolean clientTelemetryEnabled = false;

    /**
     * Instantiates a new Cosmos client builder.
     */
    public CosmosClientBuilder() {
        //  Build default connection policy with direct default connection config
        this.connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        //  Some default values
        this.userAgentSuffix = "";
        this.throttlingRetryOptions = new ThrottlingRetryOptions();
    }

    CosmosClientBuilder metadataCaches(CosmosClientMetadataCachesSnapshot metadataCachesSnapshot) {
        this.state = metadataCachesSnapshot;
        return this;
    }

    CosmosClientMetadataCachesSnapshot metadataCaches() {
        return this.state;
    }

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
     *
     *
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
     *
     * When you have multiple instances of Cosmos Client in the same JVM interacting to multiple Cosmos accounts,
     * enabling this allows connection sharing in Direct mode if possible between instances of Cosmos Client.
     *
     * Please note, when setting this option, the connection configuration (e.g., socket timeout config, idle timeout
     * config) of the first instantiated client will be used for all other client instances.
     *
     * @param connectionSharingAcrossClientsEnabled connection sharing
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder connectionSharingAcrossClientsEnabled(boolean connectionSharingAcrossClientsEnabled) {
        this.connectionSharingAcrossClientsEnabled = connectionSharingAcrossClientsEnabled;
        return this;
    }

    /**
     * Indicates whether connection sharing is enabled. The default is false.
     *
     * When you have multiple instances of Cosmos Client in the same JVM interacting to multiple Cosmos accounts,
     * enabling this allows connection sharing in Direct mode if possible between instances of Cosmos Client.
     *
     * @return the connection sharing across multiple clients
     */
    boolean isConnectionSharingAcrossClientsEnabled() {
        return this.connectionSharingAcrossClientsEnabled;
    }

    /**
     * Gets the token resolver
     *
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
    CosmosClientBuilder authorizationTokenResolver(
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
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential}.
     * @return the updated CosmosClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
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
     *
     * By default, {@link ConsistencyLevel#SESSION} consistency will be used.
     *
     * @return the consistency level
     */
    ConsistencyLevel getConsistencyLevel() {
        return this.desiredConsistencyLevel;
    }

    /**
     * Sets the {@link ConsistencyLevel} to be used
     *
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
     * Sets the {@link AzureKeyCredential} to be used
     *
     * @param credential {@link AzureKeyCredential}
     * @return current cosmosClientBuilder
     */
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
     *
     * If set to false (which is by default), service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it
     * on the client.
     *
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
     *
     * If set to false (which is by default), service doesn't return payload in the response. It reduces networking
     * and CPU load by not sending the payload back over the network and serializing it on the client.
     *
     * This feature does not impact RU usage for read or write operations.
     *
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
     *
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
     *
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
     *
     * Even in direct connection mode, some of the meta data operations go through gateway client,
     *
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
        this.clientTelemetryEnabled = clientTelemetryEnabled;
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
        return clientTelemetryEnabled;
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
     * Builds a cosmos async client with the provided properties
     *
     * @return CosmosAsyncClient
     */
    public CosmosAsyncClient buildAsyncClient() {

        validateConfig();
        buildConnectionPolicy();
        return new CosmosAsyncClient(this);
    }

    /**
     * Builds a cosmos sync client with the provided properties
     *
     * @return CosmosClient
     */
    public CosmosClient buildClient() {

        validateConfig();
        buildConnectionPolicy();
        return new CosmosClient(this);
    }

    //  Connection policy has to be built before it can be used by this builder
    private void buildConnectionPolicy() {
        if (this.directConnectionConfig != null) {
            this.connectionPolicy = new ConnectionPolicy(directConnectionConfig);
            //  Check if the user passed additional gateway connection configuration
            if (this.gatewayConnectionConfig != null) {
                this.connectionPolicy.setMaxConnectionPoolSize(this.gatewayConnectionConfig.getMaxConnectionPoolSize());
                //  TODO(kuthapar): potential bug - when we expose requestTimeout from direct and gateway connection config,
                //   as gateway connection config will overwrite direct connection config settings
                this.connectionPolicy.setRequestTimeout(this.gatewayConnectionConfig.getRequestTimeout());
                this.connectionPolicy.setIdleHttpConnectionTimeout(this.gatewayConnectionConfig.getIdleConnectionTimeout());
                this.connectionPolicy.setProxy(this.gatewayConnectionConfig.getProxy());
            }
        } else if (gatewayConnectionConfig != null) {
            this.connectionPolicy = new ConnectionPolicy(gatewayConnectionConfig);
        }
        this.connectionPolicy.setPreferredRegions(this.preferredRegions);
        this.connectionPolicy.setUserAgentSuffix(this.userAgentSuffix);
        this.connectionPolicy.setThrottlingRetryOptions(this.throttlingRetryOptions);
        this.connectionPolicy.setEndpointDiscoveryEnabled(this.endpointDiscoveryEnabled);
        this.connectionPolicy.setMultipleWriteRegionsEnabled(this.multipleWriteRegionsEnabled);
        this.connectionPolicy.setReadRequestsFallbackEnabled(this.readRequestsFallbackEnabled);
        this.connectionPolicy.setClientTelemetryEnabled(this.clientTelemetryEnabled);
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
            preferredRegions.stream().forEach(
                preferredRegion -> {
                    Preconditions.checkArgument(StringUtils.trimToNull(preferredRegion) != null, "preferredRegion can't be empty");
                    String trimmedPreferredRegion = preferredRegion.toLowerCase(Locale.ROOT).replace(" ", "");
                    LocationHelper.getLocationEndpoint(uri, trimmedPreferredRegion);
                }
            );
        }

        ifThrowIllegalArgException(this.serviceEndpoint == null,
            "cannot buildAsyncClient client without service endpoint");
        ifThrowIllegalArgException(
            this.keyOrResourceToken == null && (permissions == null || permissions.isEmpty())
                && this.credential == null && this.tokenCredential == null,
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


    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
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
            });
    }
}
