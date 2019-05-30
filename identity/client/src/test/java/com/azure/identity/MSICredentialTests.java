package com.azure.identity;

import com.azure.identity.credential.AppServiceMSICredential;
import com.azure.identity.credential.MSICredential;
import com.azure.identity.credential.VirtualMachineMSICredential;
import com.azure.identity.credential.msi.MSIResourceType;
import com.azure.identity.credential.msi.VirtualMachineMSITokenSource;
import org.junit.Assert;
import org.junit.Test;

public class MSICredentialTests {
    @Test
    public void testAppServiceMSICredentialConfigurations() {
        AppServiceMSICredential credential = MSICredential.appService().msiEndpoint("http://foo").msiSecret("bar");
        Assert.assertEquals("http://foo", credential.msiEndpoint());
        Assert.assertEquals("bar", credential.msiSecret());
        Assert.assertEquals(MSIResourceType.APP_SERVICE, credential.resourceType());
        Assert.assertEquals("Bearer", credential.scheme());
    }

    @Test
    public void testVirtualMachineMSICredentialConfigurations() {
        VirtualMachineMSICredential credential = MSICredential.virtualMachine().clientId("foo").identityId("bar").objectId("baz");
        Assert.assertEquals("foo", credential.clientId());
        Assert.assertEquals("bar", credential.identityId());
        Assert.assertEquals("baz", credential.objectId());
        Assert.assertEquals(50342, credential.msiPort());
        Assert.assertEquals(20, credential.maxRetry());
        Assert.assertEquals(VirtualMachineMSITokenSource.IMDS_ENDPOINT, credential.tokenSource());
        Assert.assertEquals(MSIResourceType.VIRTUAL_MACHINE, credential.resourceType());
        Assert.assertEquals("Bearer", credential.scheme());
    }
}
