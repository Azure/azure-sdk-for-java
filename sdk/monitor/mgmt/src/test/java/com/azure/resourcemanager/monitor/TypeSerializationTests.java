// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.monitor.fluent.inner.AlertRuleResourceInner;
import com.azure.resourcemanager.monitor.fluent.inner.LogSearchRuleResourceInner;
import com.azure.resourcemanager.monitor.fluent.inner.MetricAlertResourceInner;
import java.util.Arrays;

import com.azure.resourcemanager.monitor.models.AlertingAction;
import com.azure.resourcemanager.monitor.models.MetricAlertMultipleResourceMultipleMetricCriteria;
import com.azure.resourcemanager.monitor.models.RuleEmailAction;
import com.azure.resourcemanager.monitor.models.RuleMetricDataSource;
import com.azure.resourcemanager.monitor.models.ThresholdRuleCondition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypeSerializationTests {

    @Test
    public void testDiscriminatorSerialization() throws Exception {
        // Currently solved by @JsonFlatten annotation and escape on "odata.type" (as "odata\\.type")

        SerializerAdapter adapter = new AzureJacksonAdapter();

        MetricAlertResourceInner metricAlertInner = new MetricAlertResourceInner();
        metricAlertInner.withCriteria(new MetricAlertMultipleResourceMultipleMetricCriteria());
        String metricAlertInnerJson = adapter.serialize(metricAlertInner, SerializerEncoding.JSON);
        checkOdatatypeJson(metricAlertInnerJson);

        MetricAlertResourceInner metricAlertInner2 = adapter.deserialize(metricAlertInnerJson, MetricAlertResourceInner.class, SerializerEncoding.JSON);
        Assertions.assertTrue(metricAlertInner2.criteria() instanceof MetricAlertMultipleResourceMultipleMetricCriteria);

        AlertRuleResourceInner alertRuleInner = new AlertRuleResourceInner();
        alertRuleInner.withActions(Arrays.asList(new RuleEmailAction()));
        alertRuleInner.withCondition(new ThresholdRuleCondition().withDataSource(new RuleMetricDataSource()));
        String alertRuleInnerJson = adapter.serialize(alertRuleInner, SerializerEncoding.JSON);
        checkOdatatypeJson(alertRuleInnerJson);

        LogSearchRuleResourceInner logSearchRuleInner = new LogSearchRuleResourceInner();
        logSearchRuleInner.withAction(new AlertingAction());
        String logSearchRuleInnerJson = adapter.serialize(logSearchRuleInner, SerializerEncoding.JSON);
        checkOdatatypeJson(logSearchRuleInnerJson);
    }

    private void checkOdatatypeJson(String json) {
        final String odataTypeDiscriminatorSignature = "\"odata.type\":";
        final String incorrectOdataTypeDiscriminatorSignature = "\"odata\":";
        Assertions.assertTrue(json.contains(odataTypeDiscriminatorSignature));
        Assertions.assertFalse(json.contains(incorrectOdataTypeDiscriminatorSignature));
    }
}
