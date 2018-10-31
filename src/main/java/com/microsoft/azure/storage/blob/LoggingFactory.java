/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for logging requests and responses. In most
 * cases, it is sufficient to configure an object of the {@link LoggingOptions} type and set those as a field on a
 * {@link PipelineOptions} structure to configure a default pipeline. The factory and policy must only be used directly
 * when creating a custom pipeline.
 */
public final class LoggingFactory implements RequestPolicyFactory {

    private final LoggingOptions loggingOptions;

    /**
     * Creates a factory which can create LoggingPolicy objects to insert in the pipeline. This will allow for logging
     * requests and responses.
     *
     * @param loggingOptions
     *         The configurations for this factory. Null will indicate use of the default options.
     */
    public LoggingFactory(LoggingOptions loggingOptions) {
        this.loggingOptions = loggingOptions == null ? LoggingOptions.DEFAULT : loggingOptions;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new LoggingPolicy(this, next, options);
    }

    private final class LoggingPolicy implements RequestPolicy {

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

            if (this.options.shouldLog(HttpPipelineLogLevel.INFO)) {
                this.options.log(HttpPipelineLogLevel.INFO,
                        String.format("'%s'==> OUTGOING REQUEST (Try number='%d')%n", request.url(), this.tryCount));
            }

            // TODO: Need to change logic slightly when support for writing to event log/sys log support is added
            return nextPolicy.sendAsync(request)
                    .doOnError(throwable -> {
                        if (options.shouldLog(HttpPipelineLogLevel.ERROR)) {
                            options.log(HttpPipelineLogLevel.ERROR,
                                    String.format(
                                            "Unexpected failure attempting to make request.%nError message:'%s'%n",
                                            throwable.getMessage()));
                        }
                    })
                    .doOnSuccess(response -> {
                        long requestEndTime = System.currentTimeMillis();
                        long requestCompletionTime = requestEndTime - requestStartTime;
                        long operationDuration = requestEndTime - operationStartTime;
                        HttpPipelineLogLevel currentLevel = HttpPipelineLogLevel.INFO;
                        // Check if error should be logged since there is nothing of higher priority.
                        if (!options.shouldLog(HttpPipelineLogLevel.ERROR)) {
                            return;
                        }

                        String logMessage = Constants.EMPTY_STRING;
                        if (options.shouldLog(HttpPipelineLogLevel.INFO)) {
                            // Assume success and default to informational logging.
                            logMessage = "Successfully Received Response" + System.lineSeparator();
                        }

                        // If the response took too long, we'll upgrade to warning.
                        if (requestCompletionTime >=
                                factory.loggingOptions.minDurationToLogSlowRequestsInMs()) {
                            // Log a warning if the try duration exceeded the specified threshold.
                            if (options.shouldLog(HttpPipelineLogLevel.WARNING)) {
                                currentLevel = HttpPipelineLogLevel.WARNING;
                                logMessage = String.format(Locale.ROOT,
                                        "SLOW OPERATION. Duration > %d ms.%n",
                                        factory.loggingOptions.minDurationToLogSlowRequestsInMs());
                            }
                        }

                        if (((response.statusCode() >= 400 && response.statusCode() <= 499) &&
                                (response.statusCode() != HttpURLConnection.HTTP_NOT_FOUND &&
                                        response.statusCode() != HttpURLConnection.HTTP_CONFLICT &&
                                        response.statusCode() != HttpURLConnection.HTTP_PRECON_FAILED &&
                                        response.statusCode() != 416)) ||
                                        /* 416 is missing from the Enum but it is Range Not Satisfiable */
                                (response.statusCode() >= 500 && response.statusCode() <= 509)) {
                            String errorString = String.format(Locale.ROOT,
                                    "REQUEST ERROR%nHTTP request failed with status code:'%d'%n",
                                    response.statusCode());
                            if (currentLevel == HttpPipelineLogLevel.WARNING) {
                                logMessage += errorString;
                            } else {
                                logMessage = errorString;
                            }

                            currentLevel = HttpPipelineLogLevel.ERROR;
                            // TODO: LOG THIS TO WINDOWS EVENT LOG/SYS LOG
                        }

                        if (options.shouldLog(currentLevel)) {
                            String additionalMessageInfo = buildAdditionalMessageInfo(request);
                            String messageInfo = String.format(Locale.ROOT,
                                    "Request try:'%d', request duration:'%d' ms, operation duration:'%d' ms%n%s",
                                    tryCount, requestCompletionTime, operationDuration, additionalMessageInfo);
                            options.log(currentLevel, logMessage + messageInfo);
                        }
                    });
        }

        private String buildAdditionalMessageInfo(final HttpRequest httpRequest) {
            HttpRequest sanitizedRequest = buildSanitizedRequest(httpRequest);
            StringBuilder stringBuilder = new StringBuilder();
            String format = "%s: %s" + System.lineSeparator();
            stringBuilder.append(String.format(format, sanitizedRequest.httpMethod().toString(), sanitizedRequest.url().toString()));
            sanitizedRequest.headers().forEach((header) -> stringBuilder.append(String.format(format, header.name(), header.value())));
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
            if(resultRequest.headers().value(Constants.HeaderConstants.AUTHORIZATION) != null) {
                resultRequest.headers().set(Constants.HeaderConstants.AUTHORIZATION, Constants.REDACTED);
            }

            // Redact Copy Source header SAS signature, if present
            if(resultRequest.headers().value(Constants.HeaderConstants.COPY_SOURCE) != null) {
                try {
                    URL copySourceUrl = sanitizeURL(new URL(resultRequest.headers().value(Constants.HeaderConstants.COPY_SOURCE)));
                    resultRequest.headers().set(Constants.HeaderConstants.COPY_SOURCE, copySourceUrl.toString());
                } catch(MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            return resultRequest;
        }

        private URL sanitizeURL(URL initialURL) {
            String urlString = initialURL.toString();
            URL resultURL = initialURL;
            try {
                BlobURLParts urlParts = URLParser.parse(initialURL);
                if(urlParts.sasQueryParameters() == null || urlParts.sasQueryParameters().signature() == null) {
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
                        urlParts.sasQueryParameters().contentType()
                ));
                resultURL = urlParts.toURL();

                /* We are only making valid changes to what has already been validated as a URL (since we got it from a URL object),
               so there should be no need for either us or the caller to check this error. */
            } catch(UnknownHostException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return resultURL;
        }
    }
}
