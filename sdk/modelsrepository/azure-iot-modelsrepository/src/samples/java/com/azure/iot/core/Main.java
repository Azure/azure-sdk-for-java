// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.core;

import java.util.Scanner;

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

        Scanner userInput = new Scanner(System.in);
        System.out.println("Press any key to exit.");
        userInput.nextLine();
        System.exit(1);
    }
}
