// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryMetricsUtilsTest {

    @Test(groups = "unit", dataProvider = "malformedDelimitedStrings")
    public void parseDelimitedString_withInvalidAttributeLength_throwsIllegalArgumentException(
            String delimitedString) {
        assertThatThrownBy(() -> QueryMetricsUtils.parseDelimitedString(delimitedString))
                .isInstanceOf(IllegalArgumentException.class)
                .isNotInstanceOf(NullPointerException.class);
    }

    @DataProvider(name = "malformedDelimitedStrings")
    public Object[][] malformedDelimitedStrings() {
        return new Object[][] {
                { "=" },
                { "metric" },
                { "metric=1=2" }
        };
    }

    @Test(groups = "unit", dataProvider = "validDelimitedStrings")
    public void parseDelimitedString_withPreservedInput_returnsExpectedMetrics(
            String delimitedString, HashMap<String, Double> expectedMetrics) {
        assertThat(QueryMetricsUtils.parseDelimitedString(delimitedString)).isEqualTo(expectedMetrics);
    }

    @DataProvider(name = "validDelimitedStrings")
    public Object[][] validDelimitedStrings() {
        HashMap<String, Double> singleMetric = new HashMap<>();
        singleMetric.put("metric", 1.5);

        HashMap<String, Double> multipleMetrics = new HashMap<>();
        multipleMetrics.put("metric", 1.5);
        multipleMetrics.put("other", 2.0);

        return new Object[][] {
                { "metric=1.5", singleMetric },
                { "metric=1.5;other=2", multipleMetrics },
                { "", new HashMap<String, Double>() }
        };
    }

    @Test(groups = "unit", dataProvider = "preservedExceptionDelimitedStrings")
    public void parseDelimitedString_withPreservedExceptionInput_throwsExpectedException(
            String delimitedString, Class<? extends Throwable> expectedException) {
        assertThatThrownBy(() -> QueryMetricsUtils.parseDelimitedString(delimitedString))
                .isExactlyInstanceOf(expectedException);
    }

    @DataProvider(name = "preservedExceptionDelimitedStrings")
    public Object[][] preservedExceptionDelimitedStrings() {
        return new Object[][] {
                { null, NullPointerException.class },
                { "metric=not-a-number", NumberFormatException.class }
        };
    }
}
