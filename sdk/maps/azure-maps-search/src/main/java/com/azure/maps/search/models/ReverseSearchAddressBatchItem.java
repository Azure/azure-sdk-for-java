// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import com.azure.core.models.ResponseError;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.maps.search.implementation.helpers.ReverseSearchAddressBatchItemPropertiesHelper;

import java.io.IOException;

/** An item returned from Search Address Batch service call. */
public final class ReverseSearchAddressBatchItem implements JsonSerializable<ReverseSearchAddressBatchItem> {
    private Integer statusCode;
    private ResponseError error;
    private ReverseSearchAddressResult result;

    static {
        ReverseSearchAddressBatchItemPropertiesHelper.setAccessor(
                new ReverseSearchAddressBatchItemPropertiesHelper.ReverseSearchAddressBatchItemAccessor() {
                    @Override
                    public void setErrorDetail(ReverseSearchAddressBatchItem item, ResponseError detail) {
                        item.setErrorDetail(detail);
                    }

                    @Override
                    public void setReverseSearchAddressResult(ReverseSearchAddressBatchItem item,
                        ReverseSearchAddressResult result) {
                        item.setReverseSearchAddressResult(result);
                    }

                    @Override
                    public void setStatusCode(ReverseSearchAddressBatchItem item, Integer statusCode) {
                        item.setStatusCode(statusCode);
                    }
                });
    }

    /**
     * Creates an instance of {@link ReverseSearchAddressBatchItem}.
     */
    public ReverseSearchAddressBatchItem() {
    }

    /**
     * Get the statusCode property: HTTP request status code.
     *
     * @return the statusCode value.
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Get the error property: The error object.
     *
     * @return the error value.
     */
    public ResponseError getError() {
        return this.error;
    }

    /**
     * Results of this search.
     * @return the results of this search.
     */
    public ReverseSearchAddressResult getResult() {
        return result;
    }

    // private setters
    private void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    private void setErrorDetail(ResponseError error) {
        this.error = error;
    }

    private void setReverseSearchAddressResult(ReverseSearchAddressResult result) {
        this.result = result;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeNumberField("statusCode", statusCode);
        jsonWriter.writeJsonField("error", error);
        jsonWriter.writeJsonField("result", result);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    /**
     * Reads an instance of ReverseSearchAddressBatchItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ReverseSearchAddressBatchItem if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ReverseSearchAddressBatchItem.
     */
    public static ReverseSearchAddressBatchItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ReverseSearchAddressBatchItem reverseSearchAddressBatchItem = new ReverseSearchAddressBatchItem();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("statusCode".equals(fieldName)) {
                    reverseSearchAddressBatchItem.statusCode = reader.getNullable(JsonReader::getInt);
                } else if ("error".equals(fieldName)) {
                    reverseSearchAddressBatchItem.error = ResponseError.fromJson(reader);
                } else if ("result".equals(fieldName)) {
                    reverseSearchAddressBatchItem.result = ReverseSearchAddressResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return reverseSearchAddressBatchItem;
        });
    }
}
