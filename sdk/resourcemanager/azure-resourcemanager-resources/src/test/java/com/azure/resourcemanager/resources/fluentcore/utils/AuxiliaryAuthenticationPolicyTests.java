// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.resources.fluentcore.policy.AuxiliaryAuthenticationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuxiliaryAuthenticationPolicyTests {
    static class MockTokenCredential implements TokenCredential {
        final AccessToken token;

        MockTokenCredential(String token) {
            this.token = new AccessToken(token, OffsetDateTime.MAX);
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(token);
        }
    }

    static class CheckAuxiliaryAuthenticationHeaderPolicy implements HttpPipelinePolicy {
        private static final String AUTHORIZATION_AUXILIARY_HEADER = "x-ms-authorization-auxiliary";
        private static final Pattern SCHEMA_FORMAT = Pattern.compile("Bearer \\S+");

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            String auxiliaryHeader = context.getHttpRequest().getHeaders().getValue(AUTHORIZATION_AUXILIARY_HEADER);
            Assertions.assertTrue(auxiliaryHeader != null && !auxiliaryHeader.isEmpty());
            for (String authentication : auxiliaryHeader.split(",")) {
                Matcher m = SCHEMA_FORMAT.matcher(authentication);
                Assertions.assertTrue(m.find());
                Assertions.assertEquals(authentication, m.group());
            }
            return Mono.empty();
        }
    }

    @Test
    public void canSetAuxiliaryAuthenticationHeader() {
        TokenCredential first = new MockTokenCredential("abc");
        TokenCredential second = new MockTokenCredential("def");
        new HttpPipelineBuilder()
            .policies(new AuxiliaryAuthenticationPolicy(AzureEnvironment.AZURE, first, second), new CheckAuxiliaryAuthenticationHeaderPolicy())
            .build()
            .send(new HttpRequest(HttpMethod.GET, "https://httpbin.org"))
            .block();
    }
}
