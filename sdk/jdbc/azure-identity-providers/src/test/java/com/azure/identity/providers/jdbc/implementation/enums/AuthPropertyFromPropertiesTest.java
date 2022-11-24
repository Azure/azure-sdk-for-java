// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.enums;

import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthPropertyFromPropertiesTest {

    @Test
    void testGetValuesFromProperties() throws Exception {
        new EnvironmentVariables()
            .execute(() -> {
                Properties properties = new Properties();
                properties.put("azure.clientId", "fake-clientId");
                properties.put("azure.clientSecret", "fake-clientSecret");
                properties.put("azure.clientCertificatePath", "fake-clientCertificatePath");
                properties.put("azure.username", "fake-username");
                properties.put("azure.password", "fake-password");
                properties.put("azure.authorityHost", "fake-authorityHost");
                properties.put("azure.tenantId", "fake-azure.tenantId");

                assertEquals("fake-clientId", AuthProperty.CLIENT_ID.get(properties));
                assertEquals("fake-clientSecret", AuthProperty.CLIENT_SECRET.get(properties));
                assertEquals("fake-clientCertificatePath", AuthProperty.CLIENT_CERTIFICATE_PATH.get(properties));
                assertEquals("fake-username", AuthProperty.USERNAME.get(properties));
                assertEquals("fake-password", AuthProperty.PASSWORD.get(properties));
                assertEquals("fake-authorityHost", AuthProperty.AUTHORITY_HOST.get(properties));
                assertEquals("fake-azure.tenantId", AuthProperty.TENANT_ID.get(properties));
            });
    }

    @Test
    void testDefaultValues() {
        Properties properties = new Properties();
        assertEquals("https://login.microsoftonline.com/", AuthProperty.AUTHORITY_HOST.get(properties));
    }

}
