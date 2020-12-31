// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.util.ropc;

import org.junit.Test;

import static com.azure.test.util.EnvironmentVariables.SCOPE_GRAPH_READ;
import static org.junit.Assert.assertNotNull;

public class AADOauth2ROPCGrantClientIT {

    @Test
    public void getAccessTokenForTestAccountTest() {
        String accessToken = AADOauth2ROPCGrantClient.getAccessTokenForTestAccount(SCOPE_GRAPH_READ);
        assertNotNull(accessToken);
    }
}
