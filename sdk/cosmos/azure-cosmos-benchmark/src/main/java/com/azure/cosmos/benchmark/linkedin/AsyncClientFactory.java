package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.benchmark.Configuration;
import com.google.common.base.Preconditions;


/**
 * Factory for initializing an Async client for all CosmosDB operations
 *
 * TODO: Configure the client according to how it's done
 */
public class AsyncClientFactory {

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
            .contentResponseOnWriteEnabled(Boolean.parseBoolean(cfg.isContentResponseOnWriteEnabled()));

        // Configure the Direct/Gateway mode
        if (cfg.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder
                .directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            final GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig()
                .setMaxConnectionPoolSize(cfg.getMaxConnectionPoolSize());
            cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }

        return cosmosClientBuilder.buildAsyncClient();
    }
}
