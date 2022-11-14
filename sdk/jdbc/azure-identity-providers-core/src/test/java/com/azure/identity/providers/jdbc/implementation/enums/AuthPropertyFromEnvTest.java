// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.enums;

import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthPropertyFromEnvTest {

    @Test
    void testGetValuesFromPropertiesAndEnv() throws Exception {
        new EnvironmentVariables()
            .set("AZURE_CLIENT_ID", "ENV_AZURE_CLIENT_ID")
            .set("AZURE_CLIENT_SECRET", "ENV_AZURE_CLIENT_SECRET")
            .set("AZURE_CLIENT_CERTIFICATE_PATH", "ENV_AZURE_CLIENT_CERTIFICATE_PATH")
            .set("AZURE_USERNAME", "ENV_AZURE_USERNAME")
            .set("AZURE_PASSWORD", "ENV_AZURE_PASSWORD")
            .set("AZURE_AUTHORITY_HOST", "ENV_AZURE_AUTHORITY_HOST")
            .set("AZURE_TENANT_ID", "ENV_AZURE_TENANT_ID")
            .execute(() -> {

                Properties properties = new Properties();
                properties.put("azure.clientSecret", "fake-clientSecret");
                properties.put("azure.clientCertificatePath", "fake-clientCertificatePath");
                properties.put("azure.password", "fake-password");
                properties.put("azure.authorityHost", "fake-authorityHost");
                properties.put("azure.tenantId", "fake-azure.tenantId");

                assertEquals("ENV_AZURE_CLIENT_ID", AuthProperty.CLIENT_ID.get(properties));
                assertEquals("fake-clientSecret", AuthProperty.CLIENT_SECRET.get(properties));
                assertEquals("fake-clientCertificatePath", AuthProperty.CLIENT_CERTIFICATE_PATH.get(properties));
                assertEquals("ENV_AZURE_USERNAME", AuthProperty.USERNAME.get(properties));
                assertEquals("fake-password", AuthProperty.PASSWORD.get(properties));
                assertEquals("fake-authorityHost", AuthProperty.AUTHORITY_HOST.get(properties));
                assertEquals("fake-azure.tenantId", AuthProperty.TENANT_ID.get(properties));
            });
    }

}
