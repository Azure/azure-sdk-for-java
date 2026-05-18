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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link SubjectAlternativeNames}, focusing on the ipAddresses and uniformResourceIdentifiers fields.
 */
class SubjectAlternativeNamesTest {

    @Test
    void getAndSetIpAddresses() {
        SubjectAlternativeNames san = new SubjectAlternativeNames();
        assertNull(san.getIpAddresses());

        List<String> ips = Arrays.asList("10.0.0.1", "192.168.1.100", "2001:db8::1");
        SubjectAlternativeNames result = san.setIpAddresses(ips);

        assertEquals(ips, san.getIpAddresses());
        assertEquals(san, result, "setIpAddresses should return the same instance for fluent chaining");
    }

    @Test
    void getAndSetUniformResourceIdentifiers() {
        SubjectAlternativeNames san = new SubjectAlternativeNames();
        assertNull(san.getUniformResourceIdentifiers());

        List<String> uris = Arrays.asList("https://example.com", "spiffe://cluster.local/ns/default/sa/myapp");
        SubjectAlternativeNames result = san.setUniformResourceIdentifiers(uris);

        assertEquals(uris, san.getUniformResourceIdentifiers());
        assertEquals(san, result, "setUniformResourceIdentifiers should return the same instance for fluent chaining");
    }

    @Test
    void jsonRoundTripIpAddressesOnly() throws IOException {
        List<String> ips = Arrays.asList("10.0.0.1", "192.168.1.100");
        SubjectAlternativeNames original = new SubjectAlternativeNames().setIpAddresses(ips);

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals(ips, deserialized.getIpAddresses());
        assertNull(deserialized.getEmails());
        assertNull(deserialized.getDnsNames());
        assertNull(deserialized.getUserPrincipalNames());
        assertNull(deserialized.getUniformResourceIdentifiers());
    }

    @Test
    void jsonRoundTripUniformResourceIdentifiersOnly() throws IOException {
        List<String> uris = Arrays.asList("https://example.com", "spiffe://cluster.local/ns/default/sa/myapp");
        SubjectAlternativeNames original = new SubjectAlternativeNames().setUniformResourceIdentifiers(uris);

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals(uris, deserialized.getUniformResourceIdentifiers());
        assertNull(deserialized.getEmails());
        assertNull(deserialized.getDnsNames());
        assertNull(deserialized.getUserPrincipalNames());
        assertNull(deserialized.getIpAddresses());
    }

    @Test
    void jsonRoundTripIpv6Addresses() throws IOException {
        List<String> ips = Arrays.asList("2001:db8::1", "::1", "fe80::1%25eth0");
        SubjectAlternativeNames original = new SubjectAlternativeNames().setIpAddresses(ips);

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals(ips, deserialized.getIpAddresses());
    }

    @Test
    void jsonRoundTripAllFields() throws IOException {
        SubjectAlternativeNames original = new SubjectAlternativeNames().setEmails(Arrays.asList("user@example.com"))
            .setDnsNames(Arrays.asList("example.com", "www.example.com"))
            .setUserPrincipalNames(Arrays.asList("user@contoso.onmicrosoft.com"))
            .setUniformResourceIdentifiers(Arrays.asList("https://example.com/api"))
            .setIpAddresses(Arrays.asList("10.0.0.1", "2001:db8::1"));

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals(original.getEmails(), deserialized.getEmails());
        assertEquals(original.getDnsNames(), deserialized.getDnsNames());
        assertEquals(original.getUserPrincipalNames(), deserialized.getUserPrincipalNames());
        assertEquals(original.getUniformResourceIdentifiers(), deserialized.getUniformResourceIdentifiers());
        assertEquals(original.getIpAddresses(), deserialized.getIpAddresses());
    }

    @Test
    void jsonRoundTripEmptyLists() throws IOException {
        SubjectAlternativeNames original = new SubjectAlternativeNames().setIpAddresses(Collections.emptyList())
            .setUniformResourceIdentifiers(Collections.emptyList());

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertNotNull(deserialized.getIpAddresses());
        assertEquals(0, deserialized.getIpAddresses().size());
        assertNotNull(deserialized.getUniformResourceIdentifiers());
        assertEquals(0, deserialized.getUniformResourceIdentifiers().size());
    }

    @Test
    void jsonRoundTripSingleIpAddress() throws IOException {
        SubjectAlternativeNames original
            = new SubjectAlternativeNames().setIpAddresses(Collections.singletonList("127.0.0.1"));

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals(1, deserialized.getIpAddresses().size());
        assertEquals("127.0.0.1", deserialized.getIpAddresses().get(0));
    }

    @Test
    void jsonRoundTripSingleUri() throws IOException {
        SubjectAlternativeNames original = new SubjectAlternativeNames()
            .setUniformResourceIdentifiers(Collections.singletonList("https://contoso.vault.azure.net"));

        SubjectAlternativeNames deserialized = roundTrip(original);

        assertNotNull(deserialized);
        assertEquals(1, deserialized.getUniformResourceIdentifiers().size());
        assertEquals("https://contoso.vault.azure.net", deserialized.getUniformResourceIdentifiers().get(0));
    }

    @Test
    void deserializeFromJsonWithIpAddresses() throws IOException {
        String json = "{\"ipAddresses\":[\"10.0.0.1\",\"192.168.1.100\"]}";

        SubjectAlternativeNames deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = SubjectAlternativeNames.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(Arrays.asList("10.0.0.1", "192.168.1.100"), deserialized.getIpAddresses());
        assertNull(deserialized.getEmails());
        assertNull(deserialized.getDnsNames());
    }

    @Test
    void deserializeFromJsonWithUris() throws IOException {
        String json = "{\"uris\":[\"https://example.com\",\"spiffe://trust-domain/path\"]}";

        SubjectAlternativeNames deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = SubjectAlternativeNames.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(Arrays.asList("https://example.com", "spiffe://trust-domain/path"),
            deserialized.getUniformResourceIdentifiers());
        assertNull(deserialized.getIpAddresses());
    }

    @Test
    void deserializeFromJsonWithAllSanFields() throws IOException {
        String json = "{" + "\"emails\":[\"user@example.com\"]," + "\"dns_names\":[\"example.com\"],"
            + "\"upns\":[\"user@contoso.onmicrosoft.com\"]," + "\"uris\":[\"https://example.com\"],"
            + "\"ipAddresses\":[\"10.0.0.1\"]" + "}";

        SubjectAlternativeNames deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = SubjectAlternativeNames.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(Collections.singletonList("user@example.com"), deserialized.getEmails());
        assertEquals(Collections.singletonList("example.com"), deserialized.getDnsNames());
        assertEquals(Collections.singletonList("user@contoso.onmicrosoft.com"), deserialized.getUserPrincipalNames());
        assertEquals(Collections.singletonList("https://example.com"), deserialized.getUniformResourceIdentifiers());
        assertEquals(Collections.singletonList("10.0.0.1"), deserialized.getIpAddresses());
    }

    @Test
    void deserializeIgnoresUnknownFields() throws IOException {
        String json = "{\"ipAddresses\":[\"10.0.0.1\"],\"unknownField\":\"someValue\"}";

        SubjectAlternativeNames deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = SubjectAlternativeNames.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(Collections.singletonList("10.0.0.1"), deserialized.getIpAddresses());
    }

    @Test
    void serializeIpAddressesUsesCorrectJsonFieldName() throws IOException {
        SubjectAlternativeNames san
            = new SubjectAlternativeNames().setIpAddresses(Collections.singletonList("10.0.0.1"));

        String json = toJsonString(san);

        // The JSON field name for ipAddresses is "ipAddresses"
        assertNotNull(json);
        assertEquals(true, json.contains("\"ipAddresses\""));
        assertEquals(true, json.contains("\"10.0.0.1\""));
    }

    @Test
    void serializeUrisUsesCorrectJsonFieldName() throws IOException {
        SubjectAlternativeNames san = new SubjectAlternativeNames()
            .setUniformResourceIdentifiers(Collections.singletonList("https://example.com"));

        String json = toJsonString(san);

        // The JSON field name for uniformResourceIdentifiers is "uris"
        assertNotNull(json);
        assertEquals(true, json.contains("\"uris\""));
        assertEquals(true, json.contains("\"https://example.com\""));
    }

    @Test
    void nullFieldsAreOmittedInSerialization() throws IOException {
        SubjectAlternativeNames san = new SubjectAlternativeNames();

        String json = toJsonString(san);

        // With all fields null, the JSON should be an empty object
        assertNotNull(json);
        assertEquals(false, json.contains("\"ipAddresses\""));
        assertEquals(false, json.contains("\"uris\""));
        assertEquals(false, json.contains("\"emails\""));
        assertEquals(false, json.contains("\"dns_names\""));
        assertEquals(false, json.contains("\"upns\""));
    }

    private static SubjectAlternativeNames roundTrip(SubjectAlternativeNames original) throws IOException {
        String json = toJsonString(original);
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return SubjectAlternativeNames.fromJson(reader);
        }
    }

    private static String toJsonString(SubjectAlternativeNames san) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            san.toJson(writer);
        }
        return outputStream.toString("UTF-8");
    }
}
