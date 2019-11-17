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
import com.azure.cs.textanalytics.implementation.models.DocumentSentiment;
import com.azure.cs.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.cs.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.implementation.models.SentimentResponse;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.Entity;
import com.azure.cs.textanalytics.models.LanguageInput;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.MultiLanguageInput;
import com.azure.cs.textanalytics.models.Sentiment;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;
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
    // new user
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
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DetectedLanguage>> detectLanguageWithResponse(String text, String countryHint) {
//        try {
//            return withContext(context -> detectLanguagesWithResponse(languageBatchInput, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    // Hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DetectedLanguage> detectLanguages(List<String> documents, String countryHint) {
        return null;
    }

    // Advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectedLanguage>> detectLanguages(List<LanguageInput> documents, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectedLanguage>>> detectLanguagesWithResponse(
        List<LanguageInput> documents, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectLanguagesWithResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectedLanguage>>> detectLanguagesWithResponse(
        List<LanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return client.languagesWithRestResponseAsync(new LanguageBatchInput().setDocuments(documents),
            options.getModelVersion(), options.isShowStats(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Entity> recognizeEntities(String text, String language) {
        final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() -> recognizeEntitiesWithResponse(multiLanguageInputs, null,
            Context.NONE).map(response -> {
                final List<Entity> entities = response.getValue().getItems();
                if (entities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        entities,
                        null,
                        null);
                }
            }));
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<List<Entity>> recognizeEntities(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<Entity>> recognizeEntities(List<MultiLanguageInput> documents,
                                                                    TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<Entity>>> recognizeEntitiesWithResponse(List<MultiLanguageInput> documents,
                                                                                          TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeEntitiesWithResponse(documents, modelVersion, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<Entity>>> recognizeEntitiesWithResponse(List<MultiLanguageInput> documents,
                                                                                   TextAnalyticsRequestOptions options,
                                                                                   Context context) {
        return client.entitiesRecognitionGeneralWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(documents), options.getModelVersion(),
            options.isShowStats(), context).map(response -> new SimpleResponse<>(response, null));
    }

    // (3) PII entities
    // new user
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

        return new PagedFlux<>(() ->
            recognizeEntitiesWithResponse(multiLanguageInputs, null, Context.NONE)
            .map(response -> {
                final List<Entity> entities = response.getValue().getItems();
                if (entities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        entities,
                        null,
                        null);
                }
            }));
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<List<Entity>> recognizePiiEntities(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<Entity>> recognizePiiEntities(List<MultiLanguageInput> documents,
                                                                       TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<Entity>>> recognizePiiEntitiesWithResponse(List<MultiLanguageInput> documents,
                                                                                             TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizePiiEntitiesWithResponse(documents, modelVersion, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<Entity>>> recognizePiiEntitiesWithResponse(
        List<MultiLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesRecognitionPiiWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(documents),
            options.getModelVersion(), options.isShowStats(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (4) Link entities
    // new user
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

        return new PagedFlux<>(() ->
            recognizeLinkedEntitiesWithResponse(multiLanguageInputs, null,false, Context.NONE)
            .map(response -> {
                final List<LinkedEntity> entities = response.getValue().getItems();
                if (entities.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        entities,
                        null,
                        null);
                }
            }));
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<List<LinkedEntity>> recognizeLinkedEntities(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntities(
        List<MultiLanguageInput> documents, TextAnalyticsRequestOptions options) {
      return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<LinkedEntity>>> recognizeLinkedEntitiesWithResponse(
        List<MultiLanguageInput> documents, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeLinkedEntitiesWithResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<LinkedEntity>>> recognizeLinkedEntitiesWithResponse(
        List<MultiLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesLinkingWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(documents),
            options.getModelVersion(), options.isShowStats(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (5) key phrase
    // new user
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

        return new PagedFlux<>(() ->
            extractKeyPhrasesWithResponse(multiLanguageInputs, null, false, Context.NONE)
            .map(response -> {
                final List<String> keyPhrases = response.getValue().getItems();
                if (keyPhrases.size() == 0) {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        null, // TODO: return null instead of throw exception?
                        null,
                        null);
                } else {
                    return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(),
                        keyPhrases,
                        null,
                        null);
                }
            }));
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<List<String>> extractKeyPhrases(List<String> documents, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<String>> extractKeyPhrases(List<MultiLanguageInput> documents,
                                                                    TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<String>>> extractKeyPhrasesWithResponse(
        List<MultiLanguageInput> documents, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<String>>> extractKeyPhrasesWithResponse(List<MultiLanguageInput> documents,
                                                                                   TextAnalyticsRequestOptions options,
                                                                                   Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.keyPhrasesWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(documents),
            options., showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (6) sentiment
    // new user,
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Sentiment> analyzeSentenceSentiment(String sentence, String language) {
//        try {
//            final List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
//            multiLanguageInputs.add(new MultiLanguageInput().setText(text).setLanguage(language));
//
//            return withContext(context ->
//                analyzeSentimentWithResponse(multiLanguageInputs, null, null, context))
//                .flatMap(response -> {
//                    final List<DocumentSentiment> documentSentiments = response.getValue().getDocuments();
//                    if (documentSentiments.size() == 0) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.justOrEmpty(documentSentiments.get(0));
//                    }
//                });
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Sentiment>> analyzeSentenceSentimentWithResponse(String sentence, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Sentiment> analyzeDocumentSentiment(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<Sentiment>> analyzeDocumentSentiment(List<MultiLanguageInput> document,
                                                                      String modelVersion, Boolean showStats) {
       return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<Sentiment>>> analyzeDocumentSentimentWithResponse(
        List<MultiLanguageInput> documents, String modelVersion, Boolean showStats) {
        try {
            return withContext(context ->
                analyzeDocumentSentimentWithResponse(documents, modelVersion, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<Sentiment>>> analyzeDocumentSentimentWithResponse(List<MultiLanguageInput> documents,
                                                                   String modelVersion, Boolean showStats,
                                                                   Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.sentimentWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(documents), modelVersion, showStats, context)
            .map(response -> new SimpleResponse<>(response, null));
    }
}
