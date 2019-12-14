// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A set of options for selecting configuration settings from App Configuration service.
 *
 * <ul>
 * <li>
 * Providing {@link #getLabels() labels} will filter {@link ConfigurationSetting ConfigurationSettings} that match any
 * label name in conjunction with the keys that are passed in to the service request.
 * </li>
 * <li>
 * Providing {@link #getAcceptDateTime() acceptDateTime} will return the representation of matching {@link
 * ConfigurationSetting} at that given {@link OffsetDateTime}.
 * </li>
 * <li>
 * Providing {@link #getFields() fields} will populate only those {@link ConfigurationSetting} fields in the response.
 * By default, all of the fields are returned.
 * </li>
 * </ul>
 *
 * @see ConfigurationAsyncClient
 */
@Fluent
public class SettingSelector {
    private String[] keys;
    private String[] labels;
    private SettingFields[] fields;
    private String acceptDatetime;

    /**
     * Creates a setting selector that will populate responses with all of the {@link ConfigurationSetting
     * ConfigurationSetting's} properties and select all {@link ConfigurationSetting#getKey() keys}.
     */
    public SettingSelector() {
    }

    /**
     * Gets the expressions to filter {@link ConfigurationSetting#getKey() keys} on for the request.
     *
     * <p>
     * Examples:
     * <ol>
     * <li>If keys = "*", settings with any key are returned.</li>
     * <li>If keys = "abc1234", settings with a key equal to "abc1234" are returned.</li>
     * <li>If keys = "abc*", settings with a key starting with "abc" are returned.</li>
     * <li>If keys = "*abc*", settings with a key containing "abc" are returned.</li>
     * </ol>
     *
     * @return The expressions to filter ConfigurationSetting keys on.
     */
    public String[] getKeys() {
        return keys == null ? new String[0] : CoreUtils.clone(keys);
    }

    /**
     * Sets the expressions to filter {@link ConfigurationSetting#getKey() keys} on for the request.
     *
     * <p>
     * Examples:
     * <ul>
     * <li>If {@code keys = "*"}, settings with any key are returned.</li>
     * <li>If {@code keys = "abc1234"}, settings with a key equal to "abc1234" are returned.</li>
     * <li>If {@code keys = "abc*"}, settings with a key starting with "abc" are returned.</li>
     * <li>If {@code keys = "*abc*"}, settings with a key containing "abc" are returned.</li>
     * <li>If {@code keys = "abc,def"}, settings with a key equal to "abc" or "def" are returned.</li>
     * </ul>
     *
     * @param keys The expressions to filter ConfigurationSetting keys on.
     * @return The updated SettingSelector object
     */
    public SettingSelector setKeys(String... keys) {
        this.keys = keys;
        return this;
    }

    /**
     * Gets the labels used to filter settings based on their {@link ConfigurationSetting#getLabel() label} in the
     * service.
     *
     * If the value is {@code null} or an empty string, all ConfigurationSettings with {@link
     * ConfigurationSetting#NO_LABEL} are returned.
     *
     * <p>
     * Examples:
     * <ul>
     * <li>If {@code labels = "*"}, settings with any label are returned.</li>
     * <li>If {@code labels = "\0"}, settings without any label are returned.</li>
     * <li>If {@code labels = ""}, settings without any label are returned.</li>
     * <li>If {@code labels = null}, settings without any label are returned.</li>
     * <li>If {@code labels = "abc1234"}, settings with a label equal to "abc1234" are returned.</li>
     * <li>If {@code labels = "abc*"}, settings with a label starting with "abc" are returned.</li>
     * <li>If {@code labels = "*abc*"}, settings with a label containing "abc" are returned.</li>
     * <li>If {@code labels = "abc,def"}, settings with labels "abc" or "def" are returned.</li>
     * </ul>
     *
     * @return labels The labels used to filter GET requests from the service.
     */
    public String[] getLabels() {
        return labels == null ? new String[0] : CoreUtils.clone(labels);
    }

    /**
     * Sets the query to match {@link ConfigurationSetting#getLabel() labels} in the service.
     *
     * <p>
     * Examples:
     * <ul>
     * <li>If {@code labels = "*"}, settings with any label are returned.</li>
     * <li>If {@code labels = "\0"}, settings without any label are returned. (This is the default label.)</li>
     * <li>If {@code labels = "abc1234"}, settings with a label equal to "abc1234" are returned.</li>
     * <li>If {@code labels = "abc*"}, settings with a label starting with "abc" are returned.</li>
     * <li>If {@code labels = "*abc*"}, settings with a label containing "abc" are returned.</li>
     * <li>If {@code labels = "abc,def"}, settings with labels "abc" or "def" are returned.</li>
     * </ul>
     *
     * @param labels The ConfigurationSetting labels to match. If the provided value is {@code null} or {@code ""}, all
     * ConfigurationSettings will be returned regardless of their label.
     * @return SettingSelector The updated SettingSelector object.
     */
    public SettingSelector setLabels(String... labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Gets the date time for the request query. When the query is performed, if {@code acceptDateTime} is set, the
     * {@link ConfigurationSetting#getValue() configuration setting value} at that point in time is returned. Otherwise,
     * the current value is returned.
     *
     * @return Gets the currently set datetime in {@link DateTimeFormatter#RFC_1123_DATE_TIME} format.
     */
    public String getAcceptDateTime() {
        return this.acceptDatetime;
    }

    /**
     * If set, then configuration setting values will be retrieved as they existed at the provided datetime. Otherwise,
     * the current values are returned.
     *
     * @param datetime The value of the configuration setting at that given {@link OffsetDateTime}.
     * @return The updated SettingSelector object.
     */
    public SettingSelector setAcceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return this;
    }

    /**
     * Gets the fields on {@link ConfigurationSetting} to return from the GET request. If none are set, the service
     * returns the ConfigurationSettings with all of their fields populated.
     *
     * @return The set of {@link ConfigurationSetting} fields to return for a GET request.
     */
    public SettingFields[] getFields() {
        return fields == null ? new SettingFields[0] : CoreUtils.clone(fields);
    }

    /**
     * Sets fields that will be returned in the response corresponding to properties in {@link ConfigurationSetting}. If
     * none are set, the service returns ConfigurationSettings with all of their fields populated.
     *
     * @param fields The fields to select for the query response. If none are set, the service will return the
     * ConfigurationSettings with a default set of properties.
     * @return The updated SettingSelector object.
     */
    public SettingSelector setFields(SettingFields... fields) {
        this.fields = fields;
        return this;
    }

    @Override
    public String toString() {
        String fields;
        if (CoreUtils.isNullOrEmpty(this.fields)) {
            fields = "ALL_FIELDS";
        } else {
            fields = CoreUtils.arrayToString(this.fields, SettingFields::toStringMapper);
        }

        return String.format("SettingSelector(keys=%s, labels=%s, acceptDateTime=%s, fields=%s)",
            CoreUtils.arrayToString(this.keys, key -> key),
            CoreUtils.arrayToString(this.labels, label -> label),
            this.acceptDatetime,
            fields);
    }
}
