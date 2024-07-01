// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.validationstests;

import com.azure.spring.cloud.feature.management.FeatureManager;
import com.azure.spring.cloud.feature.management.filters.TargetingFilter;
import com.azure.spring.cloud.feature.management.filters.TargetingFilterTestContextAccessor;
import com.azure.spring.cloud.feature.management.filters.TimeWindowFilter;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementConfigProperties;
import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.validationstests.models.ValidationTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ValidationsTest {
    @Mock
    private ApplicationContext context;
    @Mock
    private FeatureManagementConfigProperties configProperties;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(configProperties.isFailFast()).thenReturn(true);
        when(context.getBean(Mockito.contains("TimeWindow"))).thenReturn(new TimeWindowFilter());
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    private boolean hasException(ValidationTestCase testCase) {
        final String exceptionStr = testCase.getIsEnabled().getException();
        return exceptionStr != null && !exceptionStr.isEmpty();
    }

    private boolean hasInput(ValidationTestCase testCase) {
        final LinkedHashMap<String, Object> inputsMap = testCase.getInputs();
        return inputsMap != null && !inputsMap.isEmpty();
    }

    private void runTestcases(String sampleFileName, String resultFileName) throws IOException {
        // initialize feature manager
        final FeatureManagementProperties managementProperties = new FeatureManagementProperties();
        managementProperties.putAll(ValidationsTestsUtils.readConfigurationFromFile(sampleFileName));
        final FeatureManager featureManager = new FeatureManager(context, managementProperties, configProperties);

        final List<ValidationTestCase> testCases = ValidationsTestsUtils.readTestcasesFromFile(resultFileName);
        for (ValidationTestCase testCase : testCases) {
            if (hasException(testCase)) {   // todo how to get exception?
                assertNull(managementProperties.getOnOff().get(testCase.getFeatureFlagName()));
            } else {
                if (hasInput(testCase)) { // Set inputs
                    final Object userObj = testCase.getInputs().get(ValidationsTestsConstants.INPUTS_USER);
                    final Object groupsObj = testCase.getInputs().get(ValidationsTestsConstants.INPUTS_GROUPS);
                    final String user = userObj != null ? userObj.toString() : null;
                    final List<String> groups = groupsObj != null ? (List<String>) groupsObj : null;
                    when(context.getBean(Mockito.contains("Targeting"))).thenReturn(new TargetingFilter(new TargetingFilterTestContextAccessor(user, groups)));
                }

                final Boolean result = featureManager.isEnabled(testCase.getFeatureFlagName());
                assertEquals(result.toString(), testCase.getIsEnabled().getResult());
            }
        }
    }

    @Test
    void validationsTest() throws IOException {
        runTestcases(ValidationsTestsConstants.NO_FILTERS_SAMPLE_PATH, ValidationsTestsConstants.NO_FILTERS_TESTS_PATH);
        runTestcases(ValidationsTestsConstants.REQUIREMENT_TYPE_SAMPLE_PATH, ValidationsTestsConstants.REQUIREMENT_TYPE_TESTS_PATH);
    }

}
