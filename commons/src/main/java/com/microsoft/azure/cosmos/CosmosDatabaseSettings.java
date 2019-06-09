/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;

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
public class CosmosDatabaseSettings extends Resource {

    /**
     * Constructor
     * @param id id of the database
     */
    public CosmosDatabaseSettings(String id) {
        super.setId(id);
    }

    CosmosDatabaseSettings(ResourceResponse<Database> response) {
        super(response.getResource().toJson());
    }

    // Converting document collection to CosmosContainerSettings
    CosmosDatabaseSettings(Database database){
        super(database.toJson());
    }

    static List<CosmosDatabaseSettings> getFromV2Results(List<Database> results){
        return results.stream().map(CosmosDatabaseSettings::new).collect(Collectors.toList());
    }
}