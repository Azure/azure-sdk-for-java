// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.slis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.monitor.slis.fluent.models.SliInner;
import com.azure.resourcemanager.monitor.slis.models.AmwAccount;
import com.azure.resourcemanager.monitor.slis.models.Baseline;
import com.azure.resourcemanager.monitor.slis.models.BaselineProperties;
import com.azure.resourcemanager.monitor.slis.models.Category;
import com.azure.resourcemanager.monitor.slis.models.Condition;
import com.azure.resourcemanager.monitor.slis.models.ConditionOperator;
import com.azure.resourcemanager.monitor.slis.models.EvaluationCalculationType;
import com.azure.resourcemanager.monitor.slis.models.EvaluationType;
import com.azure.resourcemanager.monitor.slis.models.ManagedServiceIdentity;
import com.azure.resourcemanager.monitor.slis.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.monitor.slis.models.Signal;
import com.azure.resourcemanager.monitor.slis.models.SignalSource;
import com.azure.resourcemanager.monitor.slis.models.Sli;
import com.azure.resourcemanager.monitor.slis.models.SliProperties;
import com.azure.resourcemanager.monitor.slis.models.SliResource;
import com.azure.resourcemanager.monitor.slis.models.SpatialAggregation;
import com.azure.resourcemanager.monitor.slis.models.SpatialAggregationType;
import com.azure.resourcemanager.monitor.slis.models.TemporalAggregation;
import com.azure.resourcemanager.monitor.slis.models.TemporalAggregationType;
import com.azure.resourcemanager.monitor.slis.models.UserAssignedIdentity;
import com.azure.resourcemanager.monitor.slis.models.WindowUptimeCriteria;
import com.azure.resourcemanager.monitor.slis.models.WindowUptimeCriteriaComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Record/playback CRUD test for Microsoft.Monitor/slis resource.
 * Tests: Create -> Get -> Delete -> Get (expect 404).
 */
public class SliCrudLiveTest extends TestProxyTestBase {

    // Sanitized placeholder subscription used in playback; matches what test-proxy writes into
    // recordings when the live subscription id is redacted by the default sanitizer.
    private static final String PLAYBACK_SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";

    private static final String DEFAULT_AMW_RESOURCE_ID_TEMPLATE
        = "/subscriptions/%s/resourceGroups/arm-sdk-tests-rg/providers/microsoft.monitor/accounts/amw-arm-sdk-tests-rg";
    private static final String DEFAULT_MANAGED_IDENTITY_RESOURCE_ID_TEMPLATE
        = "/subscriptions/%s/resourceGroups/arm-sdk-tests-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/uami-arm-sdk-tests-rg";

    private String serviceGroupName;
    private String amwResourceId;
    private String managedIdentityResourceId;
    private String sourceAmwResourceId;
    private String sourceManagedIdentityResourceId;

    private void initializeTestValues() {
        if (!interceptorManager.isLiveMode()) {
            // Redact real subscription ids in recordings so the playback test (and committed
            // recording) doesn't depend on / leak the recording sub id.
            interceptorManager.addSanitizers(new TestProxySanitizer(
                "/subscriptions/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
                "/subscriptions/" + PLAYBACK_SUBSCRIPTION_ID, TestProxySanitizerType.BODY_REGEX));
        }
        if (interceptorManager.isPlaybackMode()) {
            serviceGroupName = "arm-sdk-tests-sg";
            amwResourceId = String.format(DEFAULT_AMW_RESOURCE_ID_TEMPLATE, PLAYBACK_SUBSCRIPTION_ID);
            managedIdentityResourceId
                = String.format(DEFAULT_MANAGED_IDENTITY_RESOURCE_ID_TEMPLATE, PLAYBACK_SUBSCRIPTION_ID);
        } else {
            serviceGroupName = System.getenv().getOrDefault("SERVICE_GROUP_NAME", "arm-sdk-tests-sg");
            amwResourceId = System.getenv()
                .getOrDefault("AMW_RESOURCE_ID", String.format(DEFAULT_AMW_RESOURCE_ID_TEMPLATE,
                    System.getenv().getOrDefault("AZURE_SUBSCRIPTION_ID", PLAYBACK_SUBSCRIPTION_ID)));
            managedIdentityResourceId = System.getenv()
                .getOrDefault("MANAGED_IDENTITY_RESOURCE_ID",
                    String.format(DEFAULT_MANAGED_IDENTITY_RESOURCE_ID_TEMPLATE,
                        System.getenv().getOrDefault("AZURE_SUBSCRIPTION_ID", PLAYBACK_SUBSCRIPTION_ID)));
        }
        sourceAmwResourceId = System.getenv().getOrDefault("SOURCE_AMW_RESOURCE_ID", amwResourceId);
        sourceManagedIdentityResourceId
            = System.getenv().getOrDefault("SOURCE_MANAGED_IDENTITY_RESOURCE_ID", managedIdentityResourceId);
    }

    private SlisManager buildManager() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        SlisManager.Configurable configurable
            = SlisManager.configure().withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        TokenCredential credential;
        if (getTestMode() == TestMode.PLAYBACK) {
            credential = new MockTokenCredential();
            configurable.withHttpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            credential = new DefaultAzureCredentialBuilder().build();
            configurable.withPolicy(interceptorManager.getRecordPolicy());
        } else {
            // LIVE
            credential = new DefaultAzureCredentialBuilder().build();
        }
        return configurable.authenticate(credential, profile);
    }

    @Test
    public void testSliCrudLifecycle() {
        initializeTestValues();
        SlisManager manager = buildManager();
        String sliName = testResourceNamer.randomName("javasli", 16);
        boolean deleted = false;

        try {
            Sli createdSli = manager.slis()
                .createOrUpdate(serviceGroupName, sliName,
                    new SliInner().withIdentity(createIdentity()).withProperties(createSliResource()));

            Assertions.assertNotNull(createdSli);
            Assertions.assertNotNull(createdSli.name());

            Sli retrievedSli = manager.slis().get(serviceGroupName, sliName);
            Assertions.assertNotNull(retrievedSli);
            Assertions.assertNotNull(retrievedSli.properties());
            Assertions.assertEquals(Category.LATENCY, retrievedSli.properties().category());
            Assertions.assertEquals(EvaluationType.WINDOW_BASED, retrievedSli.properties().evaluationType());

            manager.slis().deleteByResourceGroup(serviceGroupName, sliName);
            deleted = true;

            Assertions.assertThrows(ManagementException.class, () -> manager.slis().get(serviceGroupName, sliName));
        } finally {
            if (!deleted) {
                try {
                    manager.slis().deleteByResourceGroup(serviceGroupName, sliName);
                } catch (ManagementException ignored) {
                    // Resource may not have been created or may already be deleted.
                }
            }
        }
    }

    private ManagedServiceIdentity createIdentity() {
        Map<String, UserAssignedIdentity> identities = new HashMap<>();
        identities.put(managedIdentityResourceId, new UserAssignedIdentity());
        if (!managedIdentityResourceId.equalsIgnoreCase(sourceManagedIdentityResourceId)) {
            identities.put(sourceManagedIdentityResourceId, new UserAssignedIdentity());
        }
        return new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
            .withUserAssignedIdentities(identities);
    }

    private SliResource createSliResource() {
        return new SliResource().withDescription("Live test SLI - measures latency of test API")
            .withCategory(Category.LATENCY)
            .withEvaluationType(EvaluationType.WINDOW_BASED)
            .withEnableAlert(true)
            .withDestinationAmwAccounts(Collections
                .singletonList(new AmwAccount().withResourceId(amwResourceId).withIdentity(managedIdentityResourceId)))
            .withBaselineProperties(new BaselineProperties().withBaseline(new Baseline().withValue(99.0)
                .withEvaluationPeriodDays(30)
                .withEvaluationCalculationType(EvaluationCalculationType.CALENDAR_DAYS)))
            .withSliProperties(new SliProperties()
                .withWindowUptimeCriteria(new WindowUptimeCriteria().withTarget(95.0)
                    .withComparator(WindowUptimeCriteriaComparator.GREATER_THAN_OR_EQUAL))
                .withSignals(new Signal().withSignalFormula("A")
                    .withSignalSources(Collections.singletonList(new SignalSource().withSignalSourceId("A")
                        .withSourceAmwAccountManagedIdentity(sourceManagedIdentityResourceId)
                        .withSourceAmwAccountResourceId(sourceAmwResourceId)
                        // Source metric is a real Azure Managed Prometheus metric scraped by AKS.
                        // Test infra (bicep) deploys an AKS cluster with the Azure Monitor metrics addon
                        // pointed at the source AMW; container_cpu_usage_seconds_total is always populated.
                        .withMetricNamespace("customdefault")
                        .withMetricName("container_cpu_usage_seconds_total")
                        .withFilters(Collections.singletonList(new Condition().withDimensionName("container")
                            .withOperator(ConditionOperator.NOT_EQUAL)
                            .withValue("POD")))
                        .withSpatialAggregation(new SpatialAggregation().withType(SpatialAggregationType.SUM)
                            .withDimensions(Arrays.asList("instance")))
                        .withTemporalAggregation(new TemporalAggregation().withType(TemporalAggregationType.RATE)
                            .withWindowSizeMinutes(1))))));
    }
}
