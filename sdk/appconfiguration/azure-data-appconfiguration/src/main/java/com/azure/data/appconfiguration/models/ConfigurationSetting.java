// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingHelper;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * ConfigurationSetting is a resource identified by unique combination of {@link #getKey() key} and {@link #getLabel()
 * label}. By default, the label is {@code null}. To explicitly reference the default label use {@link #NO_LABEL}.
 */
@Fluent
public class ConfigurationSetting implements JsonSerializable<ConfigurationSetting> {
    /**
     * The default label for configuration settings is the label, "\0". Users use this value when they want to
     * explicitly reference a configuration setting that has no label. This gets URL encoded as "%00".
     */
    public static final String NO_LABEL = "\0";

    private String key;
    private String label;
    private String value;
    private String contentType;
    private String etag;
    private OffsetDateTime lastModified;
    private boolean readOnly;
    private Map<String, String> tags;

    static {
        ConfigurationSettingHelper.setAccessor(new ConfigurationSettingHelper.ConfigurationSettingAccessor() {
            @Override
            public ConfigurationSetting setReadOnly(ConfigurationSetting setting, boolean readOnly) {
                return setting.setReadOnly(readOnly);
            }

            @Override
            public ConfigurationSetting setLastModified(ConfigurationSetting setting, OffsetDateTime lastModified) {
                return setting.setLastModified(lastModified);
            }
        });
    }

    /**
     * Creates an instance of the configuration setting.
     */
    public ConfigurationSetting() {
    }

    /**
     * Gets the key name for this configuration setting.
     *
     * @return The key for this configuration setting.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of this configuration setting.
     *
     * @param key The name of the configuration key.
     * @return ConfigurationSetting object itself.
     */
    public ConfigurationSetting setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets the label of this configuration setting.
     *
     * @return The label of this setting.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this configuration setting. {@link #NO_LABEL} is the default label used when this value is not
     * set.
     *
     * @param label The label of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the value of this configuration setting.
     *
     * @return The value of this configuration setting.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this setting.
     *
     * @param value The value to associate with this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the content type of this configuration setting. By default, this content type is null.
     *
     * @return The content type of this setting.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type. By default, the content type is null.
     *
     * @param contentType The content type of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * The ETag for this configuration setting.
     *
     * @return etag The ETag for the setting.
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets the ETag for this configuration setting.
     *
     * @param etag The ETag for the configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * The time when the configuration setting was last modified.
     *
     * @return The time when the configuration was last modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    private ConfigurationSetting setLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Gets whether or not the configuration setting is read-only. If it is, then no modifications can be
     * made to this setting.
     *
     * This is a <b>readonly</b> property. It is populated from responses from the Azure App Configuration service.
     *
     * @return true if read-only; false otherwise.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    private ConfigurationSetting setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * Gets tags associated with this configuration setting.
     *
     * @return tags Gets tags for this configuration setting.
     */
    public Map<String, String> getTags() {
        return tags == null ? Collections.emptyMap() : tags;
    }

    /**
     * Sets the tags for this configuration setting.
     *
     * @param tags The tags to add to this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public String toString() {
        return String.format("ConfigurationSetting(key=%s, label=%s, value=%s, etag=%s)", this.key, this.label,
            this.value, this.etag);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("key", key);
        jsonWriter.writeStringField("label", label);
        jsonWriter.writeStringField("value", value);
        jsonWriter.writeStringField("content_type", contentType);
        jsonWriter.writeStringField("etag", etag);
        jsonWriter.writeStringField("last_modified", Objects.toString(this.lastModified, null));
        jsonWriter.writeBooleanField("locked", readOnly);
        jsonWriter.writeMapField("tags", tags, JsonWriter::writeString);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ConfigurationSetting from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ConfigurationSetting if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ConfigurationSetting.
     */
    public static ConfigurationSetting fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ConfigurationSetting setting = new ConfigurationSetting();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.getFieldName();
                reader.nextToken();

                if ("key".equals(fieldName)) {
                    setting.setKey(reader.getString());
                } else if ("label".equals(fieldName)) {
                    setting.setLabel(reader.getString());
                } else if ("value".equals(fieldName)) {
                    setting.setKey(reader.getString());
                } else if ("content_type".equals(fieldName)) {
                    setting.setContentType(reader.getString());
                } else if ("etag".equals(fieldName)) {
                    setting.setETag(reader.getString());
                } else if ("last_modified".equals(fieldName)) {
                    setting.setLastModified(
                        reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString())));
                } else if ("locked".equals(fieldName)) {
                    setting.setReadOnly(reader.getBoolean());
                } else if ("tags".equals(fieldName)) {
                    setting.setTags(reader.readMap(JsonReader::getString));
                } else {
                    reader.skipChildren();
                }
            }

            return setting;
        });
    }
}
