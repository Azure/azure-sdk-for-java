// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionPolicyTest {

    @DataProvider(name = "connectionModeArgProvider")
    public Object[][] connectionModeArgProvider() {
        return new Object[][]{
                {  ConnectionMode.GATEWAY},
                {  ConnectionMode.DIRECT},
        };
    }

    @Test(groups = { "unit" }, dataProvider = "connectionModeArgProvider")
    public void connectionMode(ConnectionMode connectionMode) {
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.connectionMode(connectionMode);

        assertThat(policy.connectionMode()).isEqualTo(connectionMode);
    }

    @DataProvider(name = "connectionProtocolModeArgProvider")
    public Object[][] connectionProtocolModeArgProvider() {
        return new Object[][]{
                {  Protocol.HTTPS},
                {  Protocol.TCP},
        };
    }
}
