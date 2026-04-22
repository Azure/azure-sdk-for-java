// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures every @JsonProperty field in TenantWorkloadConfig has a corresponding
 * case in the applyField() switch statement, so that tenantDefaults inheritance works.
 *
 * Uses reflection to invoke the private applyField method for each property name
 * and verifies the corresponding field was set, avoiding brittle source file parsing.
 */
public class TenantWorkloadConfigApplyFieldTest {

    // Fields that are intentionally excluded from applyField
    private static final Set<String> EXCLUDED_FIELDS = new HashSet<>(Arrays.asList(
        "id" // tenant ID should not be inherited from defaults
    ));

    @Test(groups = {"unit"})
    public void allJsonPropertiesShouldHaveApplyFieldCase() throws Exception {
        // Collect all @JsonProperty names and their corresponding declared fields
        Set<String> jsonPropertyNames = new HashSet<>();
        for (Field field : TenantWorkloadConfig.class.getDeclaredFields()) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (annotation != null) {
                jsonPropertyNames.add(annotation.value());
            }
        }

        // Get the private applyField method via reflection
        Method applyField = TenantWorkloadConfig.class.getDeclaredMethod(
            "applyField", String.class, String.class, boolean.class);
        applyField.setAccessible(true);

        // For each @JsonProperty, invoke applyField and verify the field was handled
        Set<String> missingCases = new HashSet<>();
        for (String propName : jsonPropertyNames) {
            if (EXCLUDED_FIELDS.contains(propName)) {
                continue;
            }

            TenantWorkloadConfig config = new TenantWorkloadConfig();
            // Use "42" as the sentinel: it's valid for String, Integer (42), and
            // Boolean (false) fields. Note that applyField() catches parse exceptions
            // internally and does not rethrow, so we cannot rely on catching exceptions
            // here to detect Integer/Boolean case existence.
            applyField.invoke(config, propName, "42", true);

            // Check if ANY field was modified from its default (null) state.
            // If applyField silently ignored the key (no matching case), no field changes.
            boolean fieldWasSet = false;
            for (Field field : TenantWorkloadConfig.class.getDeclaredFields()) {
                JsonProperty annotation = field.getAnnotation(JsonProperty.class);
                if (annotation != null && annotation.value().equals(propName)) {
                    field.setAccessible(true);
                    Object value = field.get(config);
                    if (value != null) {
                        fieldWasSet = true;
                    }
                    break;
                }
            }

            if (!fieldWasSet) {
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
