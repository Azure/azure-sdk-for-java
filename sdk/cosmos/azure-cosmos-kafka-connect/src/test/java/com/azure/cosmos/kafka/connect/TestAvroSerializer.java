// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lightweight Avro serializer for integration tests that produces the Confluent wire format
 * (magic byte 0x0 + 4-byte schema ID + Avro binary data) without requiring the
 * io.confluent:kafka-avro-serializer dependency.
 *
 * This serializer registers schemas with a Schema Registry via its REST API and caches
 * the resulting schema IDs.
 */
public class TestAvroSerializer implements Serializer<GenericRecord> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestAvroSerializer.class);
    private static final byte MAGIC_BYTE = 0x0;

    private String schemaRegistryUrl;
    private String basicAuthHeader;
    private boolean isKey;
    private final Map<String, Integer> schemaIdCache = new ConcurrentHashMap<>();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.isKey = isKey;
        this.schemaRegistryUrl = (String) configs.get("schema.registry.url");
        if (this.schemaRegistryUrl != null && this.schemaRegistryUrl.endsWith("/")) {
            this.schemaRegistryUrl = this.schemaRegistryUrl.substring(0, this.schemaRegistryUrl.length() - 1);
        }

        String authSource = (String) configs.get("basic.auth.credentials.source");
        if ("USER_INFO".equals(authSource)) {
            String userInfo = (String) configs.get("basic.auth.user.info");
            if (userInfo != null) {
                this.basicAuthHeader =
                    "Basic " + Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @Override
    public byte[] serialize(String topic, GenericRecord record) {
        if (record == null) {
            return null;
        }

        try {
            String subject = topic + (isKey ? "-key" : "-value");
            int schemaId = getOrRegisterSchemaId(subject, record.getSchema().toString());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC_BYTE);
            out.write(ByteBuffer.allocate(4).putInt(schemaId).array());

            GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(record, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Avro record", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }

    private int getOrRegisterSchemaId(String subject, String schemaJson) {
        String cacheKey = subject + ":" + schemaJson;
        Integer cachedId = schemaIdCache.get(cacheKey);
        if (cachedId != null) {
            return cachedId;
        }

        try {
            int id = registerSchema(subject, schemaJson);
            schemaIdCache.put(cacheKey, id);
            return id;
        } catch (IOException e) {
            throw new RuntimeException("Failed to register schema for subject " + subject, e);
        }
    }

    private int registerSchema(String subject, String schemaJson) throws IOException {
        String url = schemaRegistryUrl + "/subjects/" + subject + "/versions";
        LOGGER.info("Registering schema for subject {} at {}", subject, url);

        String escapedSchema = schemaJson.replace("\\", "\\\\").replace("\"", "\\\"");
        String requestBody = "{\"schema\": \"" + escapedSchema + "\"}";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/vnd.schemaregistry.v1+json");
            conn.setRequestProperty("Accept", "application/vnd.schemaregistry.v1+json");
            if (basicAuthHeader != null) {
                conn.setRequestProperty("Authorization", basicAuthHeader);
            }
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String errorBody = readStream(conn.getErrorStream());
                throw new IOException(
                    "Schema registration failed with HTTP " + responseCode + ": " + errorBody);
            }

            String response = readStream(conn.getInputStream());
            return parseSchemaId(response);
        } finally {
            conn.disconnect();
        }
    }

    private static String readStream(InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }
    }

    private static int parseSchemaId(String response) throws IOException {
        // Response format: {"id": N} - parse the id value
        int idIndex = response.indexOf("\"id\"");
        if (idIndex == -1) {
            throw new IOException("No schema ID found in response: " + response);
        }
        int colonPos = response.indexOf(':', idIndex);
        int commaPos = response.indexOf(',', colonPos);
        int bracePos = response.indexOf('}', colonPos);
        int endPos;
        if (commaPos == -1) {
            endPos = bracePos;
        } else {
            endPos = Math.min(commaPos, bracePos);
        }
        return Integer.parseInt(response.substring(colonPos + 1, endPos).trim());
    }
}
