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
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;

/**
 * Code samples for the AUTH.md
 */
public class AuthSamples {

    public void buildClientSecretCredential() {
        // BEGIN: readme-sample-buildClientSecretCredential
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .clientSecret("<YOUR_CLIENT_SECRET>")
            .tenantId("<YOUR_TENANT_ID>")
            // authority host is optional
            .authorityHost("<AZURE_AUTHORITY_HOST>")
            .build();
        // END: readme-sample-buildClientSecretCredential
    }

    public void buildManagedIdentityCredential() {
        // BEGIN: readme-sample-buildManagedIdentityCredential
        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            // client ID is optional
            .clientId("<YOUR_CLIENT_ID>")
            .build();
        // END: readme-sample-buildManagedIdentityCredential
    }

    public void buildAzureProfile() {
        // BEGIN: readme-sample-buildAzureProfile
        // AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        AzureProfile profile = new AzureProfile("<YOUR_TENANT_ID>", "<YOUR_SUBSCRIPTION_ID>", AzureEnvironment.AZURE);
        // END: readme-sample-buildAzureProfile
    }

    public void buildEnvironmentCredential() {
        // BEGIN: readme-sample-buildEnvironmentCredential
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE_GERMANY);
        EnvironmentCredential credential = new EnvironmentCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        // END: readme-sample-buildEnvironmentCredential
    }

    public void authenticate(TokenCredential credential, AzureProfile profile) {
        // BEGIN: readme-sample-authenticateAuth
        AzureResourceManager azure = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();
        // END: readme-sample-authenticateAuth
    }

    public void authenticateAndListSubs(TokenCredential credential, AzureProfile profile) {
        // BEGIN: readme-sample-authenticateAndListSubs
        AzureResourceManager.Authenticated authenticated = AzureResourceManager.authenticate(credential, profile);
        String subscriptionId = authenticated.subscriptions().list().iterator().next().subscriptionId();
        AzureResourceManager azure = authenticated.withSubscription(subscriptionId);
        // END: readme-sample-authenticateAndListSubs
    }

    public void configure(HttpPipelinePolicy customPolicy,
                          RetryPolicy customRetryPolicy,
                          HttpClient httpClient,
                          TokenCredential credential,
                          AzureProfile profile) {
        // BEGIN: readme-sample-customPipeline
        AzureResourceManager azure = AzureResourceManager.configure()
            .withPolicy(customPolicy)
            .withRetryPolicy(customRetryPolicy)
            .withHttpClient(httpClient)
            .authenticate(credential, profile)
            .withDefaultSubscription();
        // END: readme-sample-customPipeline
    }
}
