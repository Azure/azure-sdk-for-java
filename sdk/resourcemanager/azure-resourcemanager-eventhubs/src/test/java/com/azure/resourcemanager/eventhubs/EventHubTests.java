// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.models.AccessRights;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairing;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairings;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceSkuType;
import com.azure.resourcemanager.eventhubs.models.ProvisioningStateDR;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class EventHubTests extends ResourceManagerTestBase {
    protected EventHubsManager eventHubsManager;
    protected StorageManager storageManager;
    protected ResourceManager resourceManager;
    private String rgName = "";
    private final Region region = Region.US_EAST;

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
        eventHubsManager = buildManager(EventHubsManager.class, httpPipeline, profile);
        storageManager = buildManager(StorageManager.class, httpPipeline, profile);
        resourceManager = eventHubsManager.resourceManager();
    }

    @Override
    protected void cleanUpResources() {
        if (rgName != null && !rgName.isEmpty()) {
            resourceManager.resourceGroups().deleteByName(rgName);
        }
    }

    @Test
    public void canManageEventHubNamespaceBasicSettings() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName1 = generateRandomResourceName("ns", 14);
        final String namespaceName2 = generateRandomResourceName("ns", 14);
        final String namespaceName3 = generateRandomResourceName("ns", 14);

        EventHubNamespace namespace1 = eventHubsManager.namespaces()
                .define(namespaceName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    // SDK should use Sku as 'Standard' and set capacity.capacity in it as 1
                    .withAutoScaling()
                    .create();

        Assertions.assertNotNull(namespace1);
        Assertions.assertNotNull(namespace1.innerModel());
        Assertions.assertNotNull(namespace1.sku());
        Assertions.assertTrue(namespace1.sku().equals(EventHubNamespaceSkuType.STANDARD));
        Assertions.assertTrue(namespace1.isAutoScaleEnabled());
        Assertions.assertNotNull(namespace1.innerModel().maximumThroughputUnits());
        Assertions.assertNotNull(namespace1.innerModel().sku().capacity());

        EventHubNamespace namespace2 = eventHubsManager.namespaces()
                .define(namespaceName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    // SDK should use Sku as 'Standard' and set capacity.capacity in it as 11
                    .withCurrentThroughputUnits(11)
                    .create();

        Assertions.assertNotNull(namespace2);
        Assertions.assertNotNull(namespace2.innerModel());
        Assertions.assertNotNull(namespace2.sku());
        Assertions.assertTrue(namespace2.sku().equals(EventHubNamespaceSkuType.STANDARD));
        Assertions.assertNotNull(namespace2.innerModel().maximumThroughputUnits());
        Assertions.assertNotNull(namespace2.innerModel().sku().capacity());
        Assertions.assertEquals(11, namespace2.currentThroughputUnits());

        EventHubNamespace namespace3 = eventHubsManager.namespaces()
                .define(namespaceName3)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withSku(EventHubNamespaceSkuType.BASIC)
                    .create();

        Assertions.assertNotNull(namespace3);
        Assertions.assertNotNull(namespace3.innerModel());
        Assertions.assertNotNull(namespace3.sku());
        Assertions.assertTrue(namespace3.sku().equals(EventHubNamespaceSkuType.BASIC));

        namespace3.update()
                .withSku(EventHubNamespaceSkuType.STANDARD)
                .withTag("aa", "bb")
                .apply();

        Assertions.assertNotNull(namespace3.sku());
        Assertions.assertTrue(namespace3.sku().equals(EventHubNamespaceSkuType.STANDARD));
        Assertions.assertNotNull(namespace3.tags());
        Assertions.assertTrue(namespace3.tags().size() > 0);
    }

    @Test
    public void canManageEventHubNamespaceEventHubs() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = generateRandomResourceName("ns", 14);
        final String eventHubName1 = generateRandomResourceName("eh", 14);
        final String eventHubName2 = generateRandomResourceName("eh", 14);
        final String eventHubName3 = generateRandomResourceName("eh", 14);

        EventHubNamespace namespace = eventHubsManager.namespaces()
                .define(namespaceName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewEventHub(eventHubName1)
                    .withNewEventHub(eventHubName2)
                    .create();

        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());

        PagedIterable<EventHub> hubs = namespace.listEventHubs();
        HashSet<String> set = new HashSet<>();
        for (EventHub hub : hubs) {
            set.add(hub.name());
        }
        Assertions.assertTrue(set.contains(eventHubName1));
        Assertions.assertTrue(set.contains(eventHubName2));

        hubs = eventHubsManager.namespaces()
                .eventHubs()
                .listByNamespace(namespace.resourceGroupName(), namespace.name());

        set.clear();
        for (EventHub hub : hubs) {
            set.add(hub.name());
        }
        Assertions.assertTrue(set.contains(eventHubName1));
        Assertions.assertTrue(set.contains(eventHubName2));

        eventHubsManager.namespaces()
                .eventHubs()
                    .define(eventHubName3)
                    .withExistingNamespaceId(namespace.id())
                    .withPartitionCount(5)
                    .withRetentionPeriodInDays(6)
                    .create();

        hubs = namespace.listEventHubs();
        set.clear();
        for (EventHub hub : hubs) {
            set.add(hub.name());
        }
        Assertions.assertTrue(set.contains(eventHubName1));
        Assertions.assertTrue(set.contains(eventHubName2));
        Assertions.assertTrue(set.contains(eventHubName3));
    }

    @Test
    public void canManageEventHubNamespaceAuthorizationRules() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = generateRandomResourceName("ns", 14);

        EventHubNamespace namespace = eventHubsManager.namespaces()
                .define(namespaceName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewManageRule("mngRule1")
                    .withNewSendRule("sndRule1")
                    .create();

        Assertions.assertNotNull(namespace);
        Assertions.assertNotNull(namespace.innerModel());

        PagedIterable<EventHubNamespaceAuthorizationRule> rules = namespace.listAuthorizationRules();
        HashSet<String> set = new HashSet<>();
        for (EventHubNamespaceAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("mngRule1"));
        Assertions.assertTrue(set.contains("sndRule1"));

        rules = eventHubsManager.namespaces()
                .authorizationRules()
                .listByNamespace(namespace.resourceGroupName(), namespace.name());

        set.clear();
        for (EventHubNamespaceAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("mngRule1"));
        Assertions.assertTrue(set.contains("sndRule1"));

        eventHubsManager.namespaces()
                .authorizationRules()
                    .define("sndRule2")
                    .withExistingNamespaceId(namespace.id())
                    .withSendAccess()
                    .create();

        rules = namespace.listAuthorizationRules();
        set.clear();
        for (EventHubNamespaceAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("mngRule1"));
        Assertions.assertTrue(set.contains("sndRule1"));
        Assertions.assertTrue(set.contains("sndRule2"));

        eventHubsManager.namespaces()
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
        Assertions.assertTrue(rulesMap.containsKey("sndLsnRule3"));
        Assertions.assertEquals(
                new HashSet<>(Arrays.asList(AccessRights.SEND, AccessRights.LISTEN)),
                new HashSet<>(rulesMap.get("sndLsnRule3").rights()));
    }

    @Test
    public void canManageEventHubConsumerGroups() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = generateRandomResourceName("ns", 14);
        final String eventHubName = generateRandomResourceName("eh", 14);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubsManager.namespaces()
                .define(namespaceName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName);

        EventHub eventHub = eventHubsManager.eventHubs()
                .define(eventHubName)
                    .withNewNamespace(namespaceCreatable)
                    .withNewConsumerGroup("grp1")
                    .withNewConsumerGroup("grp2", "metadata111")
                    .create();

        Assertions.assertNotNull(eventHub);
        Assertions.assertNotNull(eventHub.innerModel());

        PagedIterable<EventHubConsumerGroup> cGroups = eventHub.listConsumerGroups();
        HashSet<String> set = new HashSet<>();
        for (EventHubConsumerGroup grp : cGroups) {
            set.add(grp.name());
        }
        Assertions.assertTrue(set.contains("grp1"));
        Assertions.assertTrue(set.contains("grp2"));

        cGroups = eventHubsManager.eventHubs()
                .consumerGroups()
                .listByEventHub(eventHub.namespaceResourceGroupName(), eventHub.namespaceName(), eventHub.name());

        set.clear();
        for (EventHubConsumerGroup rule : cGroups) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("grp1"));
        Assertions.assertTrue(set.contains("grp2"));

        eventHubsManager.eventHubs()
                .consumerGroups()
                    .define("grp3")
                    .withExistingEventHubId(eventHub.id())
                    .withUserMetadata("metadata222")
                    .create();

        cGroups = eventHub.listConsumerGroups();
        set.clear();
        for (EventHubConsumerGroup grp : cGroups) {
            set.add(grp.name());
        }
        Assertions.assertTrue(set.contains("grp1"));
        Assertions.assertTrue(set.contains("grp2"));
        Assertions.assertTrue(set.contains("grp3"));
    }

    @Test
    public void canManageEventHubAuthorizationRules() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String namespaceName = generateRandomResourceName("ns", 14);
        final String eventHubName = generateRandomResourceName("eh", 14);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubsManager.namespaces()
                .define(namespaceName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName);

        EventHub eventHub = eventHubsManager.eventHubs()
                .define(eventHubName)
                    .withNewNamespace(namespaceCreatable)
                    .withNewManageRule("mngRule1")
                    .withNewSendRule("sndRule1")
                    .create();

        Assertions.assertNotNull(eventHub);
        Assertions.assertNotNull(eventHub.innerModel());

        PagedIterable<EventHubAuthorizationRule> rules = eventHub.listAuthorizationRules();
        HashSet<String> set = new HashSet<>();
        for (EventHubAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("mngRule1"));
        Assertions.assertTrue(set.contains("sndRule1"));

        rules = eventHubsManager.eventHubs()
                .authorizationRules()
                .listByEventHub(eventHub.namespaceResourceGroupName(), eventHub.namespaceName(), eventHub.name());

        set.clear();
        for (EventHubAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("mngRule1"));
        Assertions.assertTrue(set.contains("sndRule1"));

        eventHubsManager.eventHubs()
                .authorizationRules()
                .define("sndRule2")
                    .withExistingEventHubId(eventHub.id())
                    .withSendAccess()
                    .create();

        rules = eventHub.listAuthorizationRules();
        set.clear();
        for (EventHubAuthorizationRule rule : rules) {
            set.add(rule.name());
        }
        Assertions.assertTrue(set.contains("mngRule1"));
        Assertions.assertTrue(set.contains("sndRule1"));
        Assertions.assertTrue(set.contains("sndRule2"));
    }

    @Test
    public void canConfigureEventHubDataCapturing() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String stgName = generateRandomResourceName("stg", 14);
        final String namespaceName = generateRandomResourceName("ns", 14);
        final String eventHubName1 = generateRandomResourceName("eh", 14);
        final String eventHubName2 = generateRandomResourceName("eh", 14);

        Creatable<StorageAccount> storageAccountCreatable = storageManager.storageAccounts()
                .define(stgName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withSku(StorageAccountSkuType.STANDARD_LRS);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubsManager.namespaces()
                .define(namespaceName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName);

        final String containerName1 = "eventsctr1";

        EventHub eventHub1 = eventHubsManager.eventHubs()
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

        Assertions.assertNotNull(eventHub1);
        Assertions.assertNotNull(eventHub1.innerModel());

        Assertions.assertNotNull(eventHub1.name());
        Assertions.assertTrue(eventHub1.name().equalsIgnoreCase(eventHubName1));

        Assertions.assertNotNull(eventHub1.partitionIds());

        Assertions.assertTrue(eventHub1.isDataCaptureEnabled());
        Assertions.assertNotNull(eventHub1.captureDestination());
        Assertions.assertTrue(eventHub1.captureDestination().storageAccountResourceId().contains("/storageAccounts/"));
        Assertions.assertTrue(eventHub1.captureDestination().storageAccountResourceId().contains(stgName));
        Assertions.assertTrue(eventHub1.captureDestination().blobContainer().equalsIgnoreCase(containerName1));
        Assertions.assertTrue(eventHub1.dataCaptureSkipEmptyArchives());

        // Create another event Hub in the same namespace with data capture uses the same storage account
        //
        String stgAccountId = eventHub1.captureDestination().storageAccountResourceId();
        final String containerName2 = "eventsctr2";

        EventHub eventHub2 = eventHubsManager.eventHubs()
                .define(eventHubName2)
                    .withNewNamespace(namespaceCreatable)
                    .withExistingStorageAccountForCapturedData(stgAccountId, containerName2)
                    .withDataCaptureEnabled()
                    .create();

        Assertions.assertTrue(eventHub2.isDataCaptureEnabled());
        Assertions.assertNotNull(eventHub2.captureDestination());
        Assertions.assertTrue(eventHub2.captureDestination().storageAccountResourceId().contains("/storageAccounts/"));
        Assertions.assertTrue(eventHub2.captureDestination().storageAccountResourceId().contains(stgName));
        Assertions.assertTrue(eventHub2.captureDestination().blobContainer().equalsIgnoreCase(containerName2));

        eventHub2.update()
                .withDataCaptureDisabled()
                .apply();

        Assertions.assertFalse(eventHub2.isDataCaptureEnabled());
    }

    @Test
    public void canEnableEventHubDataCaptureOnUpdate() {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String stgName = generateRandomResourceName("stg", 14);
        final String namespaceName = generateRandomResourceName("ns", 14);
        final String eventHubName = generateRandomResourceName("eh", 14);

        Creatable<EventHubNamespace> namespaceCreatable = eventHubsManager.namespaces()
                .define(namespaceName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName);

        EventHub eventHub = eventHubsManager.eventHubs()
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
        Assertions.assertTrue(exceptionThrown, "Expected IllegalStateException is not thrown");

        eventHub = eventHub.refresh();

        Creatable<StorageAccount> storageAccountCreatable = storageManager.storageAccounts()
                .define(stgName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_LRS);

        eventHub.update()
                .withDataCaptureEnabled()
                .withNewStorageAccountForCapturedData(storageAccountCreatable, "eventctr")
                .apply();

        Assertions.assertTrue(eventHub.isDataCaptureEnabled());
        Assertions.assertNotNull(eventHub.captureDestination());
        Assertions.assertTrue(eventHub.captureDestination().storageAccountResourceId().contains("/storageAccounts/"));
        Assertions.assertTrue(eventHub.captureDestination().storageAccountResourceId().contains(stgName));
        Assertions.assertTrue(eventHub.captureDestination().blobContainer().equalsIgnoreCase("eventctr"));
    }

    @Test
    public void canManageGeoDisasterRecoveryPairing() throws Throwable {
        rgName = generateRandomResourceName("javacsmrg", 15);
        final String geodrName = generateRandomResourceName("geodr", 14);
        final String namespaceName1 = generateRandomResourceName("ns", 14);
        final String namespaceName2 = generateRandomResourceName("ns", 14);

        EventHubNamespace primaryNamespace = eventHubsManager.namespaces()
                .define(namespaceName1)
                .withRegion(Region.US_SOUTH_CENTRAL)
                .withNewResourceGroup(rgName)
                .create();

        EventHubNamespace secondaryNamespace = eventHubsManager.namespaces()
                .define(namespaceName2)
                .withRegion(Region.US_NORTH_CENTRAL)
                .withExistingResourceGroup(rgName)
                .create();

        Exception exception = null;
        Exception breakingFailed = null;
        EventHubDisasterRecoveryPairing pairing = null;
        try {
            pairing = eventHubsManager.eventHubDisasterRecoveryPairings()
                    .define(geodrName)
                    .withExistingPrimaryNamespace(primaryNamespace)
                    .withExistingSecondaryNamespace(secondaryNamespace)
                    .create();

            while (pairing.provisioningState() != ProvisioningStateDR.SUCCEEDED) {
                pairing = pairing.refresh();
                ResourceManagerUtils.sleep(Duration.ofSeconds(15));
                if (pairing.provisioningState() == ProvisioningStateDR.FAILED) {
                    Assertions.assertTrue(false, "Provisioning state of the pairing is FAILED");
                }
            }


            Assertions.assertTrue(pairing.name().equalsIgnoreCase(geodrName));
            Assertions.assertTrue(pairing.primaryNamespaceResourceGroupName().equalsIgnoreCase(rgName));
            Assertions.assertTrue(pairing.primaryNamespaceName().equalsIgnoreCase(primaryNamespace.name()));
            Assertions.assertTrue(pairing.secondaryNamespaceId().equalsIgnoreCase(secondaryNamespace.id()));

            PagedIterable<DisasterRecoveryPairingAuthorizationRule> rules = pairing.listAuthorizationRules();
            Assertions.assertTrue(TestUtilities.getSize(rules) > 0);
            for (DisasterRecoveryPairingAuthorizationRule rule : rules) {
                DisasterRecoveryPairingAuthorizationKey keys = rule.getKeys();
                Assertions.assertNotNull(keys.aliasPrimaryConnectionString());
                Assertions.assertNotNull(keys.aliasPrimaryConnectionString());
                Assertions.assertNotNull(keys.primaryKey());
                Assertions.assertNotNull(keys.secondaryKey());
            }

            EventHubDisasterRecoveryPairings pairingsCol = eventHubsManager.eventHubDisasterRecoveryPairings();
            PagedIterable<EventHubDisasterRecoveryPairing> pairings = pairingsCol
                    .listByNamespace(primaryNamespace.resourceGroupName(), primaryNamespace.name());

            Assertions.assertTrue(TestUtilities.getSize(pairings) > 0);

            boolean found = false;
            for (EventHubDisasterRecoveryPairing pairing1 : pairings) {
                if (pairing1.name().equalsIgnoreCase(pairing.name())) {
                    found = true;
                    Assertions.assertTrue(pairing1.primaryNamespaceResourceGroupName().equalsIgnoreCase(rgName));
                    Assertions.assertTrue(pairing1.primaryNamespaceName().equalsIgnoreCase(primaryNamespace.name()));
                    Assertions.assertTrue(pairing1.secondaryNamespaceId().equalsIgnoreCase(secondaryNamespace.id()));
                }
            }
            Assertions.assertTrue(found);
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
            throw Exceptions.addSuppressed(exception, breakingFailed);
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
