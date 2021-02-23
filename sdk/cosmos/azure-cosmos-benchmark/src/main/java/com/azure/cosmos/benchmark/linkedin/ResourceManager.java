// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosException;


/**
 * Interface modeling the resource management operations for database and containers
 */
public interface ResourceManager {
    /**
     * Initialize the CosmosDB database required for running this test, or if the database exists, delete all
     * legacy containers
     *
     * @throws CosmosException in the event of an error creating the underlying database, or deleting
     *                         containers from a previously created database of the same name
     */
    void createDatabase() throws CosmosException;

    /**
     * Create desired container/collection for the test
     *
     * @throws CosmosException if the container could not be created
     */
    void createContainer() throws CosmosException;

    /**
     * Delete all managed resources e.g. account, databases and/or containers etc
     */
    void deleteResources();
}
