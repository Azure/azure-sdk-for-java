// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.core.util.logging.ClientLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OneSettingsStatsbeatConfiguration {
    private static final ClientLogger LOGGER = new ClientLogger(OneSettingsStatsbeatConfiguration.class);
    private static final String CONFIG_URL
        = "https://settings.sdk.monitor.azure.com/AzMonSDKDynamicConfiguration?namespaces=java";

    private OneSettingsStatsbeatConfiguration() {
    }

    public static StatsbeatConnectionString fetch(ConnectionString customerConnectionString) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(CONFIG_URL).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            return resolve(customerConnectionString, parseSettings(readBody(connection.getInputStream())));
        } catch (IOException | RuntimeException ex) {
            LOGGER.verbose("Unable to fetch SDKStats routing from OneSettings; using the built-in endpoint.", ex);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static StatsbeatConnectionString resolve(ConnectionString customerConnectionString, Map<String, String> settings) {
        String defaultConnectionString = settings.get("DEFAULT_STATS_CONNECTION_STRING");
        String defaultEndpoint = settings.get("DEFAULT_SDK_STATS_ENDPOINT");
        String region
            = StatsbeatConnectionString.getGeoWithoutStampSpecific(customerConnectionString.getIngestionEndpoint());
        for (String boundary : parseStringArray(settings.get("SUPPORTED_DATA_BOUNDARIES"))) {
            if ("DEFAULT".equalsIgnoreCase(boundary)) {
                continue;
            }
            for (String candidate : parseStringArray(settings.get(boundary + "_REGIONS"))) {
                if (candidate.equalsIgnoreCase(region)) {
                    StatsbeatConnectionString resolved
                        = parseConnectionString(settings.get(boundary + "_STATS_CONNECTION_STRING"),
                            settings.getOrDefault(boundary + "_SDK_STATS_ENDPOINT", defaultEndpoint));
                    return resolved != null
                        ? resolved
                        : parseConnectionString(defaultConnectionString, defaultEndpoint);
                }
            }
        }
        return parseConnectionString(defaultConnectionString, defaultEndpoint);
    }

    private static StatsbeatConnectionString parseConnectionString(String value, String endpoint) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            ConnectionString connectionString = ConnectionString.parse(value);
            return StatsbeatConnectionString.create(connectionString, connectionString.getInstrumentationKey(),
                resolveEndpoint(endpoint, connectionString.getIngestionEndpoint()));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String resolveEndpoint(String endpoint, String fallback) {
        try {
            URL endpointUrl = endpoint == null ? null : new URL(endpoint);
            return endpointUrl != null && "https".equalsIgnoreCase(endpointUrl.getProtocol())
                ? endpointUrl.toString()
                : fallback;
        } catch (IOException ex) {
            return fallback;
        }
    }

    private static Map<String, String> parseSettings(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return reader.readObject(root -> {
                Map<String, String> settings = Collections.emptyMap();
                while (root.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = root.getFieldName();
                    root.nextToken();
                    if ("settings".equals(fieldName)) {
                        settings = root.readObject(settingReader -> {
                            Map<String, String> result = new HashMap<>();
                            while (settingReader.nextToken() != JsonToken.END_OBJECT) {
                                String settingName = settingReader.getFieldName();
                                settingReader.nextToken();
                                if (settingReader.currentToken() == JsonToken.STRING) {
                                    result.put(settingName, settingReader.getString());
                                } else {
                                    settingReader.skipChildren();
                                }
                            }
                            return result;
                        });
                    } else {
                        root.skipChildren();
                    }
                }
                return settings;
            });
        }
    }

    private static List<String> parseStringArray(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return reader.readArray(itemReader -> itemReader.getString());
        } catch (IOException | RuntimeException ex) {
            return Collections.emptyList();
        }
    }

    private static String readBody(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
}
