// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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

public class ConfigurationClientWrapperTest {

    @Mock
    private ConfigurationClient clientMock;

    @Mock
    private HttpResponseException exceptionMock;
    
    @Mock
    private HttpResponse responseMock;
    
    @Mock
    private PagedIterable<ConfigurationSetting> settingsMock;

    private String endpoint = "clienttest.azconfig.io";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getWatchKeyTest() {
        ConfigurationClientWrapper client = new ConfigurationClientWrapper(endpoint, clientMock);

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
        ConfigurationClientWrapper client = new ConfigurationClientWrapper(endpoint, clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);

        assertEquals(settingsMock, client.listSettings(new SettingSelector()));

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

}
