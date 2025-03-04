// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationAudience;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

public class ConfigurationAudienceUnitTest {
    // The endpoint can be obtained by going to your App Configuration instance in the Azure portal
    // and navigating to "Overview" page. Looking for the "Endpoint" keyword.
    String endpoint = Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");

    // Default token credential could be obtained from Identity service.
    // It tries to create a valid credential in the following order:
    //      EnvironmentCredential
    //      ManagedIdentityCredential
    //      SharedTokenCacheCredential
    //      Fails if none of the credentials above could be created.
    DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

    final ConfigurationClient client = new ConfigurationClientBuilder()
        .credential(tokenCredential) // AAD authentication
        .endpoint(endpoint)
        .buildClient();


    @Test
    public void testConfigurationAudience() {
        String cnEndpoint = "https://test.appconfig.azure.cn";
        String usEndpoint = "https://test.appconfig.azure.us";
        String publicEndpoint = "https://test.appconfig.azure.com";
        String legacyCnEndpoint = "https://test.azconfig.azure.cn";
        String legacyUsEndpoint = "https://test.azconfig.azure.us";
        String legacyPublicEndpoint = "https://test.azconfig.azure.com";

        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(tokenCredential) // AAD authentication
            .endpoint(endpoint).buildClient();
    }
}
