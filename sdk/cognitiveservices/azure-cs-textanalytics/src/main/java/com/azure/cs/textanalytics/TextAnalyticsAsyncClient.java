// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.cs.textanalytics.implementation.TextAnalyticsAPIImpl;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DocumentEntities;
import com.azure.cs.textanalytics.models.DocumentKeyPhrases;
import com.azure.cs.textanalytics.models.DocumentLanguage;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.EntitiesResult;
import com.azure.cs.textanalytics.models.Entity;
import com.azure.cs.textanalytics.models.EntityLinkingResult;
import com.azure.cs.textanalytics.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LanguageBatchInput;
import com.azure.cs.textanalytics.models.LanguageInput;
import com.azure.cs.textanalytics.models.LanguageResult;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.MultiLanguageInput;
import com.azure.cs.textanalytics.models.SentimentResponse;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.List;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

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
    public Mono<DetectedLanguage> detectLanguage(String text, String countryHint) {
         // TODO: Mono<LanguageResult> example, if choose PagedFlux, remove this
//        try {
//            final List<LanguageInput> languageInputs = new ArrayList<>();
//            languageInputs.add(new LanguageInput().setText(text).setCountryHint(countryHint));
//
//            return withContext(context -> getLanguagesWithResponse(
//                new LanguageBatchInput().setDocuments(languageInputs), showStats, context))
//                .flatMap(response -> {
//                    final List<DocumentLanguage> documentLanguages = response.getValue().getDocuments();
//
//                    if (documentLanguages.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentLanguages.get(0).getDetectedLanguages());
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
//            //TODO: PagedFlux example, PagedFlux<DetectedLanguage>, remove this if choose to use Mono example.
//            final List<LanguageInput> languageInputs = new ArrayList<>();
//            languageInputs.add(new LanguageInput().setText(text).setCountryHint(countryHint));
//
//            return new PagedFlux<>(() -> detectLanguagesWithResponse(new LanguageBatchInput().setDocuments(languageInputs),
//                showStats, Context.NONE)
//                .map(response -> {
//                    final List<DocumentLanguage> documentLanguages = response.getValue().getDocuments();
//                    if (documentLanguages.size() == 0) {
//                        return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
//                            response.getHeaders(),
//                            null,
//                            null,
//                            null);
//                    } else {
//                        final List<DetectedLanguage> detectedLanguages = documentLanguages.get(0).getDetectedLanguages();
//                        return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
//                            response.getHeaders(),
//                            detectedLanguages,
//                            null,
//                            null);
//                    }
//                }));
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LanguageResult>> detectLanguagesWithResponse(LanguageBatchInput languageBatchInput,
                                                                   boolean showStats) {
        try {
            return withContext(context -> detectLanguagesWithResponse(languageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<LanguageResult>> detectLanguagesWithResponse(LanguageBatchInput languageBatchInput, boolean showStats,
                                                               Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.languagesWithRestResponseAsync(languageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (2) entities
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Entity> recognizeEntities(String text, String language) {

        // TODO: Mono<DocumentEntities> example, remove this if choose PagedFlux
//        try {
//            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
//            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));
//
//            return withContext(context -> getEntitiesWithResponse(
//                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
//                .flatMap(response -> {
//                    final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
//                    if (documentEntities.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentEntities.get(0));
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }

        //TODO: PagedFlux example, PagedFlux<Entity>, remove this if choose to use Mono example.
        final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() -> recognizeEntitiesWithResponse(
            new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), false, Context.NONE)
            .map(response -> {
                final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
                if (documentEntities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    final List<Entity> entities = documentEntities.get(0).getEntities();
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        entities,
                        null,
                        null);
                }
            }));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> recognizeEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                        boolean showStats) {
        try {
            return withContext(context -> recognizeEntitiesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntitiesResult>> recognizeEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                 boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesRecognitionGeneralWithRestResponseAsync(multiLanguageBatchInput,
            getServiceVersion().getVersion(), showStats, context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (3) PII entities
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Entity> recognizePiiEntities(String text, String language) {
        // TODO: Mono<DocumentEntities> example, remove this if choose PagedFlux
//        try {
//            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
//            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));
//
//            return withContext(context -> getPiiEntitiesWithResponse(
//                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
//                .flatMap(response -> {
//                    final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
//                    if (documentEntities.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentEntities.get(0));
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }

        //TODO: PagedFlux example, PagedFlux<Entity>, remove this if choose to use Mono example.
        final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() -> recognizeEntitiesWithResponse(
            new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), false, Context.NONE)
            .map(response -> {
                final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
                if (documentEntities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    final List<Entity> entities = documentEntities.get(0).getEntities();
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        entities,
                        null,
                        null);
                }
            }));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> recognizePiiEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                           boolean showStats) {
        try {
            return withContext(context -> recognizePiiEntitiesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntitiesResult>> recognizePiiEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                    boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesRecognitionPiiWithRestResponseAsync(multiLanguageBatchInput, null, showStats,
            context).map(response -> new SimpleResponse<>(response, null));
    }

    // (4) Link entities
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<LinkedEntity> recognizeLinkedEntities(String text, String language) {
        // TODO: Mono<DocumentEntities> example, remove this if choose PagedFlux
//        try {
//            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
//            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));
//
//            return withContext(context -> getLinkedEntitiesWithResponse(
//                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
//                .flatMap(response -> {
//                    final List<DocumentLinkedEntities> documentLinkedEntities = response.getValue().getDocuments();
//                    if (documentLinkedEntities.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentLinkedEntities.get(0));
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }

        //TODO: PagedFlux example, PagedFlux<Entity>, remove this if choose to use Mono example.
        final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() -> recognizeEntitiesWithResponse(
            new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), false, Context.NONE)
            .map(response -> {
                final List<DocumentEntities> documentEntities = response.getValue().getDocuments();
                if (documentEntities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    final List<Entity> entities = documentEntities.get(0).getEntities();
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        entities,
                        null,
                        null);
                }
            }));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntityLinkingResult>> recognizeLinkedEntitiesWithResponse(
        MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        try {
            return withContext(context -> recognizeLinkedEntitiesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<EntityLinkingResult>> recognizeLinkedEntitiesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                            boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesLinkingWithRestResponseAsync(multiLanguageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (5) key phrase
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> extractKeyPhrases(String text, String language) {
        // TODO: Mono<DocumentKeyPhrases> example, remove this if choose PagedFlux
//        try {
//            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
//            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));
//
//            return withContext(context -> getKeyPhrasesWithResponse(
//                new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), showStats, context))
//                .flatMap(response -> {
//                    final List<DocumentKeyPhrases> documentKeyPhrases = response.getValue().getDocuments();
//                    if (documentKeyPhrases.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentKeyPhrases.get(0));
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }

        //TODO: PagedFlux example, PagedFlux<String>, remove this if choose to use Mono example.
        final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() -> extractKeyPhrasesWithResponse(
            new MultiLanguageBatchInput().setDocuments(multiLanguageInputs), false, Context.NONE)
            .map(response -> {
                final List<DocumentKeyPhrases> documentEntities = response.getValue().getDocuments();
                if (documentEntities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        documentEntities.get(0).getKeyPhrases(),
                        null,
                        null);
                }
            }));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                         boolean showStats) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                  boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.keyPhrasesWithRestResponseAsync(multiLanguageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (6) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String text, String language, boolean showStats) {
        try {
            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

            return withContext(context -> analyzeSentimentWithResponse(
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
    public Mono<Response<SentimentResponse>> analyzeSentimentWithResponse(
        MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats) {
        try {
            return withContext(context -> analyzeSentimentWithResponse(multiLanguageBatchInput, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<SentimentResponse>> analyzeSentimentWithResponse(MultiLanguageBatchInput multiLanguageBatchInput,
                                                                   boolean showStats, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.sentimentWithRestResponseAsync(multiLanguageBatchInput, getServiceVersion().getVersion(),
            showStats, context).map(response -> new SimpleResponse<>(response, null));
    }
}
