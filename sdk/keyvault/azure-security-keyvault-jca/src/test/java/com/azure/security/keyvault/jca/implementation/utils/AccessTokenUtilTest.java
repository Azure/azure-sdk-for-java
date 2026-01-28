// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.PropertyConvertorUtils;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil.getLoginUri;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.API_VERSION_POSTFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.addTrailingSlashIfRequired;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The JUnit test for the AuthClient.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class AccessTokenUtilTest {
    /**
     * Test getAuthorizationToken method.
     */
    @Test
    public void testGetAuthorizationToken() {
        String tenantId = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_TENANT_ID");
        String clientId = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_ID");
        String clientSecret = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_SECRET");
        String keyVaultEndpoint
            = addTrailingSlashIfRequired(PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"));
        String aadAuthenticationUri = getLoginUri(keyVaultEndpoint + "certificates" + API_VERSION_POSTFIX, false);
        AccessToken result
            = AccessTokenUtil.getAccessToken(keyVaultEndpoint, aadAuthenticationUri, tenantId, clientId, clientSecret);

        assertNotNull(result);
    }

    @Test
    public void testGetLoginUri() {
        String keyVaultEndpoint = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT");
        String result = getLoginUri(keyVaultEndpoint + "certificates" + API_VERSION_POSTFIX, false);

        assertNotNull(result);
        assertDoesNotThrow(() -> new URI(result));
    }

    @Test
    void testReadFile(@TempDir Path tempDir) throws Exception {
        Path tempFile = Files.createTempFile(tempDir, "simple_text_file_", ".txt");
        String expectedContent = "Just a dummy string";
        Files.write(tempFile, expectedContent.getBytes(StandardCharsets.UTF_8));

        String actualContent = AccessTokenUtil.readFile(tempFile.toAbsolutePath().toString());
        assertNotNull(actualContent);
        assertEquals(expectedContent, actualContent);
    }
}
