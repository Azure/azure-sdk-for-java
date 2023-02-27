// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.implementation.http.policy.TracingInfo;

public class AppConfigurationReplicaClientTest {

    @Mock
    private ConfigurationClient clientMock;

    @Mock
    private HttpResponseException exceptionMock;

    @Mock
    private HttpResponse responseMock;

    @Mock
    private PagedIterable<ConfigurationSetting> settingsMock;

    private final String endpoint = "clientTest.azconfig.io";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getWatchKeyTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0));

        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("watch").setLabel("\0");

        when(clientMock.getConfigurationSetting(Mockito.anyString(), Mockito.anyString())).thenReturn(watchKey);

        assertEquals(watchKey, client.getWatchKey("watch", "\0"));

        when(clientMock.getConfigurationSetting(Mockito.anyString(), Mockito.anyString()))
            .thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0"));

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0"));

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0"));

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.getWatchKey("watch", "\0"));
    }

    @Test
    public void listSettingsTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0));

        List<ConfigurationSetting> configurations = new ArrayList<>();

        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.iterator()).thenReturn(configurations.iterator());

        assertEquals(configurations, client.listSettings(new SettingSelector()));

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
    public void backoffTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, clientMock,
            new TracingInfo(false, false, 0));

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

        // Success in a list request results in a reset of failed attemtps
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class))).thenReturn(settingsMock);

        client.listSettings(new SettingSelector());
        assertTrue(client.getBackoffEndTime().isBefore(Instant.now()));
        assertEquals(0, client.getFailedAttempts());
    }

}
