// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.config;

import com.azure.cosmos.CosmosClientBuilder;

import java.beans.ConstructorProperties;

/**
 * Config properties of CosmosDB
 */
public class CosmosClientConfig {

    private final String database;

    private final CosmosClientBuilder cosmosClientBuilder;

    /**
     * Initialization
     *
     * @param cosmosClientBuilder must not be {@literal null}
     * @param database must not be {@literal null}
     */
    @ConstructorProperties({ "cosmosClientBuilder", "database" })
    public CosmosClientConfig(CosmosClientBuilder cosmosClientBuilder, String database) {
        this.cosmosClientBuilder = cosmosClientBuilder;
        this.database = database;
    }

    /**
     * Gets the cosmos client builder used to build cosmos client
     *
     * @return cosmosClientBuilder
     */
    public CosmosClientBuilder getCosmosClientBuilder() {
        return cosmosClientBuilder;
    }

    /**
     * Gets the database name
     *
     * @return database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Create a CosmosClientConfigBuilder instance
     *
     * @return CosmosClientConfigBuilder
     */
    public static CosmosClientConfigBuilder builder() {
        return new CosmosClientConfigBuilder();
    }

    /**
     * Builder class for cosmos client config
     */
    public static class CosmosClientConfigBuilder {
        private CosmosClientBuilder cosmosClientBuilder;
        private String database;
        CosmosClientConfigBuilder() {
        }

        /**
         * Set cosmosClientBuilder to use to build cosmos client
         *
         * @param cosmosClientBuilder cosmos client builder
         * @return CosmosConfigBuilder
         */
        public CosmosClientConfigBuilder cosmosClientBuilder(CosmosClientBuilder cosmosClientBuilder) {
            this.cosmosClientBuilder = cosmosClientBuilder;
            return this;
        }

        /**
         * Sets the database
         *
         * @param database database name
         * @return CosmosClientConfigBuilder
         */
        public CosmosClientConfigBuilder database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Build a CosmosClientConfig instance
         *
         * @return CosmosClientConfig
         */
        public CosmosClientConfig build() {
            return new CosmosClientConfig(this.cosmosClientBuilder, this.database);
        }

        @Override
        public String toString() {
            return "CosmosClientConfigBuilder{"
                + "cosmosClientBuilder=" + cosmosClientBuilder
                + ", database='" + database + '\''
                + '}';
        }
    }
}
