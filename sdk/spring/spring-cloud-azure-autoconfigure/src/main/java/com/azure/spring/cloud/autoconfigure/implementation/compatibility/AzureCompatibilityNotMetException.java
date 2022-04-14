// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import java.util.Arrays;
import java.util.List;

/**
 * Create SpringCloudAzureCompatibilityNotMetException for SpringCloudAzureCompatibilityNotMetFailureAnalyzer
 */
public class AzureCompatibilityNotMetException extends RuntimeException {
    private final List<VerificationResult> results;

    /**
     * Constructor of {@link AzureCompatibilityNotMetException}
     * @param results VerificationResult List
     */
    public AzureCompatibilityNotMetException(List<VerificationResult> results) {
        super("Spring Cloud Azure/ Spring Boot version compatibility checks have failed: " + Arrays.toString(results.toArray()));
        this.results = results;
    }

    List<VerificationResult> getResults() {
        return results;
    }
}
