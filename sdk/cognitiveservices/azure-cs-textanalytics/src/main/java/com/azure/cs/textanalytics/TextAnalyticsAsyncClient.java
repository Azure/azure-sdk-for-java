// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.implementation.TextAnalyticsAPIImpl;
import com.azure.cs.textanalytics.models.DocumentEntities;
import com.azure.cs.textanalytics.models.DocumentKeyPhrases;
import com.azure.cs.textanalytics.models.DocumentLanguage;
import com.azure.cs.textanalytics.models.DocumentLinkedEntities;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.EntitiesResult;
import com.azure.cs.textanalytics.models.EntityLinkingResult;
import com.azure.cs.textanalytics.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LanguageBatchInput;
import com.azure.cs.textanalytics.models.LanguageInput;
import com.azure.cs.textanalytics.models.LanguageResult;
import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.MultiLanguageInput;
import com.azure.cs.textanalytics.models.SentimentResponse;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.List;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.implementation.util.FluxUtil.withContext;


@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClient.class);

    private final TextAnalyticsAPIImpl client;
    private final TextAnalyticsServiceVersion serviceVersion;

    TextAnalyticsAsyncClient(TextAnalyticsAPIImpl client, TextAnalyticsServiceVersion serviceVersion) {
        this.client = client;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public TextAnalyticsServiceVersion getServiceVersion() {
        return serviceVersion;
    }


    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentLanguage> detectLanguage(String text, String countryHint, boolean showStats) {
        try {
            final List<LanguageInput> languageInputs = new ArrayList<>();
            languageInputs.add(new LanguageInput().setText(text).setCountryHint(countryHint));

            return withContext(context -> detectLanguageWithResponse(
                new LanguageBatchInput().setDocuments(languageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentLanguage> documentLanguages = response.getValue().getDocuments();
                    if (documentLanguages.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentLanguages.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    // (1.1) A batch of language input
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LanguageResult>> detectLanguageWithResponse(LanguageBatchInput languageBatchInput,
                                                                     boolean showStats) {
        try {
            return withContext(context -> detectLanguageWithResponse(languageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<LanguageResult>> detectLanguageWithResponse(LanguageBatchInput languageBatchInput, boolean showStats,
                                                              Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.languagesWithRestResponseAsync(languageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentEntities> detectEntities(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> detectEntitiesWithResponse(
                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
                    if (documentEntities.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentEntities.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                     boolean showStats) {
        try {
            return withContext(context -> detectEntitiesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntitiesResult>> detectEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                              boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesRecognitionGeneralWithRestResponseAsync(multiLanguageBatchInput,
            getServiceVersion().getVersion(), showStats, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (3) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentEntities> detectHealthCareEntities(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> detectHealthCareEntitiesWithResponse(
                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
                    if (documentEntities.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentEntities.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectHealthCareEntitiesWithResponse(
        MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        try {
            return withContext(context -> detectHealthCareEntitiesWithResponse(multiLanguageBatchInput, showStats,
                context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntitiesResult>> detectHealthCareEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                        boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        // TODO: Health care API is missing
        return client.entitiesRecognitionPiiWithRestResponseAsync(multiLanguageBatchInput,
            getServiceVersion().getVersion(), showStats, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (4) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentEntities> detectPIIEntities(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> detectPIIEntitiesWithResponse(
                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
                    if (documentEntities.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentEntities.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectPIIEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                        boolean showStats) {
        try {
            return withContext(context -> detectPIIEntitiesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntitiesResult>> detectPIIEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                 boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput and update modelversion
        return client.entitiesRecognitionPiiWithRestResponseAsync(multiLanguageBatchInput, null, showStats,
            context).map(response -> new SimpleResponse<>(response, null));
    }

    // (5) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentLinkedEntities> detectLinkedEntities(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> detectLinkedEntitiesWithResponse(
                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentLinkedEntities> documentLinkedEntities = response.getValue().getDocuments();
                    if (documentLinkedEntities.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentLinkedEntities.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntityLinkingResult>> detectLinkedEntitiesWithResponse(
        MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        try {
            return withContext(context -> detectLinkedEntitiesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntityLinkingResult>> detectLinkedEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                         boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesLinkingWithRestResponseAsync(multiLanguageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentKeyPhrases> detectKeyPhrases(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> detectKeyPhrasesWithResponse(
                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentKeyPhrases> documentKeyPhrases = response.getValue().getDocuments();
                    if (documentKeyPhrases.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentKeyPhrases.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> detectKeyPhrasesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                        boolean showStats) {
        try {
            return withContext(context -> detectKeyPhrasesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyPhraseResult>> detectKeyPhrasesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                 boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.keyPhrasesWithRestResponseAsync(multiLanguageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> detectSentiment(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> detectSentimentWithResponse(
                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
                .flatMap(response -> {
                    final List<DocumentSentiment> documentSentiments = response.getValue().getDocuments();
                    if (documentSentiments.size() == 0) {
                        return Mono.empty();
                    } else {
                        return Mono.justOrEmpty(documentSentiments.get(0));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SentimentResponse>> detectSentimentWithResponse(
        MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        try {
            return withContext(context -> detectSentimentWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<SentimentResponse>> detectSentimentWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                  boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.sentimentWithRestResponseAsync(multiLanguageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }
}
