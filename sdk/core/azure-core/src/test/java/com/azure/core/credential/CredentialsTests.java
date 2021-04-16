// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

public class CredentialsTests {

    private static final String DUMMY_NAME = "Dummy-Name";
    private static final String DUMMY_VALUE = "DummyValue";

    @Test
    public void basicCredentialsTest() throws Exception {
        BasicAuthenticationCredential credentials = new BasicAuthenticationCredential("user", "pass");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue("Authorization");
            Assertions.assertEquals("Basic dXNlcjpwYXNz", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies((context, next) -> credentials.getToken(new TokenRequestContext().addScopes("scope./default"))
                .flatMap(token -> {
                    context.getHttpRequest().getHeaders().set("Authorization", "Basic " + token.getToken());
                    return next.process();
                }), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
    }

    @Test
    public void tokenCredentialTest() throws Exception {
        TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue("Authorization");
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://localhost"));
        pipeline.send(request).block();
    }

    @Test
    public void tokenCredentialHttpSchemeTest() throws Exception {
        TokenCredential credentials = request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX));

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String headerValue = context.getHttpRequest().getHeaders().getValue("Authorization");
            Assertions.assertEquals("Bearer this_is_a_token", headerValue);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(new BearerTokenAuthenticationPolicy(credentials, "scope./default"), auditorPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        StepVerifier.create(pipeline.send(request))
                .expectErrorMessage("token credentials require a URL using the HTTPS protocol scheme")
                .verify();
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
        AzureSasCredential credential = new AzureSasCredential(signature);

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String actualUrl = context.getHttpRequest().getUrl().toString();
            Assertions.assertEquals(expectedUrl, actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(url));
        pipeline.send(request).block();
    }

    @Test
    public void sasCredentialsRequireHTTPSSchemeTest() throws Exception {
        AzureSasCredential credential = new AzureSasCredential("foo");

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential))
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        StepVerifier.create(pipeline.send(request))
            .expectErrorMessage("Shared access signature credentials require HTTPS to prevent leaking the shared access signature.")
            .verify();
    }

    @Test
    public void sasCredentialsDoNotRequireHTTPSchemeTest() throws Exception {
        AzureSasCredential credential = new AzureSasCredential("foo");

        HttpPipelinePolicy auditorPolicy =  (context, next) -> {
            String actualUrl = context.getHttpRequest().getUrl().toString();
            Assertions.assertEquals("http://localhost?foo", actualUrl);
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(new AzureSasCredentialPolicy(credential, false), auditorPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pipeline.send(request).block();
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
            new AzureNamedKeyCredential(name, key);
        });
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidInputsArgumentProvider.class)
    public void namedKeyCredentialsInvalidArgumentUpdateTest(String name, String key, Class<Exception> excepionType) {
        AzureNamedKeyCredential azureNamedKeyCredential =
            new AzureNamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertThrows(excepionType, () -> {
            azureNamedKeyCredential.update(name, key);
        });
    }

    @Test
    public void namedKeyCredentialValueTest() {
        AzureNamedKeyCredential azureNamedKeyCredential =
            new AzureNamedKeyCredential(DUMMY_NAME, DUMMY_VALUE);

        Assertions.assertEquals(DUMMY_NAME, azureNamedKeyCredential.getAzureNamedKey().getName());
        Assertions.assertEquals(DUMMY_VALUE, azureNamedKeyCredential.getAzureNamedKey().getKey());
    }

    @Test
    public void namedKeyCredentialUpdateTest() {
        AzureNamedKeyCredential azureNamedKeyCredential =
            new AzureNamedKeyCredential(DUMMY_NAME, DUMMY_NAME);

        String expectedName = "New-Name";
        String expectedValue = "NewValue";

        azureNamedKeyCredential.update(expectedName, expectedValue);

        Assertions.assertEquals(expectedName, azureNamedKeyCredential.getAzureNamedKey().getName());
        Assertions.assertEquals(expectedValue, azureNamedKeyCredential.getAzureNamedKey().getKey());
    }
}
