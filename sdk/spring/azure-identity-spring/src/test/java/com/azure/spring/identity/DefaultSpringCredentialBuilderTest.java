// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredential;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.azure.spring.identity.DefaultSpringCredentialBuilder.AZURE_CREDENTIAL_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(Lists.newArrayList(AZURE_CREDENTIAL_PREFIX), builder.prefixes);
    }

    @Test
    public void testWithAlternative() {
        final DefaultSpringCredentialBuilderExt builder = new DefaultSpringCredentialBuilderExt();
        final TokenCredential tokenCredential = builder.environment(buildEnvironment(new Properties()))
                                                       .alternativePrfix("abc.")
                                                       .build();

        assertTrue(tokenCredential instanceof ChainedTokenCredential);
        assertEquals(2, builder.prefixes.size());
        assertEquals(Lists.newArrayList("abc.", AZURE_CREDENTIAL_PREFIX), builder.prefixes);
    }

    @Test
    public void testPrefixWithNoPeriod() {
        final DefaultSpringCredentialBuilderExt builder = new DefaultSpringCredentialBuilderExt();
        final TokenCredential tokenCredential = builder.environment(buildEnvironment(new Properties()))
                                                       .alternativePrfix("abc")
                                                       .build();

        assertEquals("abc.", builder.prefixes.get(0));
    }


    static class DefaultSpringCredentialBuilderExt extends DefaultSpringCredentialBuilder {

        List<String> prefixes = new ArrayList<>();

        @Override
        protected TokenCredential populateTokenCredential(String prefix) {
            this.prefixes.add(prefix);
            return super.populateTokenCredential(prefix);
        }

    }


}
