// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class AuthClientTest {

    /**
     * Test getAuthorizationToken method.
     */
    @Test
    public void testGetAuthorizationToken() throws Exception {
        String tenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";
        String clientId = "2b8f123b-b18a-4077-bce0-42e10ce8bbab";
        String clientSecret = "72-~tZ9~cG~rimDI0EkQSMQ1D9DYmGmI_I";
        AuthClient authClient = new AuthClient();
        String result = authClient.getAccessToken(
            "https://management.azure.com/",
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, "UTF-8")
        );
        assertNotNull(result);
    }
}
