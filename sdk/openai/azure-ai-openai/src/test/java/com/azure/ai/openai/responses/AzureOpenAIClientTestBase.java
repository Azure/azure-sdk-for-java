package com.azure.ai.openai.responses;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;

public class AzureOpenAIClientTestBase extends TestProxyTestBase {

    AzureOpenAIClient getResponseClient(HttpClient httpClient, AzureOpenAIServiceVersion serviceVersion) {
        AzureOpenAIClientBuilder builder = new AzureOpenAIClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                .credential(new AzureKeyCredential(
                        Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY"))
                ).serviceVersion(serviceVersion)
                .httpClient(httpClient)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        return builder.buildClient();
    }
}
