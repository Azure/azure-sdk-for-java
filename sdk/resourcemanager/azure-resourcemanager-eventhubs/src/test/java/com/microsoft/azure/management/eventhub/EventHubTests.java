/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountSkuType;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.exceptions.CompositeException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EventHubTests extends TestBase {
    protected EventHubManager eventHubManager;
    protected StorageManager storageManager;
    protected ResourceManager resourceManager;
    private static String RG_NAME = "";
    private static Region REGION = Region.US_EAST;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        eventHubManager = EventHubManager.authenticate(restClient, defaultSubscription);
        storageManager = StorageManager.authenticate(restClient, defaultSubscription);
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
        if (RG_NAME != null && !RG_NAME.isEmpty()) {
            resourceManager.resourceGroups().deleteByName(RG_NAME);
        }
    }

    @Test
    public void canManageEventHubNamespaceBasicSettings() {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName1 = SdkContext.randomResourceName("ns", 14);
        final String namespaceName2 = SdkContext.randomResourceName("ns", 14);
        final String namespaceName3 = SdkContext.randomResourceName("ns", 14);

        EventHubNamespace namespace1 = eventHubManager.namespaces()
                .define(namespaceName1)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME)
                    // SDK should use Sku as 'Standard' and set capacity.capacity in it as 1
                    .withAutoScaling()
                    .create();

        Assert.assertNotNull(namespace1);
        Assert.assertNotNull(namespace1.inner());
        Assert.assertNotNull(namespace1.sku());
        Assert.assertTrue(namespace1.sku().equals(EventHubNamespaceSkuType.STANDARD));
        Assert.assertTrue(namespace1.isAutoScaleEnabled());
        Assert.assertNotNull(namespace1.inner().maximumThroughputUnits());
        Assert.assertNotNull(namespace1.inner().sku().capacity());

        EventHubNamespace namespace2 = eventHubManager.namespaces()
                .define(namespaceName2)
                    .withRegion(REGION)
                    .withExistingResourceGroup(RG_NAME)
                    // SDK should use Sku as 'Standard' and set capacity.capacity in it as 11
                    .withCurrentThroughputUnits(11)
                    .create();

        Assert.assertNotNull(namespace2);
        Assert.assertNotNull(namespace2.inner());
        Assert.assertNotNull(namespace2.sku());
        Assert.assertTrue(namespace2.sku().equals(EventHubNamespaceSkuType.STANDARD));
        Assert.assertNotNull(namespace2.inner().maximumThroughputUnits());
        Assert.assertNotNull(namespace2.inner().sku().capacity());
        Assert.assertEquals(11, namespace2.currentThroughputUnits());

        EventHubNamespace namespace3 = eventHubManager.namespaces()
                .define(namespaceName3)
                    .withRegion(REGION)
                    .withExistingResourceGroup(RG_NAME)
                    .withSku(EventHubNamespaceSkuType.BASIC)
                    .create();

        Assert.assertNotNull(namespace3);
        Assert.assertNotNull(namespace3.inner());
        Assert.assertNotNull(namespace3.sku());
        Assert.assertTrue(namespace3.sku().equals(EventHubNamespaceSkuType.BASIC));

        namespace3.update()
                .withSku(EventHubNamespaceSkuType.STANDARD)
                .withTag("aa", "bb")
                .apply();

        Assert.assertNotNull(namespace3.sku());
        Assert.assertTrue(namespace3.sku().equals(EventHubNamespaceSkuType.STANDARD));
        Assert.assertNotNull(namespace3.tags());
        Assert.assertTrue(namespace3.tags().size() > 0);
    }

    @Test
    public void canManageEventHubNamespaceEventHubs() throws Exception {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = SdkContext.randomResourceName("ns", 14);
        final String eventHubName1 = SdkContext.randomResourceName("eh", 14);
        final String eventHubName2 = SdkContext.randomResourceName("eh", 14);
        final String eventHubName3 = SdkContext.randomResourceName("eh", 14);

        EventHubNamespace namespace = eventHubManager.namespaces()
                .define(namespaceName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME)
                    .withNewEventHub(eventHubName1)
                    .withNewEventHub(eventHubName2)
                    .create();

        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());

        PagedList<EventHub> hubs = namespace.listEventHubs();
        HashSet<String> set = new HashSet<>();
        for(EventHub hub : hubs) {
            set.add(hub.name());
        }
        Assert.assertTrue(set.contains(eventHubName1));
        Assert.assertTrue(set.contains(eventHubName2));

        hubs = eventHubManager.namespaces()
                .eventHubs()
                .listByNamespace(namespace.resourceGroupName(), namespace.name());

        set.clear();
        for(EventHub hub : hubs) {
            set.add(hub.name());
        }
        Assert.assertTrue(set.contains(eventHubName1));
        Assert.assertTrue(set.contains(eventHubName2));

        eventHubManager.namespaces()
                .eventHubs()
                    .define(eventHubName3)
                    .withExistingNamespaceId(namespace.id())
                    .withPartitionCount(5)
                    .withRetentionPeriodInDays(6)
                    .create();

        hubs = namespace.listEventHubs();
        set.clear();
        for(EventHub hub : hubs) {
            set.add(hub.name());
        }
        Assert.assertTrue(set.contains(eventHubName1));
        Assert.assertTrue(set.contains(eventHubName2));
        Assert.assertTrue(set.contains(eventHubName3));
    }

    @Test
    public void canManageEventHubNamespaceAuthorizationRules() throws Exception {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = SdkContext.randomResourceName("ns", 14);

        EventHubNamespace namespace = eventHubManager.namespaces()
                .define(namespaceName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME)
                    .withNewManageRule("mngRule1")
                    .withNewSendRule("sndRule1")
                    .create();

        Assert.assertNotNull(namespace);
        Assert.assertNotNull(namespace.inner());

        PagedList<EventHubNamespaceAuthorizationRule> rules = namespace.listAuthorizationRules();
        HashSet<String> set = new HashSet<>();
        for(EventHubNamespaceAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("mngRule1"));
        Assert.assertTrue(set.contains("sndRule1"));

        rules = eventHubManager.namespaces()
                .authorizationRules()
                .listByNamespace(namespace.resourceGroupName(), namespace.name());

        set.clear();
        for(EventHubNamespaceAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("mngRule1"));
        Assert.assertTrue(set.contains("sndRule1"));

        eventHubManager.namespaces()
                .authorizationRules()
                    .define("sndRule2")
                    .withExistingNamespaceId(namespace.id())
                    .withSendAccess()
                    .create();

        rules = namespace.listAuthorizationRules();
        set.clear();
        for(EventHubNamespaceAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("mngRule1"));
        Assert.assertTrue(set.contains("sndRule1"));
        Assert.assertTrue(set.contains("sndRule2"));

        eventHubManager.namespaces()
                .authorizationRules()
                .define("sndLsnRule3")
                .withExistingNamespaceId(namespace.id())
                .withSendAndListenAccess()
                .create();

        rules = namespace.listAuthorizationRules();
        Map<String, EventHubNamespaceAuthorizationRule> rulesMap = new HashMap<>();
        for (EventHubNamespaceAuthorizationRule rule : rules) {
            rulesMap.put(rule.name(), rule);
        }
        Assert.assertTrue(rulesMap.containsKey("sndLsnRule3"));
        Assert.assertEquals(
                new HashSet<>(Arrays.asList(AccessRights.SEND, AccessRights.LISTEN)),
                new HashSet<>(rulesMap.get("sndLsnRule3").rights()));
    }

    @Test
    public void canManageEventHubConsumerGroups() throws Exception {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = SdkContext.randomResourceName("ns", 14);
        final String eventHubName = SdkContext.randomResourceName("eh", 14);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubManager.namespaces()
                .define(namespaceName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME);

        EventHub eventHub = eventHubManager.eventHubs()
                .define(eventHubName)
                    .withNewNamespace(namespaceCreatable)
                    .withNewConsumerGroup("grp1")
                    .withNewConsumerGroup("grp2", "metadata111")
                    .create();

        Assert.assertNotNull(eventHub);
        Assert.assertNotNull(eventHub.inner());

        PagedList<EventHubConsumerGroup> cGroups = eventHub.listConsumerGroups();
        HashSet<String> set = new HashSet<>();
        for(EventHubConsumerGroup grp : cGroups) {
            set.add(grp.name());
        }
        Assert.assertTrue(set.contains("grp1"));
        Assert.assertTrue(set.contains("grp2"));

        cGroups = eventHubManager.eventHubs()
                .consumerGroups()
                .listByEventHub(eventHub.namespaceResourceGroupName(), eventHub.namespaceName(), eventHub.name());

        set.clear();
        for(EventHubConsumerGroup rule : cGroups) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("grp1"));
        Assert.assertTrue(set.contains("grp2"));

        eventHubManager.eventHubs()
                .consumerGroups()
                    .define("grp3")
                    .withExistingEventHubId(eventHub.id())
                    .withUserMetadata("metadata222")
                    .create();

        cGroups = eventHub.listConsumerGroups();
        set.clear();
        for(EventHubConsumerGroup grp : cGroups) {
            set.add(grp.name());
        }
        Assert.assertTrue(set.contains("grp1"));
        Assert.assertTrue(set.contains("grp2"));
        Assert.assertTrue(set.contains("grp3"));
    }

    @Test
    public void canManageEventHubAuthorizationRules() throws Exception {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = SdkContext.randomResourceName("ns", 14);
        final String eventHubName = SdkContext.randomResourceName("eh", 14);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubManager.namespaces()
                .define(namespaceName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME);

        EventHub eventHub = eventHubManager.eventHubs()
                .define(eventHubName)
                    .withNewNamespace(namespaceCreatable)
                    .withNewManageRule("mngRule1")
                    .withNewSendRule("sndRule1")
                    .create();

        Assert.assertNotNull(eventHub);
        Assert.assertNotNull(eventHub.inner());

        PagedList<EventHubAuthorizationRule> rules = eventHub.listAuthorizationRules();
        HashSet<String> set = new HashSet<>();
        for(EventHubAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("mngRule1"));
        Assert.assertTrue(set.contains("sndRule1"));

        rules = eventHubManager.eventHubs()
                .authorizationRules()
                .listByEventHub(eventHub.namespaceResourceGroupName(), eventHub.namespaceName(), eventHub.name());

        set.clear();
        for(EventHubAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("mngRule1"));
        Assert.assertTrue(set.contains("sndRule1"));

        eventHubManager.eventHubs()
                .authorizationRules()
                .define("sndRule2")
                    .withExistingEventHubId(eventHub.id())
                    .withSendAccess()
                    .create();

        rules = eventHub.listAuthorizationRules();
        set.clear();
        for(EventHubAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assert.assertTrue(set.contains("mngRule1"));
        Assert.assertTrue(set.contains("sndRule1"));
        Assert.assertTrue(set.contains("sndRule2"));
    }

    @Test
    @Ignore("Test uses data plane storage api")
    public void canConfigureEventHubDataCapturing() throws Exception {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String stgName = SdkContext.randomResourceName("stg", 14);
        final String namespaceName = SdkContext.randomResourceName("ns", 14);
        final String eventHubName1 = SdkContext.randomResourceName("eh", 14);
        final String eventHubName2 = SdkContext.randomResourceName("eh", 14);

        Creatable<StorageAccount> storageAccountCreatable = storageManager.storageAccounts()
                .define(stgName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME)
                    .withSku(StorageAccountSkuType.STANDARD_LRS);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubManager.namespaces()
                .define(namespaceName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME);

        final String containerName1 = "eventsctr1";

        EventHub eventHub1 = eventHubManager.eventHubs()
                .define(eventHubName1)
                    .withNewNamespace(namespaceCreatable)
                    .withNewStorageAccountForCapturedData(storageAccountCreatable, containerName1)
                    .withDataCaptureEnabled()
                    // Window config is optional if not set service will choose default for it2
                    //
                    .withDataCaptureWindowSizeInSeconds(120)
                    .withDataCaptureWindowSizeInMB(300)
                    .withDataCaptureSkipEmptyArchives(true)
                    .create();

        Assert.assertNotNull(eventHub1);
        Assert.assertNotNull(eventHub1.inner());

        Assert.assertNotNull(eventHub1.name());
        Assert.assertTrue(eventHub1.name().equalsIgnoreCase(eventHubName1));

        Assert.assertNotNull(eventHub1.partitionIds());

        Assert.assertTrue(eventHub1.isDataCaptureEnabled());
        Assert.assertNotNull(eventHub1.captureDestination());
        Assert.assertTrue(eventHub1.captureDestination().storageAccountResourceId().contains("/storageAccounts/"));
        Assert.assertTrue(eventHub1.captureDestination().storageAccountResourceId().contains(stgName));
        Assert.assertTrue(eventHub1.captureDestination().blobContainer().equalsIgnoreCase(containerName1));
        Assert.assertTrue(eventHub1.dataCaptureSkipEmptyArchives());

        // Create another event Hub in the same namespace with data capture uses the same storage account
        //
        String stgAccountId = eventHub1.captureDestination().storageAccountResourceId();
        final String containerName2 = "eventsctr2";

        EventHub eventHub2 = eventHubManager.eventHubs()
                .define(eventHubName2)
                    .withNewNamespace(namespaceCreatable)
                    .withExistingStorageAccountForCapturedData(stgAccountId, containerName2)
                    .withDataCaptureEnabled()
                    .create();

        Assert.assertTrue(eventHub2.isDataCaptureEnabled());
        Assert.assertNotNull(eventHub2.captureDestination());
        Assert.assertTrue(eventHub2.captureDestination().storageAccountResourceId().contains("/storageAccounts/"));
        Assert.assertTrue(eventHub2.captureDestination().storageAccountResourceId().contains(stgName));
        Assert.assertTrue(eventHub2.captureDestination().blobContainer().equalsIgnoreCase(containerName2));

        eventHub2.update()
                .withDataCaptureDisabled()
                .apply();

        Assert.assertFalse(eventHub2.isDataCaptureEnabled());
    }

    @Test
    @Ignore("Test uses data plane storage api")
    public void canEnableEventHubDataCaptureOnUpdate() {

        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String stgName = SdkContext.randomResourceName("stg", 14);
        final String namespaceName = SdkContext.randomResourceName("ns", 14);
        final String eventHubName = SdkContext.randomResourceName("eh", 14);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubManager.namespaces()
                .define(namespaceName)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME);

        EventHub eventHub = eventHubManager.eventHubs()
                .define(eventHubName)
                    .withNewNamespace(namespaceCreatable)
                    .create();

        boolean exceptionThrown = false;
        try {
            eventHub.update()
                    .withDataCaptureEnabled()
                    .apply();
        } catch (IllegalStateException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue("Expected IllegalStateException is not thrown", exceptionThrown);

        eventHub = eventHub.refresh();

        Creatable<StorageAccount> storageAccountCreatable = storageManager.storageAccounts()
                .define(stgName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withSku(StorageAccountSkuType.STANDARD_LRS);

        eventHub.update()
                .withDataCaptureEnabled()
                .withNewStorageAccountForCapturedData(storageAccountCreatable, "eventctr")
                .apply();

        Assert.assertTrue(eventHub.isDataCaptureEnabled());
        Assert.assertNotNull(eventHub.captureDestination());
        Assert.assertTrue(eventHub.captureDestination().storageAccountResourceId().contains("/storageAccounts/"));
        Assert.assertTrue(eventHub.captureDestination().storageAccountResourceId().contains(stgName));
        Assert.assertTrue(eventHub.captureDestination().blobContainer().equalsIgnoreCase("eventctr"));
    }

    @Test
    @Ignore("Server side: resource group delete operation (final clean up) keep running for hours when contains pairing")
    public void canManageGeoDisasterRecoveryPairing() throws Exception {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        final String geodrName = SdkContext.randomResourceName("geodr", 14);
        final String namespaceName1 = SdkContext.randomResourceName("ns", 14);
        final String namespaceName2 = SdkContext.randomResourceName("ns", 14);

        EventHubNamespace primaryNamespace = eventHubManager.namespaces()
                .define(namespaceName1)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(RG_NAME)
                .create();

        EventHubNamespace secondaryNamespace = eventHubManager.namespaces()
                .define(namespaceName2)
                .withRegion(Region.US_NORTH_CENTRAL)
                .withExistingResourceGroup(RG_NAME)
                .create();

        Exception exception = null;
        Exception breakingFailed = null;
        EventHubDisasterRecoveryPairing pairing = null;
        try {
            pairing = eventHubManager.eventHubDisasterRecoveryPairings()
                    .define(geodrName)
                    .withExistingPrimaryNamespace(primaryNamespace)
                    .withExistingSecondaryNamespace(secondaryNamespace)
                    .create();

            while (pairing.provisioningState() != ProvisioningStateDR.SUCCEEDED) {
                pairing = pairing.refresh();
                SdkContext.sleep(15 * 1000);
                if (pairing.provisioningState() == ProvisioningStateDR.FAILED) {
                    Assert.assertTrue("Provisioning state of the pairing is FAILED", false);
                }
            }


            Assert.assertTrue(pairing.name().equalsIgnoreCase(geodrName));
            Assert.assertTrue(pairing.primaryNamespaceResourceGroupName().equalsIgnoreCase(RG_NAME));
            Assert.assertTrue(pairing.primaryNamespaceName().equalsIgnoreCase(primaryNamespace.name()));
            Assert.assertTrue(pairing.secondaryNamespaceId().equalsIgnoreCase(secondaryNamespace.id()));

            PagedList<DisasterRecoveryPairingAuthorizationRule> rules = pairing.listAuthorizationRules();
            Assert.assertTrue(rules.size() > 0);
            for (DisasterRecoveryPairingAuthorizationRule rule : rules) {
                DisasterRecoveryPairingAuthorizationKey keys = rule.getKeys();
                Assert.assertNotNull(keys.aliasPrimaryConnectionString());
                Assert.assertNotNull(keys.aliasPrimaryConnectionString());
                Assert.assertNotNull(keys.primaryKey());
                Assert.assertNotNull(keys.secondaryKey());
            }

            EventHubDisasterRecoveryPairings pairingsCol = eventHubManager.eventHubDisasterRecoveryPairings();
            PagedList<EventHubDisasterRecoveryPairing> pairings = pairingsCol
                    .listByNamespace(primaryNamespace.resourceGroupName(), primaryNamespace.name());

            Assert.assertTrue(pairings.size() > 0);

            boolean found = false;
            for (EventHubDisasterRecoveryPairing pairing1 : pairings) {
                if (pairing1.name().equalsIgnoreCase(pairing.name())) {
                    found = true;
                    Assert.assertTrue(pairing1.primaryNamespaceResourceGroupName().equalsIgnoreCase(RG_NAME));
                    Assert.assertTrue(pairing1.primaryNamespaceName().equalsIgnoreCase(primaryNamespace.name()));
                    Assert.assertTrue(pairing1.secondaryNamespaceId().equalsIgnoreCase(secondaryNamespace.id()));
                }
            }
            Assert.assertTrue(found);
        } catch (Exception ex) {
            exception = ex;
        } finally {
            if (exception != null && pairing != null) {
                // Resource group cannot be deleted if the pairing-replication is progress so
                // pairing must forcefully break.
                try {
                    pairing.breakPairing();
                } catch (Exception ex) {
                    breakingFailed = ex;
                }
            }
        }
        if (exception != null && breakingFailed != null) {
            CompositeException cex = new CompositeException(exception, breakingFailed);
            throw cex;
        }
        if (exception != null) {
            throw exception;
        }
        if (breakingFailed != null) {
            throw breakingFailed;
        }
        pairing.refresh();
        pairing.failOver();
    }
}