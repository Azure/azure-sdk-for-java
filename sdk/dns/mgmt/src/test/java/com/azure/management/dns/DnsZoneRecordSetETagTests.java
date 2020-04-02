/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.dns;


import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.CloudError;
import com.azure.core.management.CloudException;
import com.azure.management.RestClient;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.core.TestUtilities;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.dns.implementation.DnsZoneManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DnsZoneRecordSetETagTests extends TestBase {
    private String RG_NAME = "";

    public DnsZoneRecordSetETagTests() {
        super(TestBase.RunCondition.BOTH);
    }

    protected ResourceManager resourceManager;
    protected DnsZoneManager zoneManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);
        zoneManager = DnsZoneManager
                .authenticate(restClient, defaultSubscription, sdkContext);
        RG_NAME = generateRandomResourceName("dnsetagtest", 15);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCreateZoneWithDefaultETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
                .withETagCheck()
                .create();
        Assertions.assertNotNull(dnsZone.eTag());

        Runnable runnable = () -> zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
                .withETagCheck()
                .create();
        ensureETagExceptionIsThrown(runnable);
    }

    @Test
    public void canUpdateZoneWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
                .withETagCheck()
                .create();
        Assertions.assertNotNull(dnsZone.eTag());

        Runnable runnable = () -> dnsZone.update()
                .withETagCheck(dnsZone.eTag() + "-foo")
                .apply();
        ensureETagExceptionIsThrown(runnable);
        dnsZone.update()
                .withETagCheck(dnsZone.eTag())
                .apply();
    }

    @Test
    public void canDeleteZoneWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
                .withETagCheck()
                .create();
        Assertions.assertNotNull(dnsZone.eTag());

        Runnable runnable = () -> zoneManager.zones().deleteById(dnsZone.id(), dnsZone.eTag() + "-foo");
        ensureETagExceptionIsThrown(runnable);
        zoneManager.zones().deleteById(dnsZone.id(), dnsZone.eTag());
    }

    @Test
    public void canCreateRecordSetsWithDefaultETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
                .withPrivateAccess()
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

        Assertions.assertEquals(ZoneType.PRIVATE , dnsZone.accessType());

        Exception  compositeException = null;
        try {
            zoneManager.zones().define(topLevelDomain)
                    .withNewResourceGroup(RG_NAME, region)
                    .withPrivateAccess()
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

        Assertions.assertEquals(5, compositeException.getSuppressed().length);
        for(int i = 0; i < 4; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            Assertions.assertTrue(exception instanceof CloudException);
            CloudError cloudError = ((CloudException) exception).getValue();
            Assertions.assertNotNull(cloudError);
            Assertions.assertNotNull(cloudError.getCode());
            Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
        }
    }

    @Test
    public void canUpdateRecordSetWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
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
        Assertions.assertNotNull(aRecordSet.eTag());

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        AaaaRecordSet aaaaRecordSet = aaaaRecordSets.iterator().next();
        Assertions.assertNotNull(aaaaRecordSet.eTag());

        // by default zone access type should be public
        Assertions.assertEquals(ZoneType.PUBLIC, dnsZone.accessType());
        // Try updates with invalid eTag
        //
        Exception compositeException = null;
        try {
            dnsZone.update()
                .updateARecordSet("www")
                    .withETagCheck(aRecordSet.eTag() + "-foo")
                    .parent()
                .updateAaaaRecordSet("www")
                    .withETagCheck(aaaaRecordSet.eTag() + "-foo")
                    .parent()
                .apply();
        } catch (Exception exception) {
            compositeException = exception;
        }
        Assertions.assertNotNull(compositeException);
        Assertions.assertEquals(3, compositeException.getSuppressed().length);
        for(int i = 0; i < 2; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            Assertions.assertTrue(exception instanceof CloudException);
            CloudError cloudError = ((CloudException) exception).getValue();
            Assertions.assertNotNull(cloudError);
            Assertions.assertNotNull(cloudError.getCode());
            Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
        }
        // Try update with correct etags
        dnsZone.update()
                .updateARecordSet("www")
                    .withIPv4Address("24.97.105.45")
                    .withETagCheck(aRecordSet.eTag())
                    .parent()
                .updateAaaaRecordSet("www")
                    .withETagCheck(aaaaRecordSet.eTag())
                    .parent()
                .apply();

        // Check A records
        aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 1);
        aRecordSet = aRecordSets.iterator().next();
        Assertions.assertNotNull(aRecordSet.eTag());
        Assertions.assertTrue(aRecordSet.ipv4Addresses().size() == 3);

        // Check AAAA records
        aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        aaaaRecordSet = aaaaRecordSets.iterator().next();
        Assertions.assertNotNull(aaaaRecordSet.eTag());
    }

    @Test
    public void canDeleteRecordSetWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
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
        Assertions.assertNotNull(aRecordSet.eTag());

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 1);
        AaaaRecordSet aaaaRecordSet = aaaaRecordSets.iterator().next();
        Assertions.assertNotNull(aaaaRecordSet.eTag());

        // Try delete with invalid eTag
        //
        Exception compositeException = null;
        try {
            dnsZone.update()
                    .withoutARecordSet("www", aRecordSet.eTag() + "-foo")
                    .withoutAaaaRecordSet("www", aaaaRecordSet.eTag() + "-foo")
                    .apply();
        } catch (Exception exception) {
            compositeException = exception;
        }
        Assertions.assertNotNull(compositeException);
        Assertions.assertEquals(3, compositeException.getSuppressed().length);
        for(int i = 0; i < 2; ++i) {
            Throwable exception = compositeException.getSuppressed()[i];
            Assertions.assertTrue(exception instanceof CloudException);
            CloudError cloudError = ((CloudException) exception).getValue();
            Assertions.assertNotNull(cloudError);
            Assertions.assertNotNull(cloudError.getCode());
            Assertions.assertTrue(cloudError.getCode().contains("PreconditionFailed"));
        }
        // Try delete with correct etags
        dnsZone.update()
                .withoutARecordSet("www", aRecordSet.eTag())
                .withoutAaaaRecordSet("www", aaaaRecordSet.eTag())
                .apply();

        // Check A records
        aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aRecordSets) == 0);

        // Check AAAA records
        aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(TestUtilities.getSize(aaaaRecordSets) == 0);
    }

    /**
     * Runs the action and assert that action throws CloudException with CloudError.Code
     * property set to 'PreconditionFailed'.
     *
     * @param runnable runnable to run
     */
    private void ensureETagExceptionIsThrown(final Runnable runnable) {
        boolean isCloudExceptionThrown = false;
        boolean isCloudErrorSet = false;
        boolean isPreconditionFailedCodeSet = false;
        try {
            runnable.run();
        } catch (CloudException exception) {
            isCloudExceptionThrown = true;
            CloudError cloudError = exception.getValue();
            if (cloudError != null) {
                isCloudErrorSet = true;
                isPreconditionFailedCodeSet = cloudError.getCode().contains("PreconditionFailed");
            }
        }
        Assertions.assertTrue(isCloudExceptionThrown, "Expected CloudException is not thrown");
        Assertions.assertTrue(isCloudErrorSet, "Expected CloudError property is not set in CloudException");
        Assertions.assertTrue(isPreconditionFailedCodeSet, "Expected PreconditionFailed code is not set indicating ETag concurrency check failure");
    }
}

