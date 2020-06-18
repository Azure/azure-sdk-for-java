// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.annotation.ServiceClient;

import java.util.List;

@ServiceClient(
    builder = TableServiceClientBuilder.class)
public class TableServiceClient {

    TableServiceClient() {
    }

    public void createTable(String name) {
    }

    public void deleteTable(String name) {
    }

    public List<AzureTable> queryTables(String filterString) {
        return null;
    }

}
