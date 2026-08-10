// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.HttpPipeline;

public class IndexesTestHelpers {
    public static HttpPipeline getHttpPipeline(SearchIndexClient searchIndexClient) {
        return searchIndexClient.getHttpPipeline();
    }

    public static HttpPipeline getHttpPipeline(SearchIndexAsyncClient searchIndexAsyncClient) {
        return searchIndexAsyncClient.getHttpPipeline();
    }

    public static String getEndpoint(SearchIndexClient searchIndexClient) {
        return searchIndexClient.getEndpoint();
    }

    public static String getEndpoint(SearchIndexAsyncClient searchIndexAsyncClient) {
        return searchIndexAsyncClient.getEndpoint();
    }
}
