// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.evaluation.PolicyToken;
import com.azure.core.management.evaluation.PolicyTokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.PolicyTokenOperation;
import com.azure.resourcemanager.resources.models.PolicyTokenRequest;
import com.azure.resourcemanager.resources.models.PolicyTokenResult;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.Sku;
import com.azure.resourcemanager.storage.models.SkuName;
import com.azure.resourcemanager.storage.models.StorageAccountCreateParameters;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end test for {@link ExternalEvaluationPolicy} that exercises the full external evaluation ("Invoke") flow
 * against a real (generated) Storage management client and the real {@code acquirePolicyToken} API exposed by
 * {@code azure-resourcemanager-resources}, with Azure Resource Manager itself faked by WireMock.
 * <p>
 * This mirrors how a user would opt in: they import the policy library that exposes {@code acquirePolicyToken}, write
 * a small {@link PolicyTokenCredential} wrapper that converts the SDK-provided {@link com.azure.core.management.evaluation.PolicyTokenRequestContext}
 * into a {@link PolicyTokenRequest}, and add {@link ExternalEvaluationPolicy} to their client's pipeline.
 */
public class ExternalEvaluationPolicyE2ETests {
    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String RESOURCE_GROUP = "rg";
    private static final String ACCOUNT_NAME = "acct";
    private static final String POLICY_TOKEN_VALUE = "PoP eyJhbGciOiJSUzI1NiJ9.policy-token-value";
    private static final String EE_HEADER = "x-ms-policy-external-evaluations";

    private static final String STORAGE_PATH = "/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/" + RESOURCE_GROUP
        + "/providers/Microsoft.Storage/storageAccounts/" + ACCOUNT_NAME;
    private static final String ACQUIRE_PATH
        = "/subscriptions/" + SUBSCRIPTION_ID + "/providers/Microsoft.Authorization/acquirePolicyToken";

    private static final String DENY_BODY = "{\"error\":{\"code\":\"RequestDisallowedByPolicy\","
        + "\"message\":\"Resource was disallowed by policy.\",\"additionalInfo\":[{\"type\":\"PolicyViolation\","
        + "\"info\":{\"missingPolicyTokenDetails\":{\"shouldDeny\":true,\"endpointKind\":\"AzureResourceGraph\","
        + "\"isChangeReferenceRequired\":false}}}]}}";

    private static final String STORAGE_BODY = "{\"id\":\"" + STORAGE_PATH + "\",\"name\":\"" + ACCOUNT_NAME + "\","
        + "\"type\":\"Microsoft.Storage/storageAccounts\",\"location\":\"eastus\","
        + "\"properties\":{\"provisioningState\":\"Succeeded\"}}";

    private static final String ACQUIRE_BODY = "{\"result\":\"Succeeded\",\"token\":\"" + POLICY_TOKEN_VALUE + "\","
        + "\"tokenId\":\"11111111-1111-1111-1111-111111111111\"}";

    // The exact JSON body the Storage create operation serializes for the parameters built below. The External
    // Evaluation flow requires the retried request body to be byte-for-byte identical to the original, so the mock
    // service asserts on this exact string for both the denied and the retried PUT.
    private static final String EXPECTED_REQUEST_BODY
        = "{\"sku\":{\"name\":\"Standard_LRS\"},\"kind\":\"StorageV2\",\"location\":\"eastus\"}";

    private static final String API_VERSION = "2026-04-01";

    @Test
    public void endToEndAcquireAndRetry() throws Exception {
        // The manager's Configurable pipeline adds a BearerTokenAuthenticationPolicy, which requires HTTPS, so the
        // mock service is served over TLS and the clients trust its self-signed certificate.
        WireMockServer server = new WireMockServer(wireMockConfig().dynamicHttpsPort());
        server.start();
        try {
            String endpoint = "https://localhost:" + server.httpsPort();
            HttpClient httpClient = trustAllHttpClient();

            // The exact UTF-8 acquire request body: PolicyTokenRequest serializes {"operation":{...}} with the
            // operation's uri, httpMethod, and content in that order; content is the guarded request body echoed
            // verbatim. The mock service asserts on this exact string (byte-for-byte), not a JSON-path match.
            String expectedAcquireBody = "{\"operation\":{\"uri\":\"" + endpoint + STORAGE_PATH + "?api-version="
                + API_VERSION + "\",\"httpMethod\":\"PUT\",\"content\":" + EXPECTED_REQUEST_BODY + "}}";

            // Guarded operation is denied when the external evaluation header is absent. The mock service asserts the
            // method (PUT), the exact URL path, and the exact request body.
            server.stubFor(put(urlPathEqualTo(STORAGE_PATH)).withHeader(EE_HEADER, absent())
                .withRequestBody(equalTo(EXPECTED_REQUEST_BODY))
                .willReturn(
                    aResponse().withStatus(403).withHeader("Content-Type", "application/json").withBody(DENY_BODY)));

            // Guarded operation succeeds once the acquired policy token is applied. The retried request must carry the
            // token header and a byte-for-byte identical body, so the mock service asserts on the exact body again.
            server.stubFor(put(urlPathEqualTo(STORAGE_PATH)).withHeader(EE_HEADER, matching(".+"))
                .withRequestBody(equalTo(EXPECTED_REQUEST_BODY))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(STORAGE_BODY)));

            // The acquirePolicyToken API issues the token. The mock service asserts the exact UTF-8 request body: the
            // operation echoed to it must match the guarded request byte-for-byte (uri, method, and content).
            server.stubFor(post(urlPathEqualTo(ACQUIRE_PATH)).withRequestBody(equalTo(expectedAcquireBody))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(ACQUIRE_BODY)));

            Map<String, String> endpoints = new HashMap<>();
            endpoints.put(AzureEnvironment.Endpoint.MANAGEMENT.identifier(), endpoint);
            endpoints.put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.identifier(), endpoint);
            endpoints.put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.identifier(), endpoint);
            AzureProfile profile = new AzureProfile("tenant", SUBSCRIPTION_ID, new AzureEnvironment(endpoints));

            // The user-provided policy library that knows how to call acquirePolicyToken.
            HttpPipeline acquirePipeline
                = new HttpPipelineBuilder().httpClient(httpClient).policies(new RetryPolicy()).build();
            ResourceManager resourceManager
                = ResourceManager.authenticate(acquirePipeline, profile).withSubscription(SUBSCRIPTION_ID);

            // The small opt-in wrapper the user writes to bridge the SDK context to the acquirePolicyToken API.
            AtomicInteger acquireCount = new AtomicInteger();
            PolicyTokenCredential wrapper = context -> {
                acquireCount.incrementAndGet();
                Object content = context.getContent() == null ? null : context.getContent().toObject(Object.class);
                PolicyTokenOperation operation = new PolicyTokenOperation().withUri(context.getUri())
                    .withHttpMethod(context.getHttpMethod().toString())
                    .withContent(content);
                return resourceManager.policyClient()
                    .getPolicyTokens()
                    .acquireAsync(new PolicyTokenRequest().withOperation(operation))
                    .flatMap(response -> {
                        if (response.result() != PolicyTokenResult.SUCCEEDED
                            || CoreUtils.isNullOrEmpty(response.token())) {
                            return Mono.error(new IllegalStateException(
                                "Failed to acquire a policy token (result: " + response.result() + ")."));
                        }
                        return Mono.just(new PolicyToken(response.token(), response.expiration()));
                    });
            };

            // The guarded client opts in exactly as a user would: add the External Evaluation policy to the manager's
            // pipeline via the Configurable builder. A dummy credential is enough because the mock service does not
            // assert on the Authorization header.
            TokenCredential credential
                = request -> Mono.just(new AccessToken("dummy-token", OffsetDateTime.now().plusHours(1)));
            StorageManager storageManager = StorageManager.configure()
                .withHttpClient(httpClient)
                .withPolicy(new ExternalEvaluationPolicy(wrapper))
                .authenticate(credential, profile);

            StorageAccountInner result = storageManager.serviceClient()
                .getResourceProviders()
                .create(RESOURCE_GROUP, ACCOUNT_NAME,
                    new StorageAccountCreateParameters().withSku(new Sku().withName(SkuName.STANDARD_LRS))
                        .withKind(Kind.STORAGE_V2)
                        .withLocation("eastus"));

            assertNotNull(result);
            assertEquals(ACCOUNT_NAME, result.name());
            assertEquals(1, acquireCount.get());
            // The guarded operation is sent twice against the exact URL and method, each carrying the exact same body:
            // once denied (no token header) and once retried with the acquired token.
            server.verify(2,
                putRequestedFor(urlPathEqualTo(STORAGE_PATH)).withRequestBody(equalTo(EXPECTED_REQUEST_BODY)));
            server.verify(1, putRequestedFor(urlPathEqualTo(STORAGE_PATH)).withHeader(EE_HEADER, absent())
                .withRequestBody(equalTo(EXPECTED_REQUEST_BODY)));
            server.verify(1,
                putRequestedFor(urlPathEqualTo(STORAGE_PATH)).withHeader(EE_HEADER, equalTo(POLICY_TOKEN_VALUE))
                    .withRequestBody(equalTo(EXPECTED_REQUEST_BODY)));
            // The token is acquired exactly once, with the operation echoing the guarded request verbatim.
            server.verify(1,
                postRequestedFor(urlPathEqualTo(ACQUIRE_PATH)).withRequestBody(equalTo(expectedAcquireBody)));
        } finally {
            server.stop();
        }
    }

    // Builds a Netty HTTP client that trusts any certificate, so the clients can talk to the self-signed HTTPS mock.
    private static HttpClient trustAllHttpClient() throws Exception {
        SslContext sslContext
            = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        reactor.netty.http.client.HttpClient reactorClient
            = reactor.netty.http.client.HttpClient.create().secure(spec -> spec.sslContext(sslContext));
        return new NettyAsyncHttpClientBuilder(reactorClient).build();
    }
}
