// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class CreateClient {
    public static void main(String[] args) {
        // BEGIN:com.azure.ai.contentsafety.createclientcredential
        String endpoint = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_ENDPOINT");
        String key = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY");
        ContentSafetyClient contentSafetyClient = new ContentSafetyClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint).buildClient();
        BlocklistClient blocklistClient = new BlocklistClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint).buildClient();
        // END:com.azure.ai.contentsafety.createclientcredential

        // BEGIN:com.azure.ai.contentsafety.createclienttoken
        ContentSafetyClient contentSafetyClientOauth = new ContentSafetyClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint).buildClient();
        BlocklistClient blocklistClientOauth = new BlocklistClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint).buildClient();
        // END:com.azure.ai.contentsafety.createclienttoken
    }
}
