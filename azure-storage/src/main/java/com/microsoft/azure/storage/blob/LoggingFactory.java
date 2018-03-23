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

import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import java.net.HttpURLConnection;

/**
 * Factory for logging requests and responses.
 */
public final class LoggingFactory implements RequestPolicyFactory {

    private final LoggingOptions loggingOptions;

    /**
     * Creates a factory which can create LoggingPolicy objects to insert in the pipeline. This will allow for logging
     * requests and responses.
     *
     * @param loggingOptions
     *      The configurations for this factory.
     */
    public LoggingFactory(LoggingOptions loggingOptions) {
        this.loggingOptions = loggingOptions == null ? LoggingOptions.DEFAULT : loggingOptions;
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
         *      A {@link RequestPolicy} object.
         * @param options
         *      A {@link RequestPolicyOptions} object.
         * @param factory
         *      A {@link LoggingFactory} object.
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
         *      The request to log.
         * @return
         *      A {@link Single} representing the {@link HttpResponse} that will arrive asynchronously.
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
                        "'%s'==> OUTGOING REQUEST (Try number='%d')%n", request.url(), this.tryCount);
            }

            // TODO: Need to change logic slightly when support for writing to event log/sys log support is added
            return nextPolicy.sendAsync(request)
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            if (options.shouldLog(HttpPipelineLogLevel.ERROR)) {
                                options.log(HttpPipelineLogLevel.ERROR,
                                        "Unexpected failure attempting to make request.%nError message:'%s'%n",
                                        throwable.getMessage());
                            }
                        }
                    })
                    .doOnSuccess(new Consumer<HttpResponse>() {
                        @Override
                        public void accept(HttpResponse response) {
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
                                    factory.loggingOptions.getMinDurationToLogSlowRequestsInMs()) {
                                // Log a warning if the try duration exceeded the specified threshold.
                                if (options.shouldLog(HttpPipelineLogLevel.WARNING)) {
                                    currentLevel = HttpPipelineLogLevel.WARNING;
                                    logMessage = String.format("SLOW OPERATION. Duration > %d ms.%n",
                                            factory.loggingOptions.getMinDurationToLogSlowRequestsInMs());
                                }
                            }

                            if (response.statusCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR ||
                                    (response.statusCode() >= HttpURLConnection.HTTP_BAD_REQUEST &&
                                            response.statusCode() != HttpURLConnection.HTTP_NOT_FOUND &&
                                            response.statusCode() != HttpURLConnection.HTTP_CONFLICT &&
                                            response.statusCode() != HttpURLConnection.HTTP_PRECON_FAILED &&
                                            response.statusCode() != 416
                                            /* 416 is missing from the Enum but it is Range Not Satisfiable */)) {
                                String errorString = String.format(
                                        "REQUEST ERROR%nHTTP request failed with status code:'%d'%n",
                                        response.statusCode());
                                if (currentLevel == HttpPipelineLogLevel.WARNING) {
                                    logMessage += errorString;
                                }
                                else {
                                    logMessage = errorString;
                                }

                                currentLevel = HttpPipelineLogLevel.ERROR;
                                // TODO: LOG THIS TO WINDOWS EVENT LOG/SYS LOG
                            }

                            if (options.shouldLog(currentLevel)) {
                                String messageInfo = String.format(
                                        "Request try:'%d', request duration:'%d' ms, operation duration:'%d' ms%n",
                                        tryCount, requestCompletionTime, operationDuration);
                                options.log(HttpPipelineLogLevel.INFO, logMessage + messageInfo);
                            }
                        }
                    });
        }
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new LoggingPolicy(this, next, options);
    }
}
