// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DistinctDocumentQueryExecutionContextTest {

    @Test(groups = {"unit"})
    public void orderedDistinctPreservesHashAcrossEmptyPage() {
        FeedResponse<Integer> firstPage = readPage(null, null, "pageTwo", Collections.singletonList(1));
        FeedResponse<Integer> emptyPage = readPage(firstPage.getContinuationToken(), "pageTwo", "pageThree",
            Collections.singletonList(1));
        FeedResponse<Integer> lastPage = readPage(emptyPage.getContinuationToken(), "pageThree", null,
            Arrays.asList(1, 2));

        assertThat(firstPage.getResults()).containsExactly(1);
        assertThat(emptyPage.getResults()).isEmpty();
        assertThat(lastPage.getResults()).containsExactly(2);

        UInt128 firstHash = getLastHash(firstPage.getContinuationToken());
        assertThat(firstHash).isNotNull();
        assertThat(getLastHash(emptyPage.getContinuationToken())).isEqualTo(firstHash);
    }

    private static FeedResponse<Integer> readPage(String continuationToken, String expectedSourceToken,
        String nextSourceToken, List<Integer> values) {
        Map<String, String> headers = nextSourceToken == null
            ? Collections.emptyMap()
            : Collections.singletonMap(HttpConstants.HttpHeaders.CONTINUATION, nextSourceToken);
        FeedResponse<Integer> sourcePage = ModelBridgeInternal.createFeedResponse(values, headers);

        return DistinctDocumentQueryExecutionContext.<Integer>createAsync((sourceToken, params) -> {
            assertThat(sourceToken).isEqualTo(expectedSourceToken);
            return Flux.just(maxPageSize -> Flux.just(sourcePage));
        }, DistinctQueryType.ORDERED, continuationToken, null)
            .flatMap(component -> component.drainAsync(2))
            .single()
            .block();
    }

    private static UInt128 getLastHash(String continuationToken) {
        Utils.ValueHolder<DistinctContinuationToken> token = new Utils.ValueHolder<>();
        assertThat(DistinctContinuationToken.tryParse(continuationToken, token)).isTrue();
        return token.v.getLastHash();
    }
}
