// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TextTranslationClientBase extends TestBase {    
    private final String defaultEndpoint = "fakeEndpoint";
    private final String defaultApiKey = "fakeApiKey";
    private final String defaultRegion = "fakeRegion";
    private final String defaultCustomEndpoint = "fakeCustomEndpoint";
       

    TextTranslationClient getTranslationClient() {
        return getClient(getEndpoint());
    }
    
    TextTranslationClient getTranslationClientWithCustomEndpoint() {
        return getClient(getCustomEndpoint());
    }
    
    
    TextTranslationClient getClient(String endpoint) {
        TextTranslationClientBuilder textTranslationClientbuilder =
                new TextTranslationClientBuilder()                        
                        .credential(new AzureKeyCredential(getKey()))
                        .region(getRegion())
                        .endpoint(endpoint)
                        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            textTranslationClientbuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            textTranslationClientbuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return textTranslationClientbuilder.buildClient();
    }

    protected String getEndpoint() {
        return Configuration.getGlobalConfiguration().get("AZURE_TEXT_TRANSLATION_ENDPOINT", defaultEndpoint);
    }

    protected String getCustomEndpoint() {
        return Configuration.getGlobalConfiguration().get("AZURE_TEXT_TRANSLATION_CUSTOM_ENDPOINT", defaultCustomEndpoint);
    }

    private String getKey() {
        return Configuration.getGlobalConfiguration().get("AZURE_TEXT_TRANSLATION_API_KEY", defaultApiKey);
    }

    private String getRegion() {
    return Configuration.getGlobalConfiguration().get("AZURE_TEXT_TRANSLATION_REGION", defaultRegion);
    }  
	
    TextTranslationClient getTranslationClientWithToken() {
        TextTranslationClientBuilder textTranslationClientbuilder =
                new TextTranslationClientBuilder()                        
                        .credential(getTokenCredential())
                        .region(getRegion())
                        .endpoint(getEndpoint())
                        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (getTestMode() == TestMode.PLAYBACK) {
            textTranslationClientbuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            textTranslationClientbuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return textTranslationClientbuilder.buildClient();
    }
    
    private TokenCredential getTokenCredential()
    {
        String issueTokenURL = String.format("https://%s.api.cognitive.microsoft.com/sts/v1.0/issueToken?", getRegion()); 
        HttpClient httpClient = HttpClient.newHttpClient(); 
        URI uri = new URI(issueTokenURL + "Subscription-Key=" + getKey()); 
        HttpRequest request = HttpRequest.newBuilder().uri(uri) .POST(HttpRequest.BodyPublishers.noBody()) .build(); 
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); 
        return new TokenCredential(response.body());        
    }
}