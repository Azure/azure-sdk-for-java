// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public abstract class SipRoutingIntegrationTestBase extends TestBase {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    protected static final String NOT_EXISTING_FQDN = "not.existing.fqdn";

    protected static final String SET_TRUNK_FQDN = "4.fqdn.com";
    protected static final int SET_TRUNK_PORT = 4567;
    protected static final SipTrunk SET_TRUNK = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_PORT);

    protected static final int SET_TRUNK_UPDATED_PORT = 7651;
    protected static final SipTrunk SET_UPDATED_TRUNK = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_UPDATED_PORT);

    protected static final String DELETE_FQDN = "delete.fqdn.com";
    protected static final int DELETE_PORT = 5678;
    protected static final SipTrunk DELETE_TRUNK = new SipTrunk(DELETE_FQDN, DELETE_PORT);

    protected static final String SET_TRUNK_INVALID_FQDN = "_";
    protected static final int SET_TRUNK_INVALID_PORT = -1;

    protected static final String SET_TRUNK_ROUTE_NAME = "route99";
    protected static final String SET_TRUNK_ROUTE_NUMBER_PATTERN = "99.*";
    protected static final SipTrunkRoute SET_TRUNK_ROUTE =
        new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN);


    protected static final List<SipTrunk> EXPECTED_TRUNKS = asList(
        new SipTrunk("1.fqdn.com", 1234),
        new SipTrunk("2.fqdn.com", 2345),
        new SipTrunk("3.fqdn.com", 3456)
    );
    protected static final List<SipTrunk> UPDATED_TRUNKS = asList(
        new SipTrunk("1.fqdn.com", 9876),
        new SipTrunk("20.fqdn.com", 2340),
        new SipTrunk("30.fqdn.com", 3460),
        new SipTrunk("40.fqdn.com", 4461)
    );
    protected static final List<SipTrunkRoute> EXPECTED_ROUTES = asList(
        new SipTrunkRoute("route0", "0.*").setDescription("desc0"),
        new SipTrunkRoute("route1", "1.*").setDescription("desc1"),
        new SipTrunkRoute("route2", "2.*").setDescription("desc2")
    );
    protected static final List<SipTrunkRoute> EXPECTED_ROUTES_WITH_REFERENCED_TRUNK = asList(
        new SipTrunkRoute("route0", "0.*").setDescription("desc0"),
        new SipTrunkRoute("route1", "1.*").setDescription("desc1"),
        new SipTrunkRoute("route2", "2.*").setDescription("desc2")
            .setTrunks(asList(SET_TRUNK_FQDN))
    );
    protected static final List<SipTrunkRoute> UPDATED_ROUTES = asList(
        new SipTrunkRoute("route10", "9.*").setDescription("des90"),
        new SipTrunkRoute("route0", "8.*").setDescription("desc91"),
        new SipTrunkRoute("route21", "7.*").setDescription("desc92"),
        new SipTrunkRoute("route24", "4.*").setDescription("desc44")
    );

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT =
        new StringJoiner("\":\"|\"", "\"", "\":\"")
            .add("id")
            .add("phoneNumber");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN =
        Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()), Pattern.CASE_INSENSITIVE);

    protected SipRoutingClientBuilder getClientBuilder(HttpClient httpClient) {
        if (getTestMode() == TestMode.PLAYBACK) {
            httpClient = interceptorManager.getPlaybackClient();
        }

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        builder
            .httpClient(httpClient)
            .endpoint(communicationEndpoint)
            .credential(new AzureKeyCredential(communicationAccessKey));

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected SipRoutingClientBuilder getClientBuilderWithConnectionString(HttpClient httpClient) {
        if (getTestMode() == TestMode.PLAYBACK) {
            httpClient = interceptorManager.getPlaybackClient();
        }

        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        builder
            .httpClient(httpClient)
            .connectionString(CONNECTION_STRING);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected SipRoutingClientBuilder getClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        builder
            .endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint())
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

    protected SipRoutingClientBuilder addLoggingPolicy(SipRoutingClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                // Should sanitize printed reponse url
                System.out.println("MS-CV header for " + testName + " request "
                    + bufferedResponse.getRequest().getUrl() + ": " + bufferedResponse.getHeaderValue("MS-CV"));
                return Mono.just(bufferedResponse);
            });
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
