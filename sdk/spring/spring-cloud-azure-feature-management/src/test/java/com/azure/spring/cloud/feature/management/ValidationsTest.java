// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

@ExtendWith(SpringExtension.class)
public class ValidationsTest {
    @Mock
    private ApplicationContext context;

    @Mock
    private FeatureManagementConfigProperties configProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationsTest.class);

    private static final ObjectMapper objectMapper = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();

    private static final String testCaseFolderPath = "validations-tests";

    private final String inputsUser = "user";

    private final String inputsGroups = "groups";

    private static final String sampleFileNameFilter = "sample";

    private static final String testsFileNameFilter = "tests";

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

    private static File[] getFileList(String fileNameFilter) {
        final URL folderUrl = Thread.currentThread().getContextClassLoader().getResource(testCaseFolderPath);
        assert folderUrl != null;

        final File folderFile = new File(folderUrl.getFile());
        final File[] filteredFiles = folderFile
            .listFiles(pathname -> pathname.getName().toLowerCase().contains(fileNameFilter));
        assert filteredFiles != null;

        Arrays.sort(filteredFiles, Comparator.comparing(File::getName));
        return filteredFiles;
    }

    private List<ValidationTestCase> readTestcasesFromFile(File testFile) throws IOException {
        final String jsonString = Files.readString(testFile.toPath());
        final CollectionType typeReference = TypeFactory.defaultInstance().constructCollectionType(List.class,
            ValidationTestCase.class);
        return objectMapper.readValue(jsonString, typeReference);
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> readConfigurationFromFile(File sampleFile) throws IOException {
        final String jsonString = Files.readString(sampleFile.toPath());
        final LinkedHashMap<String, Object> configurations = objectMapper.readValue(jsonString, new TypeReference<>() {
        });
        final Object featureManagementSection = configurations.get("feature_management");
        if (featureManagementSection.getClass().isAssignableFrom(LinkedHashMap.class)) {
            return (LinkedHashMap<String, Object>) featureManagementSection;
        }
        throw new IllegalArgumentException("feature_management part is not a map");
    }

    static Stream<Arguments> testProvider() throws IOException {
        List<Arguments> arguments = new ArrayList<>();
        File[] files = getFileList(testsFileNameFilter);
        
        final File[] sampleFiles = getFileList(sampleFileNameFilter);
        List<FeatureManagementProperties> properties = new ArrayList<>();
        for (File sampleFile : sampleFiles) {
            final FeatureManagementProperties managementProperties = new FeatureManagementProperties();
            managementProperties.putAll(readConfigurationFromFile(sampleFile));
            properties.add(managementProperties);
        }
        
        for(int i = 0; i<files.length; i++) {
            if (files[i].getName().contains(("TargetingFilter.tests"))) {
                continue; // TODO(mametcal). Not run the test case until we release the little endian fix
            }
            arguments.add(Arguments.of(files[i].getName(), files[i], properties.get(i)));
        }
        
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testProvider")
    void validationTest(String name, File testsFile, FeatureManagementProperties managementProperties)
        throws IOException {
        LOGGER.debug("Running test case from file: " + name);
        final FeatureManager featureManager = new FeatureManager(context, managementProperties, configProperties);
        List<ValidationTestCase> testCases = readTestcasesFromFile(testsFile);
        for (ValidationTestCase testCase : testCases) {
            LOGGER.debug("Test case : " + testCase.getDescription());
            if (hasException(testCase)) { // TODO(mametcal). Currently we didn't throw the exception when parameter is
                                          // invalid
                assertNull(managementProperties.getOnOff().get(testCase.getFeatureFlagName()));
                continue;
            }
            if (hasInput(testCase)) { // Set inputs
                final Object userObj = testCase.getInputs().get(inputsUser);
                final Object groupsObj = testCase.getInputs().get(inputsGroups);
                final String user = userObj != null ? userObj.toString() : null;
                @SuppressWarnings("unchecked")
                final List<String> groups = groupsObj != null ? (List<String>) groupsObj : null;
                when(context.getBean(Mockito.contains("Targeting")))
                    .thenReturn(new TargetingFilter(new TargetingFilterTestContextAccessor(user, groups)));
            }

            final Boolean result = featureManager.isEnabled(testCase.getFeatureFlagName());
            assertEquals(result.toString(), testCase.getIsEnabled().getResult());
        }
    }
}
