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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.DatabaseAccount;
import com.azure.data.cosmos.ReplicationPolicy;
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
