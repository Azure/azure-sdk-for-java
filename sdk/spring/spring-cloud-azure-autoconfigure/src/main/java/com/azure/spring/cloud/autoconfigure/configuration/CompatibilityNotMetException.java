package com.azure.spring.cloud.autoconfigure.configuration;

import java.util.Arrays;
import java.util.List;

public class CompatibilityNotMetException extends RuntimeException{
    final List<VerificationResult> results;

    public CompatibilityNotMetException(List<VerificationResult> results) {
        super("Spring Cloud Azure/ Spring Boot version compatibility checks have failed: " + Arrays.toString(results.toArray()));
        this.results = results;
    }
}
