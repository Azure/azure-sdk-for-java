package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings;

public class ConversionOptions {

    private final ConversionInputOptions conversionInputOptions;
    private final ConversionOutputOptions conversionOutputOptions;

    public ConversionOptions(ConversionInputOptions conversionInputOptions, ConversionOutputOptions conversionOutputOptions) {
        this.conversionInputOptions = conversionInputOptions;
        this.conversionOutputOptions = conversionOutputOptions;
    }

    public ConversionInputOptions getConversionInputOptions() {
        return this.conversionInputOptions;
    }

    public ConversionOutputOptions getConversionOutputOptions() {
        return this.conversionOutputOptions;
    }
}
