// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CdnProfileOperationsTests extends CdnManagementTest {

    private String rgName = "";
    private final Region region = Region.US_WEST;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateCdnProfile() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardAkamaiSku()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        boolean foundProfile = false;
        PagedIterable<CdnProfile> cdnProfiles = cdnManager.profiles().list();
        for (CdnProfile profile : cdnProfiles) {
            if (profile.name().equals(cdnProfileName)) {
                foundProfile = true;
            }
        }
        Assertions.assertTrue(foundProfile);
    }

    @Test
    public void canCreateUpdateCdnProfile() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardAkamaiSku()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());
        Assertions.assertEquals(0, cdnProfile.endpoints().size());

        cdnProfile.update()
            .defineNewEndpoint(cdnEndpointName)
                .withOrigin("origin1", "www.someDomain.net")
                .withHttpAllowed(true)
                .withHttpsAllowed(true)
                .attach()
            .apply();

        Map<String, CdnEndpoint> cdnEndpointMap = cdnProfile.endpoints();
        CdnEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);

        Assertions.assertNotNull(cdnEndpoint);
        Assertions.assertTrue(cdnEndpoint.isHttpAllowed());
        Assertions.assertTrue(cdnEndpoint.isHttpsAllowed());
        Assertions.assertFalse(cdnEndpoint.isCompressionEnabled());
        Assertions.assertEquals("www.someDomain.net", cdnEndpoint.originHostName());
    }

    @Test
    public void canCreateUpdateCdnEndpoint() {
        String cdnProfileName = generateRandomResourceName("cdnp", 15);
        String cdnEndpointName = generateRandomResourceName("cdnendp", 15);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        CheckNameAvailabilityResult result = cdnManager.profiles().checkEndpointNameAvailability(cdnProfileName);
        Assertions.assertTrue(result.nameAvailable());

        CdnProfile cdnProfile = cdnManager.profiles().define(cdnProfileName)
            .withRegion(region)
            .withExistingResourceGroup(resourceGroup)
            .withStandardAkamaiSku()
            .defineNewEndpoint(cdnEndpointName)
                .withOrigin("origin1", "www.someDomain.net")
                .withHttpAllowed(false)
                .withHttpsAllowed(true)
                .attach()
            .create();

        Assertions.assertNotNull(cdnProfile);
        Assertions.assertEquals(cdnProfileName, cdnProfile.name());

        Map<String, CdnEndpoint> cdnEndpointMap = cdnProfile.endpoints();
        Assertions.assertEquals(1, cdnProfile.endpoints().size());

        CdnEndpoint cdnEndpoint = cdnEndpointMap.get(cdnEndpointName);
        Assertions.assertNotNull(cdnEndpoint);
        Assertions.assertFalse(cdnEndpoint.isHttpAllowed());
        Assertions.assertTrue(cdnEndpoint.isHttpsAllowed());
        Assertions.assertEquals(0, cdnEndpoint.customDomains().size());

        cdnProfile.update()
            .updateEndpoint(cdnEndpointName)
                .withHttpAllowed(true)
                .withHttpsAllowed(false)
                .parent()
            .apply();

        cdnEndpoint.refresh();
        Assertions.assertTrue(cdnEndpoint.isHttpAllowed());
        Assertions.assertFalse(cdnEndpoint.isHttpsAllowed());
        Assertions.assertTrue(TestUtilities.getSize(cdnEndpoint.listResourceUsage()) > 0);
    }
}
