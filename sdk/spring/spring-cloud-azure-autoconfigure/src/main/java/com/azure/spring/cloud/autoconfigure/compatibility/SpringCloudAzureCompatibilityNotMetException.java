// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import java.util.Arrays;
import java.util.List;

/**
 * Create CompatibilityNotMetException for CompatibilityNotMetFailureAnalyzer
 */
public class SpringCloudAzureCompatibilityNotMetException extends RuntimeException {
    final List<VerificationResult> results;

    /**
     * Constructor of CompatibilityNotMetException
     * @param results VerificationResult List
     */
    public SpringCloudAzureCompatibilityNotMetException(List<VerificationResult> results) {
        super("Spring Cloud Azure/ Spring Boot version compatibility checks have failed: " + Arrays.toString(results.toArray()));
        this.results = results;
    }
}
