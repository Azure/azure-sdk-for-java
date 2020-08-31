// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.accesscontrol;

import com.azure.analytics.synapse.accesscontrol.models.SynapseRole;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import org.junit.jupiter.api.Test;

public class AccessControlClientTest extends AccessControlClientTestBase {

    private AccessControlClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new AccessControlClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildClient());
    }

    /**
     * Tests that role assignments can be listed in the key vault.
     */
    @Test
    public void getRoleDefinitions() {
        for (SynapseRole expectedRole : client.getRoleDefinitions()) {
            SynapseRole actualRole = client.getRoleDefinitionById(expectedRole.getId());
            validateRoleDefinitions(expectedRole, actualRole);
        }
    }
}
