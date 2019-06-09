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

package com.microsoft.azure.cosmosdb.rx;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.UUID;

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

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosItemRequestOptions;
import com.microsoft.azure.cosmos.CosmosItemResponse;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmos.CosmosResponseValidator;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

public class LogLevelTest extends TestSuiteBase {
    public final static String COSMOS_DB_LOGGING_CATEGORY = "com.microsoft.azure.cosmosdb";
    public final static String NETWORK_LOGGING_CATEGORY = "com.microsoft.azure.cosmosdb.netty-network";
    public final static String LOG_PATTERN_1 = "HTTP/1.1 200 Ok.";
    public final static String LOG_PATTERN_2 = "|  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |";
    public final static String LOG_PATTERN_3 = "USER_EVENT: SslHandshakeCompletionEvent(SUCCESS)";
    public final static String LOG_PATTERN_4 = "CONNECT: ";

    private static CosmosContainer createdCollection;
    private static CosmosClient client;

    public LogLevelTest() {
        this.clientBuilder = createGatewayRxDocumentClient();
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    /**
     * This test will try to create document with netty wire DEBUG logging and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithDebugLevel() throws Exception {
        LogManager.getLogger(NETWORK_LOGGING_CATEGORY).setLevel(Level.DEBUG);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        LogManager.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();

        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire WARN logging and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithWarningLevel() throws Exception {
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(NETWORK_LOGGING_CATEGORY).setLevel(Level.WARN);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        Logger.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire TRACE logging and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithTraceLevel() throws Exception {
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(NETWORK_LOGGING_CATEGORY).setLevel(Level.TRACE);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        Logger.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_1);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_2);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_3);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_4);

        } finally {
            safeClose(client);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithTraceLevelAtRoot() throws Exception {
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(COSMOS_DB_LOGGING_CATEGORY).setLevel(Level.TRACE);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        Logger.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_1);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_2);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_3);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_4);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithDebugLevelAtRoot() throws Exception {
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(COSMOS_DB_LOGGING_CATEGORY).setLevel(Level.DEBUG);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        Logger.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire ERROR logging and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithErrorClient() throws Exception {
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(NETWORK_LOGGING_CATEGORY).setLevel(Level.ERROR);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        Logger.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire INFO logging and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithInfoLevel() throws Exception {
        LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger(NETWORK_LOGGING_CATEGORY).setLevel(Level.INFO);
        StringWriter consoleWriter = new StringWriter();
        WriterAppender appender = new WriterAppender(new PatternLayout(), consoleWriter);
        Logger.getLogger(NETWORK_LOGGING_CATEGORY).addAppender(appender);

        CosmosClient client = clientBuilder.build();
        try {
            CosmosItemSettings docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    private CosmosItemSettings getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemSettings doc = new CosmosItemSettings(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }

    @BeforeMethod(groups = { "simple"})
    public void beforeMethod(Method method) {
        super.beforeMethod(method);
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
    }

    @AfterMethod(groups = { "simple" })
    public void afterMethod() {
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
    }
}
