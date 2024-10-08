// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.consumption.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The start and end date for a budget.
 */
@Fluent
public final class BudgetTimePeriod implements JsonSerializable<BudgetTimePeriod> {
    /*
     * The start date for the budget.
     */
    private OffsetDateTime startDate;

    /*
     * The end date for the budget. If not provided, we default this to 10 years from the start date.
     */
    private OffsetDateTime endDate;

    /**
     * Creates an instance of BudgetTimePeriod class.
     */
    public BudgetTimePeriod() {
    }

    /**
     * Get the startDate property: The start date for the budget.
     * 
     * @return the startDate value.
     */
    public OffsetDateTime startDate() {
        return this.startDate;
    }

    /**
     * Set the startDate property: The start date for the budget.
     * 
     * @param startDate the startDate value to set.
     * @return the BudgetTimePeriod object itself.
     */
    public BudgetTimePeriod withStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Get the endDate property: The end date for the budget. If not provided, we default this to 10 years from the
     * start date.
     * 
     * @return the endDate value.
     */
    public OffsetDateTime endDate() {
        return this.endDate;
    }

    /**
     * Set the endDate property: The end date for the budget. If not provided, we default this to 10 years from the
     * start date.
     * 
     * @param endDate the endDate value to set.
     * @return the BudgetTimePeriod object itself.
     */
    public BudgetTimePeriod withEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (startDate() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException("Missing required property startDate in model BudgetTimePeriod"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(BudgetTimePeriod.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("startDate",
            this.startDate == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.startDate));
        jsonWriter.writeStringField("endDate",
            this.endDate == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.endDate));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of BudgetTimePeriod from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of BudgetTimePeriod if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the BudgetTimePeriod.
     */
    public static BudgetTimePeriod fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BudgetTimePeriod deserializedBudgetTimePeriod = new BudgetTimePeriod();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("startDate".equals(fieldName)) {
                    deserializedBudgetTimePeriod.startDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("endDate".equals(fieldName)) {
                    deserializedBudgetTimePeriod.endDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedBudgetTimePeriod;
        });
    }
}
