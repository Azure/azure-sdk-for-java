// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.CredentialUnavailableException;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AppConfigurationReplicaClientTest {

    @Mock
    private ConfigurationAsyncClient clientMock;

    @Mock
    private HttpResponseException exceptionMock;

    @Mock
    private HttpResponse responseMock;

    @Mock
    private PagedFlux<ConfigurationSetting> settingsMock;

    @Mock
    private PagedResponse<ConfigurationSetting> mockPagedResponse;

    private final String endpoint = "clientTest.azconfig.io";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getWatchKeyTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));

        Mono<ConfigurationSetting> watchKey = Mono.just(new ConfigurationSetting().setKey("watch").setLabel("\0"));

        when(clientMock.getConfigurationSetting(Mockito.anyString(), Mockito.anyString())).thenReturn(watchKey);

        assertEquals(watchKey.block(), client.getWatchKey("watch", "\0").block());

        when(clientMock.getConfigurationSetting(Mockito.anyString(), Mockito.anyString())).thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0").block());

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0").block());

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0").block());

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.getWatchKey("watch", "\0").block());
    }

    @Test
    public void listSettingsTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));

        List<ConfigurationSetting> configurations = new ArrayList<>();

        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.map(Mockito.any())).thenReturn(Flux.just());

        List<ConfigurationSetting> result = client.listSettings(new SettingSelector()).collectList().block();

        assertEquals(configurations, result);

        when(clientMock.listConfigurationSettings(Mockito.any())).thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector()));

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector()));

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector()));

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.listSettings(new SettingSelector()));
    }

    @Test
    public void listSettingsNoCredentialTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));

        when(clientMock.listConfigurationSettings(Mockito.any()))
            .thenThrow(new CredentialUnavailableException("No Credential"));
        when(settingsMock.map(Mockito.any())).thenReturn(Flux.just());

        assertThrows(CredentialUnavailableException.class, () -> client.listSettings(new SettingSelector()));
    }

    @Test
    public void getWatchNoCredentialTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));

        when(clientMock.getConfigurationSetting(Mockito.anyString(), Mockito.anyString()))
            .thenThrow(new CredentialUnavailableException("No Credential"));
        when(settingsMock.map(Mockito.any())).thenReturn(Flux.just());

        assertThrows(CredentialUnavailableException.class, () -> client.getWatchKey("key", "label"));
    }

    @Test
    public void backoffTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));

        // Setups in the past and with no errors.
        assertTrue(client.getBackoffEndTime().isBefore(Instant.now()));
        assertEquals(0, client.getFailedAttempts());

        // Failing results in an increase in failed attempts
        client.updateBackoffEndTime(Instant.now().plusSeconds(600));

        assertTrue(client.getBackoffEndTime().isAfter(Instant.now()));
        assertEquals(1, client.getFailedAttempts());

        client.updateBackoffEndTime(Instant.now().minusSeconds(600));

        assertTrue(client.getBackoffEndTime().isBefore(Instant.now()));
        assertEquals(2, client.getFailedAttempts());

        List<ConfigurationSetting> configurationSettings = new ArrayList<>();
        configurationSettings.add(new ConfigurationSetting().setKey("test"));

        Mono<PagedResponse<ConfigurationSetting>> response = Mono.just(mockPagedResponse);
        when(mockPagedResponse.getElements()).thenReturn(IterableStream.of(configurationSettings));
        Supplier<Mono<PagedResponse<ConfigurationSetting>>> pagedResponse = () -> response;
        PagedFlux<ConfigurationSetting> settings = new PagedFlux<ConfigurationSetting>(pagedResponse);

        // Success in a list request results in a reset of failed attemtps
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class))).thenReturn(settings);
        // when(settingsMock.map(Mockito.any())).thenReturn(Flux.just());

        client.listSettings(new SettingSelector()).collectList().block();
        assertTrue(client.getBackoffEndTime().isBefore(Instant.now()));
        assertEquals(0, client.getFailedAttempts());
    }

}
