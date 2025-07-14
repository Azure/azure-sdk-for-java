// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.logging;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.implementation.instrumentation.Slf4jLoggerShim;
import io.clientcore.core.implementation.instrumentation.DefaultLogger;
import io.clientcore.core.utils.configuration.Configuration;

import java.nio.file.InvalidPathException;
import java.util.Collections;
import java.util.Map;

/**
 * This is a fluent logger helper class that wraps an SLF4J Logger (if available) or a default implementation of the
 * logger.
 *
 * <p>This logger logs format-able messages that use {@code {}} as the placeholder. When a {@link Throwable throwable}
 * is the last argument of the format varargs and the logger is enabled for the stack trace for the throwable is
 * logged.</p>
 *
 * <p>A minimum logging level threshold is determined by the
 * {@link Configuration#LOG_LEVEL LOG_LEVEL} environment configuration. By default logging is
 * <b>disabled</b>.</p>
 *
 * <p>The logger is capable of producing json-formatted messages enriched with key value pairs.
 * Context can be provided in the constructor and populated on every message or added per each log record.</p>
 * @see Configuration
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class ClientLogger {
    private final Slf4jLoggerShim logger;
    private final Map<String, Object> globalContext;

    /**
     * Retrieves a logger for the passed class.
     *
     * @param clazz Class creating the logger.
     */
    public ClientLogger(Class<?> clazz) {
        this(clazz, null);
    }

    /**
     * Retrieves a logger for the passed class name.
     *
     * @param className Class name creating the logger.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(String className) {
        this(className, null);
    }

    /**
     * Retrieves a logger for the passed class name.
     *
     * @param className Class name creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     * Objects are serialized with {@code toString()} method.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(String className, Map<String, Object> context) {
        logger = new Slf4jLoggerShim(getClassPathFromClassName(className));
        globalContext = context == null ? null : Collections.unmodifiableMap(context);
    }

    /**
     * Retrieves a logger for the passed class name with
     * context that will be populated on all log records produced with this logger.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context.</p>
     * * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger#globalcontext -->
     * <pre>
     * Map&lt;String, Object&gt; context = new HashMap&lt;&gt;&#40;&#41;;
     * context.put&#40;&quot;connectionId&quot;, &quot;95a47cf&quot;&#41;;
     *
     * ClientLogger loggerWithContext = new ClientLogger&#40;ClientLoggerJavaDocCodeSnippets.class, context&#41;;
     * loggerWithContext.info&#40;&quot;A formattable message. Hello, &#123;&#125;&quot;, name&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger#globalcontext -->
     *
     * @param clazz Class creating the logger.
     * @param context Context to be populated on every log record written with this logger.
     * Objects are serialized with {@code toString()} method.
     * @throws RuntimeException when logging configuration is invalid depending on SLF4J implementation.
     */
    public ClientLogger(Class<?> clazz, Map<String, Object> context) {
        logger = new Slf4jLoggerShim(clazz);
        globalContext = context == null ? null : Collections.unmodifiableMap(context);
    }

    ClientLogger(DefaultLogger defaultLogger, Map<String, Object> context) {
        logger = new Slf4jLoggerShim(defaultLogger);
        globalContext = context == null ? null : Collections.unmodifiableMap(context);
    }

    /**
     * Determines if the app or environment logger support logging at the given log level.
     *
     * @param logLevel Logging level for the log message.
     * @return Flag indicating if the environment and logger are configured to support logging at the given log level.
     */
    public boolean canLogAtLevel(LogLevel logLevel) {
        return logger.canLogAtLevel(logLevel);
    }

    /**
     * Creates {@link LoggingEvent} for {@code error} log level that can be
     * used to enrich log with additional context.
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at error level.</p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @return instance of {@link LoggingEvent}  or no-op if error logging is disabled.
     */
    public LoggingEvent atError() {
        return LoggingEvent.create(logger, LogLevel.ERROR, globalContext);
    }

    /**
     * Creates {@link LoggingEvent} for {@code warning} log level that can be
     * used to enrich log with additional context.
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at warning level.</p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atWarning -->
     * <pre>
     * logger.atWarning&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .setThrowable&#40;exception&#41;
     *     .log&#40;&quot;A structured log message with exception.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atWarning -->
     *
     * @return instance of {@link LoggingEvent} or no-op if warn logging is disabled.
     */
    public LoggingEvent atWarning() {
        return LoggingEvent.create(logger, LogLevel.WARNING, globalContext);
    }

    /**
     * Creates {@link LoggingEvent} for {@code info} log level that can be
     * used to enrich log with additional context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at info level.</p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atInfo -->
     * <pre>
     * logger.atInfo&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .addKeyValue&#40;&quot;hello&quot;, name&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atInfo -->
     *
     * @return instance of {@link LoggingEvent} or no-op if info logging is disabled.
     */
    public LoggingEvent atInfo() {
        return LoggingEvent.create(logger, LogLevel.INFORMATIONAL, globalContext);
    }

    /**
     * Creates {@link LoggingEvent} for {@code verbose} log level that can be
     * used to enrich log with additional context.
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at verbose level.</p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#primitive -->
     * <pre>
     * logger.atVerbose&#40;&#41;
     *     .addKeyValue&#40;&quot;key&quot;, 1L&#41;
     *     .log&#40;&quot;A structured log message.&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atverbose.addKeyValue#primitive -->
     *
     * @return instance of {@link LoggingEvent} or no-op if verbose logging is disabled.
     */
    public LoggingEvent atVerbose() {
        return LoggingEvent.create(logger, LogLevel.VERBOSE, globalContext);
    }

    /**
     * Creates {@link LoggingEvent} for log level that can be
     * used to enrich log with additional context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Logging with context at provided level.</p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.atLevel -->
     * <pre>
     * LogLevel level = response.getStatusCode&#40;&#41; == 200 ? LogLevel.INFORMATIONAL : LogLevel.WARNING;
     * logger.atLevel&#40;level&#41;
     *     .addKeyValue&#40;&quot;key&quot;, &quot;value&quot;&#41;
     *     .log&#40;&quot;message&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.atLevel -->
     *
     * @param level log level.
     * @return instance of {@link LoggingEvent} or no-op if logging at provided level is disabled.
     */
    public LoggingEvent atLevel(LogLevel level) {
        return LoggingEvent.create(logger, level, globalContext);
    }

    private static String getClassPathFromClassName(String className) {
        try {
            return Class.forName(className).getCanonicalName();
        } catch (ClassNotFoundException | InvalidPathException e) {
            // Swallow ClassNotFoundException as the passed class name may not correlate to an actual class.
            // Swallow InvalidPathException as the className may contain characters that aren't legal file characters.
            return className;
        }
    }

    /**
     * Creates {@link ExceptionLoggingEvent} that creates and logs the exception augmented with
     * additional context at the {@link LogLevel#ERROR} level.
     * <p><strong>Code samples</strong></p>
     *
     * <p> Creating new exception and logging it with context.</p>
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.throwableaterror.message -->
     * <pre>
     * throw logger.throwableAtError&#40;&#41;
     *     .addKeyValue&#40;&quot;url&quot;, url&#41;
     *     .log&#40;&quot;Invalid URL&quot;, IllegalArgumentException::new&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.throwableaterror.message -->
     *
     * <p> Creating new exception with cause and logging it with context.</p>
     * <!-- src_embed io.clientcore.core.instrumentation.logging.clientlogger.throwableaterror.cause -->
     * <pre>
     * try &#123;
     *     connect&#40;&quot;xyz.com&quot;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     throw logger.throwableAtError&#40;&#41;
     *         .addKeyValue&#40;&quot;requestId&quot;, requestId&#41;
     *         .log&#40;e, CoreException::from&#41;;
     * &#125;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.logging.clientlogger.throwableaterror.cause -->
     *
     * @return {@link ExceptionLoggingEvent}.
     */
    public ExceptionLoggingEvent throwableAtError() {
        return new ExceptionLoggingEvent(new LoggingEvent(logger, LogLevel.ERROR, globalContext, true));
    }

    /**
     * Creates {@link ExceptionLoggingEvent} that creates and logs the exception augmented with
     * additional context at the {@link LogLevel#WARNING} level.
     *
     * @return {@link ExceptionLoggingEvent}.
     */
    public ExceptionLoggingEvent throwableAtWarning() {
        return new ExceptionLoggingEvent(new LoggingEvent(logger, LogLevel.WARNING, globalContext, true));
    }
}
