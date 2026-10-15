// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExternalKeyTest {
    @Test
    void externalKeyExposesId() {
        ExternalKey externalKey = new ExternalKey("external-key-reference-id");

        assertEquals("external-key-reference-id", externalKey.getId());
    }

    @Test
    void createExternalKeyOptionsExposesExternalKeyAndNoKeyType() {
        ExternalKey externalKey = new ExternalKey("external-key-reference-id");
        CreateExternalKeyOptions options = new CreateExternalKeyOptions("external-key-name", externalKey);

        assertEquals("external-key-name", options.getName());
        assertNotNull(options.getExternalKey());
        assertEquals("external-key-reference-id", options.getExternalKey().getId());
        // External keys are mutually exclusive with a key type, so no key type should be set.
        assertNull(options.getKeyType());
    }

    @Test
    void createExternalKeyOptionsFluentSettersReturnDerivedType() {
        CreateExternalKeyOptions options
            = new CreateExternalKeyOptions("external-key-name", new ExternalKey("external-key-reference-id"))
                .setEnabled(true)
                .setExportable(false)
                .setTags(java.util.Collections.singletonMap("key", "value"));

        assertEquals(Boolean.TRUE, options.isEnabled());
        assertEquals(Boolean.FALSE, options.isExportable());
        assertEquals("value", options.getTags().get("key"));
    }

    @Test
    void keyPropertiesDeserializesExternalKey() throws IOException {
        String json = "{\"kid\":\"https://managedhsmvaultname.managedhsm.azure.net/keys/external-key-name/version\","
            + "\"attributes\":{\"enabled\":true,\"external_key\":{\"id\":\"external-key-reference-id\"}}}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            KeyProperties properties = KeyProperties.fromJson(reader);

            assertNotNull(properties.getExternalKey());
            assertEquals("external-key-reference-id", properties.getExternalKey().getId());
        }
    }

    @Test
    void keyPropertiesWithoutExternalKeyIsNull() throws IOException {
        String json = "{\"kid\":\"https://managedhsmvaultname.managedhsm.azure.net/keys/rsa-key/version\","
            + "\"attributes\":{\"enabled\":true}}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            KeyProperties properties = KeyProperties.fromJson(reader);

            assertNull(properties.getExternalKey());
        }
    }
}
