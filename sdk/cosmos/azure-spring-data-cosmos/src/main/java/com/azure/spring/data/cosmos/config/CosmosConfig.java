// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.config;

import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;

import java.beans.ConstructorProperties;

/**
 * Config properties of CosmosDB
 */
public class CosmosConfig {

    private final ResponseDiagnosticsProcessor responseDiagnosticsProcessor;

    private final boolean queryMetricsEnabled;

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "queryMetricsEnabled"})
    public CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        boolean queryMetricsEnabled) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.queryMetricsEnabled = queryMetricsEnabled;
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
        private ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
        private boolean queryMetricsEnabled;
        CosmosConfigBuilder() {
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
            return new CosmosConfig(this.responseDiagnosticsProcessor, this.queryMetricsEnabled);
        }

        @Override
        public String toString() {
            return "CosmosConfigBuilder{"
                + "responseDiagnosticsProcessor=" + responseDiagnosticsProcessor
                + ", queryMetricsEnabled=" + queryMetricsEnabled
                + '}';
        }
    }
}
