// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotComposition;
import com.azure.identity.CredentialUnavailableException;

import reactor.core.publisher.Mono;

public class AppConfigurationReplicaClientTest {

    @Mock
    private ConfigurationClient clientMock;

    @Mock
    private HttpResponseException exceptionMock;

    @Mock
    private HttpResponse responseMock;

    @Mock
    private PagedIterable<ConfigurationSetting> settingsMock;

    @Mock
    private Supplier<Mono<PagedResponse<ConfigurationSetting>>> supplierMock;

    @Mock
    private Response<ConfigurationSetting> configurationSettingResponse;

    @Mock
    private Response<ConfigurationSnapshot> snapshotResponseMock;
    
    @Mock
    private Context contextMock;

    private final String endpoint = "clientTest.azconfig.io";

    private MockitoSession session;

    @BeforeEach
    public void setup() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanup() throws Exception {
        session.finishMocking();
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void getWatchKeyTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        ConfigurationSetting watchKey = new ConfigurationSetting().setKey("watch").setLabel("\0");

        when(clientMock.getConfigurationSettingWithResponse(Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
            Mockito.any())).thenReturn(configurationSettingResponse);
        when(configurationSettingResponse.getValue()).thenReturn(watchKey);

        assertEquals(watchKey, client.getWatchKey("watch", "\0", contextMock));

        when(configurationSettingResponse.getValue()).thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0", contextMock));

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0", contextMock));

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0", contextMock));

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.getWatchKey("watch", "\0", contextMock));

        when(clientMock.getConfigurationSettingWithResponse(Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
            Mockito.any())).thenThrow(new UncheckedIOException(new UnknownHostException()));
        assertThrows(AppConfigurationStatusException.class, () -> client.getWatchKey("watch", "\0", contextMock));
    }

    @Test
    public void listSettingsTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        ConfigurationSetting configurationSetting = new ConfigurationSetting().setKey("test-key");
        List<ConfigurationSetting> configurations = List.of(configurationSetting);

        PagedFlux<ConfigurationSetting> pagedFlux = new PagedFlux<>(supplierMock);
        PagedResponse<ConfigurationSetting> pagedResponse = new PagedResponseBase<Object, ConfigurationSetting>(null,
            200, null, configurations, null, null);
        when(supplierMock.get()).thenReturn(Mono.just(pagedResponse));

        when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any()))
            .thenReturn(new PagedIterable<>(pagedFlux));

        assertEquals(configurations, client.listSettings(new SettingSelector(), contextMock));

        when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any())).thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector(), contextMock));

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector(), contextMock));

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector(), contextMock));

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.listSettings(new SettingSelector(), contextMock));
    }

    @Test
    public void listFeatureFlagsTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        List<ConfigurationSetting> configurations = List.of(featureFlag);

        PagedFlux<ConfigurationSetting> pagedFlux = new PagedFlux<>(supplierMock);
        HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.ETAG, "fake-etag");
        PagedResponse<ConfigurationSetting> pagedResponse = new PagedResponseBase<Object, ConfigurationSetting>(null,
            200, headers, configurations, null, null);

        when(supplierMock.get()).thenReturn(Mono.just(pagedResponse));

        when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any()))
            .thenReturn(new PagedIterable<>(pagedFlux));

        assertEquals(configurations, client.listFeatureFlags(new SettingSelector(), contextMock).getFeatureFlags());

        when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any())).thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class,
            () -> client.listFeatureFlags(new SettingSelector(), contextMock));

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class,
            () -> client.listFeatureFlags(new SettingSelector(), contextMock));

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class,
            () -> client.listFeatureFlags(new SettingSelector(), contextMock));

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.listFeatureFlags(new SettingSelector(), contextMock));
    }

    @Test
    public void listSettingsUnknownHostTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any()))
            .thenThrow(new UncheckedIOException(new UnknownHostException()));
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettings(new SettingSelector(), contextMock));
    }

    @Test
    public void listSettingsNoCredentialTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any()))
            .thenThrow(new CredentialUnavailableException("No Credential"));

        assertThrows(CredentialUnavailableException.class, () -> client.listSettings(new SettingSelector(), contextMock));
    }

    @Test
    public void getWatchNoCredentialTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        when(clientMock.getConfigurationSettingWithResponse(Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any()))
            .thenThrow(new CredentialUnavailableException("No Credential"));

        assertThrows(CredentialUnavailableException.class, () -> client.getWatchKey("key", "label", contextMock));
    }

    @Test
    public void backoffTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

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
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class), Mockito.any()))
            .thenReturn(settingsMock);

        client.listSettings(new SettingSelector(), contextMock);
        assertTrue(client.getBackoffEndTime().isBefore(Instant.now()));
        assertEquals(0, client.getFailedAttempts());
    }

    @Test
    public void listSettingSnapshotTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        List<ConfigurationSetting> configurations = new ArrayList<>();
        ConfigurationSnapshot snapshot = new ConfigurationSnapshot(null);
        snapshot.setSnapshotComposition(SnapshotComposition.KEY);

        when(clientMock.getSnapshotWithResponse(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(snapshotResponseMock);
        when(snapshotResponseMock.getValue()).thenReturn(snapshot);
        when(clientMock.listConfigurationSettingsForSnapshot(Mockito.any())).thenReturn(settingsMock);

        assertEquals(configurations, client.listSettingSnapshot("SnapshotName", contextMock));

        when(clientMock.listConfigurationSettingsForSnapshot(Mockito.any())).thenThrow(exceptionMock);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.getStatusCode()).thenReturn(429);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettingSnapshot("SnapshotName", contextMock));

        when(responseMock.getStatusCode()).thenReturn(408);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettingSnapshot("SnapshotName", contextMock));

        when(responseMock.getStatusCode()).thenReturn(500);
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettingSnapshot("SnapshotName", contextMock));

        when(responseMock.getStatusCode()).thenReturn(499);
        assertThrows(HttpResponseException.class, () -> client.listSettingSnapshot("SnapshotName", contextMock));

        when(clientMock.getSnapshotWithResponse(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenThrow(new UncheckedIOException(new UnknownHostException()));
        assertThrows(AppConfigurationStatusException.class, () -> client.listSettingSnapshot("SnapshotName", contextMock));
    }

    @Test
    public void listSettingSnapshotInvalidCompositionTypeTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        ConfigurationSnapshot snapshot = new ConfigurationSnapshot(null);
        snapshot.setSnapshotComposition(SnapshotComposition.KEY_LABEL);

        when(clientMock.getSnapshotWithResponse(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(snapshotResponseMock);
        when(snapshotResponseMock.getValue()).thenReturn(snapshot);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> client.listSettingSnapshot("SnapshotName", contextMock));
        assertEquals("Snapshot SnapshotName needs to be of type Key.", e.getMessage());
    }

    @Test
    public void updateSyncTokenTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);
        String fakeToken = "fake_sync_token";

        client.updateSyncToken(fakeToken);
        verify(clientMock, times(1)).updateSyncToken(Mockito.eq(fakeToken));
        reset(clientMock);

        client.updateSyncToken(null);
        verify(clientMock, times(0)).updateSyncToken(Mockito.eq(fakeToken));
    }

    @Test
    public void checkWatchKeysTest() {
        AppConfigurationReplicaClient client = new AppConfigurationReplicaClient(endpoint, endpoint, clientMock);

        FeatureFlagConfigurationSetting featureFlag = new FeatureFlagConfigurationSetting("Alpha", false);
        List<ConfigurationSetting> configurations = List.of(featureFlag);

        PagedFlux<ConfigurationSetting> pagedFlux = new PagedFlux<>(supplierMock);
        HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.ETAG, "fake-etag");
        try {
            PagedResponse<ConfigurationSetting> pagedResponse = new PagedResponseBase<Object, ConfigurationSetting>(
                null, 200, headers, configurations, null, null);

            when(supplierMock.get()).thenReturn(Mono.just(pagedResponse));

            when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any()))
                .thenReturn(new PagedIterable<>(pagedFlux));

            assertTrue(client.checkWatchKeys(new SettingSelector(), contextMock));
            pagedResponse.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            PagedResponse<ConfigurationSetting> pagedResponse = new PagedResponseBase<Object, ConfigurationSetting>(
                null, 304, headers, configurations, null, null);

            when(supplierMock.get()).thenReturn(Mono.just(pagedResponse));

            when(clientMock.listConfigurationSettings(Mockito.any(), Mockito.any())).thenReturn(new PagedIterable<>(pagedFlux));

            assertFalse(client.checkWatchKeys(new SettingSelector(), contextMock));
            pagedResponse.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
