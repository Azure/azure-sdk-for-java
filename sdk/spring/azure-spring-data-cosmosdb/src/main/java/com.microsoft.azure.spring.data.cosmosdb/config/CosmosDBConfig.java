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

    public String getUri() {
        return uri;
    }

    public String getKey() {
        return key;
    }

    public String getDatabase() {
        return database;
    }

    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    public RequestOptions getRequestOptions() {
        return requestOptions;
    }

    public CosmosKeyCredential getCosmosKeyCredential() {
        return cosmosKeyCredential;
    }

    public ResponseDiagnosticsProcessor getResponseDiagnosticsProcessor() {
        return responseDiagnosticsProcessor;
    }

    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    public void setResponseDiagnosticsProcessor(ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
    }

    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }

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

    public static CosmosDBConfigBuilder builder(String uri, String key, String database) {
        return defaultBuilder()
            .uri(uri)
            .key(key)
            .database(database)
            .connectionPolicy(ConnectionPolicy.defaultPolicy())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .requestOptions(new RequestOptions());
    }

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

    public static CosmosDBConfigBuilder defaultBuilder() {
        return new CosmosDBConfigBuilder();
    }

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

        public CosmosDBConfigBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public CosmosDBConfigBuilder key(String key) {
            this.key = key;
            return this;
        }

        public CosmosDBConfigBuilder database(String database) {
            this.database = database;
            return this;
        }

        public CosmosDBConfigBuilder connectionPolicy(ConnectionPolicy connectionPolicy) {
            this.connectionPolicy = connectionPolicy;
            return this;
        }

        public CosmosDBConfigBuilder consistencyLevel(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        public CosmosDBConfigBuilder allowTelemetry(boolean allowTelemetry) {
            this.allowTelemetry = allowTelemetry;
            return this;
        }

        public CosmosDBConfigBuilder requestOptions(RequestOptions requestOptions) {
            this.requestOptions = requestOptions;
            return this;
        }

        public CosmosDBConfigBuilder cosmosKeyCredential(CosmosKeyCredential cosmosKeyCredential) {
            this.cosmosKeyCredential = cosmosKeyCredential;
            return this;
        }

        public CosmosDBConfigBuilder responseDiagnosticsProcessor(ResponseDiagnosticsProcessor
                                                                      responseDiagnosticsProcessor) {
            this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
            return this;
        }

        public CosmosDBConfigBuilder populateQueryMetrics(boolean populateQueryMetrics) {
            this.populateQueryMetrics = populateQueryMetrics;
            return this;
        }

        public CosmosDBConfig build() {
            return new CosmosDBConfig(this.uri, this.key, this.database, this.connectionPolicy, this.consistencyLevel,
                this.allowTelemetry, this.requestOptions, this.cosmosKeyCredential, this.responseDiagnosticsProcessor,
                this.populateQueryMetrics);
        }

        public String toString() {
            return "CosmosDBConfig.CosmosDBConfigBuilder(uri=" + this.uri + ", key=" + this.key
                    + ", database=" + this.database + ", connectionPolicy=" + this.connectionPolicy
                    + ", consistencyLevel=" + this.consistencyLevel + ", allowTelemetry=" + this.allowTelemetry
                    + ", requestOptions=" + this.requestOptions + ", cosmosKeyCredential=" + this.cosmosKeyCredential
                    + ", responseDiagnosticsProcessor=" + this.responseDiagnosticsProcessor + ", populateQueryMetrics="
                    + this.populateQueryMetrics + ")";
        }
    }
}
