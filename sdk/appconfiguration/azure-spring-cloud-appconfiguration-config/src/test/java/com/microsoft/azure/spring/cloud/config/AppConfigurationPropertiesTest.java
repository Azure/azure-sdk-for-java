// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.TestConstants.CACHE_EXPIRATION_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP_NEW;
import static com.microsoft.azure.spring.cloud.config.TestConstants.DEFAULT_CONTEXT_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FAIL_FAST_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.LABEL_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.PREFIX_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.SEPARATOR_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.STORE_ENDPOINT_PROP;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_WATCH_KEY_PATTERN;
import static com.microsoft.azure.spring.cloud.config.TestConstants.WATCHED_KEY_PROP;
import static com.microsoft.azure.spring.cloud.config.TestUtils.propPair;
import static com.microsoft.azure.spring.cloud.config.resource.Connection.ENDPOINT_ERR_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppConfigurationPropertiesTest {
    @InjectMocks
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AppConfigurationBootstrapConfiguration.class));

    private static final String NO_ENDPOINT_CONN_STRING = "Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    private static final String NO_ID_CONN_STRING = "Endpoint=https://fake.test.config.io;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    private static final String NO_SECRET_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;";

    private static final String[] ILLEGAL_PREFIXES = { "/ config", "config" };

    private static final String[] ILLEGAL_PROFILE_SEPARATOR = { "/", "\\", "." };

    private static final String ILLEGAL_LABELS = "*,my-label";

    @Mock
    private HttpGet mockHttpGet;

    @Mock
    private HttpClientBuilder mockHttpClientBuilder;

    @Mock
    private RequestLine mockRequestLine;

    @Mock
    private ApplicationContext context;

    @Mock
    private CloseableHttpResponse mockClosableHttpResponse;

    @Mock
    private HttpEntity mockHttpEntity;

    @Mock
    private InputStream mockInputStream;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        try {
            when(mockClosableHttpResponse.getStatusLine())
                .thenReturn(new BasicStatusLine(new ProtocolVersion("", 0, 0), 200, ""));
            when(mockClosableHttpResponse.getEntity()).thenReturn(mockHttpEntity);
            when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void validInputShouldCreatePropertiesBean() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING))
                .withPropertyValues(propPair(FAIL_FAST_PROP, "false")).run(context -> {
                    assertThat(context).hasSingleBean(AppConfigurationProperties.class);
                });
    }

    @Test
    public void endpointMustExistInConnectionString() {
        testConnStringFields(NO_ENDPOINT_CONN_STRING);
    }

    @Test
    public void idMustExistInConnectionString() {
        testConnStringFields(NO_ID_CONN_STRING);
    }

    @Test
    public void secretMustExistInConnectionString() {
        testConnStringFields(NO_SECRET_CONN_STRING);
    }

    private void testConnStringFields(String connString) {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, connString)).run(context -> {
            assertThat(context).getFailure().hasStackTraceContaining(ENDPOINT_ERR_MSG);
        });
    }

    @Test
    public void defaultContextShouldNotBeEmpty() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(DEFAULT_CONTEXT_PROP, "")).run(context -> {
                    assertInvalidField(context, "defaultContext");
                });
    }

    @Test
    public void prefixShouldFollowPattern() {
        Arrays.asList(ILLEGAL_PREFIXES).stream().forEach(prefix -> {
            this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(PREFIX_PROP, prefix)).run(context -> {
                        assertInvalidField(context, "prefix");
                    });
        });
    }

    @Test
    public void profileSeparatorShouldFollowPattern() {
        Arrays.asList(ILLEGAL_PROFILE_SEPARATOR).stream().forEach(separator -> {
            this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                    propPair(SEPARATOR_PROP, separator)).run(context -> {
                        assertInvalidField(context, "profileSeparator");
                    });
        });
    }

    @Test
    public void asteriskShouldNotBeIncludedInTheLabels() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(LABEL_PROP, ILLEGAL_LABELS)).run(context -> {
                    assertThat(context).getFailure()
                            .hasStackTraceContaining("Label must not contain asterisk(*)");
                });
    }

    @Test
    public void watchedKeyCanNotBeKeyPattern() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(WATCHED_KEY_PROP, TEST_WATCH_KEY_PATTERN)).run(context -> {
                    assertThat(context).getFailure()
                            .hasStackTraceContaining("Watched key can only be a single asterisk(*) " +
                                    "or a specific key without asterisk(*)");
                });
    }

    @Test
    public void storeNameCanBeInitIfConnectionStringConfigured() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(STORE_ENDPOINT_PROP, "")).withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
                .run(context -> {
                    AppConfigurationProperties properties = context.getBean(AppConfigurationProperties.class);
                    assertThat(properties.getStores()).isNotNull();
                    assertThat(properties.getStores().size()).isEqualTo(1);
                    assertThat(properties.getStores().get(0).getEndpoint()).isEqualTo("https://fake.test.config.io");
                });
    }

    @Test
    public void duplicateConnectionStringIsNotAllowed() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING),
                propPair(CONN_STRING_PROP_NEW, TEST_CONN_STRING)).run(context -> {
                    assertThat(context).getFailure().hasStackTraceContaining("Duplicate store name exists");
                });
    }

    @Test
    public void invalidWatchTime() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING))
                .withPropertyValues(propPair(CACHE_EXPIRATION_PROP, "99ms"))
                .run(context -> {
                    assertThat(context).getFailure().hasStackTraceContaining("Minimum Watch time is 1 Second.");
                });
    }

    @Test
    public void minValidWatchTime() {
        this.contextRunner.withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING))
                .withPropertyValues(propPair(CACHE_EXPIRATION_PROP, "1s"))
                .run(context -> {
                    assertThat(context).hasSingleBean(AppConfigurationProperties.class);
                });
    }

    private void assertInvalidField(AssertableApplicationContext context, String fieldName) {
        assertThat(context).getFailure().hasCauseInstanceOf(ConfigurationPropertiesBindException.class);
        assertThat(context).getFailure()
                .hasStackTraceContaining(String.format("field '%s': rejected value", fieldName));
    }
}
