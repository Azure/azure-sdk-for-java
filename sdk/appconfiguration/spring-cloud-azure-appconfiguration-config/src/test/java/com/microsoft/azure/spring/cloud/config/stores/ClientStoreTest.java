// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.stores;

import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_ENDPOINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.microsoft.azure.spring.cloud.config.AppConfigurationCredentialProvider;
import com.microsoft.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.microsoft.azure.spring.cloud.config.resource.Connection;
import com.microsoft.azure.spring.cloud.config.resource.ConnectionPool;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import reactor.core.publisher.Mono;

public class ClientStoreTest {

    static TokenCredential tokenCredential;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private ClientStore clientStore;
    @Mock
    private ConfigurationClientBuilder builderMock;
    @Mock
    private ConfigurationAsyncClient clientMock;
    @Mock
    private TokenCredential credentialMock;
    private List<PagedResponse<ConfigurationSetting>> pagedResponses;

    private AppConfigurationProviderProperties appProperties;

    private ConnectionPool pool;

    @Before
    public void init() {
        appProperties = new AppConfigurationProviderProperties();
        appProperties.setMaxRetries(0);
        pool = new ConnectionPool();
    }

    @Test
    public void connectWithConnectionString() throws IOException {
        pool.put(TEST_ENDPOINT, TEST_CONN_STRING);

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(getConfigurationPagedFlux(1));

        assertEquals(test.listSettings(selector, TEST_ENDPOINT).size(), 1);
    }

    @Test
    public void userAssignedIdentityTest() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_ENDPOINT, "1111-1111-1111-1111"));

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(getConfigurationPagedFlux(1));

        assertEquals(test.listSettings(selector, TEST_ENDPOINT).size(), 1);
    }

    @Test
    public void systemAssignedIdentityTest() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(TEST_ENDPOINT, ""));

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        when(clientMock.listRevisions(Mockito.any(SettingSelector.class)))
            .thenReturn(getConfigurationPagedFlux(1));

        assertTrue(test.getRevison(selector, TEST_ENDPOINT) != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noIdentityTest() throws IOException {
        pool.put(TEST_ENDPOINT, new Connection(null));

        SettingSelector selector = new SettingSelector();

        clientStore = new ClientStore(appProperties, pool, null, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        test.getRevison(selector, TEST_ENDPOINT);
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

        clientStore = new ClientStore(appProperties, pool, provider, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        when(builderMock.endpoint(Mockito.eq(TEST_ENDPOINT))).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        when(clientMock.listConfigurationSettings(Mockito.any(SettingSelector.class)))
            .thenReturn(getConfigurationPagedFlux(1));

        assertEquals(test.listSettings(selector, TEST_ENDPOINT).size(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
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

        clientStore = new ClientStore(appProperties, pool, provider, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        assertEquals(test.listSettings(selector, TEST_ENDPOINT).size(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
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

        clientStore = new ClientStore(appProperties, pool, provider, null);
        ClientStore test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.addPolicy(Mockito.any(BaseAppConfigurationPolicy.class))).thenReturn(builderMock);
        when(builderMock.retryPolicy(Mockito.any(RetryPolicy.class))).thenReturn(builderMock);

        assertEquals(test.listSettings(selector, TEST_ENDPOINT).size(), 1);
    }

    private PagedFlux<ConfigurationSetting> getConfigurationPagedFlux(int noOfPages) throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        pagedResponses = IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
            .collect(Collectors.toList());

        return new PagedFlux<ConfigurationSetting>(
            () -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private PagedResponseBase<String, ConfigurationSetting> createPagedResponse(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, 200,
            httpHeaders,
            getItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private Mono<PagedResponse<ConfigurationSetting>> getNextPage(String continuationToken,
        List<PagedResponse<ConfigurationSetting>> pagedResponses) {
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(pagedResponses.get(Integer.valueOf(continuationToken)));
    }

    private List<ConfigurationSetting> getItems(int i) {
        ArrayList<ConfigurationSetting> lst = new ArrayList<ConfigurationSetting>();
        lst.add(new ConfigurationSetting());
        return lst;
    }

}
