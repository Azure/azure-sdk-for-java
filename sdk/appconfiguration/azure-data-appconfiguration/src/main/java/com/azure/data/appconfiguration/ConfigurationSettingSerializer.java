// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

class ConfigurationSettingSerializer extends JsonSerializer<ConfigurationSetting> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addSerializer(ConfigurationSetting.class, new ConfigurationSettingSerializer());
    }

    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public void serialize(ConfigurationSetting setting, JsonGenerator generator, SerializerProvider provider)
        throws IOException {
        generator.writeStartObject();

        generator.writeStringField("key", setting.getKey());

        if (setting.getLabel() != null) {
            generator.writeStringField("label", setting.getLabel());
        }

        // Should the internal value of read only be a Boolean?
        if (setting.isReadOnly()) {
            generator.writeBooleanField("locked", setting.isReadOnly());
        }

        if (setting.getLastModified() != null) {
            generator.writeStringField("last_modified", DateTimeFormatter.ISO_INSTANT
                .format(setting.getLastModified()));
        }

        generator.writeStringField("value", setting.getValue());

        if (setting.getContentType() != null) {
            generator.writeStringField("content_type", setting.getContentType());
        }

        if (setting.getTags() != null) {
            generator.writeObjectFieldStart("tags");

            for (Map.Entry<String, String> tag : setting.getTags().entrySet()) {
                generator.writeStringField(tag.getKey(), tag.getValue());
            }

            generator.writeEndObject();
        }

        if (setting.getETag() != null) {
            generator.writeStringField("etag", setting.getETag());
        }

        generator.writeEndObject();
    }
}
