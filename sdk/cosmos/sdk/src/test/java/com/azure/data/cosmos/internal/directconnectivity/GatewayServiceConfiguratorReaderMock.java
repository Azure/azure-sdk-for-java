// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.DatabaseAccount;
import com.azure.data.cosmos.internal.ReplicationPolicy;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

public class GatewayServiceConfiguratorReaderMock {

    public GatewayServiceConfigurationReader gatewayServiceConfigurationReader;

    public static GatewayServiceConfiguratorReaderMock from(ConsistencyLevel accountConsistencyLevel) {
        return new GatewayServiceConfiguratorReaderMock(new ReplicationPolicy("{}"), new ReplicationPolicy("{}"), accountConsistencyLevel);
    }

    public static GatewayServiceConfiguratorReaderMock from(ConsistencyLevel accountConsistencyLevel,
                                                            int systemMaxReplicaCount,
                                                            int systemMinReplicaCount,
                                                            int userMaxReplicaCount,
                                                            int userMinReplicaCount) {
        ReplicationPolicy userRP = Mockito.mock(ReplicationPolicy.class);
        Mockito.doReturn(userMaxReplicaCount).when(userRP).getMaxReplicaSetSize();
        Mockito.doReturn(userMinReplicaCount).when(userRP).getMinReplicaSetSize();

        ReplicationPolicy systemRP = Mockito.mock(ReplicationPolicy.class);
        Mockito.doReturn(systemMaxReplicaCount).when(systemRP).getMaxReplicaSetSize();
        Mockito.doReturn(systemMinReplicaCount).when(systemRP).getMinReplicaSetSize();

        return new GatewayServiceConfiguratorReaderMock(userRP, systemRP, accountConsistencyLevel);
    }

    public static GatewayServiceConfiguratorReaderMock from(ConsistencyLevel accountConsistencyLevel, int maxReplicaSize, int minReplicaCase) {
        ReplicationPolicy rp = Mockito.mock(ReplicationPolicy.class);
        Mockito.doReturn(maxReplicaSize).when(rp).getMaxReplicaSetSize();
        Mockito.doReturn(minReplicaCase).when(rp).getMinReplicaSetSize();

        return new GatewayServiceConfiguratorReaderMock(rp, rp, accountConsistencyLevel);
    }


    public GatewayServiceConfiguratorReaderMock(ReplicationPolicy userReplicationPolicy,
                                                ReplicationPolicy systemReplicationPolicy,
                                                ConsistencyLevel defaultConsistencyLevel) {
        this.gatewayServiceConfigurationReader = Mockito.mock(GatewayServiceConfigurationReader.class);

        Mockito.doReturn(Mono.just(Mockito.mock(DatabaseAccount.class))).when(this.gatewayServiceConfigurationReader).initializeReaderAsync();
        Mockito.doReturn(defaultConsistencyLevel).when(this.gatewayServiceConfigurationReader).getDefaultConsistencyLevel();
        Mockito.doReturn(systemReplicationPolicy).when(this.gatewayServiceConfigurationReader).getSystemReplicationPolicy();
        Mockito.doReturn(userReplicationPolicy).when(this.gatewayServiceConfigurationReader).getUserReplicationPolicy();
    }
}
