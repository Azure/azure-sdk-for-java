// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

class ConfigurationSettingDeserializer extends JsonDeserializer<ConfigurationSetting> {
    private static final SimpleModule MODULE;

    static {
        MODULE = new SimpleModule().addDeserializer(ConfigurationSetting.class, new ConfigurationSettingDeserializer());
    }

    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public ConfigurationSetting deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ConfigurationSetting setting = new ConfigurationSetting();
        String fieldName;
        while ((fieldName = parser.nextFieldName()) != null) {
            handleField(setting, fieldName, parser);
        }

        return setting;
    }

    private void handleField(ConfigurationSetting setting, String fieldName, JsonParser parser) throws IOException {
        switch (fieldName) {
            case "content_type":
                setting.setContentType(parser.nextTextValue());
                break;
            case "etag":
                setting.setETag(parser.nextTextValue());
                break;
            case "key":
                setting.setKey(parser.nextTextValue());
                break;
            case "label":
                setting.setLabel(parser.nextTextValue());
                break;
            case "last_modified":
                JsonToken value = parser.nextValue();
                if (value.id() == JsonTokenId.ID_NULL) {
                    setting.setLastModified(null);
                } else {
                    setting.setLastModified(OffsetDateTime
                        .parse(parser.getValueAsString(), DateTimeFormatter.ISO_DATE_TIME));
                }
                break;
            case "locked":
                setting.setReadOnly(parser.nextBooleanValue());
                break;
            case "tags":
                parser.nextToken();

                if (parser.currentTokenId() == JsonTokenId.ID_NULL) {
                    return;
                }

                Map<String, String> tags = new HashMap<>();

                parser.nextToken();
                while (parser.currentToken() != JsonToken.END_OBJECT) {
                    tags.put(parser.getValueAsString(), parser.nextTextValue());
                    parser.nextToken();
                }

                setting.setTags(tags);
                break;
            case "value":
                setting.setValue(parser.nextTextValue());
                break;
            default:
                break;
        }
    }
}
