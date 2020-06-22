// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class LogLevelTest extends TestSuiteBase {
    public final static String COSMOS_DB_LOGGING_CATEGORY = "com.azure.cosmos";
    public final static String NETWORK_LOGGING_CATEGORY = "com.azure.cosmos.netty-network";
    public final static String LOG_PATTERN_1 = "HTTP/1.1 200 Ok.";
    public final static String LOG_PATTERN_2 = "|  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |";
    public final static String LOG_PATTERN_3 = "USER_EVENT: SslHandshakeCompletionEvent(SUCCESS)";
    public final static String LOG_PATTERN_4 = "CONNECT: ";

    private static final String APPENDER_NAME = "StringWriterAppender";
    private static CosmosAsyncContainer createdCollection;
    private static CosmosAsyncClient client;

    public LogLevelTest() {
        super(createGatewayRxDocumentClient());
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_LogLevelTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterMethod(groups = { "simple" })
    public void afterMethod() {
        resetLoggingConfiguration();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
        resetLoggingConfiguration();
    }

    /**
     * This test will try to create document with netty wire DEBUG logging and
     * validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithDebugLevel() throws Exception {
        final StringWriter consoleWriter = new StringWriter();

        addAppenderAndLogger(NETWORK_LOGGING_CATEGORY, Level.DEBUG, APPENDER_NAME, consoleWriter);

        final Logger logger = LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY);
        assertThat(logger.isDebugEnabled()).isTrue();

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();

        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire WARN logging and
     * validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithWarningLevel() throws Exception {
        final StringWriter consoleWriter = new StringWriter();
        addAppenderAndLogger(NETWORK_LOGGING_CATEGORY, Level.WARN, APPENDER_NAME, consoleWriter);

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire TRACE logging and
     * validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithTraceLevel() throws Exception {
        final StringWriter consoleWriter = new StringWriter();

        addAppenderAndLogger(NETWORK_LOGGING_CATEGORY, Level.TRACE, APPENDER_NAME, consoleWriter);

        Logger logger = LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY);
        assertThat(logger.isTraceEnabled()).isTrue();

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

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
        final StringWriter consoleWriter = new StringWriter();

        addAppenderAndLogger(COSMOS_DB_LOGGING_CATEGORY, Level.TRACE, APPENDER_NAME, consoleWriter);

        Logger logger = LoggerFactory.getLogger(COSMOS_DB_LOGGING_CATEGORY);
        assertThat(logger.isTraceEnabled()).isTrue();

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

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
        final StringWriter consoleWriter = new StringWriter();
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration configuration = context.getConfiguration();

        // The cosmos DB logger has its level set to DEBUG
        final AppenderRef[] cosmosAppenderRef = new AppenderRef[] {
            AppenderRef.createAppenderRef("STDOUT", null, null)
        };
        final LoggerConfig cosmosConfig = LoggerConfig.createLogger(false, Level.DEBUG,
            COSMOS_DB_LOGGING_CATEGORY, null, cosmosAppenderRef, null, configuration, null);

        configuration.addLogger(COSMOS_DB_LOGGING_CATEGORY, cosmosConfig);
        context.updateLoggers();

        // The NETWORK_LOGGING should inherit its log level from the root configuration, which is info.
        final WriterAppender appender = WriterAppender.createAppender(PatternLayout.createDefaultLayout(configuration),
            null, consoleWriter, APPENDER_NAME, false, true);
        appender.start();

        org.apache.logging.log4j.core.Logger logger = context.getLogger(NETWORK_LOGGING_CATEGORY);
        logger.addAppender(appender);

        assertThat(LoggerFactory.getLogger(COSMOS_DB_LOGGING_CATEGORY).isDebugEnabled()).isTrue();
        assertThat(LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY).isInfoEnabled()).isTrue();

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire ERROR logging and
     * validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithErrorClient() throws Exception {
        final StringWriter consoleWriter = new StringWriter();

        addAppenderAndLogger(NETWORK_LOGGING_CATEGORY, Level.ERROR, APPENDER_NAME, consoleWriter);
        Logger logger = LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY);
        assertThat(logger.isErrorEnabled()).isTrue();

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    /**
     * This test will try to create document with netty wire INFO logging and
     * validate it.
     *
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithInfoLevel() throws Exception {
        final StringWriter consoleWriter = new StringWriter();

        addAppenderAndLogger(NETWORK_LOGGING_CATEGORY, Level.INFO, APPENDER_NAME, consoleWriter);
        Logger logger = LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY);
        assertThat(logger.isInfoEnabled()).isTrue();

        CosmosAsyncClient client = getClientBuilder().buildAsyncClient();
        try {
            InternalObjectNode docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse<InternalObjectNode>> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();
            validateItemSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    private InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(
                String.format("{ " + "\"id\": \"%s\", " + "\"mypk\": \"%s\", "
                        + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]" + "}", uuid, uuid));
        return doc;
    }

    /**
     * Resets the logging configuration.
     */
    static void resetLoggingConfiguration() {
        final URL resource = LogLevelTest.class.getClassLoader().getResource("log4j2-test.properties");
        assertThat(resource).isNotNull();

        final ConfigurationSource defaultConfigurationSource;
        try {
            defaultConfigurationSource = ConfigurationSource.fromUri(resource.toURI());
        } catch (URISyntaxException e) {
            fail("Should have been able to load test properties from: " + resource, e);
            return;
        }

        final Configuration defaultConfiguration = ConfigurationBuilderFactory.newConfigurationBuilder()
                                                                              .setConfigurationSource(defaultConfigurationSource)
                                                                              .build();

        // Stopping the old context so we can reinitialise it.
        final LoggerContext oldContext = (LoggerContext) LogManager.getContext(false);
        oldContext.stop();

        final LoggerContext context = Configurator.initialize(defaultConfiguration);

        assertThat(context).isNotSameAs(oldContext);
    }

    /**
     * Adds a {@link WriterAppender} associated with the given {@code loggerName} to the current logging configuration.
     *
     * @param loggerName Name of the logger to add.
     * @param logLevel Level for the logger to log at.
     * @param appenderName The name of the appender.
     * @param consoleWriter The {@link Writer} associated with the WriterAppender.
     */
    static void addAppenderAndLogger(String loggerName, Level logLevel, String appenderName, Writer consoleWriter) {
        final LoggerContext context = (LoggerContext)LogManager.getContext(false);
        final Configuration configuration = context.getConfiguration();
        final WriterAppender appender = WriterAppender.createAppender(PatternLayout.createDefaultLayout(configuration),
            null, consoleWriter, appenderName, false, true);
        appender.start();
        configuration.addAppender(appender);

        final AppenderRef[] appenderRefs = new AppenderRef[] {
            AppenderRef.createAppenderRef(appenderName, null, null)
        };
        final LoggerConfig loggerConfiguration = LoggerConfig.createLogger(false, logLevel,
            loggerName, null, appenderRefs, null, configuration, null);

        configuration.addLogger(loggerName, loggerConfiguration);
        context.updateLoggers();

        org.apache.logging.log4j.core.Logger logger = context.getLogger(loggerName);
        configuration.addLoggerAppender(logger, appender);

        // Enable this if you want to see the logging to console.
        // logger.addAppender(configuration.getAppender("STDOUT"));
    }
}
