// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.directconnectivity.Protocol;
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
        policy.setConnectionMode(connectionMode);

        assertThat(policy.getConnectionMode()).isEqualTo(connectionMode);
    }

    @DataProvider(name = "connectionProtocolModeArgProvider")
    public Object[][] connectionProtocolModeArgProvider() {
        return new Object[][]{
                {  Protocol.HTTPS},
                {  Protocol.TCP},
        };
    }

    @Test(groups = { "unit" })
    public void usingMultipleWriteLocations() {
        ConnectionPolicy policy = new ConnectionPolicy();
        assertThat(policy.getUsingMultipleWriteLocations()).isEqualTo(true);
        policy.setUsingMultipleWriteLocations(false);
        assertThat(policy.getUsingMultipleWriteLocations()).isEqualTo(false);
    }
}
