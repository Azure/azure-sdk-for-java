// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for logging requests and responses. In most
 * cases, it is sufficient to configure an object of the {@link LoggingOptions} type and set those as a field on a
 * {@link PipelineOptions} structure to configure a default pipeline. The factory and policy must only be used directly
 * when creating a custom pipeline.
 */
public final class LoggingFactory implements RequestPolicyFactory {

    private static final Logger FORCE_LOGGER = Logger.getLogger(LoggingFactory.class.getName());
    private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger(LoggingFactory.class.getName());
    private static final Map<HttpPipelineLogLevel, Level> JAVA_LOG_LEVEL_MAP = new HashMap<>();
    private static boolean defaultLoggerLoaded;

    static {
        try {
            FORCE_LOGGER.setLevel(Level.WARNING);

            // Create the logs directory if it doesn't exist.
            File logDir = new File(System.getProperty("java.io.tmpdir"), "AzureStorageJavaSDKLogs");
            if (!logDir.exists()) {
                if (!logDir.mkdir()) {
                    throw new Exception("Could not create logs directory");
                }
            }

            /*
            "/" the local pathname separator
            "%t" the system temporary directory
            "%h" the value of the "user.home" system property
            "%g" the generation number to distinguish rotated logs
            "%u" a unique number to resolve conflicts
            "%%" translates to a single percent sign "%"

            10MB files, 5 files

            true- append mode
             */
            FileHandler handler = new FileHandler("%t/AzureStorageJavaSDKLogs/%u%g", 10 * Constants.MB, 5, false);
            handler.setLevel(Level.WARNING);
            FORCE_LOGGER.addHandler(handler);

            JAVA_LOG_LEVEL_MAP.put(HttpPipelineLogLevel.ERROR, Level.SEVERE);
            JAVA_LOG_LEVEL_MAP.put(HttpPipelineLogLevel.WARNING, Level.WARNING);
            JAVA_LOG_LEVEL_MAP.put(HttpPipelineLogLevel.INFO, Level.INFO);
            defaultLoggerLoaded = true;

        /*
        If we can't setup default logging, there's nothing we can do. We shouldn't interfere with the rest of logging.
         */
        } catch (Exception e) {
            defaultLoggerLoaded = false;
            System.err.println("Azure Storage default logging could not be configured due to the following exception: "
                    + e);
        }
    }

    private final LoggingOptions loggingOptions;

    /**
     * Creates a factory which can create LoggingPolicy objects to insert in the pipeline. This will allow for logging
     * requests and responses.
     *
     * @param loggingOptions
     *         The configurations for this factory. Null will indicate use of the default options.
     */
    public LoggingFactory(LoggingOptions loggingOptions) {
        this.loggingOptions = loggingOptions == null ? new LoggingOptions() : loggingOptions;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new LoggingPolicy(this, next, options);
    }

    private static final class LoggingPolicy implements RequestPolicy {

        private final LoggingFactory factory;

        private final RequestPolicy nextPolicy;

        private final RequestPolicyOptions options;

        // The following fields are not final because they are updated by the policy.
        private int tryCount;

        private long operationStartTime;

        private long requestStartTime;

        /**
         * Creates a policy which configures the logging behavior within the
         * {@link com.microsoft.rest.v2.http.HttpPipeline}.
         *
         * @param nextPolicy
         *         {@link RequestPolicy}
         * @param options
         *         {@link RequestPolicyOptions}
         * @param factory
         *         {@link LoggingFactory}
         */
        private LoggingPolicy(LoggingFactory factory, RequestPolicy nextPolicy, RequestPolicyOptions options) {
            this.factory = factory;
            this.nextPolicy = nextPolicy;
            this.options = options;
        }

        /**
         * Logs as appropriate.
         *
         * @param request
         *         The request to log.
         *
         * @return A {@link Single} representing the {@link HttpResponse} that will arrive asynchronously.
         */
        @Override
        public Single<HttpResponse> sendAsync(final HttpRequest request) {
            this.tryCount++;
            this.requestStartTime = System.currentTimeMillis();
            if (this.tryCount == 1) {
                this.operationStartTime = requestStartTime;
            }

            if (this.shouldLog(HttpPipelineLogLevel.INFO)) {
                String logMessage = String.format("'%s'==> OUTGOING REQUEST (Try number='%d')%n",
                        sanitizeURL(request.url()), this.tryCount);
                this.log(HttpPipelineLogLevel.INFO, logMessage);
            }

            return nextPolicy.sendAsync(request)
                    .doOnError(throwable -> {
                        if (this.shouldLog(HttpPipelineLogLevel.ERROR)) {
                            String logMessage = String.format(
                                    "Unexpected failure attempting to make request.%nError message:'%s'%n",
                                    throwable.getMessage());
                            this.log(HttpPipelineLogLevel.ERROR, logMessage);
                        }
                    })
                    .doOnSuccess(response -> {
                        long requestEndTime = System.currentTimeMillis();
                        long requestCompletionTime = requestEndTime - requestStartTime;
                        long operationDuration = requestEndTime - operationStartTime;
                        HttpPipelineLogLevel currentLevel = HttpPipelineLogLevel.INFO;

                        String logMessage = Constants.EMPTY_STRING;
                        if (this.shouldLog(HttpPipelineLogLevel.INFO)) {
                            // Assume success and default to informational logging.
                            logMessage = "Successfully Received Response" + System.lineSeparator();
                        }

                        // If the response took too long, we'll upgrade to warning.
                        if (requestCompletionTime
                                >= factory.loggingOptions.minDurationToLogSlowRequestsInMs()) {
                            // Log a warning if the try duration exceeded the specified threshold.
                            if (this.shouldLog(HttpPipelineLogLevel.WARNING)) {
                                currentLevel = HttpPipelineLogLevel.WARNING;
                                logMessage = String.format(Locale.ROOT,
                                        "SLOW OPERATION. Duration > %d ms.%n",
                                        factory.loggingOptions.minDurationToLogSlowRequestsInMs());
                            }
                        }

                        if (((response.statusCode() >= 400 && response.statusCode() <= 499)
                                 && (response.statusCode() != HttpURLConnection.HTTP_NOT_FOUND
                                         && response.statusCode() != HttpURLConnection.HTTP_CONFLICT
                                         && response.statusCode() != HttpURLConnection.HTTP_PRECON_FAILED
                                         && response.statusCode() != 416))
                                        /* 416 is missing from the Enum but it is Range Not Satisfiable */
                                || (response.statusCode() >= 500 && response.statusCode() <= 509)) {
                            String errorString = String.format(Locale.ROOT,
                                    "REQUEST ERROR%nHTTP request failed with status code:'%d'%n",
                                    response.statusCode());
                            if (currentLevel == HttpPipelineLogLevel.WARNING) {
                                logMessage += errorString;
                            } else {
                                logMessage = errorString;
                            }

                            currentLevel = HttpPipelineLogLevel.ERROR;
                        }

                        /*
                        We don't want to format the log message unless we have to. Format once we've determined that
                        either the customer wants this log level or we need to force log it.
                         */
                        if (this.shouldLog(currentLevel)) {
                            String additionalMessageInfo = buildAdditionalMessageInfo(request);
                            String messageInfo = String.format(Locale.ROOT,
                                    "Request try:'%d', request duration:'%d' ms, operation duration:'%d' ms%n%s",
                                    tryCount, requestCompletionTime, operationDuration, additionalMessageInfo);
                            this.log(currentLevel, logMessage + messageInfo);
                        }
                    });
        }

        private String buildAdditionalMessageInfo(final HttpRequest httpRequest) {
            HttpRequest sanitizedRequest = buildSanitizedRequest(httpRequest);
            StringBuilder stringBuilder = new StringBuilder();
            String format = "%s: %s" + System.lineSeparator();
            stringBuilder.append(String.format(format, sanitizedRequest.httpMethod().toString(),
                    sanitizedRequest.url().toString()));
            sanitizedRequest.headers().forEach((header) -> stringBuilder.append(String.format(format, header.name(),
                    header.value())));
            return stringBuilder.toString();
        }

        private HttpRequest buildSanitizedRequest(final HttpRequest initialRequest) {
            // Build new URL and redact SAS signature, if present
            URL url = sanitizeURL(initialRequest.url());

            // Build resultRequest
            HttpRequest resultRequest = new HttpRequest(
                    initialRequest.callerMethod(),
                    initialRequest.httpMethod(),
                    url,
                    initialRequest.headers(),
                    initialRequest.body(),
                    initialRequest.responseDecoder());

            // Redact Authorization header, if present
            if (resultRequest.headers().value(Constants.HeaderConstants.AUTHORIZATION) != null) {
                resultRequest.headers().set(Constants.HeaderConstants.AUTHORIZATION, Constants.REDACTED);
            }

            // Redact Copy Source header SAS signature, if present
            if (resultRequest.headers().value(Constants.HeaderConstants.COPY_SOURCE) != null) {
                try {
                    URL copySourceUrl = sanitizeURL(new URL(resultRequest.headers()
                            .value(Constants.HeaderConstants.COPY_SOURCE)));
                    resultRequest.headers().set(Constants.HeaderConstants.COPY_SOURCE, copySourceUrl.toString());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            return resultRequest;
        }

        private URL sanitizeURL(URL initialURL) {
            URL resultURL = initialURL;
            try {
                BlobURLParts urlParts = URLParser.parse(initialURL);
                if (urlParts.sasQueryParameters() == null || urlParts.sasQueryParameters().signature() == null) {
                    return resultURL;
                }
                urlParts.withSasQueryParameters(new SASQueryParameters(
                        urlParts.sasQueryParameters().version(),
                        urlParts.sasQueryParameters().services(),
                        urlParts.sasQueryParameters().resourceTypes(),
                        urlParts.sasQueryParameters().protocol(),
                        urlParts.sasQueryParameters().startTime(),
                        urlParts.sasQueryParameters().expiryTime(),
                        urlParts.sasQueryParameters().ipRange(),
                        urlParts.sasQueryParameters().identifier(),
                        urlParts.sasQueryParameters().resource(),
                        urlParts.sasQueryParameters().permissions(),
                        Constants.REDACTED,
                        urlParts.sasQueryParameters().cacheControl(),
                        urlParts.sasQueryParameters().contentDisposition(),
                        urlParts.sasQueryParameters().contentEncoding(),
                        urlParts.sasQueryParameters().contentLanguage(),
                        urlParts.sasQueryParameters().contentType(),
                        urlParts.sasQueryParameters().userDelegationKey()
                ));
                resultURL = urlParts.toURL();

                /*
                We are only making valid changes to what has already been validated as a URL (since we got it from a
                URL object), so there should be no need for either us or the caller to check this error.
                */
            } catch (UnknownHostException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return resultURL;
        }

        /*
        We need to support the HttpPipelineLogger as it already exists. We also want to allow users to hook up SLF4J.
        Finally, we need to do our own default logging.
         */
        private void log(HttpPipelineLogLevel level, String message) {
            /*
            We need to explicitly check before we send it to the HttpPipelineLogger as its log function may only
            expect to receive messages for which shouldLog() returns true.
             */
            if (this.options.shouldLog(level)) {
                this.options.log(level, message);
            }

            /*
            The Java logger and slf4j logger should do the correct thing given any log level. FORCE_LOGGER is
            configured to only log warnings and errors.
             */
            if (!this.factory.loggingOptions.disableDefaultLogging() && LoggingFactory.defaultLoggerLoaded) {
                FORCE_LOGGER.log(JAVA_LOG_LEVEL_MAP.get(level), message);
            }
            if (level.equals(HttpPipelineLogLevel.ERROR)) {
                SLF4J_LOGGER.error(message);
            } else if (level.equals(HttpPipelineLogLevel.WARNING)) {
                SLF4J_LOGGER.warn(message);
            } else if (level.equals(HttpPipelineLogLevel.INFO)) {
                SLF4J_LOGGER.info(message);
            }
        }

        /*
        Check the HttpPipelineLogger, SLF4J Logger, and Java Logger
         */
        private boolean shouldLog(HttpPipelineLogLevel level) {
            // Default log Warnings and Errors as long as default logging is enabled.
            if ((level.equals(HttpPipelineLogLevel.WARNING) || level.equals(HttpPipelineLogLevel.ERROR))
                    && !this.factory.loggingOptions.disableDefaultLogging() && LoggingFactory.defaultLoggerLoaded) {
                return true;
            }

            // The user has configured the HttpPipelineLogger to log at this level.
            if (this.options.shouldLog(level)) {
                return true;
            }

            // The SLF4J logger is configured at the given level.
            if ((level.equals(HttpPipelineLogLevel.INFO) && SLF4J_LOGGER.isInfoEnabled())
                    || (level.equals(HttpPipelineLogLevel.WARNING) && SLF4J_LOGGER.isWarnEnabled())
                    || (level.equals(HttpPipelineLogLevel.ERROR) && SLF4J_LOGGER.isErrorEnabled())) {
                return true;
            }

            return false;
        }
    }
}
