// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.core.test.TestMode;

import java.util.function.Function;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

final class CrossResourceCopyTestConfiguration {
    private static final String SANITIZED_ENDPOINT = "https://REDACTED.services.ai.azure.com";
    private static final String SANITIZED_RESOURCE_ID
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/REDACTED"
            + "/providers/Microsoft.CognitiveServices/accounts/REDACTED";

    private final String sourceResourceId;
    private final String sourceRegion;
    private final String targetEndpoint;
    private final String targetKey;
    private final String targetResourceId;
    private final String targetRegion;

    private CrossResourceCopyTestConfiguration(String sourceResourceId, String sourceRegion, String targetEndpoint,
        String targetKey, String targetResourceId, String targetRegion) {
        this.sourceResourceId = sourceResourceId;
        this.sourceRegion = sourceRegion;
        this.targetEndpoint = normalizeEndpoint(targetEndpoint);
        this.targetKey = targetKey;
        this.targetResourceId = targetResourceId;
        this.targetRegion = targetRegion;
    }

    static void assumeLiveEnvironmentIsConfigured(Function<String, String> environment) {
        assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_ENDPOINT");
        assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID");
        assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_SOURCE_REGION");
        assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_TARGET_ENDPOINT");
        assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_TARGET_RESOURCE_ID");
        assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_TARGET_REGION");
    }

    static CrossResourceCopyTestConfiguration load(TestMode testMode, Function<String, String> environment) {
        if (testMode == TestMode.PLAYBACK) {
            return new CrossResourceCopyTestConfiguration(SANITIZED_RESOURCE_ID, "REDACTED", SANITIZED_ENDPOINT, null,
                SANITIZED_RESOURCE_ID, "REDACTED");
        }

        return new CrossResourceCopyTestConfiguration(
            assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID"),
            assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_SOURCE_REGION"),
            assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_TARGET_ENDPOINT"),
            environment.apply("CONTENTUNDERSTANDING_TARGET_KEY"),
            assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_TARGET_RESOURCE_ID"),
            assumeEnvironmentVariable(environment, "CONTENTUNDERSTANDING_TARGET_REGION"));
    }

    String getSourceResourceId() {
        return sourceResourceId;
    }

    String getSourceRegion() {
        return sourceRegion;
    }

    String getTargetEndpoint() {
        return targetEndpoint;
    }

    String getTargetKey() {
        return targetKey;
    }

    String getTargetResourceId() {
        return targetResourceId;
    }

    String getTargetRegion() {
        return targetRegion;
    }

    private static String assumeEnvironmentVariable(Function<String, String> environment, String name) {
        String value = environment.apply(name);
        assumeTrue(value != null && !value.trim().isEmpty(), "Skipping cross-resource copy: " + name + " is not set");
        return value;
    }

    private static String normalizeEndpoint(String endpoint) {
        int end = endpoint.length();
        while (end > 0 && endpoint.charAt(end - 1) == '/') {
            end--;
        }
        return endpoint.substring(0, end);
    }
}
