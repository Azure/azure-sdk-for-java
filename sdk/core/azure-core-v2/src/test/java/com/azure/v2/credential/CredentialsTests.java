// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.credential;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import io.clientcore.core.http.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.clients.NoOpHttpClient;
import io.clientcore.core.http.pipeline.AzureSasCredentialPolicy;
import io.clientcore.core.http.pipeline.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.createUrl;

public class CredentialsTests {

    private static final String DUMMY_NAME = "Dummy-Name";
    private static final String DUMMY_VALUE = "DummyValue";

    @SyncAsyncTest
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredential credentials = new BasicAuthenticationCredential("user", "fakeKeyPlaceholder");

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String headerValue = httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(headerValue != null && headerValue.startsWith("Basic ") && headerValue.length() > 6);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies((context, next) -> credentials.getToken(new TokenRequestContext().addScopes("scope./default"))
                .flatMap(token -> {
                    httpRequest.getHeaders().set(HttpHeaderName.AUTHORIZATION, "Basic " + token.getToken());
                    return next.process();
                }), auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
    }

    @SyncAsyncTest
    public void tokenCredentialTest() throws Exception {
        TokenCredential credentials = request -> new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String headerValue = httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };
        final HttpClient httpClient = new NoOpHttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
                return new com.azure.core.http.MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, createUrl("https://localhost"));
        SyncAsyncExtension.execute(() -> pipeline.send(request), () -> pipeline.send(request).block());
    }

    @SyncAsyncTest
    public void tokenCredentialHttpSchemeTest() {
        TokenCredential credentials = request -> new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String headerValue = httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
            .build();

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
            () -> SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline)));

        Assertions.assertEquals("token credentials require a URL using the HTTPS protocol scheme", thrown.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "test_signature,https://localhost,https://localhost?test_signature",
        "?test_signature,https://localhost,https://localhost?test_signature",
        "test_signature,https://localhost?,https://localhost?test_signature",
        "?test_signature,https://localhost?,https://localhost?test_signature",
        "test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature",
        "?test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature" })
    public void sasCredentialsTest(String signature, String url, String expectedUrl) throws Exception {
        AzureSasCredential credential = new AzureSasCredential(signature);

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String actualUrl = httpRequest.getUrl().toString();
            Assertions.assertEquals(expectedUrl, actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, createUrl(url));
        pipeline.send(request).block();
    }

    @ParameterizedTest
    @CsvSource({
        "test_signature,https://localhost,https://localhost?test_signature",
        "?test_signature,https://localhost,https://localhost?test_signature",
        "test_signature,https://localhost?,https://localhost?test_signature",
        "?test_signature,https://localhost?,https://localhost?test_signature",
        "test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature",
        "?test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature" })
    public void sasCredentialsTestSync(String signature, String url, String expectedUrl) throws Exception {
        AzureSasCredential credential = new AzureSasCredential(signature);

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String actualUrl = httpRequest.getUrl().toString();
            Assertions.assertEquals(expectedUrl, actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, createUrl(url));
        pipeline.send(request);
    }

    @SyncAsyncTest
    public void sasCredentialsRequireHTTPSSchemeTest() {
        AzureSasCredential credential = new AzureSasCredential("foo");

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential))
            .build();

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
            () -> SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline)));

        Assertions.assertEquals(
            "Shared access signature credentials require HTTPS to prevent leaking the shared access signature.",
            thrown.getMessage());
    }

    @SyncAsyncTest
    public void sasCredentialsDoNotRequireHTTPSchemeTest() throws Exception {
        AzureSasCredential credential = new AzureSasCredential("foo");

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String actualUrl = httpRequest.getUrl().toString();
            Assertions.assertEquals("http://localhost?foo", actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential, false), auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
    }

    static class InvalidInputsArgumentProvider implements ArgumentsProvider {

        InvalidInputsArgumentProvider() {
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(null, null, NullPointerException.class),
                Arguments.of("", null, NullPointerException.class), Arguments.of(null, "", NullPointerException.class),
                Arguments.of("", "", IllegalArgumentException.class),
                Arguments.of("DummyName", "", IllegalArgumentException.class),
                Arguments.of("", "DummyValue", IllegalArgumentException.class));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidInputsArgumentProvider.class)
    public void namedKeyCredentialsInvalidArgumentTest(String name, String key, Class<Exception> excepionType) {
        Assertions.assertThrows(excepionType, () -> {
            new AzureNamedKeyCredential(name, key);
        });
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidInputsArgumentProvider.class)
    public void namedKeyCredentialsInvalidArgumentUpdateTest(String name, String key, Class<Exception> excepionType) {
        AzureNamedKeyCredential azureNamedKeyCredential = new AzureNamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertThrows(excepionType, () -> {
            azureNamedKeyCredential.update(name, key);
        });
    }

    @Test
    public void namedKeyCredentialValueTest() {
        AzureNamedKeyCredential azureNamedKeyCredential = new AzureNamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertEquals(DUMMY_NAME, azureNamedKeyCredential.getAzureNamedKey().getName());
        Assertions.assertEquals(DUMMY_VALUE, azureNamedKeyCredential.getAzureNamedKey().getKey());
    }

    @Test
    public void namedKeyCredentialUpdateTest() {
        AzureNamedKeyCredential azureNamedKeyCredential = new AzureNamedKeyCredential(DUMMY_NAME, DUMMY_NAME);

        String expectedName = "New-Name";
        String expectedValue = "NewValue";

        azureNamedKeyCredential.update(expectedName, expectedValue);

        Assertions.assertEquals(expectedName, azureNamedKeyCredential.getAzureNamedKey().getName());
        Assertions.assertEquals(expectedValue, azureNamedKeyCredential.getAzureNamedKey().getKey());
    }

    private HttpResponse sendRequest(HttpPipeline pipeline) throws MalformedURLException {
        return pipeline.send(new HttpRequest(HttpMethod.GET, createUrl("http://localhost"))).block();
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline) throws MalformedURLException {
        return pipeline.send(new HttpRequest(HttpMethod.GET, createUrl("http://localhost")), Context.none());
    }
}
