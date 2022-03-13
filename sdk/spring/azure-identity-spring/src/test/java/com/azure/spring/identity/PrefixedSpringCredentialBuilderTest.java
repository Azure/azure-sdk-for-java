// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrefixedSpringCredentialBuilderTest extends SpringCredentialTestBase {

    @Test
    public void testWithNoEnvironmentSet() {
        final PrefixedSpringCredentialBuilder builder = new PrefixedSpringCredentialBuilder();
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testWithNoPrefixSet() {
        final PrefixedSpringCredentialBuilder builder = new PrefixedSpringCredentialBuilder();
        builder.environment(buildEnvironment(new Properties()));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testBuild() {
        final PrefixedSpringCredentialBuilderExt builder = new PrefixedSpringCredentialBuilderExt();
        final TokenCredential tokenCredential = builder.prefix("test-prefix")
                                                       .environment(buildEnvironment(new Properties()))
                                                       .build();
        assertTrue(tokenCredential instanceof ManagedIdentityCredential);
        assertEquals(1, builder.prefixes.size());
        assertEquals(Arrays.asList("test-prefix"), builder.prefixes);
    }

    static class PrefixedSpringCredentialBuilderExt extends PrefixedSpringCredentialBuilder {

        List<String> prefixes = new ArrayList<>();

        @Override
        protected TokenCredential populateTokenCredential(String prefix) {
            this.prefixes.add(prefix);
            return super.populateTokenCredential(prefix);
        }

    }

}
