// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Immutable;

/**
 * The ElasticsearchDataFeedSource model.
 */
@Immutable
public final class ElasticsearchDataFeedSource extends DataFeedSource {
    /*
     * Host
     */
    private final String host;

    /*
     * Port
     */
    private final String port;

    /*
     * Authorization header
     */
    private final String authHeader;

    /*
     * Query
     */
    private final String query;

    /**
     * Construct a ElasticsearchDataFeedSource instance.
     *
     * @param host the host for data source.
     * @param port the port data source.
     * @param authHeader the auth header for data source.
     * @param query the query.
     */
    public ElasticsearchDataFeedSource(final String host, final String port, final String authHeader,
        final String query) {
        this.host = host;
        this.port = port;
        this.authHeader = authHeader;
        this.query = query;
    }

    /**
     * Get the host value for the Elasticsearch.
     *
     * @return the host value.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Get the port for the Elasticsearch.
     *
     * @return the port value.
     */
    public String getPort() {
        return this.port;
    }

    /**
     * Get the authorization header.
     *
     * @return the authHeader value.
     */
    public String getAuthHeader() {
        return this.authHeader;
    }

    /**
     * Get the query value.
     *
     * @return the query value.
     */
    public String getQuery() {
        return this.query;
    }
}
