// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.aad.ropc;

import org.junit.jupiter.api.Test;

import static com.azure.spring.test.Constant.MULTI_TENANT_SCOPE_GRAPH_READ;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_1;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AADOauth2ROPCGrantClientIT {

    @Test
    public void getOAuth2ROPCResponseByROPCGrantTest() {
        AADOauth2ROPCGrantClient.OAuth2ROPCResponse oAuth2ROPCResponse =
            AADOauth2ROPCGrantClient.getOAuth2ROPCResponseByROPCGrant(
                AAD_TENANT_ID_1,
                AAD_MULTI_TENANT_CLIENT_ID,
                AAD_MULTI_TENANT_CLIENT_SECRET,
                AAD_USER_NAME_1,
                AAD_USER_PASSWORD_1,
                MULTI_TENANT_SCOPE_GRAPH_READ);
        assertNotNull(oAuth2ROPCResponse);
    }
}
