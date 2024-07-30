// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * A class that contains the label selector options for a GET request to the service.
 */
@Fluent
public final class LabelSelector {
    private String nameFilter;
    private OffsetDateTime acceptDatetime;

    private List<LabelFields> fields;

    /**
     * Creates a label selector that will populate responses with all of the {@link ConfigurationSetting#getLabel() labels}.
     */
    public LabelSelector() {
    }

    /**
     * Gets the labels used to filter settings based on their {@link ConfigurationSetting#getLabel() label} in the
     * service.
     *
     * A filter for the name of the returned labels.
     *
     * <p>See <a href="https://docs.microsoft.com/azure/azure-app-configuration/rest-api-key-value#supported-filters">Filtering</a>
     * for more information about these supported filters.</p>
     *
     * @return labels The labels used to filter GET requests from the service.
     */
    public String getNameFilter() {
        return nameFilter;
    }

    /**
     * Sets the expression to filter {@link ConfigurationSetting#getLabel() labels} on for the request.
     *
     * <p>See <a href="https://docs.microsoft.com/azure/azure-app-configuration/rest-api-key-value#supported-filters">Filtering</a>
     * for more information about these supported filters.</p>
     *
     * @param nameFilter The expressions to filter ConfigurationSetting labels on.
     * @return the updated LabelSelector object.
     */
    public LabelSelector setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
        return this;
    }

    /**
     * Gets the date time for the request query. When the query is performed, if {@code acceptDateTime} is set, the
     * labels at that point in time is returned.
     *
     * @return Gets the currently set datetime in {@link DateTimeFormatter#RFC_1123_DATE_TIME} format.
     */
    public OffsetDateTime getAcceptDateTime() {
        return this.acceptDatetime;
    }

    /**
     * If set, then labels will be retrieved as they existed at the provided datetime.
     *
     * @param datetime The value of the configuration setting at that given {@link OffsetDateTime}.
     * @return The updated LabelSelector object.
     */
    public LabelSelector setAcceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = datetime;
        return this;
    }

    /**
     * Gets the fields on {@link ConfigurationSnapshot} to return from the GET request. If none are set, the
     * service returns the snapshot with all of their fields populated.
     *
     * @return The set of {@link ConfigurationSnapshot} fields to return for a GET request.
     */
    public List<LabelFields> getFields() {
        return fields;
    }

    /**
     * Sets fields that will be returned in the response corresponding to properties in
     * {@link ConfigurationSnapshot}. If none are set, the service returns snapshot with all of their fields
     * populated.
     *
     * @param fields The fields to select for the query response. If none are set, the service will return the
     * snapshot with a default set of properties.
     *
     * @return The updated LabelSelector object.
     */
    public LabelSelector setFields(LabelFields... fields) {
        this.fields = fields == null ? null : Arrays.asList(fields);
        return this;
    }

    /**
     * Sets fields that will be returned in the response corresponding to properties in
     * {@link ConfigurationSnapshot}. If none are set, the service returns snapshot with all of their fields
     * populated.
     *
     * @param fields The fields to select for the query response. If none are set, the service will return the
     * snapshot with a default set of properties.
     *
     * @return The updated LabelSelector object.
     */
    public LabelSelector setFields(List<LabelFields> fields) {
        this.fields = fields;
        return this;
    }
}
