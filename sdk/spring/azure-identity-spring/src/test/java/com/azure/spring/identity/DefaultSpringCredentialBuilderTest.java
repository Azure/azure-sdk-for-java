// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.VisualStudioCodeCredential;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultSpringCredentialBuilderTest extends SpringCredentialTestBase {

    @Test
    public void constructWithNullEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultSpringCredentialBuilder(null));
    }

    @Test
    public void testDefaultCredentialTypes() {
        final DefaultSpringCredentialBuilder builder = new DefaultSpringCredentialBuilder(
            buildEnvironment(new PropertiesBuilder().build()));

        final TokenCredential build = builder.build();

        assertTrue(build instanceof DefaultSpringCredential);

        final List<TokenCredential> tokenCredentials = ((DefaultSpringCredential) build).getTokenCredentials();
        assertEquals(6, tokenCredentials.size());
        assertTrue(tokenCredentials.get(0) instanceof SpringEnvironmentCredential);
        assertTrue(tokenCredentials.get(1) instanceof ManagedIdentityCredential);
        assertTrue(tokenCredentials.get(2) instanceof IntelliJCredential);
        assertTrue(tokenCredentials.get(3) instanceof VisualStudioCodeCredential);
        assertTrue(tokenCredentials.get(4) instanceof AzureCliCredential);
        assertTrue(tokenCredentials.get(5) instanceof AzurePowerShellCredential);
    }


}
