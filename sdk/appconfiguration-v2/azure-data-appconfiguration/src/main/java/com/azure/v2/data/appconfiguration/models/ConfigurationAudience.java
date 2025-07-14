// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration.models;

import com.azure.v2.data.appconfiguration.ConfigurationClientBuilder;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.utils.ExpandableEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Configuration Audience is used to specify the target audience for the Azure App Configuration service.
 * Microsoft Entra audience is configurable via the {@link ConfigurationClientBuilder#audience(ConfigurationAudience)} method.
 */
public final class ConfigurationAudience implements ExpandableEnum<String>, JsonSerializable<ConfigurationAudience> {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationAudience.class);
    private static final Map<String, ConfigurationAudience> VALUES = new ConcurrentHashMap<>();
    private static final Function<String, ConfigurationAudience> NEW_INSTANCE = ConfigurationAudience::new;
    private final String value;

    /**
     * The Azure App Configuration service audience for China Cloud.
     */
    public static final ConfigurationAudience AZURE_CHINA = fromString("https://appconfig.azure.cn");

    /**
     * The Azure App Configuration service audience for US Government Cloud.
     */
    public static final ConfigurationAudience AZURE_GOVERNMENT = fromString("https://appconfig.azure.us");

    /**
     * The Azure App Configuration service audience for Public Cloud.
     */
    public static final ConfigurationAudience AZURE_PUBLIC_CLOUD = fromString("https://appconfig.azure.com");

    /**
     * Creates a new instance of ConfigurationAudience value.
     */
    private ConfigurationAudience(String value) {
        this.value = value;
    }

    /**
     * Creates or finds a ConfigurationAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ConfigurationAudience.
     * @throws NullPointerException If the name is null.
     */
    public static ConfigurationAudience fromString(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        return VALUES.computeIfAbsent(name, NEW_INSTANCE);
    }

    /**
     * Gets known ConfigurationAudience values.
     *
     * @return known ConfigurationAudience values.
     */
    public static Collection<ConfigurationAudience> values() {
        return new ArrayList<>(VALUES.values());
    }

    /**
     * Gets the value of the ConfigurationAudience instance.
     *
     * @return the value of the ConfigurationAudience instance.
     */
    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeString(getValue());
    }

    /**
     * Reads an instance of ConfigurationAudience from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ConfigurationAudience if the JsonReader was pointing to an instance of it, or null
     * if the JsonReader was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ConfigurationAudience.
     * @throws IllegalStateException If unexpected JSON token is found.
     */
    public static ConfigurationAudience fromJson(JsonReader jsonReader) throws IOException {
        JsonToken nextToken = jsonReader.nextToken();
        if (nextToken == JsonToken.NULL) {
            return null;
        }
        if (nextToken != JsonToken.STRING) {
            throw LOGGER.throwableAtError()
                .addKeyValue("nextToken", nextToken.name())
                .addKeyValue("expectedToken", JsonToken.STRING.name())
                .log("Unexpected JSON token.", IllegalStateException::new);
        }
        return ConfigurationAudience.fromString(jsonReader.getString());
    }

    @Override
    public String toString() {
        return Objects.toString(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }
}
