// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers;

import org.junit.jupiter.api.Test;

public abstract class AbstractAuthenticationPluginTest {
    protected static final String OSSRDBMS_SCOPES = "https://ossrdbms-aad.database.windows.net/.default";

    protected abstract void tokenAudienceShouldConfig();

    @Test
    protected void testTokenAudienceShouldConfig() {
        tokenAudienceShouldConfig();
    }

}
