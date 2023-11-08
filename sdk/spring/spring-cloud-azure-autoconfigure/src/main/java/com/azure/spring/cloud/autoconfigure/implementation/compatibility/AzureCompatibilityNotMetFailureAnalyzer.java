// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.util.List;

/**
 * FailureAnalyzer of Spring Cloud Azure Compatibility
 */
public class AzureCompatibilityNotMetFailureAnalyzer extends AbstractFailureAnalyzer<AzureCompatibilityNotMetException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, AzureCompatibilityNotMetException cause) {
        return new FailureAnalysis(this.getDescription(cause), this.getAction(cause), cause);
    }

    private String getDescription(AzureCompatibilityNotMetException ex) {
        return String.format("Your project setup is incompatible with our requirements due to following reasons:%s", this.descriptions(ex.getResults()));
    }

    private String descriptions(List<VerificationResult> results) {
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        for (VerificationResult result : results) {
            builder.append("- ").append(result.getDescription()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String getAction(AzureCompatibilityNotMetException ex) {
        return String.format("Consider applying the following actions:%s", this.actions(ex.getResults()));
    }

    private String actions(List<VerificationResult> results) {
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        for (VerificationResult result : results) {
            builder.append("- ").append(result.getAction()).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
