// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationError;

import java.util.ArrayList;
import java.util.List;

public class ErrorTracker {
    /**
     * This list represents the errors that are found when the response from a ping or post is initially parsed.
     * The errors in this list are expected to stay the same across multiple post requests of the same configuration
     * id, and so will only be changed when a new configuration gets parsed.
     */
    private List<CollectionConfigurationError> validationTimeErrors;

    /**
     * This list represents errors that can't be caught while parsing the response - such as validation errors that would occur when
     * analyzing customDimensions present in incoming spans/logs, or when creating a projection. These errors aren't expected to be the
     * same across post requests of the same configuration id and so is expected to be regenerated for every post request.
     */
    private List<CollectionConfigurationError> runTimeErrors;

    public ErrorTracker() {
        validationTimeErrors = new ArrayList<>();
        runTimeErrors = new ArrayList<>();
    }

    public synchronized void addValidationError(CollectionConfigurationError error) {
        this.validationTimeErrors.add(error);
    }

    public synchronized void addRunTimeError(CollectionConfigurationError error) {
        this.runTimeErrors.add(error);
    }

    public synchronized List<CollectionConfigurationError> getErrors() {
        List<CollectionConfigurationError> result =  new ArrayList<>(validationTimeErrors);
        result.addAll(runTimeErrors);
        return result;
    }

    public synchronized void clearRunTimeErrors() {
        runTimeErrors.clear();
    }

    public synchronized void clearValidationTimeErrors() {
        validationTimeErrors.clear();
    }

}
