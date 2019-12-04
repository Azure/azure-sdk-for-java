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
import com.azure.cs.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.cs.textanalytics.implementation.models.DocumentLanguage;
import com.azure.cs.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.cs.textanalytics.implementation.models.LanguageResult;
import com.azure.cs.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.DetectLanguageInput;
import com.azure.cs.textanalytics.models.DetectLanguageResult;
import com.azure.cs.textanalytics.models.DocumentError;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LinkedEntityResult;
import com.azure.cs.textanalytics.models.NamedEntityResult;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.TextSentimentResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClient.class);

    private final TextAnalyticsClientImpl client;
    private final TextAnalyticsServiceVersion serviceVersion;

    TextAnalyticsAsyncClient(TextAnalyticsClientImpl client, TextAnalyticsServiceVersion serviceVersion) {
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
    public Mono<DetectLanguageResult> detectLanguage(String text) {
        return detectLanguage(text, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResult> detectLanguage(String text, String countryHint) {
        return detectLanguageWithResponse(text, countryHint)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DetectLanguageResult>> detectLanguageWithResponse(String text, String countryHint) {
        try {
            return withContext(context -> detectLanguageWithResponse(text, countryHint, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<DetectLanguageResult>> detectLanguageWithResponse(String text, String countryHint, Context context) {
        List<DetectLanguageInput> languageInputs = new ArrayList<>();
        languageInputs.add(new DetectLanguageInput(Integer.toString(0), text, countryHint));
        // TODO: should this be a random number generator?
        return detectBatchLanguagesWithResponse(languageInputs, null, context).flatMap(response -> {
            if (response.getValue().iterator().hasNext()) {
                return Mono.justOrEmpty(new SimpleResponse<>(response, response.getValue().iterator().next()));
            }
            return monoError(logger, new RuntimeException("Unable to retrieve languages."));
        });
    }

    // Hackathon user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguages(List<String> inputs) {
        return detectLanguages(inputs, null);
    }

    public Mono<DocumentResultCollection<DetectLanguageResult>> detectLanguages(List<String> inputs,
                                                                                String countryHint) {
        List<DetectLanguageInput> languageInputs = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            languageInputs.add(new DetectLanguageInput(Integer.toString(i), inputs.get(i), countryHint));
        }

        return detectBatchLanguages(languageInputs, null);
    }

    // Advanced user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguages(List<DetectLanguageInput> inputs) {
        return detectBatchLanguages(inputs, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectLanguageResult>> detectBatchLanguages(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options) {
       return detectBatchLanguagesWithResponse(inputs, options)
           .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectBatchLanguagesWithResponse(inputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate inputs
        final LanguageBatchInput languageBatchInput = new LanguageBatchInput().setDocuments(inputs);
        // TODO: confirm if options null is fine?
        return client.languagesWithRestResponseAsync(
            languageBatchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(),
            context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", languageBatchInput))
            .doOnSuccess(response -> logger.info("A batch of detected language output - {}", languageBatchInput))
            .doOnError(error -> logger.warning("Failed to detected languages - {}", languageBatchInput))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    private DocumentResultCollection<DetectLanguageResult> toDocumentResultCollection(LanguageResult value) {
        return new
            DocumentResultCollection<>(getDocumentLanguages(value), value.getModelVersion(), value.getStatistics());
    }

    private List<DetectLanguageResult> getDocumentLanguages(LanguageResult value) {
        List<DetectLanguageResult> detectedLanguageList = new ArrayList<>();
        List<DocumentLanguage> documentLanguageList = value.getDocuments();
        for (DocumentLanguage documentLanguage : documentLanguageList) {
            detectedLanguageList.add(new DetectLanguageResult(documentLanguage.getId(), documentLanguage.getStatistics(),
                getDocumentError(value, documentLanguage),
                documentLanguage.getDetectedLanguages().get(0), documentLanguage.getDetectedLanguages()));
        }
        return detectedLanguageList;
    }

    private DocumentError getDocumentError(LanguageResult value, DocumentLanguage documentLanguage) {
        value.getErrors().stream().map(val -> {
            if (val.getId() == documentLanguage.getId()) {
                return val.getError();
            }
            return null;
        });
        return null;
    }

    // (2) entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizeEntities(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizeEntities(String text, String language) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamedEntityResult>> recognizeEntitiesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> inputs) {
//        return null;
//    }
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeEntities(List<String> inputs, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntities(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchEntities(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                recognizeEntitiesWithResponse(document, modelVersion, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.entitiesRecognitionGeneralWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(document), options.getModelVersion(),
            options.showStatistics(), context).map(response -> new SimpleResponse<>(response, null));
    }

    // (3) PII entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizePiiEntities(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NamedEntityResult> recognizePiiEntities(String text, String language) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NamedEntityResult>> recognizePiiEntitiesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> inputs) {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizePiiEntities(List<String> inputs,
                                                                                  String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntities(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<NamedEntityResult>> recognizeBatchPiiEntities(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                recognizePiiEntitiesWithResponse(document, modelVersion, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    Mono<Response<DocumentResultCollection<NamedEntityResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesRecognitionPiiWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.showStatistics(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (4) Link entities
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityResult> recognizeLinkedEntities(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityResult> recognizeLinkedEntities(String text, String language) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LinkedEntityResult>> recognizeLinkedEntitiesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> inputs) {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeLinkedEntities(List<String> inputs,
                                                                                      String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntities(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<LinkedEntityResult>> recognizeBatchLinkedEntities(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                recognizeLinkedEntitiesWithResponse(document, options, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    Mono<Response<DocumentResultCollection<LinkedEntityResult>>> recognizeBatchLinkedEntitiesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.entitiesLinkingWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(inputs),
            options.getModelVersion(), options.showStatistics(), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    // (5) key phrase
    // new user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> extractKeyPhrases(String text) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> extractKeyPhrases(String text, String language) {
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
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> extractKeyPhrasesWithResponse(String text, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrases(List<String> inputs)  {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrases(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractKeyPhrases(List<String> inputs, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrases(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<KeyPhraseResult>> extractBatchKeyPhrases(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> extractBatchKeyPhrasesWithResponse(inputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<KeyPhraseResult>>> extractBatchKeyPhrasesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        return client.keyPhrasesWithRestResponseAsync(new MultiLanguageBatchInput().setDocuments(document),
            options.getModelVersion(), options.showStatistics(), context).map(response ->
            new SimpleResponse<>(response, null));
    }

    // (6) sentiment
    // new user,
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TextSentimentResult> analyzeSentiment(String input) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TextSentimentResult> analyzeSentiment(String input, String language) {
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
    public Mono<Response<TextSentimentResult>> analyzeSentimentWithResponse(String input, String language) {
        return null;
    }

    // hackathon user
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs) {
//        return null;
//    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs, String language) {
        return null;
    }

    // advantage user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentiment(List<TextDocumentInput> inputs) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeBatchSentiment(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> inputs, TextAnalyticsRequestOptions options) {
//        try {
//            return withContext(context ->
//                analyzeDocumentSentimentWithResponse(document, modelVersion, showStats, context));
//        } catch (RuntimeException ex) {
//            return monoError(logger, ex);
//        }
        return null;
    }

    Mono<Response<DocumentResultCollection<TextSentimentResult>>> analyzeBatchSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate multiLanguageBatchInput
        return client.sentimentWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(document), options.getModelVersion(), options.showStatistics(),
            context).map(response -> new SimpleResponse<>(response, null));
    }
}
