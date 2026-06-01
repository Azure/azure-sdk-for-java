// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformManagedTest {
    @Test
    void getAndSetProperties() {
        PlatformManaged platformManaged = new PlatformManaged("serverAuth");
        Map<String, Object> metadata = createMetadata();

        PlatformManaged result = platformManaged.setCertificateUsage("clientAuth").setMetadata(metadata);

        assertSame(platformManaged, result);
        assertEquals("clientAuth", platformManaged.getCertificateUsage());
        assertSame(metadata, platformManaged.getMetadata());
    }

    @Test
    void constructorRequiresCertificateUsage() {
        assertThrows(NullPointerException.class, () -> new PlatformManaged(null));
    }

    @Test
    void setCertificateUsageRequiresNonNullValue() {
        PlatformManaged platformManaged = new PlatformManaged("serverAuth");

        assertThrows(NullPointerException.class, () -> platformManaged.setCertificateUsage(null));
    }

    @Test
    void jsonRoundTripWithMetadata() throws IOException {
        PlatformManaged original = new PlatformManaged("serverAuth").setMetadata(createMetadata());

        PlatformManaged deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals("serverAuth", deserialized.getCertificateUsage());
        assertEquals("contoso", deserialized.getMetadata().get("service"));
        assertEquals(3, ((Number) deserialized.getMetadata().get("revision")).intValue());
        assertEquals(true, deserialized.getMetadata().get("enabled"));
        assertEquals(Arrays.asList("alpha", "beta"), deserialized.getMetadata().get("labels"));
        assertEquals("westus", ((Map<?, ?>) deserialized.getMetadata().get("details")).get("region"));
    }

    @Test
    void jsonRoundTripWithoutMetadata() throws IOException {
        PlatformManaged original = new PlatformManaged("serverAuth");

        String json = toJsonString(original);
        PlatformManaged deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = PlatformManaged.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals("serverAuth", deserialized.getCertificateUsage());
        assertFalse(json.contains("\"metadata\""));
    }

    @Test
    void deserializeIgnoresUnknownFields() throws IOException {
        String json = "{\"certificateUsage\":\"serverAuth\",\"unknownField\":\"ignored\"}";

        PlatformManaged deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = PlatformManaged.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals("serverAuth", deserialized.getCertificateUsage());
    }

    @Test
    void serializeUsesExpectedJsonFieldNames() throws IOException {
        PlatformManaged platformManaged = new PlatformManaged("serverAuth").setMetadata(createMetadata());

        String json = toJsonString(platformManaged);

        assertTrue(json.contains("\"certificateUsage\""));
        assertTrue(json.contains("\"metadata\""));
        assertTrue(json.contains("\"service\""));
    }

    @Test
    void deserializeRejectsMissingCertificateUsage() {
        String json = "{\"metadata\":{\"foo\":\"bar\"}}";

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            try (JsonReader reader = JsonProviders.createReader(json)) {
                PlatformManaged.fromJson(reader);
            }
        });
        assertTrue(ex.getMessage().contains("certificateUsage"));
    }

    @Test
    void deserializeRejectsEmptyObject() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            try (JsonReader reader = JsonProviders.createReader("{}")) {
                PlatformManaged.fromJson(reader);
            }
        });
        assertTrue(ex.getMessage().contains("certificateUsage"));
    }

    private static PlatformManaged roundTrip(PlatformManaged original) throws IOException {
        String json = toJsonString(original);
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return PlatformManaged.fromJson(reader);
        }
    }

    private static String toJsonString(PlatformManaged platformManaged) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            platformManaged.toJson(writer);
        }
        return outputStream.toString("UTF-8");
    }

    private static Map<String, Object> createMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("service", "contoso");
        metadata.put("revision", 3);
        metadata.put("enabled", true);
        metadata.put("labels", Arrays.asList("alpha", "beta"));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("region", "westus");
        metadata.put("details", details);

        return metadata;
    }
}
