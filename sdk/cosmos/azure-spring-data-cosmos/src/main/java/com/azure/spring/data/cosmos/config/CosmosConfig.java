// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.config;

import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;

import java.beans.ConstructorProperties;

/**
 * Config properties of CosmosDB
 */
public class CosmosConfig {

    private final String database;

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;

    private final boolean queryMetricsEnabled;

    /**
     * Initialization
     *
     * @param database name must not be {@literal null}
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     */
    @ConstructorProperties({"database", "responseDiagnosticsProcessor", "queryMetricsEnabled"})
    public CosmosConfig(String database, ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        boolean queryMetricsEnabled) {
        this.database = database;
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.queryMetricsEnabled = queryMetricsEnabled;
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
     * Gets response diagnostics processor
     *
     * @return ResponseDiagnosticsProcessor
     */
    public ResponseDiagnosticsProcessor getResponseDiagnosticsProcessor() {
        return responseDiagnosticsProcessor;
    }

    /**
     * Gets the option to enable query metrics
     *
     * @return boolean, whether to enable query metrics
     */
    public boolean isQueryMetricsEnabled() {
        return queryMetricsEnabled;
    }

    /**
     * Create a CosmosConfigBuilder instance
     *
     * @return CosmosConfigBuilder
     */
    public static CosmosConfigBuilder builder() {
        return new CosmosConfigBuilder();
    }

    /**
     * Builder class for cosmos config
     */
    public static class CosmosConfigBuilder {
        private String database;
        private ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
        private boolean queryMetricsEnabled;
        CosmosConfigBuilder() {
        }

        /**
         * Sets the database name
         * @param database database name
         * @return database name
         */
        public CosmosConfigBuilder database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Set responseDiagnosticsProcessor
         *
         * @param responseDiagnosticsProcessor value to initialize
         * @return CosmosConfigBuilder
         */
        public CosmosConfigBuilder responseDiagnosticsProcessor(ResponseDiagnosticsProcessor
                                                                    responseDiagnosticsProcessor) {
            this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
            return this;
        }

        /**
         * Set queryMetricsEnabled
         *
         * @param queryMetricsEnabled value to initialize
         * @return CosmosConfigBuilder
         */
        public CosmosConfigBuilder enableQueryMetrics(boolean queryMetricsEnabled) {
            this.queryMetricsEnabled = queryMetricsEnabled;
            return this;
        }

        /**
         * Build a CosmosConfig instance
         *
         * @return CosmosConfig
         */
        public CosmosConfig build() {
            return new CosmosConfig(this.database, this.responseDiagnosticsProcessor, this.queryMetricsEnabled);
        }

        @Override
        public String toString() {
            return "CosmosConfigBuilder{"
                + "database=" + database
                + "responseDiagnosticsProcessor=" + responseDiagnosticsProcessor
                + ", queryMetricsEnabled=" + queryMetricsEnabled
                + '}';
        }
    }
}
