// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.slis;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
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
import com.azure.resourcemanager.monitor.slis.models.Signal;
import com.azure.resourcemanager.monitor.slis.models.SignalSource;
import com.azure.resourcemanager.monitor.slis.models.Sli;
import com.azure.resourcemanager.monitor.slis.models.SliProperties;
import com.azure.resourcemanager.monitor.slis.models.SliResource;
import com.azure.resourcemanager.monitor.slis.models.SpatialAggregation;
import com.azure.resourcemanager.monitor.slis.models.SpatialAggregationType;
import com.azure.resourcemanager.monitor.slis.models.TemporalAggregation;
import com.azure.resourcemanager.monitor.slis.models.TemporalAggregationType;
import com.azure.resourcemanager.monitor.slis.models.WindowUptimeCriteria;
import com.azure.resourcemanager.monitor.slis.models.WindowUptimeCriteriaComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * Live CRUD test for Microsoft.Monitor/slis resource.
 * Tests: Create ΓåÆ Get ΓåÆ Delete ΓåÆ Get (expect 404).
 */
@LiveOnly
public class SliCrudLiveTest extends TestProxyTestBase {

    private static final String SERVICE_GROUP_NAME
        = System.getenv().getOrDefault("SERVICE_GROUP_NAME", "arm-sdk-tests-sg");
    private static final String AMW_RESOURCE_ID = System.getenv()
        .getOrDefault("AMW_RESOURCE_ID",
            "/subscriptions/6820e35f-0fe6-4af3-aad2-27414fa82621/resourceGroups/mfrei/providers/microsoft.monitor/accounts/streaming-3p-slo-am2cbn-eastus2euap-1");
    private static final String MANAGED_IDENTITY_RESOURCE_ID = System.getenv()
        .getOrDefault("MANAGED_IDENTITY_RESOURCE_ID",
            "/subscriptions/6820e35f-0fe6-4af3-aad2-27414fa82621/resourceGroups/mfrei/providers/Microsoft.ManagedIdentity/userAssignedIdentities/mfrei-test-user-managed-identity");
    private static final String SOURCE_AMW_RESOURCE_ID
        = System.getenv().getOrDefault("SOURCE_AMW_RESOURCE_ID", AMW_RESOURCE_ID);
    private static final String SOURCE_MANAGED_IDENTITY_RESOURCE_ID
        = System.getenv().getOrDefault("SOURCE_MANAGED_IDENTITY_RESOURCE_ID", MANAGED_IDENTITY_RESOURCE_ID);

    private SlisManager getManager() {
        return SlisManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));
    }

    @Test
    public void testSliCrudLifecycle() {
        SlisManager manager = getManager();
        String sliName = "javaslitest-" + UUID.randomUUID().toString().substring(0, 8);
        boolean deleted = false;

        try {
            Sli createdSli = manager.slis()
                .createOrUpdate(SERVICE_GROUP_NAME, sliName, new SliInner().withProperties(createSliResource()));

            Assertions.assertNotNull(createdSli);
            Assertions.assertEquals(sliName, createdSli.name());

            Sli retrievedSli = manager.slis().get(SERVICE_GROUP_NAME, sliName);
            Assertions.assertNotNull(retrievedSli);
            Assertions.assertNotNull(retrievedSli.properties());
            Assertions.assertEquals(Category.LATENCY, retrievedSli.properties().category());
            Assertions.assertEquals(EvaluationType.WINDOW_BASED, retrievedSli.properties().evaluationType());

            manager.slis().deleteByResourceGroup(SERVICE_GROUP_NAME, sliName);
            deleted = true;

            Assertions.assertThrows(ManagementException.class, () -> manager.slis().get(SERVICE_GROUP_NAME, sliName));
        } finally {
            if (!deleted) {
                try {
                    manager.slis().deleteByResourceGroup(SERVICE_GROUP_NAME, sliName);
                } catch (ManagementException ignored) {
                    // Resource may not have been created or may already be deleted.
                }
            }
        }
    }

    private SliResource createSliResource() {
        return new SliResource().withDescription("Live test SLI - measures latency of test API")
            .withCategory(Category.LATENCY)
            .withEvaluationType(EvaluationType.WINDOW_BASED)
            .withEnableAlert(true)
            .withDestinationAmwAccounts(Collections.singletonList(
                new AmwAccount().withResourceId(AMW_RESOURCE_ID).withIdentity(MANAGED_IDENTITY_RESOURCE_ID)))
            .withBaselineProperties(new BaselineProperties().withBaseline(new Baseline().withValue(99.0)
                .withEvaluationPeriodDays(30)
                .withEvaluationCalculationType(EvaluationCalculationType.CALENDAR_DAYS)))
            .withSliProperties(new SliProperties()
                .withWindowUptimeCriteria(new WindowUptimeCriteria().withTarget(95.0)
                    .withComparator(WindowUptimeCriteriaComparator.GREATER_THAN_OR_EQUAL))
                .withSignals(new Signal().withSignalFormula("A")
                    .withSignalSources(Collections.singletonList(new SignalSource().withSignalSourceId("A")
                        .withSourceAmwAccountManagedIdentity(SOURCE_MANAGED_IDENTITY_RESOURCE_ID)
                        .withSourceAmwAccountResourceId(SOURCE_AMW_RESOURCE_ID)
                        .withMetricNamespace("TestMetrics")
                        .withMetricName("TestLatency")
                        .withFilters(Collections.singletonList(new Condition().withDimensionName("ApiName")
                            .withOperator(ConditionOperator.EQUAL)
                            .withValue("TestApi")))
                        .withSpatialAggregation(new SpatialAggregation().withType(SpatialAggregationType.AVERAGE)
                            .withDimensions(Arrays.asList("Region")))
                        .withTemporalAggregation(new TemporalAggregation().withType(TemporalAggregationType.AVERAGE)
                            .withWindowSizeMinutes(5))))));
    }
}
