// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosException;


/**
 * ResourceManager implementation where the CTL workload does not manage the underlying
 * Database and Container resources.
 */
public class NoopResourceManagerImpl implements ResourceManager {
    @Override
    public void createDatabase() throws CosmosException {

    }

    @Override
    public void createContainer() throws CosmosException {

    }

    @Override
    public void deleteResources() {

    }
}
