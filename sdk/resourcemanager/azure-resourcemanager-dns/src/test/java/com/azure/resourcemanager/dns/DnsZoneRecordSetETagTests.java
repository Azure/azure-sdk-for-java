// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.dns;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.AaaaRecordSet;
import com.azure.resourcemanager.dns.models.CNameRecordSet;
import com.azure.resourcemanager.dns.models.CaaRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.models.ZoneType;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DnsZoneRecordSetETagTests extends TestBase {
    private String rgName = "";

    public DnsZoneRecordSetETagTests() {
        super(TestBase.RunCondition.BOTH);
    }

    protected ResourceManager resourceManager;
    protected DnsZoneManager zoneManager;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();
        zoneManager = DnsZoneManager.authenticate(httpPipeline, profile, sdkContext);
        rgName = generateRandomResourceName("dnsetagtest", 15);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canCreateZoneWithDefaultETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone =
            zoneManager.zones().define(topLevelDomain).withNewResourceGroup(rgName, region).withETagCheck().create();
        Assertions.assertNotNull(dnsZone.etag());

        Runnable runnable =
            () ->
                zoneManager
                    .zones()
                    .define(topLevelDomain)
                    .withNewResourceGroup(rgName, region)
                    .withETagCheck()
                    .create();
        ensureETagExceptionIsThrown(runnable);
    }

    @Test
    public void canUpdateZoneWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final DnsZone dnsZone =
            zoneManager.zones().define(topLevelDomain).withNewResourceGroup(rgName, region).withETagCheck().create();
        Assertions.assertNotNull(dnsZone.etag());

        Runnable runnable = () -> dnsZone.update().withETagCheck(dnsZone.etag() + "-foo").apply();
        ensureETagExceptionIsThrown(runnable);
        dnsZone.update().withETagCheck(dnsZone.etag()).apply();
    }

    @Test
    public void canDeleteZoneWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final DnsZone dnsZone =
            zoneManager.zones().define(topLevelDomain).withNewResourceGroup(rgName, region).withETagCheck().create();
        Assertions.assertNotNull(dnsZone.etag());

        Runnable runnable = () -> zoneManager.zones().deleteById(dnsZone.id(), dnsZone.etag() + "-foo");
        ensureETagExceptionIsThrown(runnable);
        zoneManager.zones().deleteById(dnsZone.id(), dnsZone.etag());
    }

    @Test
    public void canCreateRecordSetsWithDefaultETag() throws Exception {
        if (isPlaybackMode()) {
            return; // TODO: fix playback random fail
        }
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone =
            zoneManager
                .zones()
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
                .defineCaaRecordSet("caaName")
                .withRecord(4, "sometag", "someValue")
                .attach()
                .defineCNameRecordSet("documents")
                .withAlias("doc.contoso.com")
                .withETagCheck()
                .attach()
                .defineCNameRecordSet("userguide")
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
        PagedIterable<CNameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(cnameRecordSets) == 2);

        // Check Caa records
        PagedIterable<CaaRecordSet> caaRecordSets = dnsZone.caaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(caaRecordSets) == 1);
        CaaRecordSet caaRecordSet1 = caaRecordSets.iterator().next();
        Assertions.assertTrue(caaRecordSet1.name().startsWith("caaname"));
        Assertions.assertTrue(caaRecordSet1.records().get(0).value().equalsIgnoreCase("someValue"));
        Assertions.assertEquals(4, (long) caaRecordSet1.records().get(0).flags());
        Assertions.assertTrue(caaRecordSet1.fqdn().startsWith("caaname.www.contoso"));

        Assertions.assertEquals(ZoneType.PUBLIC, dnsZone.accessType());

        Exception compositeException = null;
        try {
            zoneManager
                .zones()
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
                .defineCNameRecordSet("documents")
                .withAlias("doc.contoso.com")
                .withETagCheck()
                .attach()
                .defineCNameRecordSet("userguide")
                .withAlias("doc.contoso.com")
                .withETagCheck()
                .attach()
                .create();
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
    }

    @Test
    public void canUpdateRecordSetWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone =
            zoneManager
                .zones()
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

        // by default zone access type should be public
        Assertions.assertEquals(ZoneType.PUBLIC, dnsZone.accessType());
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

        DnsZone dnsZone =
            zoneManager
                .zones()
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
