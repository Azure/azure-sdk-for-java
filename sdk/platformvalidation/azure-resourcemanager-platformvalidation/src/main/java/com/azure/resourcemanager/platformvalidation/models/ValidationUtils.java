// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation.models;

final class ValidationUtils {
    private ValidationUtils() {
    }

    static void requireNonBlank(String value, String path) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(path + " is required.");
        }
    }
}
