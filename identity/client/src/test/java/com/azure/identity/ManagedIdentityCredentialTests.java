// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.identity.credential.ManagedIdentityCredential;
import org.junit.Assert;
import org.junit.Test;

public class ManagedIdentityCredentialTests {
    @Test
    public void testAppServiceMSICredentialConfigurations() {
        ConfigurationManager.getConfiguration().put(BaseConfigurations.MSI_ENDPOINT, "").put(BaseConfigurations.MSI_SECRET, "");
        ManagedIdentityCredential credential = new ManagedIdentityCredential().msiEndpoint("http://foo").msiSecret("bar");
        Assert.assertEquals("http://foo", credential.msiEndpoint());
        Assert.assertEquals("bar", credential.msiSecret());
    }

    @Test
    public void testVirtualMachineMSICredentialConfigurations() {
        ConfigurationManager.getConfiguration().remove(BaseConfigurations.MSI_ENDPOINT);
        ConfigurationManager.getConfiguration().remove(BaseConfigurations.MSI_SECRET);
        ManagedIdentityCredential credential = new ManagedIdentityCredential().clientId("foo").identityId("bar").objectId("baz");
        Assert.assertEquals("foo", credential.clientId());
        Assert.assertEquals("bar", credential.identityId());
        Assert.assertEquals("baz", credential.objectId());
    }
}
