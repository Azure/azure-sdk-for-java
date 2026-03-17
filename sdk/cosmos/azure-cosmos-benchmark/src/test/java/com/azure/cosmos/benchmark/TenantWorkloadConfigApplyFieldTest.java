// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures every @JsonProperty field in TenantWorkloadConfig has a corresponding
 * case in the applyField() switch statement, so that tenantDefaults inheritance works.
 */
public class TenantWorkloadConfigApplyFieldTest {

    // Fields that are intentionally excluded from applyField
    private static final Set<String> EXCLUDED_FIELDS = new HashSet<>(Arrays.asList(
        "id" // tenant ID should not be inherited from defaults
    ));

    @Test(groups = {"unit"})
    public void allJsonPropertiesShouldHaveApplyFieldCase() throws IOException {
        // Collect all @JsonProperty names from the class
        Set<String> jsonPropertyNames = new HashSet<>();
        for (Field field : TenantWorkloadConfig.class.getDeclaredFields()) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (annotation != null) {
                jsonPropertyNames.add(annotation.value());
            }
        }

        // Parse the source file to find all case "..." entries in applyField
        String basedir = System.getProperty("basedir", System.getProperty("user.dir"));
        Path sourceFile = Paths.get(basedir, "src/main/java/com/azure/cosmos/benchmark/TenantWorkloadConfig.java");
        String source = new String(Files.readAllBytes(sourceFile), StandardCharsets.UTF_8);
        // Extract the applyField method body to avoid matching case statements from other switches
        int applyFieldStart = source.indexOf("void applyField(");
        if (applyFieldStart < 0) {
            applyFieldStart = source.indexOf("applyField(String");
        }
        String applyFieldSource = applyFieldStart >= 0 ? source.substring(applyFieldStart) : source;

        Set<String> caseNames = new HashSet<>();
        Matcher matcher = Pattern.compile("case\\s+\"([^\"]+)\"").matcher(applyFieldSource);
        while (matcher.find()) {
            caseNames.add(matcher.group(1));
        }

        // Every @JsonProperty (except excluded) should have a case in applyField
        Set<String> missingCases = new HashSet<>();
        for (String propName : jsonPropertyNames) {
            if (!EXCLUDED_FIELDS.contains(propName) && !caseNames.contains(propName)) {
                missingCases.add(propName);
            }
        }

        assertThat(missingCases)
            .as("@JsonProperty fields missing from applyField() switch — "
                + "these fields won't be inherited from tenantDefaults. "
                + "Add a case in applyField() for each, or add to EXCLUDED_FIELDS if intentional.")
            .isEmpty();
    }
}
