// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.ResourceResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a CosmosDatabase in the Azure Cosmos database service. A cosmos database manages users, permissions and a set of containers
 * <p>
 * Each Azure Cosmos DB Service is able to support multiple independent named databases, with the database being the
 * logical container for data. Each Database consists of one or more cosmos containers, each of which in turn contain one or
 * more cosmos items. Since databases are an an administrative resource and the Service Key will be required in
 * order to access and successfully complete any action using the User APIs.
 */
public class CosmosDatabaseProperties extends Resource {

    /**
     * Constructor
     * @param id id of the database
     */
    public CosmosDatabaseProperties(String id) {
        super.id(id);
    }

    CosmosDatabaseProperties(ResourceResponse<Database> response) {
        super(response.getResource().toJson());
    }

    // Converting document collection to CosmosContainerProperties
    CosmosDatabaseProperties(Database database){
        super(database.toJson());
    }

    static List<CosmosDatabaseProperties> getFromV2Results(List<Database> results){
        return results.stream().map(CosmosDatabaseProperties::new).collect(Collectors.toList());
    }
}