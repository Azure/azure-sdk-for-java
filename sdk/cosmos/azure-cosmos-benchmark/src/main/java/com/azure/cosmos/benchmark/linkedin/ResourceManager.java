// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosException;


/**
 * Interface modeling the resource management operations for database and containers
 */
public interface ResourceManager {
    /**
     * Initialize this resource required for running this test
     *
     * @throws CosmosException in the event of an error creating the underlying database, or deleting
     *                         containers from a previously created database of the same name
     */
    void createResources() throws CosmosException;

    /**
     * Delete all managed resources e.g. account, databases and/or containers etc
     */
    void deleteResources();
}
