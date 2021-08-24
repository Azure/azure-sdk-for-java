// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.TestConstants.CONN_STRING_PROP;
import static com.azure.spring.cloud.config.TestConstants.FAIL_FAST_PROP;
import static com.azure.spring.cloud.config.TestConstants.STORE_ENDPOINT_PROP;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.TestUtils.propPair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.azure.spring.cloud.config.stores.ClientStore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AppConfigurationBootstrapConfigurationTest {

    private static final ApplicationContextRunner CONTEXT_RUNNER = new ApplicationContextRunner()
        .withPropertyValues(propPair(STORE_ENDPOINT_PROP, TEST_STORE_NAME))
        .withConfiguration(AutoConfigurations.of(AppConfigurationBootstrapConfiguration.class));
    @Mock
    HttpEntity mockHttpEntity;
    @Mock
    InputStream mockInputStream;
    @Mock
    ObjectMapper mockObjectMapper;
    @Mock
    ClientStore clientStoreMock;
    @Mock
    private CloseableHttpResponse mockClosableHttpResponse;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        try {
            PowerMockito.whenNew(ObjectMapper.class).withAnyArguments().thenReturn(mockObjectMapper);
            when(mockClosableHttpResponse.getStatusLine())
                .thenReturn(new BasicStatusLine(new ProtocolVersion("", 0, 0), 200, ""));
            when(mockClosableHttpResponse.getEntity()).thenReturn(mockHttpEntity);
            when(mockHttpEntity.getContent()).thenReturn(mockInputStream);
        } catch (Exception e) {
            fail();
        }
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void iniConnectionStringSystemAssigned() throws Exception {
        whenNew(ClientStore.class).withAnyArguments().thenReturn(clientStoreMock);
        CONTEXT_RUNNER.withPropertyValues(propPair(FAIL_FAST_PROP, "false"))
            .run(context -> assertThat(context).hasSingleBean(AppConfigurationPropertySourceLocator.class));
    }

    @Test
    public void iniConnectionStringUserAssigned() throws Exception {
        whenNew(ClientStore.class).withAnyArguments().thenReturn(clientStoreMock);
        CONTEXT_RUNNER
            .withPropertyValues(propPair(FAIL_FAST_PROP, "false"),
                propPair("spring.cloud.azure.appconfiguration.managed-identity.client-id", "client-id"))
            .run(context -> assertThat(context).hasSingleBean(AppConfigurationPropertySourceLocator.class));
    }

    @Test
    public void propertySourceLocatorBeanCreated() throws Exception {
        whenNew(ClientStore.class).withAnyArguments().thenReturn(clientStoreMock);
        CONTEXT_RUNNER
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING), propPair(FAIL_FAST_PROP, "false"))
            .run(context -> assertThat(context).hasSingleBean(AppConfigurationPropertySourceLocator.class));
    }

    @Test
    public void clientsBeanCreated() throws Exception {
        whenNew(ClientStore.class).withAnyArguments().thenReturn(clientStoreMock);
        CONTEXT_RUNNER
            .withPropertyValues(propPair(CONN_STRING_PROP, TEST_CONN_STRING), propPair(FAIL_FAST_PROP, "false"))
            .run(context -> {
                assertThat(context).hasSingleBean(ClientStore.class);
            });
    }
}
