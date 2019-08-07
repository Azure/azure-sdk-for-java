// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.SearchIndexASyncClient;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.customization.SearchIndexClientBuilderImpl;
import org.junit.Before;
import org.junit.Test;

public class SearchIndexClientTest {

    String searchServiceName = "";
    String apiKey = "";
    String indexName = "";
    String apiVersion = "";
    String dnsSuffix = "";

    @Before
    public void initialize() {
        searchServiceName = "";
        apiKey = "";
        indexName = "hotels";
        apiVersion = "2019-05-06";
        dnsSuffix = "search.windows.net";
    }

}
