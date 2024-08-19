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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
    private final String testCaseFolderPath = "validations-tests";
    private final String inputsUser = "user";
    private final String inputsGroups = "groups";
    private final String sampleFileNameFilter = "sample";
    private final String testsFileNameFilter = "tests";

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

    private File[] getFileList(String fileNameFilter) {
        final URL folderUrl = Thread.currentThread().getContextClassLoader().getResource(testCaseFolderPath);
        assert folderUrl != null;
        final File folderFile = new File(folderUrl.getFile());
        return folderFile.listFiles(pathname -> pathname.getName().toLowerCase().contains(fileNameFilter));
    }

    private List<ValidationTestCase> readTestcasesFromFile(File testFile) throws IOException {
        final String jsonString = Files.readString(testFile.toPath());
        final CollectionType typeReference =
            TypeFactory.defaultInstance().constructCollectionType(List.class, ValidationTestCase.class);
        return objectMapper.readValue(jsonString, typeReference);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> readConfigurationFromFile(File sampleFile) throws IOException {
        final String jsonString = Files.readString(sampleFile.toPath());
        final LinkedHashMap<String, Object> configurations = objectMapper.readValue(jsonString, new TypeReference<>() {
        });
        final Object featureManagementSection = configurations.get("feature_management");
        if (featureManagementSection.getClass().isAssignableFrom(LinkedHashMap.class)) {
            return (LinkedHashMap<String, Object>) featureManagementSection;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private void runTestcases(File sampleFile, File testsFile) throws IOException {
        // initialize feature manager
        final FeatureManagementProperties managementProperties = new FeatureManagementProperties();
        managementProperties.putAll(readConfigurationFromFile(sampleFile));
        final FeatureManager featureManager = new FeatureManager(context, managementProperties, configProperties);

        final List<ValidationTestCase> testCases = readTestcasesFromFile(testsFile);
        for (ValidationTestCase testCase : testCases) {
            System.out.println("Running test case " + testCase.getFriendlyName());
            if (hasException(testCase)) {   // TODO. Currently we didn't throw the exception when parameter is invalid
                assertNull(managementProperties.getOnOff().get(testCase.getFeatureFlagName()));
            } else {
                if (hasInput(testCase)) { // Set inputs
                    final Object userObj = testCase.getInputs().get(inputsUser);
                    final Object groupsObj = testCase.getInputs().get(inputsGroups);
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
        final File[] sampleFiles = getFileList(sampleFileNameFilter);
        final File[] testsFiles = getFileList(testsFileNameFilter);
        if (sampleFiles.length != testsFiles.length) {
            throw new IllegalArgumentException("The sample files and tests files should have same count.");
        }
        for (int i = 0; i < sampleFiles.length; i++) {
            System.out.println("sampleFiles[i].getName(): " + sampleFiles[i].getName());
            if (sampleFiles[i].getName().contains("TargetingFilter.sample")) { // TODO. Not run the test case until we release the little endian fix
                continue;
            }
            runTestcases(sampleFiles[i], testsFiles[i]);
        }
    }

}
