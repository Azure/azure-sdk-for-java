// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.websocket.WebSocketClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;

import java.util.function.BiConsumer;

public abstract class RealtimeClientTestBase { //} extends TestProxyTestBase {

    RealtimeClientBuilder getRealtimeClientBuilder(WebSocketClient webSocketClient, OpenAIServiceVersion serviceVersion) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        return new RealtimeClientBuilder()
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .serviceVersion(serviceVersion)
                .credential(new AzureKeyCredential(azureOpenaiKey));
    }

    RealtimeClientBuilder getNonAzureRealtimeClientBuilder(WebSocketClient webSocketClient) {
        String openAIKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
        String openAIModel = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

        return new RealtimeClientBuilder()
                .deploymentOrModelName(openAIModel)
                .credential(new KeyCredential(openAIKey));
    }

//    void getALAWFileRunner(BiConsumer<String, >)
}
