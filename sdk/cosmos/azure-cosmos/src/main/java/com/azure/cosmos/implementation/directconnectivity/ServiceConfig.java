// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

class ServiceConfig {
    final static ServiceConfig instance = new ServiceConfig();

    public static ServiceConfig getInstance() {
        return instance;
    }

    static class SystemReplicationPolicy {
        public static final int MaxReplicaSetSize = 4;
    }
}
