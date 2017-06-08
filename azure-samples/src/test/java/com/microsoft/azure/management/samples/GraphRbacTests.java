/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.samples;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.samples.ManageServicePrincipal;
import com.microsoft.azure.management.graphrbac.samples.ManageServicePrincipalCredentials;
import com.microsoft.azure.management.graphrbac.samples.ManageUsersGroupsAndRoles;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class GraphRbacTests extends TestBase {
    private Azure.Authenticated authenticated;
    private String defaultSubscription;

    public GraphRbacTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    @Test
    public void testManageUsersGroupsAndRoles() {
        Assert.assertTrue(ManageUsersGroupsAndRoles.runSample(authenticated, defaultSubscription));
    }

    @Test
    public void testManageServicePrincipal() {
        Assert.assertTrue(ManageServicePrincipal.runSample(authenticated, defaultSubscription));
    }

    @Test
    public void testManageServicePrincipalCredentials() {
        Assert.assertTrue(ManageServicePrincipalCredentials.runSample(authenticated, defaultSubscription, AzureEnvironment.AZURE));
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        authenticated = Azure.authenticate(restClient, domain, defaultSubscription);
        this.defaultSubscription = defaultSubscription;
    }

    @Override
    protected void cleanUpResources() {
    }
}

