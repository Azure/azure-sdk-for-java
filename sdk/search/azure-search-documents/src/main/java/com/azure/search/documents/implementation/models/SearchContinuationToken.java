// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.models;

import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.util.SearchPagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static com.azure.search.documents.implementation.util.Utility.getDefaultSerializerAdapter;

/**
 * Serialization and deserialization of search page continuation token.
 */
public final class SearchContinuationToken {
    /**
     * Api version which is used by continuation token.
     */
    public static final String API_VERSION = "apiVersion";

    /**
     * Next link which is used by continuation token.
     */
    public static final String NEXT_LINK = "nextLink";

    /**
     * Next page parameters which is used by continuation token.
     */
    public static final String NEXT_PAGE_PARAMETERS = "nextPageParameters";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SearchContinuationToken() {
    }

    /**
     * Serialize to search continuation token using {@code apiVersion}, {@code nextLink} and {@link SearchRequest}
     *
     * @param apiVersion The api version string.
     * @param nextLink The next link from search document result. {@link SearchDocumentsResult}
     * @param nextPageParameters {@link SearchRequest} The next page parameters which use to fetch next page.
     * @return The search encoded continuation token.
     */
    public static String serializeToken(String apiVersion, String nextLink, SearchRequest nextPageParameters) {
        Objects.requireNonNull(apiVersion);
        if (nextLink == null || nextPageParameters == null || nextPageParameters.getSkip() == null) {
            return null;
        }

        String nextParametersString;
        try {
            nextParametersString = getDefaultSerializerAdapter().serialize(nextPageParameters, SerializerEncoding.JSON);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to serialize the search request.");
        }

        ObjectNode tokenJson = MAPPER.createObjectNode();
        tokenJson.put(API_VERSION, apiVersion);
        tokenJson.put(NEXT_LINK, nextLink);
        tokenJson.put(NEXT_PAGE_PARAMETERS, nextParametersString);

        return Base64.getEncoder().encodeToString(tokenJson.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Deserialize the continuation token to {@link SearchRequest}
     *
     * @param apiVersion The api version string.
     * @param continuationToken The continuation token from {@link SearchPagedResponse}
     * @return {@link SearchRequest} The search request used for fetching next page.
     */
    @SuppressWarnings("unchecked")
    public static SearchRequest deserializeToken(String apiVersion, String continuationToken) {
        try {
            String decodedToken = new String(Base64.getDecoder().decode(continuationToken), StandardCharsets.UTF_8);
            Map<String, String> tokenFields = MAPPER.readValue(decodedToken, Map.class);
            if (!apiVersion.equals(tokenFields.get(API_VERSION))) {
                throw new IllegalStateException("Continuation token uses invalid apiVersion" + apiVersion);
            }
            return getDefaultSerializerAdapter().deserialize(tokenFields.get(NEXT_PAGE_PARAMETERS), SearchRequest.class,
                SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new IllegalArgumentException("The continuation token is invalid. Token: " + continuationToken);
        }
    }
}
