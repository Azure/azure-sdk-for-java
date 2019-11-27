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
import com.azure.cs.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.cs.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.DetectedLanguageResult;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.KeyPhraseResult;
import com.azure.cs.textanalytics.models.LinkedEntityResult;
import com.azure.cs.textanalytics.models.DetectLanguageInput;
import com.azure.cs.textanalytics.models.NamedEntityResult;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.cs.textanalytics.models.TextSentimentResult;
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
    public Mono<DetectedLanguageResult> detectLanguage(String text) { return detectLanguage(text, null);}

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguageResult> detectLanguage(String text, String countryHint) {
        return detectLanguageWithResponse(text, countryHint)
            .flatMap(response ->  Mono.justOrEmpty(response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DetectedLanguageResult>> detectLanguageWithResponse(String text, String countryHint) {
        List<DetectLanguageInput> languageInputs = new ArrayList<>();
        languageInputs.add(new DetectLanguageInput(Integer.toString(0), text).setCountryHint(countryHint));
        // TODO: correct it
//        return detectLanguagesWithResponse(languageInputs, null).flatMap(
//            response -> {
//                final Iterator<DetectedLanguageResult> resultIterator = response.getValue().iterator();
//                if (resultIterator.hasNext()) {
//                    return Mono.justOrEmpty(resultIterator.next());
//                }
//                return monoError(logger, new RuntimeException(""));
//            });
        return null;
    }

    // Hackathon user
   @ServiceMethod(returns = ReturnType.SINGLE)
   public Mono<DocumentResultCollection<DetectedLanguageResult>> detectLanguages(List<String> inputs)  {
       return null;
   }

    public Mono<DocumentResultCollection<DetectedLanguageResult>> detectLanguages(List<String> inputs,
                                                                                  String countryHint) {
        List<DetectLanguageInput> languageInputs = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            languageInputs.add(new DetectLanguageInput(Integer.toString(i), inputs.get(i)).setCountryHint(countryHint));
        }

        return detectBatchLanguages(languageInputs, null);
    }

    // Advanced user
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectedLanguageResult>> detectBatchLanguages(List<DetectLanguageInput> inputs) {
        return detectBatchLanguages(inputs, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<DetectedLanguageResult>> detectBatchLanguages(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> detectBatchLanguagesWithResponse(inputs, options, context)
                .flatMap(response -> Mono.justOrEmpty(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentResultCollection<DetectedLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options) {
        try {
            return withContext(
                context -> detectBatchLanguagesWithResponse(inputs, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentResultCollection<DetectedLanguageResult>>> detectBatchLanguagesWithResponse(
        List<DetectLanguageInput> inputs, TextAnalyticsRequestOptions options, Context context) {
        // TODO: validate inputs
        final LanguageBatchInput languageBatchInput = new LanguageBatchInput().setDocuments(inputs);
        // return client.languagesWithRestResponseAsync(languageBatchInput, options.getModelVersion(),
        //     options.showStatistics(), context)
        //     .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", languageBatchInput))
        //     .doOnSuccess(response -> logger.info("A batch of detected language output - {}", languageBatchInput))
        //     .doOnError(error -> logger.warning("Failed to detected languages - {}", languageBatchInput))
        //     .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
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
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs)  {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentResultCollection<TextSentimentResult>> analyzeSentiment(List<String> inputs, String language)  {
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


//     private DocumentResultCollection toDocumentResultCollection(Object object) {
//         // TODO: add support to
// //        if (object instanceof ) {
// //
// //        }
// //       return new DocumentResultCollection();
//         return null;
//     }


}
