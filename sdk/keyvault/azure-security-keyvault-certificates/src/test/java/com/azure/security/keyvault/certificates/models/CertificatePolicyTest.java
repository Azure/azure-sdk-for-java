// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificatePolicyTest {
    @Test
    void getAndSetPlatformManaged() {
        CertificatePolicy policy = CertificatePolicy.getDefault();
        PlatformManaged platformManaged = new PlatformManaged("serverAuth");

        CertificatePolicy result = policy.setPlatformManaged(platformManaged);

        assertSame(policy, result);
        assertSame(platformManaged, policy.getPlatformManaged());
    }

    @Test
    void platformManagedDefaultsToNull() {
        assertNull(CertificatePolicy.getDefault().getPlatformManaged());
    }

    @Test
    void jsonRoundTripPlatformManaged() throws IOException {
        CertificatePolicy policy = CertificatePolicy.getDefault()
            .setPlatformManaged(
                new PlatformManaged("serverAuth").setMetadata(Collections.singletonMap("service", "contoso")));

        CertificatePolicy deserialized = roundTrip(policy);

        assertNotNull(deserialized);
        assertNotNull(deserialized.getPlatformManaged());
        assertEquals("serverAuth", deserialized.getPlatformManaged().getCertificateUsage());
        assertEquals("contoso", deserialized.getPlatformManaged().getMetadata().get("service"));
    }

    @Test
    void serializePlatformManagedUsesExpectedJsonFieldName() throws IOException {
        CertificatePolicy policy = CertificatePolicy.getDefault().setPlatformManaged(new PlatformManaged("serverAuth"));

        String json = toJsonString(policy);

        assertTrue(json.contains("\"platformManaged\""));
        assertTrue(json.contains("\"certificateUsage\""));
    }

    @Test
    void nullPlatformManagedIsOmittedInSerialization() throws IOException {
        String json = toJsonString(CertificatePolicy.getDefault());

        assertFalse(json.contains("\"platformManaged\""));
    }

    private static CertificatePolicy roundTrip(CertificatePolicy original) throws IOException {
        String json = toJsonString(original);
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return CertificatePolicy.fromJson(reader);
        }
    }

    private static String toJsonString(CertificatePolicy policy) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            policy.toJson(writer);
        }
        return outputStream.toString("UTF-8");
    }
}
