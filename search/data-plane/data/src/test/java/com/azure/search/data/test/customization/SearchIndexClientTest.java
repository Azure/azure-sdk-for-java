// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.test.customization;

import org.junit.Before;

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
