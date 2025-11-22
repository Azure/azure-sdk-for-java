package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedResponseBuilderForJsonNode<T extends JsonNode> {
    private final boolean isChangeFeed;
    private final Class<T> klass;

    private Map<String, String> headers = new HashMap<>();
    private boolean noMoreChangesInChangeFeed = false;
    private List<T> results;

    private FeedResponseBuilderForJsonNode(Class<T> klass, boolean isChangeFeed) {
        this.klass = klass;
        this.isChangeFeed = isChangeFeed;
    }

    public FeedResponseBuilderForJsonNode<T> withContinuationToken(String continuationToken) {

        if (isChangeFeed) {
            headers.put(HttpConstants.HttpHeaders.E_TAG, continuationToken);
        } else {
            headers.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
        }
        return this;
    }

    public FeedResponseBuilderForJsonNode<T> withResults(List<T> results) {
        this.results = results;
        return this;
    }


    // https://stackoverflow.com/a/47949197
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final FeedResponseBuilderForJsonNode<T> withResults(T... results) {
        this.results = Arrays.<T>asList(results);
        return this;
    }

    public FeedResponseBuilderForJsonNode<T> lastChangeFeedPage() {
        this.noMoreChangesInChangeFeed = true;
        return this;
    }

    public FeedResponse<T> build() {
        RxDocumentServiceResponse rsp = mock(RxDocumentServiceResponse.class);
        when(rsp.getResponseHeaders()).thenReturn(headers);
        when(rsp.getQueryResponse(null, klass)).thenReturn(results);
        if (isChangeFeed) {
            when(rsp.getStatusCode()).thenReturn(noMoreChangesInChangeFeed?
                HttpConstants.StatusCodes.NOT_MODIFIED : 200);
            return ImplementationBridgeHelpers
                .FeedResponseHelper
                .getFeedResponseAccessor()
                .createChangeFeedResponse(rsp, null, klass);
        } else {
            return ImplementationBridgeHelpers
                .FeedResponseHelper
                .getFeedResponseAccessor()
                .createFeedResponse(rsp, null, klass);
        }
    }

    public static <T extends JsonNode> FeedResponseBuilderForJsonNode<T> queryFeedResponseBuilder(Class<T> klass) {
        return new FeedResponseBuilderForJsonNode<T>(klass, false);
    }

    public static <T extends JsonNode> FeedResponseBuilderForJsonNode<T> changeFeedResponseBuilder(Class<T> klass) {
        return new FeedResponseBuilderForJsonNode<T>(klass, true);
    }
}
