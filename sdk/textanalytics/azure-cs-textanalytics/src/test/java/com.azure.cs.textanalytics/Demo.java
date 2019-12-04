// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cs.textanalytics;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.cs.textanalytics.implementation.TextAnalyticsClientImplBuilder;
import com.azure.cs.textanalytics.implementation.models.DocumentLanguage;
import com.azure.cs.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.cs.textanalytics.implementation.models.LanguageResult;
import com.azure.cs.textanalytics.models.DetectLanguageInput;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Demo {
    private final ClientLogger logger = new ClientLogger(Demo.class);

    private static final String AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY = "AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY";
    private String subscriptionKey;

    @Test
    public void test() {

        String endpoint = "https://shawnjavatextanalytics.cognitiveservices.azure.com/";

        subscriptionKey = Configuration.getGlobalConfiguration().get(AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY);
        System.out.println("Subscription Key = " + subscriptionKey);

        HttpHeaders headers = new HttpHeaders()
            .put("Ocp-Apim-Subscription-Key", subscriptionKey);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new RequestIdPolicy());
        policies.add(new AddHeadersPolicy(headers));
        policies.add(new AddDatePolicy());
        // customized pipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();


        TextAnalyticsClientImpl ta = new TextAnalyticsClientImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline)
            .build();

        System.out.println("Endpoint = " + ta.getEndpoint());

        final List<DetectLanguageInput> inputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English","US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );
        final LanguageBatchInput languageBatchInput = new LanguageBatchInput().setDocuments(inputs);
        Mono<SimpleResponse<LanguageResult>> result = ta.languagesWithRestResponseAsync(languageBatchInput, Context.NONE)
             .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", languageBatchInput))
             .doOnSuccess(response -> logger.info("A batch of detected language output - {}", languageBatchInput))
             .doOnError(error -> logger.warning("Failed to detected languages - {}", languageBatchInput))
             .map(response -> new SimpleResponse<>(response, response.getValue()));

        List<DocumentLanguage> documentLanguages = result.block().getValue().getDocuments();
        for (DocumentLanguage documentLanguage : documentLanguages) {
            System.out.println("document language = " + documentLanguage.getId());

            documentLanguage.getDetectedLanguages().forEach(detectedLanguage ->
                System.out.printf("detected language, name = %s, iso name = %s, score = %s.",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore()));
        }
    }
}
