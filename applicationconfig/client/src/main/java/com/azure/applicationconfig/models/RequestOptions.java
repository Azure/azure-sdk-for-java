// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

/**
 * Represents a set of request options for querying App Configuration.
 *
 * Providing {@link RequestOptions#label()} will filter {@link ConfigurationSetting}s that match that label name in
 * conjunction with the key that is passed in to the service request.
 *
 * Providing {@link RequestOptions#acceptDateTime()} will return the representation of matching {@link ConfigurationSetting}
 * at that given {@link OffsetDateTime}.
 *
 * Providing {@link RequestOptions#fields()} will populate only those {@link ConfigurationSetting} fields in the response.
 */
public abstract class RequestOptions<T extends RequestOptions> {
    private String label;
    private EnumSet<ConfigurationSettingFields> fields;
    private String acceptDatetime;

    protected RequestOptions() {
        fields = EnumSet.of(ConfigurationSettingFields.DEFAULT);
    }

    /**
     * The label used to filter settings based on their {@link ConfigurationSetting#label()} in the service.
     *
     * @return label The label used to filter GET requests from the service.
     */
    public String label() {
        return label;
    }

    /**
     * Sets specific label of the key.
     * @param label the label
     * @return RequestOptions object itself
     */
    @SuppressWarnings("unchecked")
    public T label(String label) {
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
     * @return RequestOptions object itself
     */
    @SuppressWarnings("unchecked")
    public T acceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return (T) this;
    }

    /**
     * @return fields
     */
    public EnumSet<ConfigurationSettingFields> fields() {
        return fields;
    }

    /**
     * Sets fields that will be returned in the response.
     * @param fields the fields to select
     * @return RequestOptions object itself
     */
    @SuppressWarnings("unchecked")
    public T fields(EnumSet<ConfigurationSettingFields> fields) {
        this.fields = fields;
        return (T) this;
    }
}
