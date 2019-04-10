/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class AuthFileTests {
    @Test
    public void canReadJavaPropertiesAuthFile() throws Exception {
        File file = new File(AuthFileTests.class.getResource("/properties.azureauth").toURI());
        AuthFile authFile = AuthFile.parse(file);
        ApplicationTokenCredentials credentials = authFile.generateCredentials();

        Assert.assertNotNull(credentials);
        Assert.assertEquals("test-client", credentials.clientId());
        Assert.assertEquals("test-tenant", credentials.domain());
        Assert.assertEquals("test-subscription", credentials.defaultSubscriptionId());
        Assert.assertEquals("https://testbase.com/", credentials.environment().resourceManagerEndpoint());
        Assert.assertEquals("https://testmanagement.com/", credentials.environment().managementEndpoint());
        Assert.assertEquals("https://testgraph.net/", credentials.environment().graphEndpoint());
        Assert.assertEquals("https://testauth.net/", credentials.environment().activeDirectoryEndpoint());
        Assert.assertEquals("https://management.core.windows.net:8443/", credentials.environment().sqlManagementEndpoint());
    }

    @Test
    public void canReadJsonAuthFile() throws Exception {
        File file = new File(AuthFileTests.class.getResource("/json.azureauth").toURI());
        AuthFile authFile = AuthFile.parse(file);
        ApplicationTokenCredentials credentials = authFile.generateCredentials();

        Assert.assertNotNull(credentials);
        Assert.assertEquals("sample-clientid", credentials.clientId());
        Assert.assertEquals("sample-tenant", credentials.domain());
        Assert.assertEquals("sample-subscription", credentials.defaultSubscriptionId());
        Assert.assertEquals("https://samplearm.com/", credentials.environment().resourceManagerEndpoint());
        Assert.assertEquals("https://samplemanagement.net/", credentials.environment().managementEndpoint());
        Assert.assertEquals("https://samplegraph.net/", credentials.environment().graphEndpoint());
        Assert.assertEquals("https://samplead.com/", credentials.environment().activeDirectoryEndpoint());
        Assert.assertEquals("https://samplesql.net:8443/", credentials.environment().sqlManagementEndpoint());
        Assert.assertEquals("http://go.microsoft.com/fwlink/?LinkId=254432", credentials.environment().publishingProfile());
    }
}
