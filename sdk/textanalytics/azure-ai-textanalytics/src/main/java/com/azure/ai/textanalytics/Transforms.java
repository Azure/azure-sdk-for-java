// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.models.DocumentStatistics;
import com.azure.ai.textanalytics.implementation.models.LanguageInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.TextAnalyticsErrorCode;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextDocumentInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Helper class to convert service level models to SDK exposes models.
 */
final class Transforms {
    private Transforms() {
    }

    /**
     * Given a list of documents will apply the indexing function to it and return the updated list.
     *
     * @param documents the inputs to apply the mapping function to.
     * @param mappingFunction the function which applies the index to the incoming input value.
     * @param <T> the type of items being returned in the list.
     * @return The list holding all the generic items combined.
     */
    static <T> List<T> mapByIndex(Iterable<String> documents, BiFunction<String, String, T> mappingFunction) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        AtomicInteger i = new AtomicInteger(0);
        List<T> result = new ArrayList<>();
        documents.forEach(document ->
            result.add(mappingFunction.apply(String.valueOf(i.getAndIncrement()), document))
        );
        return result;
    }

    /**
     * Convert {@link DocumentStatistics} to {@link TextDocumentStatistics}
     *
     * @param statistics the {@link DocumentStatistics} provided by the service.
     * @return the {@link TextDocumentStatistics} returned by the SDK.
     */
    static TextDocumentStatistics toTextDocumentStatistics(DocumentStatistics statistics) {
        return new TextDocumentStatistics(statistics.getCharactersCount(), statistics.getTransactionsCount());
    }

    /**
     * Convert {@link RequestStatistics} to {@link TextDocumentBatchStatistics}
     *
     * @param statistics the {@link RequestStatistics} provided by the service.
     * @return the {@link TextDocumentBatchStatistics} returned by the SDK.
     */
    static TextDocumentBatchStatistics toBatchStatistics(RequestStatistics statistics) {
        return new TextDocumentBatchStatistics(statistics.getDocumentsCount(), statistics.getValidDocumentsCount(),
            statistics.getErroneousDocumentsCount(), statistics.getTransactionsCount());
    }

    /**
     * Convert {@link TextAnalyticsError} to {@link com.azure.ai.textanalytics.models.TextAnalyticsError}
     * This function maps the service returned {@link TextAnalyticsError inner error} to the top level
     * {@link com.azure.ai.textanalytics.models.TextAnalyticsError error}, if inner error present.
     *
     * @param textAnalyticsError the {@link TextAnalyticsError} returned by the service.
     * @return the {@link com.azure.ai.textanalytics.models.TextAnalyticsError} returned by the SDK.
     */
    static com.azure.ai.textanalytics.models.TextAnalyticsError toTextAnalyticsError(
        TextAnalyticsError textAnalyticsError) {
        if (textAnalyticsError.getInnererror() == null) {
            return new com.azure.ai.textanalytics.models.TextAnalyticsError(
                TextAnalyticsErrorCode.fromString(textAnalyticsError.getCode().toString()),
                textAnalyticsError.getMessage(),
                textAnalyticsError.getTarget());
        }
        return new com.azure.ai.textanalytics.models.TextAnalyticsError(
            TextAnalyticsErrorCode.fromString(textAnalyticsError.getInnererror().getCode().toString()),
            textAnalyticsError.getInnererror().getMessage(),
            textAnalyticsError.getInnererror().getTarget());
    }

    /**
     * Convert the incoming input {@link TextDocumentInput} to the service expected {@link MultiLanguageInput}.
     *
     * @param documents the user provided input in {@link TextDocumentInput}
     * @return the service required input {@link MultiLanguageInput}
     */
    static List<MultiLanguageInput> toMultiLanguageInput(Iterable<TextDocumentInput> documents) {
        List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        for (TextDocumentInput textDocumentInput : documents) {
            multiLanguageInputs.add(new MultiLanguageInput().setId(textDocumentInput.getId())
                .setText(textDocumentInput.getText()).setLanguage(textDocumentInput.getLanguage()));
        }
        return multiLanguageInputs;
    }

    /**
     * Convert the incoming input {@link com.azure.ai.textanalytics.models.TextAnalyticsError}
     * to a {@link TextAnalyticsException}.
     *
     * @param error the {@link com.azure.ai.textanalytics.models.TextAnalyticsError}.
     * @return the {@link TextAnalyticsException} to be thrown.
     */
    static TextAnalyticsException toTextAnalyticsException(
        com.azure.ai.textanalytics.models.TextAnalyticsError error) {
        return new TextAnalyticsException(error.getMessage(), error.getErrorCode().toString(), error.getTarget());
    }

    /**
     * Convert to a list of {@link LanguageInput} from {@link DetectLanguageInput}.
     *
     * @param documents The list of documents to detect languages for.
     *
     * @return a list of {@link LanguageInput}.
     */
    static List<LanguageInput> toLanguageInput(Iterable<DetectLanguageInput> documents) {
        final List<LanguageInput> multiLanguageInputs = new ArrayList<>();
        documents.forEach(textDocumentInput -> multiLanguageInputs.add(new LanguageInput()
            .setId(textDocumentInput.getId())
            .setText(textDocumentInput.getText())
            .setCountryHint(textDocumentInput.getCountryHint())));
        return multiLanguageInputs;
    }
}
