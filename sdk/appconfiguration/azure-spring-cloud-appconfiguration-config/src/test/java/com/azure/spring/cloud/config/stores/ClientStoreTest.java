// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;

import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.resource.Connection;
import com.azure.spring.cloud.config.resource.ConnectionPool;

import reactor.core.publisher.Mono;

public class ClientStoreTest {

    static TokenCredential tokenCredential;

    private ClientStore clientStore;

    @Mock
    private ConfigurationClientBuilder builderMock;

    @Mock
    private ConfigurationClient clientMock;

    @Mock
    private TokenCredential credentialMock;

    private AppConfigurationProviderProperties appProperties;

    private ConnectionPool pool;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        appProperties = new AppConfigurationProviderProperties();
        appProperties.setMaxRetries(0);
        pool = new ConnectionPool();
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void connectWithConnectionString() throws IOException {
        pool.put(TEST_ENDPOINT, TEST_CONN_STRING);

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null, false, false);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildClient()).thenReturn(clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
                .thenReturn(getConfigurationPagedFlux(1));

        assertEquals(1, test.listSettings(selector, TEST_ENDPOINT).stream().count());
    }

    @Test
    public void userAssignedIdentityTest() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_ENDPOINT, "1111-1111-1111-1111"));

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null, false, false);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildClient()).thenReturn(clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
                .thenReturn(getConfigurationPagedFlux(1));

        assertEquals(1, test.listSettings(selector, TEST_ENDPOINT).stream().count());
    }

    @Test
    public void systemAssignedIdentityTest() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_ENDPOINT, ""));

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null, false, false);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildClient()).thenReturn(clientMock);

        when(clientMock.getConfigurationSetting(Mockito.any(), Mockito.any()))
                .thenReturn(new ConfigurationSetting());

        assertTrue(test.getWatchKey(selector.getKeyFilter(), selector.getLabelFilter(), TEST_ENDPOINT) != null);
    }

    @Test
    public void noIdentityTest() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> pool.put(TEST_ENDPOINT, new Connection(null)));
    }

    @Test
    public void testPrivider() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_ENDPOINT, ""));

        SettingSelector selector = new SettingSelector();
        AppConfigurationCredentialProvider provider = new AppConfigurationCredentialProvider() {

            @Override
            public TokenCredential getAppConfigCredential(String uri) {
                assertEquals(TEST_ENDPOINT, uri);
                return credentialMock;
            }
        };

        clientStore = new ClientStore(appProperties, pool, provider, null, false, false);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildClient()).thenReturn(clientMock);
        
        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(getConfigurationPagedFlux(1));

        assertEquals(1, test.listSettings(selector, TEST_ENDPOINT).stream().count());
    }

    @Test
    public void multipleArgumentsClientIdProvider() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_ENDPOINT, "testclientid"));

        SettingSelector selector = new SettingSelector();
        AppConfigurationCredentialProvider provider = new AppConfigurationCredentialProvider() {

            @Override
            public TokenCredential getAppConfigCredential(String uri) {
                assertEquals(TEST_ENDPOINT, uri);
                return credentialMock;
            }
        };

        clientStore = new ClientStore(appProperties, pool, provider, null, false, false);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        assertThrows(IllegalArgumentException.class, () -> test.listSettings(selector, TEST_ENDPOINT).stream().count());
    }

    @Test
    public void multipleArgumentsConnectionStringProvider() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_CONN_STRING));

        SettingSelector selector = new SettingSelector();
        AppConfigurationCredentialProvider provider = new AppConfigurationCredentialProvider() {

            @Override
            public TokenCredential getAppConfigCredential(String uri) {
                assertEquals(TEST_ENDPOINT, uri);
                return credentialMock;
            }
        };

        clientStore = new ClientStore(appProperties, pool, provider, null, false, false);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        assertThrows(IllegalArgumentException.class, () -> test.listSettings(selector, TEST_ENDPOINT).stream().count());
    }

    private PagedIterable<ConfigurationSetting> getConfigurationPagedFlux(int noOfPages) throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().set("header1", "value1").set("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        Supplier<Mono<PagedResponse<ConfigurationSetting>>> s = () -> Mono
                .just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, noOfPages, noOfPages));

        PagedFlux<ConfigurationSetting> page = new PagedFlux<ConfigurationSetting>(s);

        PagedIterable<ConfigurationSetting> settings = new PagedIterable<ConfigurationSetting>(page);
        return settings;
    }

    private PagedResponseBase<String, ConfigurationSetting> createPagedResponse(HttpRequest httpRequest,
            HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, 200, httpHeaders, getItems(i),
                i < noOfPages - 1 ? String.valueOf(i + 1) : null, deserializedHeaders);
    }

    private List<ConfigurationSetting> getItems(int i) {
        ArrayList<ConfigurationSetting> lst = new ArrayList<ConfigurationSetting>();
        lst.add(new ConfigurationSetting().setKey("testKey").setLabel("\0"));
        return lst;
    }

}
