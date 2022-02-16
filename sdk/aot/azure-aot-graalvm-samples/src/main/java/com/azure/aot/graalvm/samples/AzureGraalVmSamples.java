// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples;

import com.azure.aot.graalvm.samples.appconfiguration.AppConfigurationSample;
import com.azure.aot.graalvm.samples.cosmos.CosmosSample;
import com.azure.aot.graalvm.samples.eventhubs.EventHubsSample;
import com.azure.aot.graalvm.samples.keyvault.certificates.KeyVaultCertificatesSample;
import com.azure.aot.graalvm.samples.keyvault.keys.KeyVaultKeysSample;
import com.azure.aot.graalvm.samples.keyvault.secrets.KeyVaultSecretsSample;
import com.azure.aot.graalvm.samples.storage.blob.StorageBlobSample;
import com.azure.aot.graalvm.samples.textanalytics.TextAnalyticsSample;

public class AzureGraalVmSamples {
    public static void main(String[] args) {
        AppConfigurationSample.runSample();
        // CosmosSample.runSample();
        EventHubsSample.runSample();
        KeyVaultCertificatesSample.runSample();
        KeyVaultKeysSample.runSample();
        KeyVaultSecretsSample.runSample();
        StorageBlobSample.runSample();
        TextAnalyticsSample.runSample();
    }
}
