// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cognitiveservices.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Network Security Perimeter Profile Information.
 */
@Fluent
public final class NetworkSecurityPerimeterProfileInfo
    implements JsonSerializable<NetworkSecurityPerimeterProfileInfo> {
    /*
     * Name of the resource profile
     */
    private String name;

    /*
     * Access rules version of the resource profile
     */
    private Long accessRulesVersion;

    /*
     * The accessRules property.
     */
    private List<NetworkSecurityPerimeterAccessRule> accessRules;

    /*
     * Current diagnostic settings version
     */
    private Long diagnosticSettingsVersion;

    /*
     * List of enabled log categories
     */
    private List<String> enabledLogCategories;

    /**
     * Creates an instance of NetworkSecurityPerimeterProfileInfo class.
     */
    public NetworkSecurityPerimeterProfileInfo() {
    }

    /**
     * Get the name property: Name of the resource profile.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name property: Name of the resource profile.
     * 
     * @param name the name value to set.
     * @return the NetworkSecurityPerimeterProfileInfo object itself.
     */
    public NetworkSecurityPerimeterProfileInfo withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the accessRulesVersion property: Access rules version of the resource profile.
     * 
     * @return the accessRulesVersion value.
     */
    public Long accessRulesVersion() {
        return this.accessRulesVersion;
    }

    /**
     * Set the accessRulesVersion property: Access rules version of the resource profile.
     * 
     * @param accessRulesVersion the accessRulesVersion value to set.
     * @return the NetworkSecurityPerimeterProfileInfo object itself.
     */
    public NetworkSecurityPerimeterProfileInfo withAccessRulesVersion(Long accessRulesVersion) {
        this.accessRulesVersion = accessRulesVersion;
        return this;
    }

    /**
     * Get the accessRules property: The accessRules property.
     * 
     * @return the accessRules value.
     */
    public List<NetworkSecurityPerimeterAccessRule> accessRules() {
        return this.accessRules;
    }

    /**
     * Set the accessRules property: The accessRules property.
     * 
     * @param accessRules the accessRules value to set.
     * @return the NetworkSecurityPerimeterProfileInfo object itself.
     */
    public NetworkSecurityPerimeterProfileInfo withAccessRules(List<NetworkSecurityPerimeterAccessRule> accessRules) {
        this.accessRules = accessRules;
        return this;
    }

    /**
     * Get the diagnosticSettingsVersion property: Current diagnostic settings version.
     * 
     * @return the diagnosticSettingsVersion value.
     */
    public Long diagnosticSettingsVersion() {
        return this.diagnosticSettingsVersion;
    }

    /**
     * Set the diagnosticSettingsVersion property: Current diagnostic settings version.
     * 
     * @param diagnosticSettingsVersion the diagnosticSettingsVersion value to set.
     * @return the NetworkSecurityPerimeterProfileInfo object itself.
     */
    public NetworkSecurityPerimeterProfileInfo withDiagnosticSettingsVersion(Long diagnosticSettingsVersion) {
        this.diagnosticSettingsVersion = diagnosticSettingsVersion;
        return this;
    }

    /**
     * Get the enabledLogCategories property: List of enabled log categories.
     * 
     * @return the enabledLogCategories value.
     */
    public List<String> enabledLogCategories() {
        return this.enabledLogCategories;
    }

    /**
     * Set the enabledLogCategories property: List of enabled log categories.
     * 
     * @param enabledLogCategories the enabledLogCategories value to set.
     * @return the NetworkSecurityPerimeterProfileInfo object itself.
     */
    public NetworkSecurityPerimeterProfileInfo withEnabledLogCategories(List<String> enabledLogCategories) {
        this.enabledLogCategories = enabledLogCategories;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (accessRules() != null) {
            accessRules().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeNumberField("accessRulesVersion", this.accessRulesVersion);
        jsonWriter.writeArrayField("accessRules", this.accessRules, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeNumberField("diagnosticSettingsVersion", this.diagnosticSettingsVersion);
        jsonWriter.writeArrayField("enabledLogCategories", this.enabledLogCategories,
            (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of NetworkSecurityPerimeterProfileInfo from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of NetworkSecurityPerimeterProfileInfo if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the NetworkSecurityPerimeterProfileInfo.
     */
    public static NetworkSecurityPerimeterProfileInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NetworkSecurityPerimeterProfileInfo deserializedNetworkSecurityPerimeterProfileInfo
                = new NetworkSecurityPerimeterProfileInfo();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    deserializedNetworkSecurityPerimeterProfileInfo.name = reader.getString();
                } else if ("accessRulesVersion".equals(fieldName)) {
                    deserializedNetworkSecurityPerimeterProfileInfo.accessRulesVersion
                        = reader.getNullable(JsonReader::getLong);
                } else if ("accessRules".equals(fieldName)) {
                    List<NetworkSecurityPerimeterAccessRule> accessRules
                        = reader.readArray(reader1 -> NetworkSecurityPerimeterAccessRule.fromJson(reader1));
                    deserializedNetworkSecurityPerimeterProfileInfo.accessRules = accessRules;
                } else if ("diagnosticSettingsVersion".equals(fieldName)) {
                    deserializedNetworkSecurityPerimeterProfileInfo.diagnosticSettingsVersion
                        = reader.getNullable(JsonReader::getLong);
                } else if ("enabledLogCategories".equals(fieldName)) {
                    List<String> enabledLogCategories = reader.readArray(reader1 -> reader1.getString());
                    deserializedNetworkSecurityPerimeterProfileInfo.enabledLogCategories = enabledLogCategories;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNetworkSecurityPerimeterProfileInfo;
        });
    }
}
