package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings;

import java.time.Duration;

public class ConversionOptions {

    private final ConversionInputOptions conversionInputOptions;
    private final ConversionOutputOptions conversionOutputOptions;
    private Duration pollInterval;

    public ConversionOptions(ConversionInputOptions conversionInputOptions, ConversionOutputOptions conversionOutputOptions) {
        this.conversionInputOptions = conversionInputOptions;
        this.conversionOutputOptions = conversionOutputOptions;
        this.pollInterval = Duration.ofSeconds(10);
    }

    public ConversionInputOptions getConversionInputOptions() {
        return this.conversionInputOptions;
    }

    public ConversionOutputOptions getConversionOutputOptions() {
        return this.conversionOutputOptions;
    }

    /**
     * Set the polling interval to use during long-running beginConversion operations.
     * @return The current polling interval.
     */
    public Duration getPollInterval() { return this.pollInterval; }

    /**
     * Set the polling interval to use during long-running beginConversion operations.
     * @param interval The new period to use for polling.
     */
    public void setPollInterval(Duration interval) {
        this.pollInterval = interval;
    }
}
