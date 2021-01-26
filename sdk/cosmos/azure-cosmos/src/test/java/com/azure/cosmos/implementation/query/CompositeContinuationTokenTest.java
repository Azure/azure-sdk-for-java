// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeContinuationTokenTest {
    @Test(groups = { "unit" })
    public void compositeContinuationToken_canStillParseStringifiedRange() {

        String continuationDummy = UUID.randomUUID().toString();
        Range<String> range = new Range<>("AA", "BB", true, false);
        CompositeContinuationToken expectedContinuation =
            new CompositeContinuationToken(continuationDummy, range);

        String jsonWithStringifiedRange = String.format(
            "{\"token\":\"%s\",\"range\":\"{\\\"min\\\":\\\"AA\\\",\\\"max\\\":\\\"BB\\\"}\"}",
            continuationDummy);

        Utils.ValueHolder<CompositeContinuationToken> outParsedContinuation =
            new Utils.ValueHolder<>();
        assertThat(CompositeContinuationToken.tryParse(jsonWithStringifiedRange,
            outParsedContinuation))
            .isTrue();

        assertThat(outParsedContinuation.v)
            .isNotNull()
            .isInstanceOf(CompositeContinuationToken.class);

        CompositeContinuationToken continuationDeserialized = outParsedContinuation.v;
        assertThat(continuationDeserialized.toJson()).isEqualTo(expectedContinuation.toJson());
    }

    @Test(groups = { "unit" })
    public void compositeContinuationToken_toJsonFromJson() {

        String continuationDummy = UUID.randomUUID().toString();
        Range<String> range = new Range<>("AA", "BB", true, false);
        CompositeContinuationToken continuation =
            new CompositeContinuationToken(continuationDummy, range);

        String representation = continuation.toJson();
        assertThat(representation)
            .isEqualTo(
                String.format(
                    "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}",
                    continuationDummy));

        Utils.ValueHolder<CompositeContinuationToken> outParsedContinuation =
            new Utils.ValueHolder<>();
        assertThat(CompositeContinuationToken.tryParse(representation, outParsedContinuation))
            .isTrue();

        assertThat(outParsedContinuation.v)
            .isNotNull()
            .isInstanceOf(CompositeContinuationToken.class);

        CompositeContinuationToken continuationDeserialized = outParsedContinuation.v;

        String representationAfterDeserialization = continuationDeserialized.toJson();
        assertThat(representationAfterDeserialization).isEqualTo(representation);
    }
}
