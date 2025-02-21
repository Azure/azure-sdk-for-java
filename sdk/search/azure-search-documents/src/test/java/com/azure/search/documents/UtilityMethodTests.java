// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.QueryAnswer;
import com.azure.search.documents.models.QueryAnswerType;
import com.azure.search.documents.models.QueryCaption;
import com.azure.search.documents.models.QueryCaptionType;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test general utility methods.
 */
@Execution(ExecutionMode.CONCURRENT)
public class UtilityMethodTests {
    @ParameterizedTest
    @MethodSource("createSearchRequestAnswersTestsSupplier")
    public void createSearchRequestAnswersTests(QueryAnswer queryAnswer, String expected) {
        assertEquals(expected, SearchAsyncClient.createSearchRequestAnswers(queryAnswer));
    }

    static Stream<Arguments> createSearchRequestAnswersTestsSupplier() {
        return Stream.of(
            // No QueryAnswer provided returns null.
            Arguments.of(null, null),

            // None returns none
            Arguments.of(new QueryAnswer(QueryAnswerType.NONE), QueryAnswerType.NONE.toString()),

            // Only QueryAnswer provided returns the string value of QueryAnswer.
            Arguments.of(new QueryAnswer(QueryAnswerType.EXTRACTIVE), QueryAnswerType.EXTRACTIVE.toString()),

            // Both QueryAnswer and count provided returns the concatenated string mentioned in docs.
            Arguments.of(new QueryAnswer(QueryAnswerType.EXTRACTIVE).setCount(5),
                QueryAnswerType.EXTRACTIVE + "|count-5"),

            Arguments.of(new QueryAnswer(QueryAnswerType.EXTRACTIVE).setThreshold(0.7),
                QueryAnswerType.EXTRACTIVE + "|threshold-0.7"),

            Arguments.of(new QueryAnswer(QueryAnswerType.EXTRACTIVE).setCount(5).setThreshold(0.7),
                QueryAnswerType.EXTRACTIVE + "|count-5,threshold-0.7"));
    }

    @ParameterizedTest
    @MethodSource("createSearchRequestCaptionsTestsSupplier")
    public void createSearchRequestCaptionsTests(QueryCaption queryCaption, String expected) {
        assertEquals(expected, SearchAsyncClient.createSearchRequestCaptions(queryCaption));
    }

    static Stream<Arguments> createSearchRequestCaptionsTestsSupplier() {
        return Stream.of(
            // No QueryCaption provided returns null.
            Arguments.of(null, null),

            // None returns none
            Arguments.of(new QueryCaption(QueryCaptionType.NONE), QueryCaptionType.NONE.toString()),

            // Only QueryCaption provided returns the string value of QueryCaption.
            Arguments.of(new QueryCaption(QueryCaptionType.EXTRACTIVE), QueryAnswerType.EXTRACTIVE.toString()),

            // Both QueryCaption and highlight provided returns the concatenated string mentioned in docs.
            Arguments.of(new QueryCaption(QueryCaptionType.EXTRACTIVE).setHighlightEnabled(true),
                QueryAnswerType.EXTRACTIVE + "|highlight-true"),
            Arguments.of(new QueryCaption(QueryCaptionType.EXTRACTIVE).setHighlightEnabled(false),
                QueryAnswerType.EXTRACTIVE + "|highlight-false"));
    }
}
