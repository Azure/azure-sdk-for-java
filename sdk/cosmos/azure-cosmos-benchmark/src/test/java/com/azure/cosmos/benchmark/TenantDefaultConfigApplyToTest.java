// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures every @JsonProperty field in TenantDefaultConfig has a corresponding
 * assignment in applyTo(), so that tenantDefaults inheritance works for all shared fields.
 *
 * Uses reflection to verify that after applyTo() is called with non-null defaults,
 * all shared fields on the tenant config are populated.
 */
public class TenantDefaultConfigApplyToTest {

    @Test(groups = {"unit"})
    public void applyToShouldCoverAllSharedFields() throws Exception {
        // Create a defaults config with all fields set to non-null sentinel values
        TenantDefaultConfig defaults = new TenantDefaultConfig();
        for (Field field : TenantDefaultConfig.class.getDeclaredFields()) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (annotation == null) continue;
            field.setAccessible(true);
            Class<?> type = field.getType();
            if (type == String.class) {
                field.set(defaults, "test-value");
            } else if (type == Integer.class) {
                field.set(defaults, 42);
            } else if (type == Boolean.class) {
                field.set(defaults, true);
            }
        }

        // Create an empty tenant config and apply defaults
        TenantWorkloadConfig tenant = new TenantWorkloadConfig();
        defaults.applyTo(tenant);

        // Verify all shared fields were applied (should not be null)
        Set<String> missingFields = new HashSet<>();
        for (Field field : TenantDefaultConfig.class.getDeclaredFields()) {
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (annotation == null) continue;
            field.setAccessible(true);
            Object value = field.get(tenant);
            if (value == null) {
                missingFields.add(annotation.value());
            }
        }

        assertThat(missingFields)
            .as("TenantDefaultConfig fields not applied by applyTo() — "
                + "these fields won't be inherited from tenantDefaults. "
                + "Add an assignment in applyTo() for each.")
            .isEmpty();
    }
}
