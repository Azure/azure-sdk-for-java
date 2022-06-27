// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.models;

import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.DefaultJsonReader;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.util.SearchPagedResponse;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Objects;

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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonWriter jsonWriter = DefaultJsonWriter.fromStream(outputStream);
        jsonWriter.writeStartObject()
            .writeStringField(API_VERSION, apiVersion)
            .writeStringField(NEXT_LINK, nextLink)
            .writeJsonField(NEXT_PAGE_PARAMETERS, nextPageParameters)
            .writeEndObject()
            .flush();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * Deserialize the continuation token to {@link SearchRequest}
     *
     * @param apiVersion The api version string.
     * @param continuationToken The continuation token from {@link SearchPagedResponse}
     * @return {@link SearchRequest} The search request used for fetching next page.
     */
    public static SearchRequest deserializeToken(String apiVersion, String continuationToken) {
        return JsonUtils.readObject(DefaultJsonReader.fromBytes(Base64.getDecoder().decode(continuationToken)),
            reader -> {
                String version = null;
                SearchRequest token = null;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if (API_VERSION.equals(fieldName)) {
                        version = reader.getStringValue();
                    } else if (NEXT_PAGE_PARAMETERS.equals(fieldName)) {
                        token = SearchRequest.fromJson(reader);
                    } else {
                        reader.skipChildren();
                    }
                }

                if (!Objects.equals(apiVersion, version)) {
                    throw new IllegalStateException("Continuation token uses invalid apiVersion" + apiVersion);
                }

                return token;
            });
    }
}
