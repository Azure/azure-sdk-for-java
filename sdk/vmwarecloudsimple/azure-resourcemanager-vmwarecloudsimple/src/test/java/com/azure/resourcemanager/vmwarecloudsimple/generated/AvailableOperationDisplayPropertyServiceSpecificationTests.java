// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.vmwarecloudsimple.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.vmwarecloudsimple.fluent.models.AvailableOperationDisplayPropertyServiceSpecification;
import com.azure.resourcemanager.vmwarecloudsimple.models.AggregationType;
import com.azure.resourcemanager.vmwarecloudsimple.models.AvailableOperationDisplayPropertyServiceSpecificationMetricsItem;
import com.azure.resourcemanager.vmwarecloudsimple.models.AvailableOperationDisplayPropertyServiceSpecificationMetricsList;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class AvailableOperationDisplayPropertyServiceSpecificationTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AvailableOperationDisplayPropertyServiceSpecification model =
            BinaryData
                .fromString(
                    "{\"serviceSpecification\":{\"metricSpecifications\":[{\"aggregationType\":\"Average\",\"displayDescription\":\"s\",\"displayName\":\"git\",\"name\":\"xqhabi\",\"unit\":\"pikxwczbyscnpqxu\"},{\"aggregationType\":\"Total\",\"displayDescription\":\"vyq\",\"displayName\":\"iwbybrkxvdumjg\",\"name\":\"tfwvukxgaudc\",\"unit\":\"snhsjcnyejhkryh\"}]}}")
                .toObject(AvailableOperationDisplayPropertyServiceSpecification.class);
        Assertions
            .assertEquals(
                AggregationType.AVERAGE, model.serviceSpecification().metricSpecifications().get(0).aggregationType());
        Assertions.assertEquals("s", model.serviceSpecification().metricSpecifications().get(0).displayDescription());
        Assertions.assertEquals("git", model.serviceSpecification().metricSpecifications().get(0).displayName());
        Assertions.assertEquals("xqhabi", model.serviceSpecification().metricSpecifications().get(0).name());
        Assertions.assertEquals("pikxwczbyscnpqxu", model.serviceSpecification().metricSpecifications().get(0).unit());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AvailableOperationDisplayPropertyServiceSpecification model =
            new AvailableOperationDisplayPropertyServiceSpecification()
                .withServiceSpecification(
                    new AvailableOperationDisplayPropertyServiceSpecificationMetricsList()
                        .withMetricSpecifications(
                            Arrays
                                .asList(
                                    new AvailableOperationDisplayPropertyServiceSpecificationMetricsItem()
                                        .withAggregationType(AggregationType.AVERAGE)
                                        .withDisplayDescription("s")
                                        .withDisplayName("git")
                                        .withName("xqhabi")
                                        .withUnit("pikxwczbyscnpqxu"),
                                    new AvailableOperationDisplayPropertyServiceSpecificationMetricsItem()
                                        .withAggregationType(AggregationType.TOTAL)
                                        .withDisplayDescription("vyq")
                                        .withDisplayName("iwbybrkxvdumjg")
                                        .withName("tfwvukxgaudc")
                                        .withUnit("snhsjcnyejhkryh"))));
        model = BinaryData.fromObject(model).toObject(AvailableOperationDisplayPropertyServiceSpecification.class);
        Assertions
            .assertEquals(
                AggregationType.AVERAGE, model.serviceSpecification().metricSpecifications().get(0).aggregationType());
        Assertions.assertEquals("s", model.serviceSpecification().metricSpecifications().get(0).displayDescription());
        Assertions.assertEquals("git", model.serviceSpecification().metricSpecifications().get(0).displayName());
        Assertions.assertEquals("xqhabi", model.serviceSpecification().metricSpecifications().get(0).name());
        Assertions.assertEquals("pikxwczbyscnpqxu", model.serviceSpecification().metricSpecifications().get(0).unit());
    }
}
