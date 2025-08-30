// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * List Documents Status options.
 */
@Fluent
public final class ListDocumentStatusesOptions implements JsonSerializable<ListDocumentStatusesOptions> {
    /*
     * Format - uuid. The translation/operation Id
     */
    private final String translationId;

    /*
     * top indicates the total number of records the user wants to be returned across
     * all pages.
     * 
     * Clients MAY use top and skip query parameters to
     * specify a number of results to return and an offset into the collection.
     * When
     * both top and skip are given by a client, the server SHOULD first apply skip
     * and then top on the collection.
     * 
     * Note: If the server can't honor
     * top and/or skip, the server MUST return an error to the client informing
     * about it instead of just ignoring the query options.
     */
    private Integer top;

    /*
     * skip indicates the number of records to skip from the list of records held by
     * the server based on the sorting method specified. By default, we sort by
     * descending start time.
     * 
     * Clients MAY use top and skip query
     * parameters to specify a number of results to return and an offset into the
     * collection.
     * When both top and skip are given by a client, the server SHOULD
     * first apply skip and then top on the collection.
     * 
     * Note: If the
     * server can't honor top and/or skip, the server MUST return an error to the
     * client informing about it instead of just ignoring the query options.
     */
    private Integer skip;

    /*
     * maxPageSize is the maximum items returned in a page. If more items are
     * requested via top (or top is not specified and there are more items to be
     * returned), @nextLink will contain the link to the next page.
     * 
     * 
     * Clients MAY request server-driven paging with a specific page size by
     * specifying a maxPageSize preference. The server SHOULD honor this preference
     * if the specified page size is smaller than the server's default page size.
     */
    private Integer maxPageSize;

    /*
     * documentIds to use in filtering
     */
    private List<String> documentIds;

    /*
     * Statuses to use in filtering
     */
    private List<String> statuses;

    /*
     * the start datetime to get items after
     */
    private OffsetDateTime createdAfter;

    /*
     * the end datetime to get items before
     */
    private OffsetDateTime createdBefore;

    /*
     * the sorting query for the collection (ex: 'CreatedDateTimeUtc
     * asc','CreatedDateTimeUtc desc')
     */
    private List<String> orderBy;

    /**
     * Creates an instance of ListDocumentStatusesOptions class.
     * 
     * @param translationId the translationId value to set.
     */
    public ListDocumentStatusesOptions(String translationId) {
        this.translationId = translationId;
    }

    /**
     * Get the translationId property: Format - uuid. The translationId.
     * 
     * @return the translationId value.
     */
    public String getTranslationId() {
        return this.translationId;
    }

    /**
     * Get the top property: top indicates the total number of records the user wants to be returned across
     * all pages.
     * 
     * Clients MAY use top and skip query parameters to
     * specify a number of results to return and an offset into the collection.
     * When
     * both top and skip are given by a client, the server SHOULD first apply skip
     * and then top on the collection.
     * 
     * Note: If the server can't honor
     * top and/or skip, the server MUST return an error to the client informing
     * about it instead of just ignoring the query options.
     * 
     * @return the top value.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Set the top property: top indicates the total number of records the user wants to be returned across
     * all pages.
     * 
     * Clients MAY use top and skip query parameters to
     * specify a number of results to return and an offset into the collection.
     * When
     * both top and skip are given by a client, the server SHOULD first apply skip
     * and then top on the collection.
     * 
     * Note: If the server can't honor
     * top and/or skip, the server MUST return an error to the client informing
     * about it instead of just ignoring the query options.
     * 
     * @param top the top value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Get the skip property: skip indicates the number of records to skip from the list of records held by
     * the server based on the sorting method specified. By default, we sort by
     * descending start time.
     * 
     * Clients MAY use top and skip query
     * parameters to specify a number of results to return and an offset into the
     * collection.
     * When both top and skip are given by a client, the server SHOULD
     * first apply skip and then top on the collection.
     * 
     * Note: If the
     * server can't honor top and/or skip, the server MUST return an error to the
     * client informing about it instead of just ignoring the query options.
     * 
     * @return the skip value.
     */
    public Integer getSkip() {
        return this.skip;
    }

    /**
     * Set the skip property: skip indicates the number of records to skip from the list of records held by
     * the server based on the sorting method specified. By default, we sort by
     * descending start time.
     * 
     * Clients MAY use top and skip query
     * parameters to specify a number of results to return and an offset into the
     * collection.
     * When both top and skip are given by a client, the server SHOULD
     * first apply skip and then top on the collection.
     * 
     * Note: If the
     * server can't honor top and/or skip, the server MUST return an error to the
     * client informing about it instead of just ignoring the query options.
     * 
     * @param skip the skip value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Get the maxPageSize property: maxPageSize is the maximum items returned in a page. If more items are
     * requested via top (or top is not specified and there are more items to be
     * returned), &#064;nextLink will contain the link to the next page.
     * 
     * 
     * Clients MAY request server-driven paging with a specific page size by
     * specifying a maxPageSize preference. The server SHOULD honor this preference
     * if the specified page size is smaller than the server's default page size.
     * 
     * @return the maxPageSize value.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }

    /**
     * Set the maxPageSize property: maxPageSize is the maximum items returned in a page. If more items are
     * requested via top (or top is not specified and there are more items to be
     * returned), &#064;nextLink will contain the link to the next page.
     * 
     * 
     * Clients MAY request server-driven paging with a specific page size by
     * specifying a maxPageSize preference. The server SHOULD honor this preference
     * if the specified page size is smaller than the server's default page size.
     * 
     * @param maxPageSize the maxPageSize value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Get the documentIds property: documentIds to use in filtering.
     * 
     * @return the documentIds value.
     */
    public List<String> getDocumentIds() {
        return this.documentIds == null ? null : new ArrayList<>(this.documentIds);
    }

    /**
     * Set the documentIds property: documentIds to use in filtering.
     * 
     * @param documentIds the documentIds value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setDocumentIds(List<String> documentIds) {
        this.documentIds = documentIds == null ? null : new ArrayList<>(documentIds);
        return this;
    }

    /**
     * Get the statuses property: Statuses to use in filtering.
     * 
     * @return the statuses value.
     */
    public List<String> getStatuses() {
        return this.statuses == null ? null : new ArrayList<>(this.statuses);
    }

    /**
     * Set the statuses property: Statuses to use in filtering.
     * 
     * @param statuses the statuses value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setStatuses(List<String> statuses) {
        this.statuses = statuses == null ? null : new ArrayList<>(statuses);
        return this;
    }

    /**
     * Get the createdAfter property: the start datetime to get items after.
     * 
     * @return the createdAfter value.
     */
    public OffsetDateTime getCreatedAfter() {
        return this.createdAfter;
    }

    /**
     * Set the createdAfter property: the start datetime to get items after.
     * 
     * @param createdAfter the createdAfter value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setCreatedAfter(OffsetDateTime createdAfter) {
        this.createdAfter = createdAfter;
        return this;
    }

    /**
     * Get the createdBefore property: the end datetime to get items before.
     * 
     * @return the createdBefore value.
     */
    public OffsetDateTime getCreatedBefore() {
        return this.createdBefore;
    }

    /**
     * Set the createdBefore property: the end datetime to get items before.
     * 
     * @param createdBefore the createdBefore value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setCreatedBefore(OffsetDateTime createdBefore) {
        this.createdBefore = createdBefore;
        return this;
    }

    /**
     * Get the orderBy property: the sorting query for the collection (ex: 'CreatedDateTimeUtc asc','CreatedDateTimeUtc
     * desc').
     * 
     * @return the orderBy value.
     */
    public List<String> getOrderBy() {
        return this.orderBy == null ? null : new ArrayList<>(this.orderBy);
    }

    /**
     * Set the orderBy property: the sorting query for the collection (ex: 'CreatedDateTimeUtc asc','CreatedDateTimeUtc
     * desc').
     * 
     * @param orderBy the orderBy value to set.
     * @return the ListDocumentStatusesOptions object itself.
     */
    public ListDocumentStatusesOptions setOrderBy(List<String> orderBy) {
        this.orderBy = orderBy == null ? null : new ArrayList<>(orderBy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("translationId", this.translationId);
        jsonWriter.writeNumberField("top", this.top);
        jsonWriter.writeNumberField("skip", this.skip);
        jsonWriter.writeNumberField("maxPageSize", this.maxPageSize);
        jsonWriter.writeArrayField("documentIds", this.documentIds, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("statuses", this.statuses, (writer, element) -> writer.writeString(element));
        jsonWriter.writeStringField("createdAfter",
            this.createdAfter == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.createdAfter));
        jsonWriter.writeStringField("createdBefore",
            this.createdBefore == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.createdBefore));
        jsonWriter.writeArrayField("orderBy", this.orderBy, (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ListDocumentStatusesOptions from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ListDocumentStatusesOptions if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ListDocumentStatusesOptions.
     */
    public static ListDocumentStatusesOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String translationId = null;
            Integer top = null;
            Integer skip = null;
            Integer maxPageSize = null;
            List<String> documentIds = null;
            List<String> statuses = null;
            OffsetDateTime createdAfter = null;
            OffsetDateTime createdBefore = null;
            List<String> orderBy = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("translationId".equals(fieldName)) {
                    translationId = reader.getString();
                } else if ("top".equals(fieldName)) {
                    top = reader.getNullable(JsonReader::getInt);
                } else if ("skip".equals(fieldName)) {
                    skip = reader.getNullable(JsonReader::getInt);
                } else if ("maxPageSize".equals(fieldName)) {
                    maxPageSize = reader.getNullable(JsonReader::getInt);
                } else if ("documentIds".equals(fieldName)) {
                    documentIds = reader.readArray(reader1 -> reader1.getString());
                } else if ("statuses".equals(fieldName)) {
                    statuses = reader.readArray(reader1 -> reader1.getString());
                } else if ("createdAfter".equals(fieldName)) {
                    createdAfter = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("createdBefore".equals(fieldName)) {
                    createdBefore = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("orderBy".equals(fieldName)) {
                    orderBy = reader.readArray(reader1 -> reader1.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ListDocumentStatusesOptions deserializedListDocumentStatusesOptions
                = new ListDocumentStatusesOptions(translationId);
            deserializedListDocumentStatusesOptions.top = top;
            deserializedListDocumentStatusesOptions.skip = skip;
            deserializedListDocumentStatusesOptions.maxPageSize = maxPageSize;
            deserializedListDocumentStatusesOptions.documentIds = documentIds;
            deserializedListDocumentStatusesOptions.statuses = statuses;
            deserializedListDocumentStatusesOptions.createdAfter = createdAfter;
            deserializedListDocumentStatusesOptions.createdBefore = createdBefore;
            deserializedListDocumentStatusesOptions.orderBy = orderBy;

            return deserializedListDocumentStatusesOptions;
        });
    }
}
