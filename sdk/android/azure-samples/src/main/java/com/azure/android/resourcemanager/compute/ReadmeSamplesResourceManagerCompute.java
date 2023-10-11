// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.resourcemanager.compute;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.compute.ComputeManager;

/**
 * Code samples for the README.md
 */
public class ReadmeSamplesResourceManagerCompute {

    public static void authenticate() {
        // BEGIN: readme-sample-authenticate
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
        ComputeManager manager = ComputeManager
            .authenticate(credential, profile);
        // END: readme-sample-authenticate
    }
}
