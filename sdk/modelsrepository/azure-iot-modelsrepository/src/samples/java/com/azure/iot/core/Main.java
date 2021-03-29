// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.core;

/**
 * Entry point for running samples.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {

        // DtmiConventions samples
        DtmiConventionsSamples.isValidDtmi();
        DtmiConventionsSamples.getModelUri();

        // Client initialization samples
        ModelResolutionSamples.clientInitializationSamples();

        // Model resolution samples
        ModelResolutionSamples.getModelsFromGlobalRepository();
        ModelResolutionSamples.getModelsFromLocalRepository();
        ModelResolutionSamples.getMultipleModelsFromGlobalRepository();

        // Allow enough time for async APIs to get notified and execute the subscribe supplier.
        Thread.sleep(2 * 1000);
    }
}
