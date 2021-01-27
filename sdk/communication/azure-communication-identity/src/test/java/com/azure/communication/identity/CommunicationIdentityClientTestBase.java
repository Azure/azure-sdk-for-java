// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunicationIdentityClientTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ENDPOINT", "https://REDACTED.communication.azure.com");
        
    protected static final String ACCESSKEYRAW = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    protected static final String ACCESSKEYENCODED = Base64.getEncoder().encodeToString(ACCESSKEYRAW.getBytes());
    protected static final String ACCESSKEY = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ACCESS_KEY", ACCESSKEYENCODED);

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=" + ACCESSKEYENCODED);
    
    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("id")
        .add("token");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()),
        Pattern.CASE_INSENSITIVE);

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClient(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.endpoint(ENDPOINT)
            .accessKey(ACCESSKEY)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
       
        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
            .endpoint(ENDPOINT)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
        
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientUsingConnectionString(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(CommunicationIdentityClientTestBase.class);
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            System.out.println("azureTestMode: " + azureTestMode);
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        } else {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
            return TestMode.PLAYBACK;
        }
    }
    
    protected CommunicationIdentityClientBuilder addLoggingPolicy(CommunicationIdentityClientBuilder builder, String testName) {
        return builder.addPolicy(new CommunicationLoggerPolicy(testName));
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }
    
    private String redact(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), replacement);
            }
        }

        return content;
    }
}
