// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.validationstests;

import com.azure.spring.cloud.feature.management.validationstests.models.ValidationTestCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;

public class ValidationsTestsUtils {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
    private static final String TEST_RESOURCES_FOLDER = "src/test/resources/validations-tests";

    public static List<ValidationTestCase> readTestcasesFromFile(String fileName) throws IOException {
        final File file = new File(TEST_RESOURCES_FOLDER + "/" + fileName);
        final String jsonString = Files.readString(file.toPath());
        final CollectionType typeReference =
            TypeFactory.defaultInstance().constructCollectionType(List.class, ValidationTestCase.class);
        return OBJECT_MAPPER.readValue(jsonString, typeReference);
    }

    public static LinkedHashMap<String, Object> readConfigurationFromFile(String fileName) throws IOException {
        final File file = new File(TEST_RESOURCES_FOLDER + "/" + fileName);
        final String jsonString = Files.readString(file.toPath());
        final LinkedHashMap<String, Object> configurations = OBJECT_MAPPER.readValue(jsonString, new TypeReference<>() {
        });
        final Object featureManagementSection = configurations.get("feature_management");
        if (featureManagementSection.getClass().isAssignableFrom(LinkedHashMap.class)) {
            return (LinkedHashMap<String, Object>) featureManagementSection;
        }
        return new LinkedHashMap<>();
    }
}
