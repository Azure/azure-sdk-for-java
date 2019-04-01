// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

/**
 * Represents a set of request options for querying App Configuration.
 *
 * <p>
 * Providing {@link RequestOptions#label()} will filter {@link ConfigurationSetting}s that match that label name in
 * conjunction with the key that is passed in to the service request.
 *
 * <p>
 * Providing {@link RequestOptions#acceptDateTime()} will return the representation of matching {@link ConfigurationSetting}
 * at that given {@link OffsetDateTime}.
 *
 * <p>
 * Providing {@link RequestOptions#fields()} will populate only those {@link ConfigurationSetting} fields in the response.
 */
public class RequestOptions {
    private String key;
    private String label;
    private EnumSet<SettingFields> fields;
    private String acceptDatetime;

    /**
     * Creates request options that will populate {@link ConfigurationSetting} responses with default properties and
     * select all {@link ConfigurationSetting#key()}s.
     */
    public RequestOptions() {
        key = "*";
    }

    /**
     * Gets the expression to filter {@link ConfigurationSetting#key()} on for the request.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If key = "*", all settings are returned.</li>
     *     <li>If key = "abc1234", settings with a key equal to "abc1234" are returned.</li>
     *     <li>If key = "abc*", settings with a key starting with "abc" are returned.</li>
     *     <li>If key = "*abc*", settings with a key containing "abc" are returned.</li>
     * </ul>
     *
     * @return The expression to filter ConfigurationSetting keys on.
     */
    public String key() {
        return key;
    }

    /**
     * Sets the expression to filter {@link ConfigurationSetting#key()} on for the request.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If {@code key = "*"}, all settings are returned.</li>
     *     <li>If {@code key = "abc1234"}, settings with a key equal to "abc1234" are returned.</li>
     *     <li>If {@code key = "abc*"}, settings with a key starting with "abc" are returned.</li>
     *     <li>If {@code key = "*abc*"}, settings with a key containing "abc" are returned.</li>
     * </ul>
     *
     * @param key The expression to filter ConfigurationSetting keys on.
     * @return The updated RequestOptions object
     */
    public RequestOptions key(String key) {
        this.key = key;
        return this;
    }

    /**
     * The label used to filter settings based on their {@link ConfigurationSetting#label()} in the service.
     *
     * If the value is {@code null} or an empty string, all ConfigurationSettings with
     * {@link ConfigurationSetting#NO_LABEL} are returned.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If {@code label = "*"}, all settings are returned.</li>
     *     <li>If {@code label = "\0"}, settings without any label are returned.</li>
     *     <li>If {@code label = ""}, settings without any label are returned.</li>
     *     <li>If {@code label = null}, settings without any label are returned.</li>
     *     <li>If {@code label = "abc1234"}, settings with a label equal to "abc1234" are returned.</li>
     *     <li>If {@code label = "abc*"}, settings with a label starting with "abc" are returned.</li>
     *     <li>If {@code label = "*abc*"}, settings with a label containing "abc" are returned.</li>
     * </ul>
     *
     * @return label The label used to filter GET requests from the service.
     */
    public String label() {
        return label;
    }

    /**
     * Sets the query to match {@link ConfigurationSetting#label()}s in the service.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If {@code label = "*"}, all settings are returned.</li>
     *     <li>If {@code label = "\0"}, settings without any label are returned. (This is the default label.)</li>
     *     <li>If {@code label = "abc1234"}, settings with a label equal to "abc1234" are returned.</li>
     *     <li>If {@code label = "abc*"}, settings with a label starting with "abc" are returned.</li>
     *     <li>If {@code label = "*abc*"}, settings with a label containing "abc" are returned.</li>
     * </ul>
     *
     * @param label The ConfigurationSetting label to match. If the provided value is {@code null} or {@code ""}, all
     *              ConfigurationSettings will be returned regardless of their label.
     * @return RequestOptions The updated RequestOptions object.
     */
    public RequestOptions label(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the date time for the request query. When the query is performed, the state of the
     * {@link ConfigurationSetting}s at that point in time is returned based on the provided acceptDateTime.
     *
     * <p>
     * If the value is not set, then the {@link ConfigurationSetting}s at their current state is returned.
     *
     * @return Gets the currently set datetime in {@link DateTimeFormatter#RFC_1123_DATE_TIME} format.
     */
    public String acceptDateTime() {
        return this.acceptDatetime;
    }

    /**
     * If set, then key values will be retrieved exactly as they existed at the provided time.
     *
     * @param datetime The value of the configuration setting at that given {@link OffsetDateTime}.
     * @return The updated RequestOptions object.
     */
    public RequestOptions acceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return this;
    }

    /**
     * Gets the fields on {@link ConfigurationSetting} to return from the GET request. If none are set, the service
     * returns the ConfigurationSettings with a default set of properties populated.
     *
     * @return The set of {@link ConfigurationSetting} fields to return for a GET request.
     */
    public EnumSet<SettingFields> fields() {
        return fields;
    }

    /**
     * Sets fields that will be returned in the response corresponding to properties in {@link ConfigurationSetting}.
     *
     * @param fields the fields to select for the query response. If none are set, the service will return the
     *               ConfigurationSettings with a default set of properties.
     * @return The updated RequestOptions object.
     */
    public RequestOptions fields(EnumSet<SettingFields> fields) {
        this.fields = fields;
        return this;
    }
}
