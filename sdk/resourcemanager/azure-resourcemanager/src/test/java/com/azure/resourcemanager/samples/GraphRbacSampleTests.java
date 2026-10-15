// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.resourcemanager.authorization.samples.ManageServicePrincipal;
import com.azure.resourcemanager.authorization.samples.ManageUsersGroupsAndRoles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Microsoft Entra ID (Graph/RBAC) samples run live-only: they create users, groups, applications and service
 * principals whose secrets (passwords, client secrets, tokens) must never be persisted in a recording, and the
 * service-principal re-authentication cannot be played back through the test proxy.
 */
public class GraphRbacSampleTests extends SamplesTestBase {

    private AzureProfile profile;

    @Test
    @LiveOnly
    public void testManageUsersGroupsAndRoles() {
        Assertions.assertTrue(ManageUsersGroupsAndRoles.runSample(azureResourceManager));
    }

    @Test
    @LiveOnly
    public void testManageServicePrincipal() {
        Assertions.assertTrue(ManageServicePrincipal.runSample(azureResourceManager, profile));
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        super.initializeClients(httpPipeline, profile);
        this.profile = profile;
    }
}
