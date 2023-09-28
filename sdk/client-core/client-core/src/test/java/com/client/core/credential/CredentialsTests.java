// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.credential;

import com.client.core.SyncAsyncExtension;
import com.client.core.SyncAsyncTest;
import com.client.core.http.HttpClient;
import com.client.core.http.HttpHeaderName;
import com.client.core.http.HttpMethod;
import com.client.core.http.HttpPipeline;
import com.client.core.http.HttpPipelineBuilder;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import com.client.core.http.clients.NoOpHttpClient;
import com.client.core.http.policy.ClientSasCredentialPolicy;
import com.client.core.http.policy.BearerTokenAuthenticationPolicy;
import com.client.core.http.policy.HttpPipelinePolicy;
import com.client.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.client.core.CoreTestUtils.createUrl;

public class CredentialsTests {

    private static final String DUMMY_NAME = "Dummy-Name";
    private static final String DUMMY_VALUE = "DummyValue";

    @SyncAsyncTest
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredential credentials = new BasicAuthenticationCredential("user",
                "fakeKeyPlaceholder");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(headerValue != null && headerValue.startsWith("Basic ") && headerValue.length() > 6);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies((context, next) -> credentials.getToken(new TokenRequestContext().addScopes("scope./default"))
                .flatMap(token -> {
                    context.getHttpRequest().getHeaders().set(HttpHeaderName.AUTHORIZATION, "Basic " + token.getToken());
                    return next.process();
                }), auditorPolicy)
            .build();

        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );
    }

    @SyncAsyncTest
    public void tokenCredentialTest() throws Exception {
        TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };
        final HttpClient httpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(new com.client.core.http.MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, createUrl("https://localhost"));
        SyncAsyncExtension.execute(
            () -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request).block()
        );
    }
    @SyncAsyncTest
    public void tokenCredentialHttpSchemeTest() {
        TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
                .build();

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        ));

        Assertions.assertEquals("token credentials require a URL using the HTTPS protocol scheme", thrown.getMessage());
    }

    @ParameterizedTest
    @CsvSource(
        {   "test_signature,https://localhost,https://localhost?test_signature",
            "?test_signature,https://localhost,https://localhost?test_signature",
            "test_signature,https://localhost?,https://localhost?test_signature",
            "?test_signature,https://localhost?,https://localhost?test_signature",
            "test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature",
            "?test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature"})
    public void sasCredentialsTest(String signature, String url, String expectedUrl) throws Exception {
        ClientSasCredential credential = new ClientSasCredential(signature);

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String actualUrl = context.getHttpRequest().getUrl().toString();
            Assertions.assertEquals(expectedUrl, actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ClientSasCredentialPolicy(credential), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET,  createUrl(url));
        pipeline.send(request).block();
    }

    @ParameterizedTest
    @CsvSource(
        {   "test_signature,https://localhost,https://localhost?test_signature",
            "?test_signature,https://localhost,https://localhost?test_signature",
            "test_signature,https://localhost?,https://localhost?test_signature",
            "?test_signature,https://localhost?,https://localhost?test_signature",
            "test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature",
            "?test_signature,https://localhost?foo=bar,https://localhost?foo=bar&test_signature"})
    public void sasCredentialsTestSync(String signature, String url, String expectedUrl) throws Exception {
        ClientSasCredential credential = new ClientSasCredential(signature);

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            String actualUrl = context.getHttpRequest().getUrl().toString();
            Assertions.assertEquals(expectedUrl, actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ClientSasCredentialPolicy(credential), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, createUrl(url));
        pipeline.sendSync(request, Context.NONE);
    }

    @SyncAsyncTest
    public void sasCredentialsRequireHTTPSSchemeTest() {
        ClientSasCredential credential = new ClientSasCredential("foo");

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ClientSasCredentialPolicy(credential))
            .build();

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        ));

        Assertions.assertEquals("Shared access signature credentials require HTTPS to prevent leaking the shared access signature.", thrown.getMessage());
    }

    @SyncAsyncTest
    public void sasCredentialsDoNotRequireHTTPSchemeTest() throws Exception {
        ClientSasCredential credential = new ClientSasCredential("foo");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String actualUrl = context.getHttpRequest().getUrl().toString();
            Assertions.assertEquals("http://localhost?foo", actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new ClientSasCredentialPolicy(credential, false), auditorPolicy)
            .build();

        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline),
            () -> sendRequest(pipeline)
        );
    }

    static class InvalidInputsArgumentProvider implements ArgumentsProvider {

        InvalidInputsArgumentProvider() { }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(null, null, NullPointerException.class),
                Arguments.of("", null, NullPointerException.class),
                Arguments.of(null, "", NullPointerException.class),
                Arguments.of("", "", IllegalArgumentException.class),
                Arguments.of("DummyName", "", IllegalArgumentException.class),
                Arguments.of("", "DummyValue", IllegalArgumentException.class)
            );
        }
    }


    @ParameterizedTest
    @ArgumentsSource(InvalidInputsArgumentProvider.class)
    public void namedKeyCredentialsInvalidArgumentTest(String name, String key, Class<Exception> excepionType) {
        Assertions.assertThrows(excepionType, () -> {
            new ClientNamedKeyCredential(name, key);
        });
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidInputsArgumentProvider.class)
    public void namedKeyCredentialsInvalidArgumentUpdateTest(String name, String key, Class<Exception> excepionType) {
        ClientNamedKeyCredential azureNamedKeyCredential =
            new ClientNamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertThrows(excepionType, () -> {
            azureNamedKeyCredential.update(name, key);
        });
    }

    @Test
    public void namedKeyCredentialValueTest() {
        ClientNamedKeyCredential azureNamedKeyCredential =
            new ClientNamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertEquals(DUMMY_NAME, azureNamedKeyCredential.getClientNamedKey().getName());
        Assertions.assertEquals(DUMMY_VALUE, azureNamedKeyCredential.getClientNamedKey().getKey());
    }

    @Test
    public void namedKeyCredentialUpdateTest() {
        ClientNamedKeyCredential azureNamedKeyCredential =
            new ClientNamedKeyCredential(DUMMY_NAME, DUMMY_NAME);

        String expectedName = "New-Name";
        String expectedValue = "NewValue";

        azureNamedKeyCredential.update(expectedName, expectedValue);

        Assertions.assertEquals(expectedName, azureNamedKeyCredential.getClientNamedKey().getName());
        Assertions.assertEquals(expectedValue, azureNamedKeyCredential.getClientNamedKey().getKey());
    }

    private HttpResponse sendRequest(HttpPipeline pipeline) throws MalformedURLException {
        return pipeline.send(new HttpRequest(HttpMethod.GET, createUrl("http://localhost"))).block();
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline) throws MalformedURLException {
        return pipeline.sendSync(new HttpRequest(HttpMethod.GET, createUrl("http://localhost")), Context.NONE);
    }
}
