// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.authorization.samples.ManageServicePrincipalCredentials;
import com.azure.resourcemanager.authorization.samples.ManageUsersGroupsAndRoles;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphRbacTests extends TestBase {
    private Azure.Authenticated authenticated;
    private AzureProfile profile;

    public GraphRbacTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageUsersGroupsAndRoles() {
        Assertions.assertTrue(ManageUsersGroupsAndRoles.runSample(authenticated, profile));
    }

//    @Test
//    public void testManageServicePrincipal() {
//        Assertions.assertTrue(ManageServicePrincipal.runSample(authenticated, defaultSubscription));
//    }

    @Test
    public void testManageServicePrincipalCredentials() {
        Assertions.assertTrue(ManageServicePrincipalCredentials.runSample(authenticated, profile));
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        authenticated = Azure.authenticate(httpPipeline, profile);
        this.profile = profile;
    }

    @Override
    protected void cleanUpResources() {
    }
}

