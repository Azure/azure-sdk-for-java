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
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.privatedns.models.ARecordSet;
import com.azure.resourcemanager.privatedns.models.AaaaRecordSet;
import com.azure.resourcemanager.privatedns.models.CnameRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PrivateDnsZoneRecordSetETagTests extends ResourceManagerTestBase {
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
        rgName = generateRandomResourceName("prdnsrstest", 15);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canUpdateZone() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        PrivateDnsZone privateDnsZone =
            privateZoneManager.privateZones().define(topLevelDomain)
                .withNewResourceGroup(rgName, region)
                .withTag("key1", "value1")
                .create();

        privateDnsZone.update()
            .withoutTag("key1")
            .withTag("key2", "value2")
            .withTag("key3", "value3")
            .apply();

        privateDnsZone.refresh();
        Assertions.assertEquals(2, privateDnsZone.tags().size());
        Assertions.assertEquals(new HashSet<>(Arrays.asList("key2", "key3")), privateDnsZone.tags().keySet());
        Assertions.assertEquals(Arrays.asList("value2", "value3"), new ArrayList<>(privateDnsZone.tags().values()));

        privateDnsZone.update()
            .defineARecordSet("www")
                .withIPv4Address("23.96.104.40")
                .withIPv4Address("24.97.105.41")
                .withTimeToLive(7200)
                .withETagCheck()
                .attach()
            .apply();
        Assertions.assertEquals(1, TestUtilities.getSize(privateDnsZone.aRecordSets().list()));
    }

    @Test
    public void canCreateZoneWithDefaultETag() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        PrivateDnsZone privateDnsZone =
            privateZoneManager.privateZones().define(topLevelDomain).withNewResourceGroup(rgName, region).withETagCheck().create();
        Assertions.assertNotNull(privateDnsZone.etag());

        Runnable runnable =
            () ->
                privateZoneManager
                    .privateZones()
                    .define(topLevelDomain)
                        .withNewResourceGroup(rgName, region)
                        .withETagCheck()
                    .create();
        ensureETagExceptionIsThrown(runnable);
    }

    @Test
    public void canUpdateZoneWithExplicitETag() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final PrivateDnsZone privateDnsZone =
            privateZoneManager.privateZones().define(topLevelDomain).withNewResourceGroup(rgName, region).withETagCheck().create();
        Assertions.assertNotNull(privateDnsZone.etag());

        Runnable runnable = () -> privateDnsZone.update()
            .withTag("k1", "v1")
            .withETagCheck(privateDnsZone.etag() + "-foo")
            .apply();
        ensureETagExceptionIsThrown(runnable);
        privateDnsZone.update().withETagCheck(privateDnsZone.etag()).apply();
    }

    @Test
    public void canDeleteZoneWithExplicitETag() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final PrivateDnsZone dnsZone =
            privateZoneManager.privateZones().define(topLevelDomain).withNewResourceGroup(rgName, region).withETagCheck().create();
        Assertions.assertNotNull(dnsZone.etag());

        Runnable runnable = () -> privateZoneManager.privateZones().deleteById(dnsZone.id(), dnsZone.etag() + "-foo");
        ensureETagExceptionIsThrown(runnable);
        privateZoneManager.privateZones().deleteById(dnsZone.id(), dnsZone.etag());
    }

    @Test
    public void canCreateRecordSetsWithDefaultETag() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        PrivateDnsZone dnsZone =
            privateZoneManager
                .privateZones()
                .define(topLevelDomain)
                    .withNewResourceGroup(rgName, region)
                    .defineARecordSet("www")
                        .withIPv4Address("23.96.104.40")
                        .withIPv4Address("24.97.105.41")
                        .withTimeToLive(7200)
                        .withETagCheck()
                        .attach()
                    .defineAaaaRecordSet("www")
                        .withIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                        .withIPv6Address("2002:0db9:85a4:0000:0000:8a2e:0371:7335")
                        .withETagCheck()
                        .attach()
                    .defineCnameRecordSet("documents")
                        .withAlias("doc.contoso.com")
                        .withETagCheck()
                        .attach()
                    .defineCnameRecordSet("userguide")
                        .withAlias("doc.contoso.com")
                        .withETagCheck()
                        .attach()
                    .create();

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 1);
        ARecordSet aRecordSet1 = aRecordSets.iterator().next();
        Assertions.assertTrue(aRecordSet1.timeToLive() == 7200);

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        AaaaRecordSet aaaaRecordSet1 = aaaaRecordSets.iterator().next();
        Assertions.assertTrue(aaaaRecordSet1.name().startsWith("www"));
        Assertions.assertTrue(aaaaRecordSet1.ipv6Addresses().size() == 2);

        // Check CNAME records
        PagedIterable<CnameRecordSet> cnameRecordSets = dnsZone.cnameRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(cnameRecordSets) == 2);

        Exception compositeException = null;
        try {
            privateZoneManager
                .privateZones()
                .define(topLevelDomain)
                    .withNewResourceGroup(rgName, region)
                    .defineARecordSet("www")
                        .withIPv4Address("23.96.104.40")
                        .withIPv4Address("24.97.105.41")
                        .withTimeToLive(7200)
                        .withETagCheck()
                        .attach()
                    .defineAaaaRecordSet("www")
                        .withIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                        .withIPv6Address("2002:0db9:85a4:0000:0000:8a2e:0371:7335")
                        .withETagCheck()
                    .attach()
                    .defineCnameRecordSet("documents")
                        .withAlias("doc.contoso.com")
                        .withETagCheck()
                        .attach()
                    .defineCnameRecordSet("userguide")
                        .withAlias("doc.contoso.com")
                        .withETagCheck()
                        .attach()
                    .create();
        } catch (ManagementException exception) {
            compositeException = exception;
        }
        Assertions.assertNotNull(compositeException);

        Assertions.assertTrue(compositeException.getSuppressed().length > 0);
        for (int i = 0; i < compositeException.getSuppressed().length; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            if (exception instanceof ManagementException) {
                ManagementError cloudError = ((ManagementException) exception).getValue();
                Assertions.assertNotNull(cloudError);
                Assertions.assertNotNull(cloudError.getCode());
                Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
            }
        }
    }

    @Test
    public void canUpdateRecordSetWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        PrivateDnsZone dnsZone =
            privateZoneManager
                .privateZones()
                .define(topLevelDomain)
                .withNewResourceGroup(rgName, region)
                .defineARecordSet("www")
                    .withIPv4Address("23.96.104.40")
                    .withIPv4Address("24.97.105.41")
                    .withTimeToLive(7200)
                    .withETagCheck()
                    .attach()
                .defineAaaaRecordSet("www")
                    .withIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                    .withIPv6Address("2002:0db9:85a4:0000:0000:8a2e:0371:7335")
                    .withETagCheck()
                    .attach()
                .create();

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 1);
        ARecordSet aRecordSet = aRecordSets.iterator().next();
        Assertions.assertNotNull(aRecordSet.etag());

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        AaaaRecordSet aaaaRecordSet = aaaaRecordSets.iterator().next();
        Assertions.assertNotNull(aaaaRecordSet.etag());

        // Try updates with invalid etag
        //
        Exception compositeException = null;
        try {
            dnsZone
                .update()
                .updateARecordSet("www")
                .withETagCheck(aRecordSet.etag() + "-foo")
                .parent()
                .updateAaaaRecordSet("www")
                .withETagCheck(aaaaRecordSet.etag() + "-foo")
                .parent()
                .apply();
        } catch (Exception exception) {
            compositeException = exception;
        }
        Assertions.assertNotNull(compositeException);
        Assertions.assertTrue(compositeException.getSuppressed().length > 0);
        for (int i = 0; i < compositeException.getSuppressed().length; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            if (exception instanceof ManagementException) {
                ManagementError cloudError = ((ManagementException) exception).getValue();
                Assertions.assertNotNull(cloudError);
                Assertions.assertNotNull(cloudError.getCode());
                Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
            }
        }
        // Try update with correct etags
        dnsZone
            .update()
            .updateARecordSet("www")
                .withIPv4Address("24.97.105.45")
                .withETagCheck(aRecordSet.etag())
                .parent()
            .updateAaaaRecordSet("www")
                .withETagCheck(aaaaRecordSet.etag())
                .parent()
            .apply();

        // Check A records
        aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 1);
        aRecordSet = aRecordSets.iterator().next();
        Assertions.assertNotNull(aRecordSet.etag());
        Assertions.assertTrue(aRecordSet.ipv4Addresses().size() == 3);

        // Check AAAA records
        aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        aaaaRecordSet = aaaaRecordSets.iterator().next();
        Assertions.assertNotNull(aaaaRecordSet.etag());
    }

    @Test
    public void canDeleteRecordSetWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        PrivateDnsZone dnsZone =
            privateZoneManager
                .privateZones()
                .define(topLevelDomain)
                    .withNewResourceGroup(rgName, region)
                    .defineARecordSet("www")
                        .withIPv4Address("23.96.104.40")
                        .withIPv4Address("24.97.105.41")
                        .withTimeToLive(7200)
                        .withETagCheck()
                        .attach()
                    .defineAaaaRecordSet("www")
                        .withIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                        .withIPv6Address("2002:0db9:85a4:0000:0000:8a2e:0371:7335")
                        .withETagCheck()
                        .attach()
                    .create();

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 1);
        ARecordSet aRecordSet = aRecordSets.iterator().next();
        Assertions.assertNotNull(aRecordSet.etag());

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        AaaaRecordSet aaaaRecordSet = aaaaRecordSets.iterator().next();
        Assertions.assertNotNull(aaaaRecordSet.etag());

        // Try delete with invalid etag
        //
        Exception compositeException = null;
        try {
            dnsZone
                .update()
                .withoutARecordSet("www", aRecordSet.etag() + "-foo")
                .withoutAaaaRecordSet("www", aaaaRecordSet.etag() + "-foo")
                .apply();
        } catch (Exception exception) {
            compositeException = exception;
        }
        Assertions.assertNotNull(compositeException);
        Assertions.assertTrue(compositeException.getSuppressed().length > 0);
        for (int i = 0; i < compositeException.getSuppressed().length; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            if (exception instanceof ManagementException) {
                ManagementError cloudError = ((ManagementException) exception).getValue();
                Assertions.assertNotNull(cloudError);
                Assertions.assertNotNull(cloudError.getCode());
                Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
            }
        }
        // Try delete with correct etags
        dnsZone
            .update()
            .withoutARecordSet("www", aRecordSet.etag())
            .withoutAaaaRecordSet("www", aaaaRecordSet.etag())
            .apply();

        // Check A records
        aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 0);

        // Check AAAA records
        aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 0);
    }

    /**
     * Runs the action and assert that action throws ManagementException with CloudError.Code property set to
     * 'PreconditionFailed'.
     *
     * @param runnable runnable to run
     */
    private void ensureETagExceptionIsThrown(final Runnable runnable) {
        boolean isManagementExceptionThrown = false;
        boolean isCloudErrorSet = false;
        boolean isPreconditionFailedCodeSet = false;
        try {
            runnable.run();
        } catch (ManagementException exception) {
            isManagementExceptionThrown = true;
            ManagementError cloudError = exception.getValue();
            if (cloudError != null) {
                isCloudErrorSet = true;
                isPreconditionFailedCodeSet = cloudError.getCode().contains("PreconditionFailed");
            }
        }
        Assertions.assertTrue(isManagementExceptionThrown, "Expected ManagementException is not thrown");
        Assertions.assertTrue(isCloudErrorSet, "Expected CloudError property is not set in ManagementException");
        Assertions
            .assertTrue(
                isPreconditionFailedCodeSet,
                "Expected PreconditionFailed code is not set indicating ETag concurrency check failure");
    }
}
