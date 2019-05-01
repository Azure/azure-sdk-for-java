// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig.models;

import com.azure.applicationconfig.ConfigurationAsyncClient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * A set of options for selecting configuration settings from Application Configuration service.
 *
 * <ul>
 *     <li>
 *         Providing {@link SettingSelector#label() label} will filter
 *         {@link ConfigurationSetting ConfigurationSettings} that match that label name in conjunction with the key
 *         that is passed in to the service request.
 *     </li>
 *     <li>
 *         Providing {@link SettingSelector#acceptDateTime() acceptDateTime} will return the representation of matching
 *         {@link ConfigurationSetting} at that given {@link OffsetDateTime}.
 *     </li>
 *     <li>
 *         Providing {@link SettingSelector#fields() fields} will populate only those {@link ConfigurationSetting}
 *         fields in the response. By default, all of the fields are returned.
 *     </li>
 * </ul>
 *
 * @see ConfigurationAsyncClient
 */
public class SettingSelector {
    private String key;
    private String label;
    private SettingFields[] fields;
    private String acceptDatetime;
    private Range range;

    /**
     * Creates a setting selector that will populate responses with all of the
     * {@link ConfigurationSetting ConfigurationSetting's} properties and select all
     * {@link ConfigurationSetting#key() keys}.
     */
    public SettingSelector() {
    }

    /**
     * Gets the expression to filter {@link ConfigurationSetting#key() key} on for the request.
     *
     * <p>
     * Examples:
     * <ol>
     *     <li>If key = "*", settings with any key are returned.</li>
     *     <li>If key = "abc1234", settings with a key equal to "abc1234" are returned.</li>
     *     <li>If key = "abc*", settings with a key starting with "abc" are returned.</li>
     *     <li>If key = "*abc*", settings with a key containing "abc" are returned.</li>
     * </ol>
     *
     * @return The expression to filter ConfigurationSetting keys on.
     */
    public String key() {
        return key;
    }

    /**
     * Sets the expression to filter {@link ConfigurationSetting#key() key} on for the request.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If {@code key = "*"}, settings with any key are returned.</li>
     *     <li>If {@code key = "abc1234"}, settings with a key equal to "abc1234" are returned.</li>
     *     <li>If {@code key = "abc*"}, settings with a key starting with "abc" are returned.</li>
     *     <li>If {@code key = "*abc*"}, settings with a key containing "abc" are returned.</li>
     * </ul>
     *
     * @param key The expression to filter ConfigurationSetting keys on.
     * @return The updated SettingSelector object
     */
    public SettingSelector key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets the label used to filter settings based on their {@link ConfigurationSetting#label() label} in the service.
     *
     * If the value is {@code null} or an empty string, all ConfigurationSettings with
     * {@link ConfigurationSetting#NO_LABEL} are returned.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If {@code label = "*"}, settings with any label are returned.</li>
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
     * Sets the query to match {@link ConfigurationSetting#label() labels} in the service.
     *
     * <p>
     * Examples:
     * <ul>
     *     <li>If {@code label = "*"}, settings with any label are returned.</li>
     *     <li>If {@code label = "\0"}, settings without any label are returned. (This is the default label.)</li>
     *     <li>If {@code label = "abc1234"}, settings with a label equal to "abc1234" are returned.</li>
     *     <li>If {@code label = "abc*"}, settings with a label starting with "abc" are returned.</li>
     *     <li>If {@code label = "*abc*"}, settings with a label containing "abc" are returned.</li>
     * </ul>
     *
     * @param label The ConfigurationSetting label to match. If the provided value is {@code null} or {@code ""}, all
     * ConfigurationSettings will be returned regardless of their label.
     * @return SettingSelector The updated SettingSelector object.
     */
    public SettingSelector label(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the date time for the request query. When the query is performed, if {@code acceptDateTime} is set, the
     * {@link ConfigurationSetting#value() configuration setting value} at that point in time is returned. Otherwise,
     * the current value is returned.
     *
     * @return Gets the currently set datetime in {@link DateTimeFormatter#RFC_1123_DATE_TIME} format.
     */
    public String acceptDateTime() {
        return this.acceptDatetime;
    }

    /**
     * If set, then configuration setting values will be retrieved as they existed at the provided datetime. Otherwise,
     * the current values are returned.
     *
     * @param datetime The value of the configuration setting at that given {@link OffsetDateTime}.
     * @return The updated SettingSelector object.
     */
    public SettingSelector acceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return this;
    }

    /**
     * Gets the fields on {@link ConfigurationSetting} to return from the GET request. If none are set, the service
     * returns the ConfigurationSettings with all of their fields populated.
     *
     * @return The set of {@link ConfigurationSetting} fields to return for a GET request.
     */
    public SettingFields[] fields() {
        return fields == null
            ? new SettingFields[0]
            : Arrays.copyOf(fields, fields.length);
    }

    /**
     * Sets fields that will be returned in the response corresponding to properties in {@link ConfigurationSetting}.
     * If none are set, the service returns ConfigurationSettings with all of their fields populated.
     *
     * @param fields The fields to select for the query response. If none are set, the service will return the
     * ConfigurationSettings with a default set of properties.
     * @return The updated SettingSelector object.
     */
    public SettingSelector fields(SettingFields... fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Gets the {@link Range} used to select a specific range of revisions with {@code listSettingRevisions}.
     * If {@code null}, the service returns all revisions.
     * @return The {@link Range} used to select a range of revisions.
     */
    public Range range() {
        return range;
    }

    /**
     * Sets the {@link Range} used to select a specific range of revisions. If null, the service returns all revisions.
     * @param range The range of revisions to select.
     * @return The updated SettingSelector object.
     */
    public SettingSelector range(Range range) {
        this.range = range;
        return this;
    }
}
