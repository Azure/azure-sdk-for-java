// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

class ServiceConfig {
    final static ServiceConfig instance = new ServiceConfig();
    public SystemReplicationPolicy systemReplicationPolicy = new SystemReplicationPolicy();
    public SystemReplicationPolicy userReplicationPolicy = new SystemReplicationPolicy();

    public static ServiceConfig getInstance() {
        return instance;
    }

    class SystemReplicationPolicy {
        public static final int MaxReplicaSetSize = 4;
    }
}
