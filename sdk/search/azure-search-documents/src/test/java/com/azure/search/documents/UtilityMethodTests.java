// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.QueryAnswer;
import com.azure.search.documents.models.QueryCaption;
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
            Arguments.of(new SearchOptions().setAnswers(QueryAnswer.EXTRACTIVE), QueryAnswer.EXTRACTIVE.toString()),

            // Both QueryAnswer and count provided returns the concatenated string mentioned in docs.
            Arguments.of(new SearchOptions().setAnswers(QueryAnswer.EXTRACTIVE).setAnswersCount(5),
                QueryAnswer.EXTRACTIVE + "|count-5")
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
            Arguments.of(new SearchOptions().setQueryCaption(QueryCaption.EXTRACTIVE),
                QueryAnswer.EXTRACTIVE.toString()),

            // Both QueryCaption and highlight provided returns the concatenated string mentioned in docs.
            Arguments.of(new SearchOptions().setQueryCaption(QueryCaption.EXTRACTIVE).setQueryCaptionHighlight(true),
                QueryAnswer.EXTRACTIVE + "|highlight-true"),
            Arguments.of(new SearchOptions().setQueryCaption(QueryCaption.EXTRACTIVE).setQueryCaptionHighlight(false),
                QueryAnswer.EXTRACTIVE + "|highlight-false")
        );
    }
}
