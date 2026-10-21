// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Objects;

public class EdgeActionsSanitizerTests {
    @ParameterizedTest
    @CsvSource({
        "?t=123&c=encoded%26value&s=signature%3D&h=hash, ?t=REDACTED&c=REDACTED&s=REDACTED&h=REDACTED",
        "?api-version=2026-10-01&C=opaque#fragment, ?api-version=2026-10-01&C=REDACTED#fragment",
        "?count=2&st=unchanged&c=&other=value, ?count=2&st=unchanged&c=&other=value" })
    public void preservesNonSecretQueryFields(String query, String expected) {
        String path = "https://example.test/operations/one";
        Assertions.assertEquals(path + expected, sanitize(TestProxySanitizerType.URL, null, path + query));
        for (String header : new String[] { "Location", "Azure-AsyncOperation", "Operation-Location" }) {
            Assertions.assertEquals(path + expected, sanitize(TestProxySanitizerType.HEADER, header, path + query));
        }
    }

    @Test
    public void preservesJsonAndResourceIdentity() {
        String resource = "/subscriptions/11111111-2222-3333-4444-555555555555"
            + "/resourceGroups/test-rg/providers/Microsoft.Cdn/edgeActions/test-action";
        String body = "{\"versionId\":\"" + resource + "/versions/v1\","
            + "\"pollingUrl\":\"https://example.test/operations/one?api-version=2026-10-01&c=opaque\","
            + "\"name\":\"test-action\",\"sku\":{\"name\":\"Standard\"}}";
        String expected = body.replace("11111111-2222-3333-4444-555555555555", "00000000-0000-0000-0000-000000000000")
            .replace("c=opaque", "c=REDACTED");
        Assertions.assertEquals(expected, sanitize(TestProxySanitizerType.BODY_REGEX, null, body));
        Assertions.assertEquals("{\"message\":\"?c=REDACTED\\\" trailing\"}",
            sanitize(TestProxySanitizerType.BODY_REGEX, null, "{\"message\":\"?c=opaque\\\" trailing\"}"));
        Assertions.assertEquals("{\"scope\":\"/subscriptions/00000000-0000-0000-0000-000000000000\"}",
            sanitize(TestProxySanitizerType.BODY_REGEX, null,
                "{\"scope\":\"/subscriptions/11111111-2222-3333-4444-555555555555\"}"));
    }

    @Test
    public void redactsSensitiveFieldsAndHeaders() {
        for (String field : new String[] {
            "access_token",
            "refresh_token",
            "id_token",
            "client_secret",
            "systemData.createdBy",
            "systemData.lastModifiedBy" }) {
            Assertions.assertEquals("REDACTED", sanitize(TestProxySanitizerType.BODY_KEY, "$.." + field, "secret"));
        }
        for (String field : new String[] { "tenantId", "clientId", "principalId" }) {
            Assertions.assertEquals("00000000-0000-0000-0000-000000000000",
                sanitize(TestProxySanitizerType.BODY_KEY, "$.." + field, "11111111-2222-3333-4444-555555555555"));
        }
        for (String header : new String[] {
            "x-ms-correlation-request-id",
            "x-ms-routing-request-id",
            "x-ms-operation-identifier",
            "x-ms-original-request-ids",
            "X-MSEdge-Ref",
            "Set-Cookie",
            "Cookie" }) {
            Assertions.assertEquals("REDACTED", sanitize(TestProxySanitizerType.HEADER, header, "secret"));
        }
        Assertions.assertEquals("test-action", sanitize(TestProxySanitizerType.BODY_KEY, "$..name", "test-action"));
    }

    private static String sanitize(TestProxySanitizerType type, String key, String value) {
        for (TestProxySanitizer sanitizer : EdgeActionsTestBase.edgeActionsSanitizers()) {
            if (sanitizer.getType() == type && Objects.equals(key, sanitizer.getKey())) {
                value = sanitizer.getRegex() == null
                    ? sanitizer.getRedactedValue()
                    : value.replaceAll(BinaryData.fromString("\"" + sanitizer.getRegex() + "\"").toObject(String.class),
                        sanitizer.getRedactedValue());
            }
        }
        return value;
    }
}
