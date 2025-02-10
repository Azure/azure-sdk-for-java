// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.monitor.fluent.models.ScheduledQueryRuleResourceInner;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.monitor.models.Actions;
import com.azure.resourcemanager.monitor.models.AlertSeverity;
import com.azure.resourcemanager.monitor.models.Condition;
import com.azure.resourcemanager.monitor.models.ConditionFailingPeriods;
import com.azure.resourcemanager.monitor.models.ConditionOperator;
import com.azure.resourcemanager.monitor.models.Dimension;
import com.azure.resourcemanager.monitor.models.DimensionOperator;
import com.azure.resourcemanager.monitor.models.ScheduledQueryRuleCriteria;
import com.azure.resourcemanager.monitor.models.TimeAggregation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

public class ScheduledQueryTests extends MonitorManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("jMonitor_", 18);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void createScheduledQuery() {
        Region region = Region.AUSTRALIA_SOUTHEAST;
        String actionGroupName = generateRandomResourceName("ag", 15);
        ActionGroup ag = monitorManager.actionGroups()
            .define(actionGroupName)
            .withNewResourceGroup(rgName, region)
            .defineReceiver("first")
            .withPushNotification("azurepush@outlook.com")
            .withEmail("justemail@outlook.com")
            .withSms("1", "4255655665")
            .withVoice("1", "2062066050")
            .withWebhook("https://www.rate.am")
            .attach()
            .defineReceiver("second")
            .withEmail("secondemail@outlook.com")
            .withWebhook("https://www.spyur.am")
            .attach()
            .create();
        ScheduledQueryRuleResourceInner resourceInner = monitorManager.serviceClient()
            .getScheduledQueryRules()
            .createOrUpdateWithResponse(rgName, generateRandomResourceName("perf", 15),
                new ScheduledQueryRuleResourceInner().withLocation(region.name())
                    .withDescription("Performance rule")
                    .withSeverity(AlertSeverity.FOUR)
                    .withEnabled(true)
                    .withScopes(Arrays.asList("/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57"))
                    .withEvaluationFrequency(Duration.parse("PT5M"))
                    .withWindowSize(Duration.parse("PT10M"))
                    .withTargetResourceTypes(Arrays.asList("Microsoft.Compute/virtualMachines"))
                    .withCriteria(new ScheduledQueryRuleCriteria()
                        .withAllOf(Arrays.asList(new Condition().withQuery("Perf | where ObjectName == \"Processor\"")
                            .withTimeAggregation(TimeAggregation.AVERAGE)
                            .withMetricMeasureColumn("% Processor Time")
                            .withResourceIdColumn("resourceId")
                            .withDimensions(Arrays.asList(
                                new Dimension().withName("ComputerIp")
                                    .withOperator(DimensionOperator.EXCLUDE)
                                    .withValues(Arrays.asList("192.168.1.1")),
                                new Dimension().withName("OSType")
                                    .withOperator(DimensionOperator.INCLUDE)
                                    .withValues(Arrays.asList("*"))))
                            .withOperator(ConditionOperator.GREATER_THAN)
                            .withThreshold(70.0D)
                            .withFailingPeriods(new ConditionFailingPeriods().withNumberOfEvaluationPeriods(1L)
                                .withMinFailingPeriodsToAlert(1L)))))
                    .withMuteActionsDuration(Duration.parse("PT30M"))
                    .withActions(new Actions().withActionGroups(Arrays.asList(ag.id())))
                    .withCheckWorkspaceAlertsStorageConfigured(false)
                    .withSkipQueryValidation(true)
                    .withAutoMitigate(false),
                com.azure.core.util.Context.NONE)
            .getValue();
        resourceInner
            = monitorManager.serviceClient().getScheduledQueryRules().getByResourceGroup(rgName, resourceInner.name());
        Assertions.assertEquals(AlertSeverity.FOUR, resourceInner.severity());
    }
}
