// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.privatedns.models.CnameRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class PrivateDnsZoneCnameRecordSetTests extends ResourceManagerTestBase {
    private String rgName = "";

    protected ResourceManager resourceManager;
    protected PrivateDnsZoneManager privateZoneManager;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }



    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        privateZoneManager = buildManager(PrivateDnsZoneManager.class, httpPipeline, profile);
        resourceManager = privateZoneManager.resourceManager();
        rgName = generateRandomResourceName("prdncsrstest", 15);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canUpdateCname() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        PrivateDnsZone dnsZone =
            privateZoneManager
                .privateZones()
                .define(topLevelDomain)
                .withNewResourceGroup(rgName, region)
                .defineCnameRecordSet("www")
                    .withAlias("cname.contoso.com")
                    .withTimeToLive(7200)
                    .attach()
                .create();

        // Check CNAME records
        dnsZone.refresh();
        PagedIterable<CnameRecordSet> cnameRecordSets = dnsZone.cnameRecordSets().list();
        Assertions.assertEquals(1, TestUtilities.getSize(cnameRecordSets));
        CnameRecordSet cnameRecordSet = cnameRecordSets.iterator().next();
        Assertions.assertEquals("www", cnameRecordSet.name());
        Assertions.assertEquals(7200, cnameRecordSet.timeToLive());
        Assertions.assertEquals("cname.contoso.com", cnameRecordSet.canonicalName());

        // Update alias and ttl:
        dnsZone
            .update()
            .updateCnameRecordSet("www")
            .withAlias("new.contoso.com")
            .withTimeToLive(1234)
            .parent()
            .apply();

        // Check CNAME records
        dnsZone.refresh();
        PagedIterable<CnameRecordSet> updatedCnameRecordSets = dnsZone.cnameRecordSets().list();
        Assertions.assertEquals(1, TestUtilities.getSize(updatedCnameRecordSets));
        CnameRecordSet updatedCnameRecordSet = updatedCnameRecordSets.iterator().next();
        Assertions.assertEquals(1234, updatedCnameRecordSet.timeToLive());
        Assertions.assertEquals("new.contoso.com", updatedCnameRecordSet.canonicalName());
    }
}
