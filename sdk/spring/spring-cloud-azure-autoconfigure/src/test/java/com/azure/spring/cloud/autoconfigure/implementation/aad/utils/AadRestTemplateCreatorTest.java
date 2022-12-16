// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.utils;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.RestTemplateProxyCustomizerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.RestTemplateProxyCustomizerTestConfiguration.FACTORY;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createOAuth2AccessTokenResponseClientRestTemplate;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createOAuth2ErrorResponseHandledRestTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class AadRestTemplateCreatorTest {

    @Test
    void testAadRestOperationConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(RestTemplateAutoConfiguration.class)
                .run((context) -> {
                    RestTemplateBuilder builder = context.getBean(RestTemplateBuilder.class);
                    testCreateOAuth2ErrorResponseHandledRestTemplate(builder);
                    testCreateOAuth2AccessTokenResponseClientRestTemplate(builder);
                });
    }

    private void testCreateOAuth2ErrorResponseHandledRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = createOAuth2ErrorResponseHandledRestTemplate(builder);
        ResponseErrorHandler handler = restTemplate.getErrorHandler();
        assertEquals(OAuth2ErrorResponseErrorHandler.class, handler.getClass());
    }

    private void testCreateOAuth2AccessTokenResponseClientRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = createOAuth2AccessTokenResponseClientRestTemplate(builder);
        ResponseErrorHandler handler = restTemplate.getErrorHandler();
        assertEquals(OAuth2ErrorResponseErrorHandler.class, handler.getClass());
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        assertEquals(2, converters.size());
        assertThat(converters, hasItem(isA(FormHttpMessageConverter.class)));
        assertThat(converters, hasItem(isA(OAuth2AccessTokenResponseHttpMessageConverter.class)));

        testOAuth2AccessTokenResponseCanBeConstructed(restTemplate);
    }

    private void testOAuth2AccessTokenResponseCanBeConstructed(RestTemplate restTemplate) {
        URI url;
        try {
            url = new URI("https://login.microsoftonline.comv/common/oauth2/v2.0/token");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer
                .expect(ExpectedCount.once(), requestTo(url))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(readAccessTokenResponse()));
        OAuth2AccessTokenResponse response = restTemplate
                .exchange(new RequestEntity<>(HttpMethod.POST, url), OAuth2AccessTokenResponse.class)
                .getBody();
        assertNotNull(response);
        assertEquals("test_access_token_value", response.getAccessToken().getTokenValue());
    }

    private String readAccessTokenResponse() {
        try {
            return new String(Files.readAllBytes(
                    Paths.get("src/test/resources/aad/access-token-response.json")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void testRestOperationProxyConfiguration() {
        new ApplicationContextRunner()
                .withUserConfiguration(
                        RestTemplateAutoConfiguration.class,
                        RestTemplateProxyCustomizerTestConfiguration.class)
                .run((context) -> {
                    RestTemplate restTemplate = context.getBean(RestTemplateBuilder.class).build();
                    assertSame(restTemplate.getRequestFactory(), FACTORY);
                });
    }

}
