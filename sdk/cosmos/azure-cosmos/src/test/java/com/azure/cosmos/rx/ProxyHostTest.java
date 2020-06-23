// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.proxy.HttpProxyServer;
import org.apache.logging.log4j.Level;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class help to test proxy host feature scenarios where user can provide proxy
 * host server during AsyncDocumentClient initialization and all its request will
 * go through that particular host.
 *
 */
public class ProxyHostTest extends TestSuiteBase {

    private static CosmosAsyncDatabase createdDatabase;
    private static CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;
    private final String PROXY_HOST = "localhost";
    private final int PROXY_PORT = 8080;
    private HttpProxyServer httpProxyServer;

    public ProxyHostTest() {
        super(createGatewayRxDocumentClient());
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_ProxyHostTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        httpProxyServer = new HttpProxyServer();
        httpProxyServer.start();
        // wait for proxy server to be ready
        TimeUnit.SECONDS.sleep(1);
        LogLevelTest.resetLoggingConfiguration();
    }

    /**
     * This test will try to create document via http proxy server and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithValidHttpProxy() throws Exception {
        CosmosAsyncClient clientWithRightProxy = null;
        try {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setProxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT)));
            clientWithRightProxy = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
                                                            .key(TestConfigurations.MASTER_KEY)
                                                            .gatewayMode(gatewayConnectionConfig)
                                                            .consistencyLevel(ConsistencyLevel.SESSION)
                                                            .contentResponseOnWriteEnabled(true)
                                                            .buildAsyncClient();
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = clientWithRightProxy.getDatabase(createdDatabase.getId()).getContainer(createdCollection.getId())
                                                                                                .createItem(docDefinition, new CosmosItemRequestOptions());

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            this.validateItemSuccess(createObservable, validator);

        } finally {
            safeClose(clientWithRightProxy);
        }
    }

    /**
     * This test will try to create document via http proxy server with netty wire logging and validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithValidHttpProxyWithNettyWireLogging() throws Exception {
        CosmosAsyncClient clientWithRightProxy = null;
        try {
            final StringWriter consoleWriter = new StringWriter();
            LogLevelTest.addAppenderAndLogger(LogLevelTest.NETWORK_LOGGING_CATEGORY, Level.TRACE,
                "ProxyStringAppender", consoleWriter);

            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setProxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT)));
            clientWithRightProxy = new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
                                                            .key(TestConfigurations.MASTER_KEY)
                                                            .gatewayMode(gatewayConnectionConfig)
                                                            .consistencyLevel(ConsistencyLevel.SESSION)
                                                            .contentResponseOnWriteEnabled(true)
                                                            .buildAsyncClient();
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = clientWithRightProxy.getDatabase(createdDatabase.getId()).getContainer(createdCollection.getId())
                                                                                                .createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            this.validateItemSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).contains(LogLevelTest.LOG_PATTERN_1);
            assertThat(consoleWriter.toString()).contains(LogLevelTest.LOG_PATTERN_2);
            assertThat(consoleWriter.toString()).contains(LogLevelTest.LOG_PATTERN_3);
        } finally {
            safeClose(clientWithRightProxy);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() throws Exception {
        safeClose(client);
        httpProxyServer.shutDown();
        // wait for getProxy server to be shutdown
        TimeUnit.SECONDS.sleep(1);

        LogLevelTest.resetLoggingConfiguration();
    }

    @AfterMethod(groups = { "simple" })
    public void afterMethod(Method method) {
        LogLevelTest.resetLoggingConfiguration();
    }

    private InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }

    /**
     * This test will try to create gateway connection policy via non http proxy.
     *
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT,
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "Only http proxy type is supported.")
    public void createWithNonHttpProxy() {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setProxy(new ProxyOptions(ProxyOptions.Type.SOCKS4, new InetSocketAddress(PROXY_HOST, PROXY_PORT)));
    }
}
