// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package example.springdata.cosmosdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.cosmosdb")
public class CosmosDbProperties {

    private String uri;

    private String key;

    private String secondaryKey;

    private String database;

    private boolean populateQueryMetrics;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setSecondaryKey(String secondaryKey) {
        this.secondaryKey = secondaryKey;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isPopulateQueryMetrics() {
        return populateQueryMetrics;
    }

    public void setPopulateQueryMetrics(boolean populateQueryMetrics) {
        this.populateQueryMetrics = populateQueryMetrics;
    }
}
