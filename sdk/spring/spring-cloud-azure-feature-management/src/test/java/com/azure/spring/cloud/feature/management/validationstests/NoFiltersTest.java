// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.validationstests;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.validationstests.models.ValidationTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = FeatureManagementProperties.class)
@TestPropertySource(value = { ValidationsTestsConstants.NO_FILTERS_SAMPLE_PATH }, factory= JsonPropertySourceFactory.class)
public class NoFiltersTest {
    @Autowired
    private FeatureManagementProperties properties;

    @Test
    void noFiltersTest() throws IOException {
        final List<ValidationTestCase> testCases = ValidationsTestsUtils.readTestcasesFromFile(ValidationsTestsConstants.NO_FILTERS_TESTS_PATH);
        for (ValidationTestCase testCase : testCases) {
            final String exceptionStr = testCase.getIsEnabled().getException();
            if (exceptionStr == null || exceptionStr.isEmpty()) {
                final Boolean result = properties.getOnOff().get(testCase.getFeatureFlagName());
                assertEquals(result.toString(), testCase.getIsEnabled().getResult());
            } else {    // todo how to get exception?
                assertNull(properties.getOnOff().get(testCase.getFeatureFlagName()));
            }
        }
    }

}
