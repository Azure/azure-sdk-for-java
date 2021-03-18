package com.azure.iot.modelsrepository.implementation;

public class StandardStrings {
    public static final String GenericGetModelsError = "Failure handling \"%s\".";
    public static final String InvalidDtmiFormat = "Invalid DTMI format \"%s\".";
    public static final String ProcessingDtmi = "Processing DTMI \"%s\". ";
    public static final String DiscoveredDependencies = "Discovered dependencies \"%s\".";
    public static final String FetchingModelContent = "Attempting to fetch model content from \"%s\".";
    public static final String ErrorFetchingModelContent = "Model file \"%s\" not found or not accessible in target repository.";
    public static final String IncorrectDtmiCasing = "Fetched model has incorrect DTMI casing. Expected \"%s\", parsed \"%s\".";
}
