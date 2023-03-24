// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;

import java.net.*;
import java.io.*;

public class TextTranslationClientBase extends TestBase {
    private final String defaultEndpoint = "https://fakeEndpoint.cognitive.microsofttranslator.com";
    private final String defaultApiKey = "fakeApiKey";
    private final String defaultRegion = "fakeRegion";
    private final String defaultCustomEndpoint = "https://fakeCustomEndpoint.cognitiveservices.azure.com";
    private final String defaultTokenURL = "https://fakeTokenEndpoint.api.cognitive.microsoft.com";

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

	private String getTokenURL() {
        return Configuration.getGlobalConfiguration().get("AZURE_TEXT_TRANSLATION_TOKENURL", defaultTokenURL);
    }

    TextTranslationClient getTranslationClientWithToken() throws MalformedURLException, IOException {
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

    private TokenCredential getTokenCredential() throws MalformedURLException, IOException {
        String tokenResponse;

        if (getTestMode() == TestMode.PLAYBACK) {
            tokenResponse = "FAKE_TOKEN";
        } else {
            URL tokenService = new URL(getTokenURL() + "/sts/v1.0/issueToken?Subscription-Key=" + getKey());
            HttpURLConnection connection = (HttpURLConnection) tokenService.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter writers = new OutputStreamWriter(connection.getOutputStream());
            writers.write("{}"); //body
            writers.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            tokenResponse = in.readLine();
            in.close();
        }
        return new StaticTokenForTest(tokenResponse);
    }
}
