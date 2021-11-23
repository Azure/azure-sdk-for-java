// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.regex.Pattern;

/**
 * Configuration properties for Cosmos database, consistency, telemetry, connection, query metrics and diagnostics.
 */
@Validated
@ConfigurationProperties("azure.cosmos")
public class CosmosProperties implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosProperties.class);

    /**
     * URI regular expression
     */
    public static final String URI_REGEX = "http[s]{0,1}://.*.documents.azure.com.*";

    /**
     * Local URI regular expressions
     */
    public static final String LOCAL_URI_REGEX = "^(http[s]{0,1}://)*localhost.*|^127(?:\\.[0-9]+){0,2}\\.[0-9]+.*";

    /**
     * Document DB URI.
     */
    @NotEmpty
    private String uri;

    /**
     * Whether to validate the uri, default is true.
     */
    private boolean validateUri = true;

    /**
     * Document DB key.
     */
    @NotEmpty
    private String key;

    /**
     * Document DB consistency level.
     */
    private ConsistencyLevel consistencyLevel;

    /**
     * Document DB database name.
     */
    @NotEmpty
    private String database;

    /**
     * Populate Diagnostics Strings and Query metrics
     */
    private boolean populateQueryMetrics;

    /**
     * Whether allow Microsoft to collect telemetry data.
     */
    private boolean allowTelemetry = true;

    /**
     * Represents the connection mode to be used by the client in the Azure Cosmos DB database service.
     */
    private ConnectionMode connectionMode;

    /**
     * Response Diagnostics processor
     * Default implementation is to log the response diagnostics string
     */
    private ResponseDiagnosticsProcessor responseDiagnosticsProcessor =
        responseDiagnostics -> {
            if (populateQueryMetrics) {
                LOGGER.info("Response Diagnostics {}", responseDiagnostics);
            }
        };

    @Override
    public void afterPropertiesSet() {
        validateUri();
    }

    /**
     * Gets the URI.
     *
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI.
     *
     * @param uri the URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the database name.
     *
     * @return the database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Sets the database name.
     *
     * @param databaseName the database name
     */
    public void setDatabase(String databaseName) {
        this.database = databaseName;
    }

    /**
     * Gets the consistency level.
     *
     * @return the consistency level
     */
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level.
     *
     * @param consistencyLevel the consistency level
     */
    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    /**
     * Whether telemetry is allowed.
     *
     * @return whether telemetry is allowed
     * @deprecated Determined by HTTP header User-Agent instead
     */
    @Deprecated
    @DeprecatedConfigurationProperty(
        reason = "Deprecate the telemetry endpoint and use HTTP header User Agent instead.")
    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    /**
     * Sets whether telemetry is allowed.
     *
     * @param allowTelemetry whether telemetry is allowed
     */
    public void setAllowTelemetry(boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    /**
     * Whether to validate the URI.
     *
     * @return whether to validate the URI
     */
    public boolean isValidateUri() {
        return validateUri;
    }

    /**
     * Sets whether to validate the URI.
     *
     * @param validateUri whether to validate the URI
     */
    public void setValidateUri(boolean validateUri) {
        this.validateUri = validateUri;
    }

    /**
     * Whether to populate query metrics.
     *
     * @return whether to populate query metrics
     */
    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    /**
     * Sets whether to populate query metrics.
     *
     * @param populateQueryMetrics whether to populate query metrics
     */
    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }

    /**
     * Gets the response diagnostics processor.
     *
     * @return the response diagnostics processor
     */
    public ResponseDiagnosticsProcessor getResponseDiagnosticsProcessor() {
        return responseDiagnosticsProcessor;
    }

    /**
     * Sets the response diagnostics processor.
     *
     * @param responseDiagnosticsProcessor the response diagnostics processor
     */
    public void setResponseDiagnosticsProcessor(ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        this.responseDiagnosticsProcessor = responseDiagnosticsProcessor;
    }

    /**
     * Gets the connection mode.
     *
     * @return the connection mode
     */
    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    /**
     * Sets the connection mode.
     *
     * @param connectionMode the connection mode
     */
    public void setConnectionMode(ConnectionMode connectionMode) {
        this.connectionMode = connectionMode;
    }

    private void validateUri() {
        if (!isValidateUri()) {
            return;
        }
        if (Pattern.matches(LOCAL_URI_REGEX, uri)) {
            return;
        }
        if (!Pattern.matches(URI_REGEX, uri)) {
            throw new IllegalArgumentException("the uri's pattern specified in 'azure.cosmos.uri' is not supported, "
                + "only sql/core api is supported, please check https://docs.microsoft.com/en-us/azure/cosmos-db/ "
                + "for more info.");
        }
    }
}
