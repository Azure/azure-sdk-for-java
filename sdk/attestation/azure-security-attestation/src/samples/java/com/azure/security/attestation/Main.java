// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import java.util.Scanner;

/**
 * Entry point for running samples.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {

        // Attestation samples.
        ReadmeSamples.signingCertificatesGet();

        Scanner userInput = new Scanner(System.in);
        System.out.println("Press any key to exit.");
        userInput.nextLine();
        System.exit(1);
    }
}
