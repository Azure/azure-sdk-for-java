// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;

import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.containers.containerregistry.TestUtils.SLEEP_TIME_IN_MILLISECONDS;

public class ContainerRegistryClientsTestBase extends TestBase {

    private static final String AZURE_CONTAINERREGISTRY_ENDPOINT = "CONTAINERREGISTRY_ENDPOINT";

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile("(\".*_token\":\"(.*)\".*)");

    ContainerRegistryClientBuilder getContainerRegistryBuilder(HttpClient httpClient) {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));

        ContainerRegistryClientBuilder builder = new ContainerRegistryClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .addPolicy(interceptorManager.getRecordPolicy(redactors));

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        return builder;
    }

    ContainerRepositoryClientBuilder getContainerRepositoryBuilder(String repositoryName, HttpClient httpClient) {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));

        ContainerRepositoryClientBuilder builder = new ContainerRepositoryClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY))
            .addPolicy(interceptorManager.getRecordPolicy(redactors))
            .repository(repositoryName);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        return builder;
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }


    protected String getEndpoint() {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_CONTAINERREGISTRY_ENDPOINT);
    }

    private String redact(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            if (matcher.groupCount() == 2) {
                String captureGroup = matcher.group(1);
                if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                    content = content.replace(matcher.group(2), replacement);
                }
            }
        }

        return content;
    }

    void testDelay() {
        if (getTestMode() != TestMode.PLAYBACK) {
            // The service has a cache of 6 seconds, so we need to wait until we run this.
            try {
                Thread.sleep(SLEEP_TIME_IN_MILLISECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    Mono<Long> monoDelay() {
        return Mono.defer(() -> getTestMode() == TestMode.PLAYBACK ? Mono.delay(Duration.ZERO) : Mono.delay(Duration.ofMillis(SLEEP_TIME_IN_MILLISECONDS)));
    }
}
