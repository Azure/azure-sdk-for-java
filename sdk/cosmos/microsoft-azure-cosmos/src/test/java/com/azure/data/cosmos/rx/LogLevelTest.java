// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class LogLevelTest extends TestSuiteBase {
    public final static String COSMOS_DB_LOGGING_CATEGORY = "com.azure.data.cosmos";
    public final static String NETWORK_LOGGING_CATEGORY = "com.azure.data.cosmos.netty-network";
    public final static String LOG_PATTERN_1 = "HTTP/1.1 200 Ok.";
    public final static String LOG_PATTERN_2 = "|  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |";
    public final static String LOG_PATTERN_3 = "USER_EVENT: SslHandshakeCompletionEvent(SUCCESS)";
    public final static String LOG_PATTERN_4 = "CONNECT: ";

    private static final String APPENDER_NAME = "StringWriterAppender";
    private static CosmosContainer createdCollection;
    private static CosmosClient client;

    public LogLevelTest() {
        super(createGatewayRxDocumentClient());
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterMethod(groups = { "simple" })
    public void afterMethod() {
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration configuration = context.getConfiguration();

        final Appender existingAppender = configuration.getAppender(APPENDER_NAME);
        if (existingAppender != null) {
            configuration.getAppenders().remove(APPENDER_NAME);
            existingAppender.stop();
        }

        Set<String> loggers = new HashSet<>();
        loggers.add(COSMOS_DB_LOGGING_CATEGORY);
        loggers.add(NETWORK_LOGGING_CATEGORY);

        configuration.getLoggers().values().stream()
            .filter(x -> loggers.contains(x.getName()))
            .forEach(loggerConfig -> {
                System.out.printf("Removing existing logger '%s'.%n", loggerConfig.getName());
                configuration.removeLogger(loggerConfig.getName());
                loggerConfig.stop();
            });
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("log4j2-test.properties");

        Assert.assertNotNull(resource);
        ConfigurationSource configurationSource = ConfigurationSource.fromUri(resource.toURI());
        Configurator.initialize(null, configurationSource);
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

        Logger logger = LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY);
        Assert.assertTrue(logger.isDebugEnabled());

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
            validateSuccess(createObservable, validator);

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

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
            validateSuccess(createObservable, validator);

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
        Assert.assertTrue(logger.isTraceEnabled());

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_1);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_2);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_3);
            assertThat(consoleWriter.toString()).contains(LOG_PATTERN_4);

        } finally {
            safeClose(client);
        }
    }

    //FIXME test is flaky
    @Ignore
    @Test(timeOut = TIMEOUT)
    public void createDocumentWithTraceLevelAtRoot() throws Exception {
        final StringWriter consoleWriter = new StringWriter();

        addAppenderAndLogger(COSMOS_DB_LOGGING_CATEGORY, Level.INFO, APPENDER_NAME, consoleWriter);

        Logger logger = LoggerFactory.getLogger(COSMOS_DB_LOGGING_CATEGORY);
        Assert.assertTrue(logger.isInfoEnabled());

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
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

        Assert.assertTrue(LoggerFactory.getLogger(COSMOS_DB_LOGGING_CATEGORY).isDebugEnabled());
        Assert.assertTrue(LoggerFactory.getLogger(NETWORK_LOGGING_CATEGORY).isInfoEnabled());

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
            validateSuccess(createObservable, validator);

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
        Assert.assertTrue(logger.isErrorEnabled());

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
            validateSuccess(createObservable, validator);

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
        Assert.assertTrue(logger.isInfoEnabled());

        CosmosClient client = clientBuilder().build();
        try {
            CosmosItemProperties docDefinition = getDocumentDefinition();
            Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition,
                    new CosmosItemRequestOptions());
            CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                    .withId(docDefinition.id()).build();
            validateSuccess(createObservable, validator);

            assertThat(consoleWriter.toString()).isEmpty();
        } finally {
            safeClose(client);
        }
    }

    private CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(
                String.format("{ " + "\"id\": \"%s\", " + "\"mypk\": \"%s\", "
                        + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]" + "}", uuid, uuid));
        return doc;
    }

    private void addAppenderAndLogger(String loggerName, Level logLevel, String appenderName,
        StringWriter consoleWriter) {
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration configuration = context.getConfiguration();
        final WriterAppender appender = WriterAppender.createAppender(PatternLayout.createDefaultLayout(configuration),
            null, consoleWriter, appenderName, false, true);
        appender.start();

        final AppenderRef[] appenderRefs = new AppenderRef[] {
            AppenderRef.createAppenderRef(appenderName, null, null)
        };
        final LoggerConfig loggerConfiguration = LoggerConfig.createLogger(false, logLevel,
            loggerName, null, appenderRefs, null, configuration, null);

        configuration.addLogger(loggerName, loggerConfiguration);
        context.updateLoggers();

        org.apache.logging.log4j.core.Logger logger = context.getLogger(loggerName);
        logger.addAppender(appender);

        // Enable this if you want to see the logging to console.
        // logger.addAppender(configuration.getAppender("STDOUT"));
    }
}
