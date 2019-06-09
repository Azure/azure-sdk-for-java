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
package com.azure.data.cosmos.rx;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.azure.data.cosmos.rx.proxy.HttpProxyServer;

import reactor.core.publisher.Mono;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class help to test proxy host feature scenarios where user can provide proxy
 * host server during AsyncDocumentClient initialization and all its request will
 * go through that particular host. 
 *
 */
public class ProxyHostTest extends TestSuiteBase {

    private static CosmosDatabase createdDatabase;
    private static CosmosContainer createdCollection;

    private CosmosClient client;
    private final String PROXY_HOST = "localhost";
    private final int PROXY_PORT = 8080;
    private HttpProxyServer httpProxyServer;

    public ProxyHostTest() {
        this.clientBuilder = createGatewayRxDocumentClient();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        httpProxyServer = new HttpProxyServer();
        httpProxyServer.start();
        // wait for proxy server to be ready
        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * This test will try to create document via http proxy server and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithValidHttpProxy() throws Exception {
        CosmosClient clientWithRightProxy = null;
        try {
            ConnectionPolicy connectionPolicy =new ConnectionPolicy();
            connectionPolicy.proxy(PROXY_HOST, PROXY_PORT);
            clientWithRightProxy = CosmosClient.builder().endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .connectionPolicy(connectionPolicy)
                    .consistencyLevel(ConsistencyLevel.SESSION).build();
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = clientWithRightProxy.getDatabase(createdDatabase.id()).getContainer(createdCollection.id())
                    .createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id())
                    .build();
            validateSuccess(createObservable, validator);
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
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(LogLevelTest.NETWORK_LOGGING_CATEGORY).setLevel(Level.TRACE);
        CosmosClient clientWithRightProxy = null;
        try {
            StringWriter consoleWriter = new StringWriter();
            WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
            Logger.getLogger(LogLevelTest.NETWORK_LOGGING_CATEGORY).addAppender(appender);

            ConnectionPolicy connectionPolicy =new ConnectionPolicy();
            connectionPolicy.proxy(PROXY_HOST, PROXY_PORT);
            clientWithRightProxy = CosmosClient.builder().endpoint(TestConfigurations.HOST)
                    .key(TestConfigurations.MASTER_KEY)
                    .connectionPolicy(connectionPolicy)
                    .consistencyLevel(ConsistencyLevel.SESSION).build();
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = clientWithRightProxy.getDatabase(createdDatabase.id()).getContainer(createdCollection.id())
                    .createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id())
                    .build();
            validateSuccess(createObservable, validator);

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
        // wait for proxy server to be shutdown
        TimeUnit.SECONDS.sleep(1);

        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
    }

    @BeforeMethod(groups = { "simple"})
    public void beforeMethod() {
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
    }

    @AfterMethod(groups = { "simple" })
    public void afterMethod(Method method) {
        super.beforeMethod(method);
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
    }

    private CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }
}
