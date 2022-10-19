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

    private final int maxDegreeOfParallelism;

    private final int maxBufferedItemCount;

    private final int responseContinuationTokenLimitInKb;

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
     * @param databaseThroughputConfig may be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "databaseThroughputConfig", "queryMetricsEnabled"})
    public CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        DatabaseThroughputConfig databaseThroughputConfig,
                        boolean queryMetricsEnabled) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.databaseThroughputConfig = databaseThroughputConfig;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.maxDegreeOfParallelism = 0;
        this.maxBufferedItemCount = 0;
        this.responseContinuationTokenLimitInKb = 0;
    }

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param databaseThroughputConfig may be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     * @param maxDegreeOfParallelism must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "databaseThroughputConfig", "queryMetricsEnabled", "maxDegreeOfParallelism"})
    CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                        DatabaseThroughputConfig databaseThroughputConfig,
                        boolean queryMetricsEnabled,
                        int maxDegreeOfParallelism) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.databaseThroughputConfig = databaseThroughputConfig;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        this.maxBufferedItemCount = 0;
        this.responseContinuationTokenLimitInKb = 0;
    }

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param databaseThroughputConfig may be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     * @param maxDegreeOfParallelism must not be {@literal null}
     * @param maxBufferedItemCount must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "databaseThroughputConfig", "queryMetricsEnabled", "maxDegreeOfParallelism", "maxBufferedItemCount"})
    CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                 DatabaseThroughputConfig databaseThroughputConfig,
                 boolean queryMetricsEnabled,
                 int maxDegreeOfParallelism,
                 int maxBufferedItemCount) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.databaseThroughputConfig = databaseThroughputConfig;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        this.maxBufferedItemCount = maxBufferedItemCount;
        this.responseContinuationTokenLimitInKb = 0;
    }

    /**
     * Initialization
     *
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param databaseThroughputConfig may be {@literal null}
     * @param queryMetricsEnabled must not be {@literal null}
     * @param maxDegreeOfParallelism must not be {@literal null}
     * @param maxBufferedItemCount must not be {@literal null}
     * @param responseContinuationTokenLimitInKb must not be {@literal null}
     */
    @ConstructorProperties({"responseDiagnosticsProcessor", "databaseThroughputConfig", "queryMetricsEnabled", "maxDegreeOfParallelism", "maxBufferedItemCount", "responseContinuationTokenLimitInKb"})
    CosmosConfig(ResponseDiagnosticsProcessor responseDiagnosticsProcessor,
                 DatabaseThroughputConfig databaseThroughputConfig,
                 boolean queryMetricsEnabled,
                 int maxDegreeOfParallelism,
                 int maxBufferedItemCount,
                 int responseContinuationTokenLimitInKb) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.databaseThroughputConfig = databaseThroughputConfig;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        this.maxBufferedItemCount = maxBufferedItemCount;
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
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
     * Gets the value of maxDegreeOfParallelism
     *
     * @return int, value of maxDegreeOfParallelism
     */
    public int getMaxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    /**
     * Gets the value of maxBufferedItemCount
     *
     * @return int, value of maxBufferedItemCount
     */
    public int getMaxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    /**
     * Gets the value of responseContinuationTokenLimitInKb
     *
     * @return int, value of responseContinuationTokenLimitInKb
     */
    public int getResponseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
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
        private int maxDegreeOfParallelism;
        private int maxBufferedItemCount;
        private int responseContinuationTokenLimitInKb;
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
         * Set maxDegreeOfParallelism
         *
         * @param maxDegreeOfParallelism value to initialize
         * @return CosmosConfigBuilder
         */
        public CosmosConfigBuilder maxDegreeOfParallelism(int maxDegreeOfParallelism) {
            this.maxDegreeOfParallelism = maxDegreeOfParallelism;
            return this;
        }

        /**
         * Set maxBufferedItemCount
         *
         * @param maxBufferedItemCount value to initialize
         * @return CosmosConfigBuilder
         */
        public CosmosConfigBuilder maxBufferedItemCount(int maxBufferedItemCount) {
            this.maxBufferedItemCount = maxBufferedItemCount;
            return this;
        }

        /**
         * Set responseContinuationTokenLimitInKb
         *
         * @param responseContinuationTokenLimitInKb value to initialize
         * @return CosmosConfigBuilder
         */
        public CosmosConfigBuilder responseContinuationTokenLimitInKb(int responseContinuationTokenLimitInKb) {
            this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
            return this;
        }

        /**
         * Enable database throughput
         *
         * @param autoscale Autoscaling
         * @param requestUnits Request units
         * @return CosmosConfigBuilder
         */
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
            return new CosmosConfig(this.responseDiagnosticsProcessor, this.databaseThroughputConfig, this.queryMetricsEnabled,
                this.maxDegreeOfParallelism, this.maxBufferedItemCount, this.responseContinuationTokenLimitInKb);
        }

        @Override
        public String toString() {
            return "CosmosConfigBuilder{"
                + "responseDiagnosticsProcessor=" + responseDiagnosticsProcessor
                + ", databaseThroughputConfig=" + databaseThroughputConfig
                + ", queryMetricsEnabled=" + queryMetricsEnabled
                + ", maxDegreeOfParallelism=" + maxDegreeOfParallelism
                + ", maxBufferedItemCount=" + maxBufferedItemCount
                + ", responseContinuationTokenLimitInKb=" + responseContinuationTokenLimitInKb
                + '}';
        }
    }
}
