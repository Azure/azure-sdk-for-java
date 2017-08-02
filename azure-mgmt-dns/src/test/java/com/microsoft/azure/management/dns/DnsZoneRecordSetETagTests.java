/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.dns;


import com.microsoft.azure.CloudError;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;
import rx.exceptions.CompositeException;
import rx.functions.Action0;

public class DnsZoneRecordSetETagTests extends TestBase {
    private static String RG_NAME = "";

    public DnsZoneRecordSetETagTests() {
        super(TestBase.RunCondition.BOTH);
    }

    protected ResourceManager resourceManager;
    protected DnsZoneManager zoneManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);
        zoneManager = DnsZoneManager
                .authenticate(restClient, defaultSubscription);
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
        Assert.assertNotNull(dnsZone.eTag());
        Action0 action = new Action0() {
            @Override
            public void call() {
                zoneManager.zones().define(topLevelDomain)
                        .withNewResourceGroup(RG_NAME, region)
                        .withETagCheck()
                        .create();
            }
        };
        ensureETagExceptionIsThrown(action);
    }

    @Test
    public void canUpdateZoneWithExplicitETag() throws Exception {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + generateRandomResourceName("z", 10) + ".com";

        final DnsZone dnsZone = zoneManager.zones().define(topLevelDomain)
                .withNewResourceGroup(RG_NAME, region)
                .withETagCheck()
                .create();
        Assert.assertNotNull(dnsZone.eTag());
        Action0 action = new Action0() {
            @Override
            public void call() {
                dnsZone.update()
                        .withETagCheck(dnsZone.eTag() + "-foo")
                        .apply();
            }
        };
        ensureETagExceptionIsThrown(action);
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
        Assert.assertNotNull(dnsZone.eTag());
        Action0 action = new Action0() {
            @Override
            public void call() {
                zoneManager.zones().deleteById(dnsZone.id(), dnsZone.eTag() + "-foo");
            }
        };
        ensureETagExceptionIsThrown(action);
        zoneManager.zones().deleteById(dnsZone.id(), dnsZone.eTag());
    }

    @Test
    public void canCreateRecordSetsWithDefaultETag() throws Exception {
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
        PagedList<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assert.assertTrue(aRecordSets.size() == 1);
        Assert.assertTrue(aRecordSets.get(0).timeToLive() == 7200);

        // Check AAAA records
        PagedList<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assert.assertTrue(aaaaRecordSets.size() == 1);
        Assert.assertTrue(aaaaRecordSets.get(0).name().startsWith("www"));
        Assert.assertTrue(aaaaRecordSets.get(0).ipv6Addresses().size() == 2);

        // Check CNAME records
        PagedList<CNameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        Assert.assertTrue(cnameRecordSets.size() == 2);

        CompositeException compositeException = null;
        try {
            zoneManager.zones().define(topLevelDomain)
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
                    .defineCNameRecordSet("documents")
                        .withAlias("doc.contoso.com")
                        .withETagCheck()
                        .attach()
                    .defineCNameRecordSet("userguide")
                        .withAlias("doc.contoso.com")
                        .withETagCheck()
                        .attach()
                    .create();
        } catch (CompositeException exception) {
            compositeException = exception;
        }
        Assert.assertNotNull(compositeException);
        Assert.assertEquals(4, compositeException.getExceptions().size());
        for(Throwable exception : compositeException.getExceptions()) {
            Assert.assertTrue(exception instanceof CloudException);
            CloudError cloudError = ((CloudException) exception).body();
            Assert.assertNotNull(cloudError);
            Assert.assertNotNull(cloudError.code());
            Assert.assertTrue(cloudError.code().contains("PreconditionFailed"));
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
        PagedList<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assert.assertTrue(aRecordSets.size() == 1);
        ARecordSet aRecordSet = aRecordSets.get(0);
        Assert.assertNotNull(aRecordSet.eTag());

        // Check AAAA records
        PagedList<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assert.assertTrue(aaaaRecordSets.size() == 1);
        AaaaRecordSet aaaaRecordSet = aaaaRecordSets.get(0);
        Assert.assertNotNull(aaaaRecordSet.eTag());

        // Try updates with invalid eTag
        //
        CompositeException compositeException = null;
        try {
            dnsZone.update()
                .updateARecordSet("www")
                    .withETagCheck(aRecordSet.eTag() + "-foo")
                    .parent()
                .updateAaaaRecordSet("www")
                    .withETagCheck(aaaaRecordSet.eTag() + "-foo")
                    .parent()
                .apply();
        } catch (CompositeException exception) {
            compositeException = exception;
        }
        Assert.assertNotNull(compositeException);
        Assert.assertEquals(2, compositeException.getExceptions().size());
        for(Throwable exception : compositeException.getExceptions()) {
            Assert.assertTrue(exception instanceof CloudException);
            CloudError cloudError = ((CloudException) exception).body();
            Assert.assertNotNull(cloudError);
            Assert.assertNotNull(cloudError.code());
            Assert.assertTrue(cloudError.code().contains("PreconditionFailed"));
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
        Assert.assertTrue(aRecordSets.size() == 1);
        aRecordSet = aRecordSets.get(0);
        Assert.assertNotNull(aRecordSet.eTag());
        Assert.assertTrue(aRecordSet.ipv4Addresses().size() == 3);

        // Check AAAA records
        aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assert.assertTrue(aaaaRecordSets.size() == 1);
        aaaaRecordSet = aaaaRecordSets.get(0);
        Assert.assertNotNull(aaaaRecordSet.eTag());
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
        PagedList<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assert.assertTrue(aRecordSets.size() == 1);
        ARecordSet aRecordSet = aRecordSets.get(0);
        Assert.assertNotNull(aRecordSet.eTag());

        // Check AAAA records
        PagedList<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assert.assertTrue(aaaaRecordSets.size() == 1);
        AaaaRecordSet aaaaRecordSet = aaaaRecordSets.get(0);
        Assert.assertNotNull(aaaaRecordSet.eTag());

        // Try delete with invalid eTag
        //
        CompositeException compositeException = null;
        try {
            dnsZone.update()
                    .withoutARecordSet("www", aRecordSet.eTag() + "-foo")
                    .withoutAaaaRecordSet("www", aaaaRecordSet.eTag() + "-foo")
                    .apply();
        } catch (CompositeException exception) {
            compositeException = exception;
        }
        Assert.assertNotNull(compositeException);
        Assert.assertEquals(2, compositeException.getExceptions().size());
        for(Throwable exception : compositeException.getExceptions()) {
            Assert.assertTrue(exception instanceof CloudException);
            CloudError cloudError = ((CloudException) exception).body();
            Assert.assertNotNull(cloudError);
            Assert.assertNotNull(cloudError.code());
            Assert.assertTrue(cloudError.code().contains("PreconditionFailed"));
        }
        // Try delete with correct etags
        dnsZone.update()
                .withoutARecordSet("www", aRecordSet.eTag())
                .withoutAaaaRecordSet("www", aaaaRecordSet.eTag())
                .apply();

        // Check A records
        aRecordSets = dnsZone.aRecordSets().list();
        Assert.assertTrue(aRecordSets.size() == 0);

        // Check AAAA records
        aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assert.assertTrue(aaaaRecordSets.size() == 0);
    }

    /**
     * Runs the action and assert that action throws CloudException with CloudError.Code
     * property set to 'PreconditionFailed'.
     *
     * @param action action to run
     */
    private void ensureETagExceptionIsThrown(final Action0 action) {
        boolean isCloudExceptionThrown = false;
        boolean isCloudErrorSet = false;
        boolean isPreconditionFailedCodeSet = false;
        try {
            action.call();
        } catch (CloudException exception) {
            isCloudExceptionThrown = true;
            CloudError cloudError = exception.body();
            if (cloudError != null) {
                isCloudErrorSet = true;
                isPreconditionFailedCodeSet = cloudError.code().contains("PreconditionFailed");
            }
        }
        Assert.assertTrue("Expected CloudException is not thrown", isCloudExceptionThrown);
        Assert.assertTrue("Expected CloudError property is not set in CloudException", isCloudErrorSet);
        Assert.assertTrue("Expected PreconditionFailed code is not set indicating ETag concurrency check failure", isPreconditionFailedCodeSet);
    }
}

