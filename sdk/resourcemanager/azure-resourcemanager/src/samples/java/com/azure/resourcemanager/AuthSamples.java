// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.EnvironmentCredential;
import com.azure.identity.EnvironmentCredentialBuilder;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO AUTH.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the AUTH.md
 */
public class AuthSamples {
    // RESERVE SPACE FOR MORE IMPORT LINES
























    // START FROM LINE#50
    public void buildClientSecretCredential() {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .clientSecret("<YOUR_CLIENT_SECRET>")
            .tenantId("<YOUR_TENANT_ID>")
            // authority host is optional
            .authorityHost("<AZURE_AUTHORITY_HOST>")
            .build();
    }

    public void buildAzureProfile() {
        // AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        AzureProfile profile = new AzureProfile("<YOUR_TENANT_ID>", "<YOUR_SUBSCRIPTION_ID>", AzureEnvironment.AZURE);
    }

    public void init() {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE_GERMANY);
        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
    }

    public void authenticate(TokenCredential credential, AzureProfile profile) {
        AzureResourceManager azure = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();
    }

    public void authenticateAndListSubs(TokenCredential credential, AzureProfile profile) {
        AzureResourceManager.Authenticated authenticated = AzureResourceManager.authenticate(credential, profile);
        String subscriptionId = authenticated.subscriptions().list().iterator().next().subscriptionId();
        AzureResourceManager azure = authenticated.withSubscription(subscriptionId);
    }

    public void configure(HttpPipelinePolicy customPolicy,
                          RetryPolicy customRetryPolicy,
                          HttpClient httpClient,
                          TokenCredential credential,
                          AzureProfile profile) {
        AzureResourceManager azure = AzureResourceManager.configure()
            .withPolicy(customPolicy)
            .withRetryPolicy(customRetryPolicy)
            .withHttpClient(httpClient)
            .authenticate(credential, profile)
            .withDefaultSubscription();
    }
}
