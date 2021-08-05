// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.exception;

import com.azure.core.management.exception.ManagementException;

import java.util.Arrays;
import java.util.Optional;

/**
 * Exception thrown for an invalid response with custom error information.
 */
public final class AggregatedManagementException extends ManagementException {

    /**
     * Creates an instance of AggregatedManagementException.
     *
     * @param aggregatedException the aggregated exception.
     * @param firstManagementException the first ManagementException in suppressed.
     */
    private AggregatedManagementException(RuntimeException aggregatedException,
                                          ManagementException firstManagementException) {
        super(aggregatedException.getMessage(),
            firstManagementException.getResponse(), firstManagementException.getValue());
        for (Throwable exception : aggregatedException.getSuppressed()) {
            this.addSuppressed(exception);
        }
    }

    /**
     * Tries to convert the exception to an AggregatedManagementException.
     * <p>
     * Then conversion will happen when the exception suppresses at least one ManagementException.
     *
     * @param exception the exception.
     * @return an AggregatedManagementException if the exception can be converted to AggregatedManagementException,
     * the same exception if cannot.
     */
    public static Throwable convertToManagementException(Throwable exception) {
        if (exception instanceof ManagementException || !(exception instanceof RuntimeException)) {
            return exception;
        }

        Optional<ManagementException> firstManagementException = Arrays.stream(exception.getSuppressed())
            .filter(e -> e instanceof ManagementException)
            .map(e -> (ManagementException) e)
            .findFirst();
        if (firstManagementException.isPresent()) {
            return new AggregatedManagementException((RuntimeException) exception, firstManagementException.get());
        } else {
            return exception;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        boolean skipException = true;
        // Information on first ManagementException is already contains in supper.toString()
        for (Throwable exception : this.getSuppressed()) {
            if (exception instanceof ManagementException) {
                if (!skipException) {
                    ManagementException managementException = (ManagementException) exception;
                    if (managementException.getValue() != null && managementException.getValue().getMessage() != null) {
                        builder.append("; ").append(managementException.getValue().getMessage());
                    }
                }
                skipException = false;
            }
        }

        return builder.toString();
    }
}
