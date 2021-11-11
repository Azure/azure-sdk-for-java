// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.QueryAnswerType;
import com.azure.search.documents.models.QueryCaptionType;
import com.azure.search.documents.models.SearchOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test general utility methods.
 */
public class UtilityMethodTests {
    @ParameterizedTest
    @MethodSource("createSearchRequestAnswersTestsSupplier")
    public void createSearchRequestAnswersTests(SearchOptions searchOptions, String expected) {
        assertEquals(expected, SearchAsyncClient.createSearchRequestAnswers(searchOptions));
    }

    private static Stream<Arguments> createSearchRequestAnswersTestsSupplier() {
        return Stream.of(
            // No QueryAnswer provided returns null.
            Arguments.of(new SearchOptions(), null),

            // Only QueryAnswer provided returns the string value of QueryAnswer.
            Arguments.of(new SearchOptions().setAnswers(QueryAnswerType.EXTRACTIVE),
                QueryAnswerType.EXTRACTIVE.toString()),

            // Both QueryAnswer and count provided returns the concatenated string mentioned in docs.
            Arguments.of(new SearchOptions().setAnswers(QueryAnswerType.EXTRACTIVE).setAnswersCount(5),
                QueryAnswerType.EXTRACTIVE + "|count-5")
        );
    }

    @ParameterizedTest
    @MethodSource("createSearchRequestCaptionsTestsSupplier")
    public void createSearchRequestCaptionsTests(SearchOptions searchOptions, String expected) {
        assertEquals(expected, SearchAsyncClient.createSearchRequestCaptions(searchOptions));
    }

    private static Stream<Arguments> createSearchRequestCaptionsTestsSupplier() {
        return Stream.of(
            // No QueryCaption provided returns null.
            Arguments.of(new SearchOptions(), null),

            // Only QueryCaption provided returns the string value of QueryCaption.
            Arguments.of(new SearchOptions().setQueryCaption(QueryCaptionType.EXTRACTIVE),
                QueryAnswerType.EXTRACTIVE.toString()),

            // Both QueryCaption and highlight provided returns the concatenated string mentioned in docs.
            Arguments.of(new SearchOptions().setQueryCaption(QueryCaptionType.EXTRACTIVE)
                .setQueryCaptionHighlightEnabled(true), QueryAnswerType.EXTRACTIVE + "|highlight-true"),
            Arguments.of(new SearchOptions().setQueryCaption(QueryCaptionType.EXTRACTIVE)
                .setQueryCaptionHighlightEnabled(false), QueryAnswerType.EXTRACTIVE + "|highlight-false")
        );
    }
}
