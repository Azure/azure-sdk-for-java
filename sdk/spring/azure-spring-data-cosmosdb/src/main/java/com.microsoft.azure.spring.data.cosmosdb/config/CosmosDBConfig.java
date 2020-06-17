// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.config;

import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosKeyCredential;
import com.azure.data.cosmos.internal.RequestOptions;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnosticsProcessor;
import com.microsoft.azure.spring.data.cosmosdb.exception.CosmosDBAccessException;
import org.springframework.util.Assert;

import java.beans.ConstructorProperties;

/**
 * Config properties of CosmosDB
 */
public class CosmosDBConfig {
    private String uri;

    private String key;

    private String database;

    private ConnectionPolicy connectionPolicy;

    private ConsistencyLevel consistencyLevel;

    private boolean allowTelemetry;

    private RequestOptions requestOptions;

    private CosmosKeyCredential cosmosKeyCredential;

    private ResponseDiagnosticsProcessor responseDiagnosticsProcessor;

    private boolean populateQueryMetrics;

    /**
     * Initialization
     * @param uri must not be {@literal null}
     * @param key must not be {@literal null}
     * @param database must not be {@literal null}
     * @param connectionPolicy must not be {@literal null}
     * @param consistencyLevel must not be {@literal null}
     * @param allowTelemetry must not be {@literal null}
     * @param requestOptions must not be {@literal null}
     * @param cosmosKeyCredential must not be {@literal null}
     * @param responseDiagnosticsProcessor must not be {@literal null}
     * @param populateQueryMetrics must not be {@literal null}
     */
    @ConstructorProperties({"uri", "key", "database", "connectionPolicy", "consistencyLevel", "allowTelemetry",
                            "requestOptions", "cosmosKeyCredential", "responseDiagnosticsProcessor",
                            "populateQueryMetrics"})
    public CosmosDBConfig(String uri, String key, String database, ConnectionPolicy connectionPolicy,
                          ConsistencyLevel consistencyLevel, boolean allowTelemetry, RequestOptions requestOptions,
                          CosmosKeyCredential cosmosKeyCredential,
                          ResponseDiagnosticsProcessor responseDiagnosticsProcessor, boolean populateQueryMetrics) {
        this.uri = uri;
        this.key = key;
        this.database = database;
        this.connectionPolicy = connectionPolicy;
        this.consistencyLevel = consistencyLevel;
        this.allowTelemetry = allowTelemetry;
        this.requestOptions = requestOptions;
        this.cosmosKeyCredential = cosmosKeyCredential;
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
        this.populateQueryMetrics = populateQueryMetrics;
    }

    /**
     * Gets uri
     * @return uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets key
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets database
     * @return database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Gets connection policy
     * @return connectionPolicy
     */
    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Gets consistency level
     * @return ConsistencyLevel
     */
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Checks if telemetry is allowed
     * @return boolean
     */
    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    /**
     * Gets request options
     * @return RequestOptions
     */
    public RequestOptions getRequestOptions() {
        return requestOptions;
    }

    /**
     * Gets Cosmos key credential
     * @return CosmosKeyCredential
     */
    public CosmosKeyCredential getCosmosKeyCredential() {
        return cosmosKeyCredential;
    }

    /**
     * Gets response diagnostics processor
     * @return ResponseDiagnosticsProcessor
     */
    public ResponseDiagnosticsProcessor getResponseDiagnosticsProcessor() {
        return responseDiagnosticsProcessor;
    }

    /**
     * Checks if is populate query metrics
     * @return boolean
     */
    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    /**
     * Sets response diagnostics processor
     * @param responseDiagnosticsProcessor must not be {@literal null}
     */
    public void setResponseDiagnosticsProcessor(ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
    }

    /**
     * Sets populate query metrics
     * @param populateQueryMetrics must not be {@literal null}
     */
    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }

    /**
     * create a CosmosDBConfigBuilder with cosmos uri, cosmosKeyCredential and database name
     * @param uri must not be {@literal null}
     * @param cosmosKeyCredential must not be {@literal null}
     * @param database must not be {@literal null}
     * @return CosmosDBConfigBuilder
     */
    public static CosmosDBConfigBuilder builder(String uri, CosmosKeyCredential cosmosKeyCredential,
                                                String database) {
        return defaultBuilder()
            .uri(uri)
            .cosmosKeyCredential(cosmosKeyCredential)
            .database(database)
            .connectionPolicy(ConnectionPolicy.defaultPolicy())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .requestOptions(new RequestOptions());
    }

    /**
     * create a CosmosDBConfigBuilder with cosmos uri, key and database name
     * @param uri must not be {@literal null}
     * @param key must not be {@literal null}
     * @param database must not be {@literal null}
     * @return CosmosDBConfigBuilder
     */
    public static CosmosDBConfigBuilder builder(String uri, String key, String database) {
        return defaultBuilder()
            .uri(uri)
            .key(key)
            .database(database)
            .connectionPolicy(ConnectionPolicy.defaultPolicy())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .requestOptions(new RequestOptions());
    }

    /**
     * create a CosmosDBConfigBuilder with connection string and database name
     * @param connectionString must not be {@literal null}
     * @param database must not be {@literal null}
     * @return CosmosDBConfigBuilder
     * @throws CosmosDBAccessException for invalid connection string
     */
    public static CosmosDBConfigBuilder builder(String connectionString, String database) {
        Assert.hasText(connectionString, "connection string should have text!");
        try {
            final String uri = connectionString.split(";")[0].split("=")[1];
            final String key = connectionString.split(";")[1].split("=")[1];
            return builder(uri, key, database);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CosmosDBAccessException("could not parse connection string");
        }
    }

    /**
     * create a CosmosDBConfigBuilder instance
     * @return CosmosDBConfigBuilder
     */
    public static CosmosDBConfigBuilder defaultBuilder() {
        return new CosmosDBConfigBuilder();
    }

    /**
     * Builder class for cosmos db config
     */
    public static class CosmosDBConfigBuilder {
        private String uri;
        private String key;
        private String database;
        private ConnectionPolicy connectionPolicy;
        private ConsistencyLevel consistencyLevel;
        private boolean allowTelemetry;
        private RequestOptions requestOptions;
        private CosmosKeyCredential cosmosKeyCredential;
        private ResponseDiagnosticsProcessor responseDiagnosticsProcessor;
        private boolean populateQueryMetrics;

        CosmosDBConfigBuilder() {
        }

        /**
         * Set uri
         *
         * @param uri value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Set key
         *
         * @param key value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Set database
         *
         * @param database value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Set connectionPolicy
         *
         * @param connectionPolicy value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder connectionPolicy(ConnectionPolicy connectionPolicy) {
            this.connectionPolicy = connectionPolicy;
            return this;
        }

        /**
         * Set consistencyLevel
         *
         * @param consistencyLevel value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder consistencyLevel(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        /**
         * Set allowTelemetry
         *
         * @param allowTelemetry value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder allowTelemetry(boolean allowTelemetry) {
            this.allowTelemetry = allowTelemetry;
            return this;
        }

        /**
         * Set requestOptions
         *
         * @param requestOptions value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder requestOptions(RequestOptions requestOptions) {
            this.requestOptions = requestOptions;
            return this;
        }

        /**
         * Set cosmosKeyCredential
         *
         * @param cosmosKeyCredential value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder cosmosKeyCredential(CosmosKeyCredential cosmosKeyCredential) {
            this.cosmosKeyCredential = cosmosKeyCredential;
            return this;
        }

        /**
         * Set responseDiagnosticsProcessor
         *
         * @param responseDiagnosticsProcessor value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder responseDiagnosticsProcessor(ResponseDiagnosticsProcessor
                                                                      responseDiagnosticsProcessor) {
            this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
            return this;
        }

        /**
         * Set populateQueryMetrics
         *
         * @param populateQueryMetrics value to initialize
         * @return CosmosDBConfigBuilder
         */
        public CosmosDBConfigBuilder populateQueryMetrics(boolean populateQueryMetrics) {
            this.populateQueryMetrics = populateQueryMetrics;
            return this;
        }

        /**
         * Build a CosmosDBConfig instance
         *
         * @return CosmosDBConfig
         */
        public CosmosDBConfig build() {
            return new CosmosDBConfig(this.uri, this.key, this.database, this.connectionPolicy, this.consistencyLevel,
                this.allowTelemetry, this.requestOptions, this.cosmosKeyCredential, this.responseDiagnosticsProcessor,
                this.populateQueryMetrics);
        }

        /**
         * Generate string info of instance
         *
         * @return String
         */
        public String toString() {
            return "CosmosDBConfig.CosmosDBConfigBuilder(uri="
                    + this.uri
                    + ", key="
                    + this.key
                    + ", database="
                    + this.database
                    + ", connectionPolicy="
                    + this.connectionPolicy
                    + ", consistencyLevel="
                    + this.consistencyLevel
                    + ", allowTelemetry="
                    + this.allowTelemetry
                    + ", requestOptions="
                    + this.requestOptions
                    + ", cosmosKeyCredential="
                    + this.cosmosKeyCredential
                    + ", responseDiagnosticsProcessor="
                    + this.responseDiagnosticsProcessor
                    + ", populateQueryMetrics="
                    + this.populateQueryMetrics
                    + ")";
        }
    }
}
