// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.models.DocumentStatistics;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageInput;
import com.azure.ai.textanalytics.implementation.models.RequestStatistics;
import com.azure.ai.textanalytics.implementation.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.ErrorCodeValue;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Transforms {
    static <T> List<T> mapByIndex(List<String> textInputs, BiFunction<String, String, T> mappingFunction) {
        return IntStream.range(0, textInputs.size())
            .mapToObj(index -> mappingFunction.apply(String.valueOf(index), textInputs.get(index)))
            .collect(Collectors.toList());
    }

    static TextDocumentStatistics toTextDocumentStatistics(DocumentStatistics statistics) {
        return new TextDocumentStatistics(statistics.getCharactersCount(), statistics.getTransactionsCount());
    }

    static TextDocumentBatchStatistics toBatchStatistics(RequestStatistics statistics) {
        return new TextDocumentBatchStatistics(statistics.getDocumentsCount(), statistics.getErroneousDocumentsCount(),
            statistics.getValidDocumentsCount(), statistics.getTransactionsCount());
    }

    static com.azure.ai.textanalytics.models.TextAnalyticsError toTextAnalyticsError(
        TextAnalyticsError textAnalyticsError) {
        return new com.azure.ai.textanalytics.models.TextAnalyticsError(
            ErrorCodeValue.fromString(textAnalyticsError.getCode().toString()), textAnalyticsError.getMessage(),
            textAnalyticsError.getTarget(), textAnalyticsError.getDetails() == null ? null
            : setErrors(textAnalyticsError.getDetails()));
    }

    static List<MultiLanguageInput> toMultiLanguageInput(List<TextDocumentInput> textInputs) {
        List<MultiLanguageInput> multiLanguageInputs = new ArrayList<>();
        for (TextDocumentInput textDocumentInput : textInputs) {
            multiLanguageInputs.add(new MultiLanguageInput().setId(textDocumentInput.getId())
                .setText(textDocumentInput.getText()).setLanguage(textDocumentInput.getLanguage()));
        }
        return multiLanguageInputs;
    }

    private static List<com.azure.ai.textanalytics.models.TextAnalyticsError> setErrors(
        List<TextAnalyticsError> details) {
        List<com.azure.ai.textanalytics.models.TextAnalyticsError> detailsList = new ArrayList<>();
        for (TextAnalyticsError error : details) {
            detailsList.add(new com.azure.ai.textanalytics.models.TextAnalyticsError(
                ErrorCodeValue.fromString(error.getCode().toString()),
                error.getMessage(),
                error.getTarget(), error.getDetails() == null ? null : setErrors(error.getDetails())));
        }
        return detailsList;
    }
}
