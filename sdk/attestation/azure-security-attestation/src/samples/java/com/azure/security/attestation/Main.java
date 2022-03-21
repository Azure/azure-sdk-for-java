// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

/**
 * Entry point for running samples.
 */
public class Main {
    public static void main(String[] args) {
        PolicySamples.resetAllPolicies();

        // Readme samples.
        ReadmeSamples.executeSamples();

        // JavaDocSnippets
        AttestationClientJavaDocCodeSnippets.executeSamples();

        // Attestation Samples
        AttestationSamples.executeSamples();

        // Policy samples.
        PolicySamples.executeSamples();

        // Policy Management Certificates.
        PolicyManagementCertificatesSamples.executeSamples();

        System.exit(0);
    }
}
