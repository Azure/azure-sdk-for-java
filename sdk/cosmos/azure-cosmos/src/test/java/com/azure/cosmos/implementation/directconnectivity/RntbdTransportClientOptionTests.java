// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.ConnectionPolicy;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RntbdTransportClientOptionTests {

    @Test(groups = { "unit" })
    public void RntbdTransportClientOptionTest() {
        System.setProperty("azure.cosmos.directTcp.defaultOptions", "{\"requestTimeout\":\"PT10S\"}");

        RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(ConnectionPolicy.getDefaultPolicy()).build();
        assertThat(options.requestTimeout().getSeconds()).isEqualTo(10);
    }
}
