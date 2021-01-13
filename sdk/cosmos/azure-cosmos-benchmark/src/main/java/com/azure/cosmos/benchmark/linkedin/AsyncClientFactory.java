package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.benchmark.Configuration;
import com.google.common.base.Preconditions;
import java.time.Duration;


/**
 * Factory for initializing an Async client for all CosmosDB operations
 *
 * Different values for the DirectConnection and GatewayConnectionConfigs are explicitly defined.
 * In most cases, the value is the default from the SDK at the time the class was implemented.
 * This will allow us to tweak each param, and observe performance changes + also decouple the CTL
 * from changes to the default values.
 */
public class AsyncClientFactory {

    private static final DirectConnectionConfig DIRECT_CONNECTION_CONFIG = defaultDirectConfig();
    private static final GatewayConnectionConfig GATEWAY_CONNECTION_CONFIG = defaultGatewayConfig();
    private static final ThrottlingRetryOptions THROTTLING_RETRY_OPTIONS = defaultThrottlingRetryOptions();

    /**
     * Prevent direct initialization
     */
    private AsyncClientFactory() {
    }

    /**
     * Builds a Cosmos async client using the configuration options defined
     *
     * @param cfg Configuration encapsulating options for configuring the AsyncClient
     * @return CosmosAsyncClient initialized using the parameters in the Configuration
     */
    public static CosmosAsyncClient buildAsyncClient(final Configuration cfg) {
        Preconditions.checkNotNull(cfg, "The Workload configuration defining the parameters can not be null");
        final CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(cfg.getServiceEndpoint())
            .key(cfg.getMasterKey())
            .consistencyLevel(cfg.getConsistencyLevel())
            .throttlingRetryOptions(THROTTLING_RETRY_OPTIONS)
            .contentResponseOnWriteEnabled(Boolean.parseBoolean(cfg.isContentResponseOnWriteEnabled()));

        // Configure the Direct/Gateway mode
        if (cfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder.directMode(DIRECT_CONNECTION_CONFIG, GATEWAY_CONNECTION_CONFIG);
        } else {

            cosmosClientBuilder.gatewayMode(GATEWAY_CONNECTION_CONFIG);
        }

        return cosmosClientBuilder
            .endpointDiscoveryEnabled(false)
            .multipleWriteRegionsEnabled(false)
            .buildAsyncClient();
    }

    private static DirectConnectionConfig defaultDirectConfig() {
        return new DirectConnectionConfig()
            .setConnectTimeout(Duration.ofSeconds(5L)) // Default
            .setConnectionEndpointRediscoveryEnabled(true) // Custom
            .setIdleEndpointTimeout(Duration.ofHours(1L)) // Default
            .setIdleConnectionTimeout(Duration.ofSeconds(60)) // Custom
            .setMaxConnectionsPerEndpoint(130) // Default
            .setMaxRequestsPerConnection(30); // Default
    }

    private static GatewayConnectionConfig defaultGatewayConfig() {
        return new GatewayConnectionConfig()
            .setMaxConnectionPoolSize(3200) // Custom
            .setIdleConnectionTimeout(Duration.ofSeconds(60)); // Default
    }

    private static ThrottlingRetryOptions defaultThrottlingRetryOptions() {
        return new ThrottlingRetryOptions()
            .setMaxRetryAttemptsOnThrottledRequests(0) // Custom
            .setMaxRetryWaitTime(Duration.ofMillis(0)); // Custom
    }
}
