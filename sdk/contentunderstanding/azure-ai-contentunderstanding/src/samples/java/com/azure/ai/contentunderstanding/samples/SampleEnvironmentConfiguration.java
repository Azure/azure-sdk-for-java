// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

final class SampleEnvironmentConfiguration {
    private SampleEnvironmentConfiguration() {
    }

    static String requireEnvironmentValue(String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required environment variable is not configured: " + name + ".");
        }
        return value.trim();
    }
}