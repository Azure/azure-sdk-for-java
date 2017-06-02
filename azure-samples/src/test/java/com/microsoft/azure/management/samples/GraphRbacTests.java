/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.samples.ManageUsers;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class GraphRbacTests extends TestBase {
    private Azure.Authenticated authenticated;
    private String defaultSubscription;

    @Test
    public void testManageUsers() {
        Assert.assertTrue(ManageUsers.runSample(authenticated, defaultSubscription));
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        authenticated = Azure.authenticate(restClient, defaultSubscription, domain);
        this.defaultSubscription = defaultSubscription;
    }

    @Override
    protected void cleanUpResources() {
    }
}

