// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import java.util.List;

/**
 * FailureAnalyzer of Spring Cloud Azure Compatibility
 */
public class SpringCloudAzureCompatibilityNotMetFailureAnalyzer extends AbstractFailureAnalyzer<SpringCloudAzureCompatibilityNotMetException> {

    /**
     * Constructor of {@link SpringCloudAzureCompatibilityNotMetFailureAnalyzer}
     */
    public SpringCloudAzureCompatibilityNotMetFailureAnalyzer() {
    }

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, SpringCloudAzureCompatibilityNotMetException cause) {
        return new FailureAnalysis(this.getDescription(cause), this.getAction(cause), cause);
    }

    private String getDescription(SpringCloudAzureCompatibilityNotMetException ex) {
        return String.format("Your project setup is incompatible with our requirements due to following reasons:%s", this.descriptions(ex.results));
    }

    private String descriptions(List<VerificationResult> results) {
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        for (VerificationResult result : results) {
            builder.append("- ").append(result.description).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String getAction(SpringCloudAzureCompatibilityNotMetException ex) {
        return String.format("Consider applying the following actions:%s", this.actions(ex.results));
    }

    private String actions(List<VerificationResult> results) {
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        for (VerificationResult result : results) {
            builder.append("- ").append(result.action).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
