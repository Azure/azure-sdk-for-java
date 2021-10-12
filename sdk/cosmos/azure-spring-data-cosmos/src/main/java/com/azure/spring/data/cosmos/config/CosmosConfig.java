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

    private final DatabaseThroughputConfig databaseThroughputConfig;

    private final boolean queryMetricsEnabled;

    private final boolean lazyPageTotalCount;

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "queryMetricsEnabled"})
    public CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        boolean queryMetricsEnabled) {
        this(responseDiagnosticsProcessor, null, queryMetricsEnabled);
    }

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param databaseThroughputConfig may be @{literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "databaseThroughputConfig", "queryMetricsEnabled"})
    public CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        DatabaseThroughputConfig databaseThroughputConfig,
                        boolean queryMetricsEnabled) {
        this(responseDiagnosticsProcessor, databaseThroughputConfig, queryMetricsEnabled, true);
    }

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param databaseThroughputConfig may be @{literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     * @param lazyPageTotalCount true if Page:getTotalCount should be lazily invoked
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "databaseThroughputConfig", "queryMetricsEnabled", "lazyPageTotalCount"})
    private CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        DatabaseThroughputConfig databaseThroughputConfig,
                        boolean queryMetricsEnabled, boolean lazyPageTotalCount) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.databaseThroughputConfig = databaseThroughputConfig;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.lazyPageTotalCount = lazyPageTotalCount;
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
     * Gets the option to enable lazy evaluation of page totals.
     *
     * @return boolean, whether to resolve the page count lazily.
     */
    public boolean isLazyPageTotalCount() {
        return lazyPageTotalCount;
    }

    /**
     * Gets the database throughput configuration.
     *
     * @return DatabaseThroughputConfig, or null if no database throughput is configured
     */
    public DatabaseThroughputConfig getDatabaseThroughputConfig() {
        return databaseThroughputConfig;
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
        private DatabaseThroughputConfig databaseThroughputConfig;
        private boolean queryMetricsEnabled;
        private boolean lazyPageTotalCount = true;

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
         * Enable eager fetching of Page:getTotalCount on CosmosTemplate#paginationQuery (all Repository paged requests).
         * By default, Page:getTotalCount will be lazily evaluated since retrieving the count of a query in cosmos can be
         * a very expensive operation.  This method may be used to instead evaluate the count at the time of the query.
         *
         * @return CosmosConfigBuilder
         */
        public CosmosConfigBuilder eagerFetchPageTotalCount() {
            this.lazyPageTotalCount = false;
            return this;
        }

        public CosmosConfigBuilder enableDatabaseThroughput(boolean autoscale, int requestUnits) {
            this.databaseThroughputConfig = new DatabaseThroughputConfig(autoscale, requestUnits);
            return this;
        }

        /**
         * Build a CosmosConfig instance
         *
         * @return CosmosConfig
         */
        public CosmosConfig build() {
            return new CosmosConfig(this.responseDiagnosticsProcessor, this.databaseThroughputConfig,
                                    this.queryMetricsEnabled, this.lazyPageTotalCount);
        }

        @Override
        public String toString() {
            return "CosmosConfigBuilder{"
                + "responseDiagnosticsProcessor=" + responseDiagnosticsProcessor
                + ", databaseThroughputConfig=" + databaseThroughputConfig
                + ", queryMetricsEnabled=" + queryMetricsEnabled
                + ", lazyPageTotalCount=" + lazyPageTotalCount
                + '}';
        }
    }
}
