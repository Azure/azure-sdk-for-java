// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples;

import com.azure.aot.graalvm.samples.appconfiguration.AppConfigurationSample;
import com.azure.aot.graalvm.samples.eventhubs.EventHubsSample;
import com.azure.aot.graalvm.samples.keyvault.certificates.KeyVaultCertificatesSample;
import com.azure.aot.graalvm.samples.keyvault.keys.KeyVaultKeysSample;
import com.azure.aot.graalvm.samples.keyvault.secrets.KeyVaultSecretsSample;
import com.azure.aot.graalvm.samples.storage.blob.StorageBlobSample;
import com.azure.aot.graalvm.samples.textanalytics.TextAnalyticsSample;

/**
 * Main class to run Azure client samples using GraalVM.
 */
public class AzureGraalVmSamples {
    /**
     * Main method to run the samples.
     * @param args args to samples.
     */
    public static void main(String[] args) {
        AppConfigurationSample.runSample();
        EventHubsSample.runSample();
        KeyVaultCertificatesSample.runSample();
        KeyVaultKeysSample.runSample();
        KeyVaultSecretsSample.runSample();
        StorageBlobSample.runSample();
        TextAnalyticsSample.runSample();


        // GraalVM does not support AfterBurner and Cosmos explicitly adds AfterBurner
        // CosmosSample runs successfully if afterburner registration is removed in Cosmos
        // https://github.com/micronaut-projects/micronaut-core/issues/2575
        // https://github.com/awslabs/aws-serverless-java-container/issues/428
        // CosmosSample.runSample();
    }
}
