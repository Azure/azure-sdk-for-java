package com.azure.spring.cloud.autoconfigure.configuration;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.util.Iterator;
import java.util.List;

public class CompatibilityNotMetFailureAnalyzer extends AbstractFailureAnalyzer<CompatibilityNotMetException> {

    public CompatibilityNotMetFailureAnalyzer() {
    }

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, CompatibilityNotMetException cause) {
        return new FailureAnalysis(this.getDescription(cause), this.getAction(cause), cause);
    }

    private String getDescription(CompatibilityNotMetException ex) {
        return String.format("Your project setup is incompatible with our requirements due to following reasons:%s", this.descriptions(ex.results));
    }

    private String descriptions(List<VerificationResult> results) {
        StringBuilder builder = new StringBuilder("\n\n");
        Iterator var3 = results.iterator();

        while(var3.hasNext()) {
            VerificationResult result = (VerificationResult)var3.next();
            builder.append("- ").append(result.description).append("\n");
        }

        return builder.toString();
    }

    private String getAction(CompatibilityNotMetException ex) {
        return String.format("Consider applying the following actions:%s", this.actions(ex.results));
    }

    private String actions(List<VerificationResult> results) {
        StringBuilder builder = new StringBuilder("\n\n");
        Iterator var3 = results.iterator();

        while(var3.hasNext()) {
            VerificationResult result = (VerificationResult)var3.next();
            builder.append("- ").append(result.action).append("\n");
        }

        return builder.toString();
    }
}
