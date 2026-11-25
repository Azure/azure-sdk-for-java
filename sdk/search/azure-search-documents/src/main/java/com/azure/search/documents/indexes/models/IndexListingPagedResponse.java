// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A page of indexes or index statistics, including the total count when returned by the service.
 *
 * @param <T> the type of items in the page.
 */
public final class IndexListingPagedResponse<T> extends PagedResponseBase<Void, T> {
    private final Long count;

    /**
     * Creates a page from the service's complete JSON response.
     *
     * @param response the HTTP response, including the listing envelope.
     * @param itemType the type used to deserialize each item.
     * @throws NullPointerException if response or itemType is null.
     * @throws UncheckedIOException if the response cannot be read.
     */
    public IndexListingPagedResponse(Response<BinaryData> response, Class<T> itemType) {
        this(response, readPage(Objects.requireNonNull(response, "'response' cannot be null.").getValue(), itemType));
    }

    private IndexListingPagedResponse(Response<BinaryData> response, PageData<T> page) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), page.values,
            page.continuationToken, null);
        this.count = page.count;
    }

    private static <T> PageData<T> readPage(BinaryData body, Class<T> itemType) {
        Objects.requireNonNull(itemType, "'itemType' cannot be null.");
        try (JsonReader jsonReader = JsonProviders.createReader(body.toString())) {
            return jsonReader.readObject(reader -> {
                List<T> values = Collections.emptyList();
                Long count = null;
                String continuationToken = null;
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("value".equals(fieldName)) {
                        List<T> items
                            = reader.readArray(item -> BinaryData.fromString(item.readChildren()).toObject(itemType));
                        values = items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
                    } else if ("@odata.count".equals(fieldName)) {
                        count = reader.getNullable(JsonReader::getLong);
                    } else if ("@odata.nextLink".equals(fieldName)) {
                        continuationToken = reader.getString();
                    } else {
                        reader.skipChildren();
                    }
                }
                return new PageData<>(values, count, continuationToken);
            });
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read the index listing response.", exception);
        }
    }

    /**
     * Gets the total count supplied by the service, which may exceed the number of items on this page.
     * This is null when the service omits the count; it is never inferred from the page size.
     * The preview listing contract does not define a count-request parameter.
     *
     * @return the total count, or null when unavailable.
     */
    public Long getCount() {
        return count;
    }

    private static final class PageData<T> {
        private final List<T> values;
        private final Long count;
        private final String continuationToken;

        private PageData(List<T> values, Long count, String continuationToken) {
            this.values = values;
            this.count = count;
            this.continuationToken = continuationToken;
        }
    }
}
