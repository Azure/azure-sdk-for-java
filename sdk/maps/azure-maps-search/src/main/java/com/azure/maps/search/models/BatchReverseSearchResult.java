// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** This object is returned from a successful Search Address Batch service call. */
public final class BatchReverseSearchResult implements JsonSerializable<BatchReverseSearchResult> {
    private final BatchResultSummary batchSummary;
    private final List<ReverseSearchAddressBatchItem> batchItems;
    private String batchId;

    /**
     * Creates a new {@link BatchReverseSearchResult} with default properties.
     */
    public BatchReverseSearchResult() {
        this.batchSummary = null;
        this.batchItems = null;
    }

    /**
     * Creates a new {@link BatchReverseSearchResult} with a summary and batch items.
     *
     * @param batchSummary the summary of this batch's search results.
     * @param batchItems the items returned in this search.
     */
    public BatchReverseSearchResult(BatchResultSummary batchSummary, List<ReverseSearchAddressBatchItem> batchItems) {
        this.batchSummary = batchSummary;
        this.batchItems = batchItems;
    }

    /**
     * Get the batchSummary property: Summary of the results for the batch request.
     *
     * @return the batchSummary value.
     */
    public BatchResultSummary getBatchSummary() {
        return this.batchSummary;
    }

    /**
     * Get the batchItems property: Array containing the batch results.
     *
     * @return the batchItems value.
     */
    public List<ReverseSearchAddressBatchItem> getBatchItems() {
        return this.batchItems;
    }

    /**
     * Return this id for this batch. Only available when the batch is cached.
     * @return the batch id
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the if of this batch.
     *
     * @param batchId the id of this batch, retrieved from the asynchronous API.
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("batchSummary", batchSummary);
        jsonWriter.writeArrayField("batchItems", batchItems, JsonWriter::writeJson);
        jsonWriter.writeStringField("batchId", batchId);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    /**
     * Reads an instance of BatchReverseSearchResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BatchReverseSearchResult if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BatchReverseSearchResult.
     */
    public static BatchReverseSearchResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BatchResultSummary batchSummary = null;
            List<ReverseSearchAddressBatchItem> batchItems = null;
            String batchId = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("batchSummary".equals(fieldName)) {
                    batchSummary = BatchResultSummary.fromJson(reader);
                } else if ("batchItems".equals(fieldName)) {
                    batchItems = reader.readArray(ReverseSearchAddressBatchItem::fromJson);
                } else if ("batchId".equals(fieldName)) {
                    batchId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            BatchReverseSearchResult batchReverseSearchResult = new BatchReverseSearchResult(batchSummary, batchItems);
            batchReverseSearchResult.setBatchId(batchId);

            return batchReverseSearchResult;
        });
    }
}
