/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.DatabaseAccount;
import com.azure.data.cosmos.internal.BaseAuthorizationTokenProvider;
import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.AsyncDocumentClient.Builder;
import com.azure.data.cosmos.rx.TestConfigurations;
import com.azure.data.cosmos.internal.TestSuiteBase;
import com.azure.data.cosmos.internal.SpyClientUnderTestFactory;
import com.azure.data.cosmos.internal.SpyClientUnderTestFactory.ClientUnderTest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;

public class GatewayServiceConfigurationReaderTest extends TestSuiteBase {

    private static final int TIMEOUT = 8000;
    private CompositeHttpClient<ByteBuf, ByteBuf> mockHttpClient;
    private CompositeHttpClient<ByteBuf, ByteBuf> httpClient;
    private BaseAuthorizationTokenProvider baseAuthorizationTokenProvider;
    private ConnectionPolicy connectionPolicy;
    private GatewayServiceConfigurationReader mockGatewayServiceConfigurationReader;
    private GatewayServiceConfigurationReader gatewayServiceConfigurationReader;
    private AsyncDocumentClient client;
    private String databaseAccountJson;
    private DatabaseAccount expectedDatabaseAccount;

    @Factory(dataProvider = "clientBuilders")
    public GatewayServiceConfigurationReaderTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @BeforeClass(groups = "simple")
    public void setup() throws Exception {
        client = clientBuilder.build();
        mockHttpClient = (CompositeHttpClient<ByteBuf, ByteBuf>) Mockito.mock(CompositeHttpClient.class);

        ClientUnderTest clientUnderTest = SpyClientUnderTestFactory.createClientUnderTest(this.clientBuilder);
        httpClient = clientUnderTest.getSpyHttpClient();
        baseAuthorizationTokenProvider = new BaseAuthorizationTokenProvider(TestConfigurations.MASTER_KEY);
        connectionPolicy = ConnectionPolicy.defaultPolicy();
        mockGatewayServiceConfigurationReader = new GatewayServiceConfigurationReader(new URI(TestConfigurations.HOST),
                false, TestConfigurations.MASTER_KEY, connectionPolicy, baseAuthorizationTokenProvider, mockHttpClient);

        gatewayServiceConfigurationReader = new GatewayServiceConfigurationReader(new URI(TestConfigurations.HOST),
                                                                                  false,
                                                                                  TestConfigurations.MASTER_KEY,
                                                                                  connectionPolicy,
                                                                                  baseAuthorizationTokenProvider,
                                                                                  httpClient);
        databaseAccountJson = IOUtils
                .toString(getClass().getClassLoader().getResourceAsStream("databaseAccount.json"), "UTF-8");
        expectedDatabaseAccount = new DatabaseAccount(databaseAccountJson);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @Test(groups = "simple")
    public void mockInitializeReaderAsync() throws Exception {

        HttpClientResponse<ByteBuf> mockedResponse = getMockResponse(databaseAccountJson);

        Mockito.when(mockHttpClient.submit(Matchers.any(RxClient.ServerInfo.class), Matchers.any()))
                .thenReturn(Observable.just(mockedResponse));

        Single<DatabaseAccount> databaseAccount = mockGatewayServiceConfigurationReader.initializeReaderAsync();
        validateSuccess(databaseAccount, expectedDatabaseAccount);
    }

    @Test(groups = "simple")
    public void mockInitializeReaderAsyncWithResourceToken() throws Exception {
        mockGatewayServiceConfigurationReader = new GatewayServiceConfigurationReader(new URI(TestConfigurations.HOST),
                true, "SampleResourceToken", connectionPolicy, baseAuthorizationTokenProvider, mockHttpClient);

        HttpClientResponse<ByteBuf> mockedResponse = getMockResponse(databaseAccountJson);

        Mockito.when(mockHttpClient.submit(Matchers.any(RxClient.ServerInfo.class), Matchers.any()))
                .thenReturn(Observable.just(mockedResponse));

        Single<DatabaseAccount> databaseAccount = mockGatewayServiceConfigurationReader.initializeReaderAsync();
        validateSuccess(databaseAccount, expectedDatabaseAccount);
    }

    @Test(groups = "simple")
    public void initializeReaderAsync() {
        Single<DatabaseAccount> databaseAccount = gatewayServiceConfigurationReader.initializeReaderAsync();
        validateSuccess(databaseAccount);
    }

    public static void validateSuccess(Single<DatabaseAccount> observable) {
        TestSubscriber<DatabaseAccount> testSubscriber = new TestSubscriber<DatabaseAccount>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        assertThat(BridgeInternal.getQueryEngineConfiuration(testSubscriber.getOnNextEvents().get(0)).size() > 0).isTrue();
        assertThat(BridgeInternal.getReplicationPolicy(testSubscriber.getOnNextEvents().get(0))).isNotNull();
        assertThat(BridgeInternal.getSystemReplicationPolicy(testSubscriber.getOnNextEvents().get(0))).isNotNull();
    }

    public static void validateSuccess(Single<DatabaseAccount> observable, DatabaseAccount expectedDatabaseAccount)
            throws InterruptedException {
        TestSubscriber<DatabaseAccount> testSubscriber = new TestSubscriber<DatabaseAccount>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        assertThat(testSubscriber.getOnNextEvents().get(0).id()).isEqualTo(expectedDatabaseAccount.id());
        assertThat(testSubscriber.getOnNextEvents().get(0).getAddressesLink())
                .isEqualTo(expectedDatabaseAccount.getAddressesLink());
        assertThat(testSubscriber.getOnNextEvents().get(0).getWritableLocations().iterator().next().getEndpoint())
                .isEqualTo(expectedDatabaseAccount.getWritableLocations().iterator().next().getEndpoint());
        assertThat(BridgeInternal.getSystemReplicationPolicy(testSubscriber.getOnNextEvents().get(0)).getMaxReplicaSetSize())
                .isEqualTo(BridgeInternal.getSystemReplicationPolicy(expectedDatabaseAccount).getMaxReplicaSetSize());
        assertThat(BridgeInternal.getSystemReplicationPolicy(testSubscriber.getOnNextEvents().get(0)).getMaxReplicaSetSize())
                .isEqualTo(BridgeInternal.getSystemReplicationPolicy(expectedDatabaseAccount).getMaxReplicaSetSize());
        assertThat(BridgeInternal.getQueryEngineConfiuration(testSubscriber.getOnNextEvents().get(0)))
                .isEqualTo(BridgeInternal.getQueryEngineConfiuration(expectedDatabaseAccount));
    }

    private HttpClientResponse<ByteBuf> getMockResponse(String databaseAccountJson) {
        HttpClientResponse<ByteBuf> resp = Mockito.mock(HttpClientResponse.class);
        Mockito.doReturn(HttpResponseStatus.valueOf(200)).when(resp).getStatus();
        Mockito.doReturn(Observable.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, databaseAccountJson)))
                .when(resp).getContent();

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        DefaultHttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(200), httpHeaders);

        try {
            Constructor<HttpResponseHeaders> constructor = HttpResponseHeaders.class
                    .getDeclaredConstructor(HttpResponse.class);
            constructor.setAccessible(true);
            HttpResponseHeaders httpResponseHeaders = constructor.newInstance(httpResponse);
            Mockito.doReturn(httpResponseHeaders).when(resp).getHeaders();

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Failed to instantiate class object.", e);
        }
        return resp;
    }
}