// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are, detect language, recognize entities, recognize PII entities,
 * recognize linked entities, and analyze sentiment for a text input or a list of text inputs.
 *
 * <p><strong>Instantiating a synchronous Text Analytics Client</strong></p>
 * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.instantiation}
 *
 * <p>View {@link TextAnalyticsClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see TextAnalyticsClientBuilder
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient client;

    /**
     * Create a {@code TextAnalyticsClient client} that sends requests to the Text Analytics service's endpoint.
     * Each service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link TextAnalyticsClient} that the client routes its request through.
     */
    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    /**
     * Returns the detected language and a numeric score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String}
     *
     * @param text The text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @return the {@link DetectedLanguage detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String text) {
        return detectLanguageWithResponse(text, client.getDefaultCountryHint(), Context.NONE).getValue();
    }

    /**
     * Returns a {@link Response} containing the detected language and a numeric score between zero and one.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language with http response in a text with a provided country hint.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageWithResponse#String-String-Context}
     *
     * @param text The text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DetectedLanguage detected language} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DetectedLanguage> detectLanguageWithResponse(String text, String countryHint, Context context) {
        return client.detectLanguageAsyncClient.detectLanguageWithResponse(text, countryHint, context).block();
    }

    /**
     * Detects Language for a batch of input.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the languages in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#List}
     *
     * @param textInputs The list of texts to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link DetectLanguageResult detected language} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<DetectLanguageResult> detectLanguageBatch(List<String> textInputs) {
        return detectLanguageBatchWithResponse(textInputs, client.getDefaultCountryHint(), null,
            Context.NONE).getValue();
    }

    /**
     * Detects Language for a batch of input with the provided country hint.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language with http response in a list of text with a provided country hint.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs The list of texts to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param countryHint A country hint for the entire batch. Accepts two letter country codes specified by ISO 3166-1
     * alpha-2. Defaults to "US" if not specified.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link DetectLanguageResult detected language} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectLanguageResult>> detectLanguageBatchWithResponse(
        List<String> textInputs, String countryHint, TextAnalyticsRequestOptions options, Context context) {
        return client.detectLanguageAsyncClient.detectLanguageBatchWithResponse(textInputs, countryHint, options,
            context).block();
    }

    /**
     * Detects Language for a batch of input with the provided {@link TextAnalyticsRequestOptions}.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the languages with http response in a list of {@link DetectLanguageInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatchWithResponse#List-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs The list of {@link DetectLanguageInput inputs/documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link DetectLanguageResult detected languages}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<DetectLanguageResult>> detectLanguageBatchWithResponse(
        List<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.detectLanguageAsyncClient.detectLanguageBatchWithResponse(textInputs, options, context).block();
    }

    // Categorized Entity
    /**
     * Returns a list of general categorized entities in the provided text.
     * For a list of supported entity types, check: <a href="https://aka.ms/taner"></a>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the entities of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String}
     *
     * @param text the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return the {@link PagedIterable} containing the {@link CategorizedEntity categorized entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CategorizedEntity> recognizeEntities(String text) {
        return recognizeEntities(text, client.getDefaultLanguage(), Context.NONE);
    }

    /**
     * Returns a list of general categorized entities in the provided text.
     * For a list of supported entity types, check: <a href="https://aka.ms/taner"></a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs"></a>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities with http response in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntities#String-String-Context}
     *
     * @param text the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link CategorizedEntity categorized entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CategorizedEntity> recognizeEntities(String text, String language, Context context) {
        return new PagedIterable<>(
            client.recognizeEntityAsyncClient.recognizeEntities(text, language, context));
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#List}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link RecognizeEntitiesResult categorized entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<RecognizeEntitiesResult> recognizeEntitiesBatch(List<String> textInputs) {
        return recognizeEntitiesBatchWithResponse(textInputs, client.getDefaultLanguage(), null,
            Context.NONE).getValue();
    }

    /**
     * Returns a list of general categorized entities for the provided list of texts.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities with http response in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of texts to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeEntitiesResult categorized entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<RecognizeEntitiesResult>> recognizeEntitiesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeEntityAsyncClient.recognizeEntitiesWithResponse(textInputs, language, options,
            context).block();
    }

    /**
     * Returns a list of general categorized entities for the provided list of text inputs.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeEntitiesResult categorized entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<RecognizeEntitiesResult>> recognizeEntitiesBatchWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeEntityAsyncClient.recognizeEntitiesBatchWithResponse(textInputs, options,
            context).block();
    }

    // PII Entities
    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a> PII.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the PII entities of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String}
     *
     * @param text the text to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @return A {@link PagedIterable} containing the {@link PiiEntity PII entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PiiEntity> recognizePiiEntities(String text) {
        return recognizePiiEntities(text, client.getDefaultLanguage(), Context.NONE);
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the text.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a>
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities with http response in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-Context}
     *
     * @param text the text to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link PiiEntity PII entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PiiEntity> recognizePiiEntities(String text, String language, Context context) {
        return new PagedIterable<>(client.recognizePiiEntityAsyncClient.recognizePiiEntities(text, language, context));
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check https://aka.ms/tanerpii.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#List}
     *
     * @param textInputs A list of text to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link DocumentResultCollection batch} of the {@link RecognizePiiEntitiesResult PII entity}
     * of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<RecognizePiiEntitiesResult> recognizePiiEntitiesBatch(List<String> textInputs) {
        return recognizePiiEntitiesBatchWithResponse(textInputs, client.getDefaultLanguage(), null,
            Context.NONE).getValue();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the list of texts.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a>.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities with http response in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of text to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizePiiEntitiesResult PII entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizePiiEntitiesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizePiiEntityAsyncClient.recognizePiiEntitiesWithResponse(textInputs, language,
            options, context).block();
    }

    /**
     * Returns a list of personal information entities ("SSN", "Bank Account", etc) in the batch of document inputs.
     * For the list of supported entity types, check <a href="https://aka.ms/tanerpii"></a>.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeEntitiesResult PII entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<RecognizePiiEntitiesResult>> recognizePiiEntitiesBatchWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizePiiEntityAsyncClient.recognizePiiEntitiesBatchWithResponse(textInputs, options,
            context).block();
    }

    // Linked Entities
    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the linked entities of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String}
     *
     * @param text the text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link PagedIterable} containing the {@link LinkedEntity linked entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text) {
        return recognizeLinkedEntities(text, client.getDefaultLanguage(), Context.NONE);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided text.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities with http response in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String-Context}
     *
     * @param text the text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link LinkedEntity linked entities} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language, Context context) {
        return new PagedIterable<>(
            client.recognizeLinkedEntityAsyncClient.recognizeLinkedEntities(text, language, context));
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#List}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity} of the text.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizeLinkedEntitiesBatch(
        List<String> textInputs) {
        return recognizeLinkedEntitiesBatchWithResponse(textInputs, client.getDefaultLanguage(), null,
            Context.NONE).getValue();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of texts.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities with http response in a list of text with a provided language representation.
     * </p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of text to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link RecognizeLinkedEntitiesResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesWithResponse(textInputs, language,
            options, context).block();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of inputs.
     * See <a href="https://aka.ms/talangs"></a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse#List-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link RecognizeLinkedEntitiesResult linked entity}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>> recognizeLinkedEntitiesBatchWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatchWithResponse(textInputs, options,
            context).block();
    }

    // Key Phrase
    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link PagedIterable} containing the key phrases of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> extractKeyPhrases(String text) {
        return extractKeyPhrases(text, client.getDefaultLanguage(), Context.NONE);
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases with http response in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the key phrases of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> extractKeyPhrases(String text, String language, Context context) {
        return new PagedIterable<>(client.extractKeyPhraseAsyncClient.extractKeyPhrases(text, language, context));
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#List}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @return A {@link DocumentResultCollection batch} of the {@link ExtractKeyPhraseResult key phrases} of the text.
     *
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<ExtractKeyPhraseResult> extractKeyPhrasesBatch(List<String> textInputs) {
        return extractKeyPhrasesBatchWithResponse(textInputs, client.getDefaultLanguage(), null,
            Context.NONE).getValue();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases with http response in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of the
     * {@link ExtractKeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<ExtractKeyPhraseResult>> extractKeyPhrasesBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
        return client.extractKeyPhraseAsyncClient.extractKeyPhrasesWithResponse(textInputs, language, options,
            context).block();
    }

    /**
     * Returns a list of strings denoting the key phrases in the input text.
     * See <a href="https://aka.ms/talangs"></a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatchWithResponse#List-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link DocumentResultCollection batch} of {@link ExtractKeyPhraseResult key phrases}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<ExtractKeyPhraseResult>> extractKeyPhrasesBatchWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.extractKeyPhraseAsyncClient.extractKeyPhrasesBatchWithResponse(textInputs, options,
            context).block();
    }

    // Sentiment
    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments of single input text</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return the {@link DocumentSentiment document sentiment} of the text.
     *
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String text) {
        return analyzeSentimentWithResponse(text, client.getDefaultLanguage(), Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within i
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments with http response in a text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentWithResponse#String-String-Context}
     *
     * @param text the text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentSentiment document sentiment} of the text.
     * @throws NullPointerException if {@code text} is {@code null}.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentSentiment> analyzeSentimentWithResponse(String text, String language, Context context) {
        return client.analyzeSentimentAsyncClient.analyzeSentimentWithResponse(text, language, context).block();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a list of text.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#List}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     *
     * @return A {@link DocumentResultCollection batch} containing the list of
     * {@link AnalyzeSentimentResult text sentiments} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentResultCollection<AnalyzeSentimentResult> analyzeSentimentBatch(List<String> textInputs) {
        return analyzeSentimentBatchWithResponse(textInputs, client.getDefaultLanguage(), null,
            Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments with http response in a list of text with a provided language representation.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatchWithResponse#List-String-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of text to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link AnalyzeSentimentResult text sentiments} with their numeric scores.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<AnalyzeSentimentResult>> analyzeSentimentBatchWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
        return client.analyzeSentimentAsyncClient.analyzeSentimentWithResponse(textInputs, language, options,
            context).block();
    }

    /**
     * Returns a sentiment prediction, as well as sentiment scores for each sentiment class
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments with http response in a list of {@link TextDocumentInput}.</p>
     * {@codesnippet com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatchWithResponse#List-TextAnalyticsRequestOptions-Context}
     *
     * @param textInputs A list of {@link TextDocumentInput inputs/documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits"/>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link DocumentResultCollection batch} of
     * {@link AnalyzeSentimentResult text sentiments}.
     * @throws NullPointerException if {@code textInputs} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentResultCollection<AnalyzeSentimentResult>> analyzeSentimentBatchWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        return client.analyzeSentimentAsyncClient.analyzeSentimentBatchWithResponse(textInputs, options,
            context).block();
    }
}
