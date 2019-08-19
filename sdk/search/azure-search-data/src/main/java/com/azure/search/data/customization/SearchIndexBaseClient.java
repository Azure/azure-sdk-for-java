// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

public abstract class SearchIndexBaseClient {

    /**
     * Return index name
     * @return index name
     */
    public abstract String getIndexName();

    /**
     * Return api version
     * @return api version
     */
    public abstract String getApiVersion();

    /**
     * Return dns suffix for the search service
     * @return dns suffix
     */
    public abstract String getSearchDnsSuffix();

    /**
     * Return Search Service name
     * @return search service name
     */
    public abstract String getSearchServiceName();
}
