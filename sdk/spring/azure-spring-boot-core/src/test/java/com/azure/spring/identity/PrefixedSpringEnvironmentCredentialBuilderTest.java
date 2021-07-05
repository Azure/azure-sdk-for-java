// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.CredentialUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PrefixedSpringEnvironmentCredentialBuilderTest extends SpringCredentialTestBase {

    @Test
    public void constructWithNullEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> new PrefixedSpringEnvironmentCredentialBuilder(null));
    }

    @Test
    public void testWithNoPrefixSet() {
        final PrefixedSpringEnvironmentCredentialBuilder builder = new PrefixedSpringEnvironmentCredentialBuilder(
            buildEnvironment(new Properties()));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    public void testBuildWithNoPropertiesSet() {
        final StandardEnvironment environment = buildEnvironment(new Properties());
        final TokenCredential tokenCredential = new PrefixedSpringEnvironmentCredentialBuilder(environment)
                                                    .prefix("test-prefix")
                                                    .build();
        assertTrue(tokenCredential instanceof PrefixedSpringEnvironmentCredential);


        StepVerifier.create(tokenCredential.getToken(new TokenRequestContext().addScopes("qux/.default")))
                    .expectError(CredentialUnavailableException.class)
                    .verify();
    }

    @Test
    public void testBuildWithClientIdAndSecretSet() {
        final Properties properties = new PropertiesBuilder().prefix("test-prefix")
                                                             .property("client-id", "fake-id")
                                                             .property("client-secret", "fake-secret")
                                                             .property("tenant-id", "fake-tenant")
                                                             .build();
        // TODO (xiaolu) change to use mock
//        final TokenCredential tokenCredential = new PrefixedSpringEnvironmentCredentialBuilder(
//            buildEnvironment(properties)).prefix("test-prefix").build();
//
//        StepVerifier.create(tokenCredential.getToken(new TokenRequestContext().addScopes("qux/.default"))
//                    .doOnSuccess(s -> fail())
//                    .onErrorResume(t -> {
//                        String message = t.getMessage();
//
//                        return Mono.just(new AccessToken("token", OffsetDateTime.MAX));
//                    }))
//            .expectNextMatches(token -> "token".equals(token.getToken()))
//                 .verifyComplete();
    }


}
