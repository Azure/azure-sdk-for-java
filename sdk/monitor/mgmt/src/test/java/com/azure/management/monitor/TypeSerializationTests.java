// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.monitor;

import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.management.monitor.models.AlertRuleResourceInner;
import com.azure.management.monitor.models.LogSearchRuleResourceInner;
import com.azure.management.monitor.models.MetricAlertResourceInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TypeSerializationTests {

    @Test
    public void testDiscriminatorSerialization() throws Exception {
        // Currently the issue is solved by OdataTypeDiscriminatorTypeResolver, without which JsonFlatten would flatten "odata.type".

        SerializerAdapter adapter = new AzureJacksonAdapter();

        MetricAlertResourceInner metricAlertInner = new MetricAlertResourceInner();
        metricAlertInner.withCriteria(new MetricAlertMultipleResourceMultipleMetricCriteria());
        String metricAlertInnerJson = adapter.serialize(metricAlertInner, SerializerEncoding.JSON);
        checkOdatatypeJson(metricAlertInnerJson);

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
