// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.azconfig.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Filtering options for Get- requests.
 */
public abstract class KeyValueGenericFilter<T extends KeyValueGenericFilter> {
    private String label;
    private String fields;
    private String acceptDatetime;

    /**
     * @return label
     */
    public String label() {
        return label;
    }

    /**
     * Sets specific labe of the key.
     * @param label the label
     * @return KeyValueGenericFilter object itself
     */
    @SuppressWarnings("unchecked")
    public T withLabel(String label) {
        this.label = label;
        return (T) this;
    }

    /**
     * @return preferredDateTime
     */
    public String acceptDateTime() {
        return this.acceptDatetime;
    }

    /**
     * If set, then key values will be retrieved exactly as they existed at the provided time.
     * @param datetime the preferredDateTime
     * @return KeyValueGenericFilter object itself
     */
    @SuppressWarnings("unchecked")
    public T withAcceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return (T) this;
    }

    /**
     * @return fields
     */
    public String fields() {
        return fields;
    }

    /**
     * Sets fiels that will be returned in the response.
     * @param fields the fields to select
     * @return KeyValueGenericFilter object itself
     */
    @SuppressWarnings("unchecked")
    public T withFields(String fields) {
        this.fields = fields;
        return (T) this;
    }

}
