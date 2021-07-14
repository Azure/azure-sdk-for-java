// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;

public abstract class CertificatesTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final CertificateClient certificateClient;
    protected final CertificateAsyncClient certificateAsyncClient;
    private final Configuration configuration;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public CertificatesTest(TOptions options) {
        super(options);

        configuration = Configuration.getGlobalConfiguration().clone();
        String vaultUrl = configuration.get("AZURE_KEYVAULT_URL");

        if (CoreUtils.isNullOrEmpty(vaultUrl)) {
            throw new IllegalStateException("Environment variable AZURE_KEYVAULT_URL must be set");
        }

        // Setup the service client
        CertificateClientBuilder builder = new CertificateClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build());

        ConfigureClientBuilder(builder);

        certificateClient = builder.buildClient();
        certificateAsyncClient = builder.buildAsyncClient();
    }
}
