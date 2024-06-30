// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.validationstests;

import com.azure.spring.cloud.feature.management.validationstests.models.ValidationTestCase;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ValidationsTestsUtils {
    public static List<ValidationTestCase> readTestcasesFromFile(String filePath) throws IOException {
        final File file = new File(filePath);
        final String jsonString = Files.readString(file.toPath());
        final ObjectMapper om = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
        final CollectionType typeReference =
            TypeFactory.defaultInstance().constructCollectionType(List.class, ValidationTestCase.class);
        return om.readValue(jsonString, typeReference);
    }
}
