// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.azure.spring.identity.DefaultSpringCredentialBuilder.AZURE_CREDENTIAL_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultSpringCredentialBuilderTest extends SpringCredentialTestBase {

    @Test
    public void testWithNoEnvironmentSet() {
        final DefaultSpringCredentialBuilder builder = new DefaultSpringCredentialBuilder();
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testWithoutAlternative() {
        final DefaultSpringCredentialBuilderExt builder = new DefaultSpringCredentialBuilderExt();
        final TokenCredential tokenCredential = builder.environment(buildEnvironment(new Properties()))
                                                       .build();

        assertTrue(tokenCredential instanceof ChainedTokenCredential);

        assertEquals(1, builder.prefixes.size());
        assertEquals(Arrays.asList(AZURE_CREDENTIAL_PREFIX), builder.prefixes);

        assertEquals(2, builder.tokenCredentials.size());
        assertNull(builder.tokenCredentials.get(0));
        assertTrue(builder.tokenCredentials.get(1) instanceof ManagedIdentityCredential);
    }

    @Test
    public void testWithAlternative() {
        final DefaultSpringCredentialBuilderExt builder = new DefaultSpringCredentialBuilderExt();
        final TokenCredential tokenCredential = builder.environment(buildEnvironment(new Properties()))
                                                       .alternativePrefix("abc.")
                                                       .build();

        assertTrue(tokenCredential instanceof ChainedTokenCredential);

        assertEquals(2, builder.prefixes.size());
        assertEquals(Arrays.asList("abc.", AZURE_CREDENTIAL_PREFIX), builder.prefixes);

        assertEquals(3, builder.tokenCredentials.size());
        assertNull(builder.tokenCredentials.get(0));
        assertNull(builder.tokenCredentials.get(1));
        assertTrue(builder.tokenCredentials.get(2) instanceof ManagedIdentityCredential);
    }

    @Test
    public void testPrefixWithNoPeriod() {
        final DefaultSpringCredentialBuilderExt builder = new DefaultSpringCredentialBuilderExt();
        final TokenCredential tokenCredential = builder.environment(buildEnvironment(new Properties()))
                                                       .alternativePrefix("abc")
                                                       .build();

        assertTrue(tokenCredential instanceof ChainedTokenCredential);
        assertEquals("abc.", builder.prefixes.get(0));
    }

    static class DefaultSpringCredentialBuilderExt extends DefaultSpringCredentialBuilder {

        List<String> prefixes = new ArrayList<>();
        List<TokenCredential> tokenCredentials = new ArrayList<>();

        @Override
        protected TokenCredential populateTokenCredentialBasedOnClientId(String prefix) {
            this.prefixes.add(prefix);
            final TokenCredential tokenCredential = super.populateTokenCredentialBasedOnClientId(prefix);
            tokenCredentials.add(tokenCredential);
            return tokenCredential;
        }

        @Override
        protected ManagedIdentityCredential defaultManagedIdentityCredential() {
            final ManagedIdentityCredential tokenCredential = super.defaultManagedIdentityCredential();
            tokenCredentials.add(tokenCredential);
            return tokenCredential;
        }
    }


}
