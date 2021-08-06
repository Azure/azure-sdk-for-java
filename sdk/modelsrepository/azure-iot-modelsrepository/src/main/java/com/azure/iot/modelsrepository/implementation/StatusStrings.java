// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

public class StatusStrings {
    public static final String GENERIC_GET_MODELS_ERROR = "Failure handling \"%s\".";
    public static final String INVALID_DTMI_FORMAT_S = "Invalid DTMI format \"%s\".";
    public static final String PROCESSING_DTMIS = "Processing DTMI \"%s\". ";
    public static final String DISCOVERED_DEPENDENCIES = "Discovered dependencies \"%s\".";
    public static final String FETCHING_MODEL_CONTENT = "Attempting to fetch model content from \"{}\".";
    public static final String ERROR_FETCHING_MODEL_CONTENT = "Model file \"%s\" not found or not accessible in target repository.";
    public static final String INCORRECT_DTMI_CASING = "Fetched model has incorrect DTMI casing. Expected \"%s\", parsed \"%s\".";
}
