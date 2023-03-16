// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.siprouting;

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
import com.azure.core.test.implementation.TestingHelpers;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class SipRoutingIntegrationTestBase extends TestBase {
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");
    private static final String AZURE_TEST_DOMAIN = Configuration.getGlobalConfiguration()
        .get("AZURE_TEST_DOMAIN", "testdomain.com");

    protected static final String SET_TRUNK_ROUTE_NAME = "route99";
    protected static final String SET_TRUNK_ROUTE_NUMBER_PATTERN = "99.*";
    protected static final SipTrunkRoute SET_TRUNK_ROUTE =
        new SipTrunkRoute(SET_TRUNK_ROUTE_NAME, SET_TRUNK_ROUTE_NUMBER_PATTERN);

    protected static final String FIRST_FQDN = getUniqueFqdn("first");
    protected static final String SECOND_FQDN = getUniqueFqdn("second");
    protected static final String THIRD_FQDN = getUniqueFqdn("third");
    protected static final String FOURTH_FQDN = getUniqueFqdn("fourth");
    protected static final String FIFTH_FQDN = getUniqueFqdn("fifth");
    protected static final String SIXTH_FQDN = getUniqueFqdn("sixth");
    protected static final String DELETE_FQDN = getUniqueFqdn("delete");
    protected static final String SET_TRUNK_FQDN = getUniqueFqdn("set");
    protected static final String NOT_EXISTING_FQDN = "not.existing.fqdn";

    protected static final int SET_TRUNK_PORT = 4567;
    protected static final SipTrunk SET_TRUNK = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_PORT);

    protected static final int SET_TRUNK_UPDATED_PORT = 7651;
    protected static final SipTrunk SET_UPDATED_TRUNK = new SipTrunk(SET_TRUNK_FQDN, SET_TRUNK_UPDATED_PORT);

    protected static final String SET_TRUNK_INVALID_FQDN = "_";
    protected static final int SET_TRUNK_INVALID_PORT = -1;

    protected static final int DELETE_PORT = 5678;
    protected static final SipTrunk DELETE_TRUNK = new SipTrunk(DELETE_FQDN, DELETE_PORT);

    protected static final List<SipTrunk> EXPECTED_TRUNKS = asList(
        new SipTrunk(FIRST_FQDN, 1234),
        new SipTrunk(SECOND_FQDN, 2345),
        new SipTrunk(THIRD_FQDN, 3456)
    );
    protected static final List<SipTrunk> UPDATED_TRUNKS = asList(
        new SipTrunk(FIRST_FQDN, 9876),
        new SipTrunk(FOURTH_FQDN, 2340),
        new SipTrunk(FIFTH_FQDN, 3460),
        new SipTrunk(SIXTH_FQDN, 4461)
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
    private static final Pattern UUID_FQDN_REDACTION_PATTERN =
        Pattern.compile("-[0-9a-f]{32}\\.[0-9a-z\\.]*(\\.com|\\.net)", Pattern.CASE_INSENSITIVE);

    protected static final String MESSAGE_DUPLICATE_ROUTES = 
        "Status code 400, \"{\"error\":{\"code\":\"UnprocessableConfiguration\",\"message\":\"One or more request inputs are not valid.\",\"innererror\":{\"code\":\"DuplicatedRoute\",\"message\":\"There is a duplicated route.\"}}}\"";
    protected static final String MESSAGE_DUPLICATE_TRUNKS = 
        "Status code 400, \"{\"error\":{\"code\":\"UnprocessableConfiguration\",\"message\":\"One or more request inputs are not valid.\",\"innererror\":{\"code\":\"RouteWithDuplicatedTrunk\",\"message\":\"There is a duplicated trunk in a route.\"}}}\"";
    protected static final String MESSAGE_MISSING_TRUNK =
        "Status code 422, \"{\"error\":{\"code\":\"UnprocessableConfiguration\",\"message\":\"One or more request inputs are not valid.\",\"innererror\":{\"code\":\"MissingTrunk\",\"message\":\"Route targeting a missing trunk.\"}}}\"";
    protected static final String MESSAGE_INVALID_NUMBER_PATTERN =
        "Status code 422, \"{\"error\":{\"code\":\"UnprocessableConfiguration\",\"message\":\"One or more request inputs are not valid.\",\"innererror\":{\"code\":\"InvalidRouteNumberPattern\",\"message\":\"Route with an invalid number pattern.\"}}}\"";
    protected static final String MESSAGE_INVALID_ROUTE_NAME =
        "Status code 422, \"{\"error\":{\"code\":\"UnprocessableConfiguration\",\"message\":\"One or more request inputs are not valid.\",\"innererror\":{\"code\":\"InvalidRouteName\",\"message\":\"Route with an invalid name.\"}}}\"";

    protected SipRoutingClientBuilder getClientBuilder(HttpClient httpClient) {
        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        builder
            .httpClient(getHttpClient(httpClient))
            .endpoint(communicationEndpoint)
            .credential(new AzureKeyCredential(communicationAccessKey));

        if (shouldRecord()) {
            addRedactors(builder);
        }

        return builder;
    }

    protected SipRoutingClientBuilder getClientBuilderWithConnectionString(HttpClient httpClient) {
        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        builder
            .httpClient(getHttpClient(httpClient))
            .connectionString(CONNECTION_STRING);

        if (shouldRecord()) {
            addRedactors(builder);
        }

        return builder;
    }

    protected SipRoutingClientBuilder getClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        SipRoutingClientBuilder builder = new SipRoutingClientBuilder();
        builder
            .httpClient(getHttpClient(httpClient))
            .endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (shouldRecord()) {
            addRedactors(builder);
        }

        return builder;
    }

    private void addRedactors(SipRoutingClientBuilder builder) {
        List<Function<String, String>> redactors = new ArrayList<>();
        redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
        redactors.add(data -> redact(data, UUID_FQDN_REDACTION_PATTERN.matcher(data), ".redacted.com"));
        builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
    }

    private boolean shouldRecord() {
        return getTestMode() == TestMode.RECORD;
    }

    private HttpClient getHttpClient(HttpClient httpClient) {
        if (httpClient == null || getTestMode() == TestMode.PLAYBACK) {
            return interceptorManager.getPlaybackClient();
        }
        return httpClient;
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
            String captureGroup = matcher.group();
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(), replacement);
            }
        }

        return content;
    }

    private static String getUniqueFqdn(String order) {
        if (TestingHelpers.getTestMode() == TestMode.PLAYBACK) {
            return order + ".redacted.com";
        }
        String unique = UUID.randomUUID().toString().replace("-", "");
        return order + "-" + unique + "." + AZURE_TEST_DOMAIN;
    }
}
