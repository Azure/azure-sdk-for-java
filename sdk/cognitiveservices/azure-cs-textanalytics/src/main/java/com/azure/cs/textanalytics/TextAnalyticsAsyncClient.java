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
import com.azure.cs.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.cs.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.NamedEntity;
import com.azure.cs.textanalytics.models.TextSentiment;
import com.azure.cs.textanalytics.models.UnknownLanguageInput;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.TextDocumentInput;
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
//            final List<UnknownLanguageInput> languageInputs = new ArrayList<>();
//            languageInputs.add(new UnknownLanguageInput().setText(text).setCountryHint(countryHint));
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
    public PagedFlux<DetectedLanguage> detectLanguages(List<String> document, String countryHint) {
        return null;
    }

    // Advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectedLanguage>> detectLanguages(List<UnknownLanguageInput> document, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectedLanguage>>> detectLanguagesWithResponse(
        List<UnknownLanguageInput> document, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectLanguagesWithResponse(document, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectedLanguage>>> detectLanguagesWithResponse(
        List<UnknownLanguageInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.languagesWithRestResponseAsync(new LanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.isShowStatistics(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<NamedEntity> recognizeEntities(String text, String language) {
        final List<TextDocumentInput> textDocumentInputs = new ArrayList<>();
        textDocumentInputs.add(new TextDocumentInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() -> recognizeEntitiesWithResponse(textDocumentInputs, null,
            Context.NONE).map(response -> {
                final List<NamedEntity> entities = response.getValue().getItems();
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
    public PagedFlux<List<NamedEntity>> recognizeEntities(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntity>> recognizeEntities(List<TextDocumentInput> document,
                                                                         TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntity>>> recognizeEntitiesWithResponse(List<TextDocumentInput> document,
                                                                                               TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeEntitiesWithResponse(document, modelVersion, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<NamedEntity>>> recognizeEntitiesWithResponse(List<TextDocumentInput> document,
                                                                                        TextAnalyticsRequestOptions options,
                                                                                        Context context) {
        return client.entitiesRecognitionGeneralWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(document), options.getModelVersion(),
            options.isShowStatistics(), context).map(response -> new SimpleResponse<>(response, null));
    }

    // (3) PII entities
    // new user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<NamedEntity> recognizePiiEntities(String text, String language) {
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

        //TODO: PagedFlux example, PagedFlux<NamedEntity>, remove this if choose to use Mono example.
        final List<TextDocumentInput> textDocumentInputs = new ArrayList<>();
        textDocumentInputs.add(new TextDocumentInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() ->
            recognizeEntitiesWithResponse(textDocumentInputs, null, Context.NONE)
            .map(response -> {
                final List<NamedEntity> entities = response.getValue().getItems();
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
    public PagedFlux<List<NamedEntity>> recognizePiiEntities(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntity>> recognizePiiEntities(List<TextDocumentInput> document,
                                                                            TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntity>>> recognizePiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizePiiEntitiesWithResponse(document, modelVersion, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<NamedEntity>>> recognizePiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesRecognitionPiiWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.isShowStatistics(), context)
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

        //TODO: PagedFlux example, PagedFlux<NamedEntity>, remove this if choose to use Mono example.
        final List<TextDocumentInput> textDocumentInputs = new ArrayList<>();
        textDocumentInputs.add(new TextDocumentInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() ->
            recognizeLinkedEntitiesWithResponse(textDocumentInputs, null,false, Context.NONE)
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
    public PagedFlux<List<LinkedEntity>> recognizeLinkedEntities(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
      return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<LinkedEntity>>> recognizeLinkedEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                recognizeLinkedEntitiesWithResponse(document, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<LinkedEntity>>> recognizeLinkedEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesLinkingWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.isShowStatistics(), context)
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
        final List<TextDocumentInput> textDocumentInputs = new ArrayList<>();
        textDocumentInputs.add(new TextDocumentInput().setText(text).setLanguage(language));

        return new PagedFlux<>(() ->
            extractKeyPhrasesWithResponse(textDocumentInputs, null, false, Context.NONE)
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
    public PagedFlux<List<String>> extractKeyPhrases(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<String>> extractKeyPhrases(List<TextDocumentInput> document,
                                                                    TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<String>>> extractKeyPhrasesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractKeyPhrasesWithResponse(document, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<String>>> extractKeyPhrasesWithResponse(List<TextDocumentInput> document,
                                                                                   TextAnalyticsRequestOptions options,
                                                                                   Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.keyPhrasesWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options., showStats, context).map(response -> new SimpleResponse<>(response, null));
    }

    // (6) sentiment
    // new user,
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TextSentiment> analyzeSentenceSentiment(String sentence, String language) {
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
    public Mono<Response<TextSentiment>> analyzeSentenceSentimentWithResponse(String sentence, String language) {
        return null;
    }

    // hackathon user
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<TextSentiment> analyzeDocumentSentiment(List<String> document, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DocumentSentiment>> analyzeDocumentSentiment(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
       return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DocumentSentiment>>> analyzeDocumentSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context ->
                analyzeDocumentSentimentWithResponse(document, modelVersion, showStats, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DocumentSentiment>>> analyzeDocumentSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.sentimentWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(document), modelVersion, showStats, context)
            .map(response -> new SimpleResponse<>(response, null));
    }
}
