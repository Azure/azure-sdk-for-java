/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.HttpClientRequestPolicyAdapter;
import com.microsoft.rest.v2.policy.UserAgentPolicy;
import io.reactivex.Single;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of RequestPolicies that will be applied to a HTTP request before it is sent and will
 * be applied to a HTTP response when it is received.
 */
public final class HttpPipeline {
    /**
     * The list of RequestPolicy factories that will be applied to HTTP requests and responses.
     * The factories appear in this list in the order that they will be applied to outgoing
     * requests.
     */
    private final RequestPolicy.Factory[] requestPolicyFactories;

    /**
     * The HttpClient that will be used to send requests unless the sendRequestAsync() method is
     * called with a different HttpClient.
     */
    private final HttpClientRequestPolicyAdapter httpClientRequestPolicyAdapter;

    /**
     * The optional properties that will be passed to each RequestPolicy as it is being created.
     */
    private final RequestPolicy.Options requestPolicyOptions;

    /**
     * Create a new HttpPipeline with the provided RequestPolicy factories.
     * @param requestPolicyFactories The RequestPolicy factories to apply to HTTP requests and
     *                               responses that pass through this HttpPipeline.
     * @param options The optional properties that will be set on this HTTP pipelines.
     */
    private HttpPipeline(RequestPolicy.Factory[] requestPolicyFactories, Options options) {
        this.requestPolicyFactories = requestPolicyFactories;

        final HttpClient httpClient = (options != null && options.httpClient() != null ? options.httpClient() : HttpClient.createDefault());
        this.httpClientRequestPolicyAdapter = new HttpClientRequestPolicyAdapter(httpClient);

        final Logger logger = (options != null ? options.logger() : null);
        this.requestPolicyOptions = new RequestPolicy.Options(logger);
    }

    /**
     * Get the HttpClient.
     * @return the HttpClient.
     */
    HttpClient httpClient() {
        return httpClientRequestPolicyAdapter.httpClient();
    }

    /**
     * Get the RequestPolicy factories that this pipeline contains.
     * @return the RequestPolicy factories that this pipeline contains.
     */
    RequestPolicy.Factory[] requestPolicyFactories() {
        return requestPolicyFactories;
    }

    /**
     * Send the provided HTTP request using this HttpPipeline's HttpClient after it has passed through
     * each of the RequestPolicies that have been configured on this HttpPipeline.
     * @param httpRequest The HttpRequest to send.
     * @return The HttpResponse that was received.
     */
    public Single<HttpResponse> sendRequestAsync(HttpRequest httpRequest) {
        RequestPolicy requestPolicy = httpClientRequestPolicyAdapter;
        for (final RequestPolicy.Factory requestPolicyFactory : requestPolicyFactories) {
            requestPolicy = requestPolicyFactory.create(requestPolicy, requestPolicyOptions);
        }
        return requestPolicy.sendAsync(httpRequest);
    }

    /**
     * Build a new HttpPipeline that will use the provided RequestPolicy factories.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(RequestPolicy.Factory... requestPolicyFactories) {
        return build((Options) null, requestPolicyFactories);
    }

    /**
     * Build a new HttpPipeline that will use the provided HttpClient and RequestPolicy factories.
     * @param httpClient The HttpClient to use.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(HttpClient httpClient, RequestPolicy.Factory... requestPolicyFactories) {
        return build(new Options().withHttpClient(httpClient), requestPolicyFactories);
    }

    /**
     * Build a new HttpPipeline that will use the provided HttpClient and RequestPolicy factories.
     * @param pipelineOptions The optional properties that can be set on the created HttpPipeline.
     * @param requestPolicyFactories The RequestPolicy factories to use.
     * @return The built HttpPipeline.
     */
    public static HttpPipeline build(Options pipelineOptions, RequestPolicy.Factory... requestPolicyFactories) {
        final HttpPipeline.Builder builder = new HttpPipeline.Builder(pipelineOptions);
        if (requestPolicyFactories != null) {
            for (final RequestPolicy.Factory requestPolicyFactory : requestPolicyFactories) {
                builder.withRequestPolicy(requestPolicyFactory);
            }
        }
        return builder.build();
    }

    /**
     * A builder class that can be used to create a HttpPipeline.
     */
    public static class Builder {
        /**
         * The optional properties that will be set on the created HTTP pipelines.
         */
        private Options options;

        /**
         * The list of RequestPolicy factories that will be applied to HTTP requests and responses.
         * The factories appear in this list in the reverse order that they will be applied to
         * outgoing requests.
         */
        private final List<RequestPolicy.Factory> requestPolicyFactories;

        /**
         * Create a new HttpPipeline builder.
         */
        public Builder() {
            this(null);
        }

        /**
         * Create a new HttpPipeline builder.
         *
         * @param options The optional properties that will be set on the created HTTP pipelines.
         */
        public Builder(Options options) {
            this.options = options;
            this.requestPolicyFactories = new ArrayList<>();
        }

        /**
         * Get the RequestPolicy factories in this HttpPipeline builder.
         * @return the RequestPolicy factories in this HttpPipeline builder.
         */
        List<RequestPolicy.Factory> requestPolicyFactories() {
            return requestPolicyFactories;
        }

        /**
         * Get the options for this HttpPipeline builder.
         * @return the options for this HttpPipeline builder.
         */
        Options options() {
            return options;
        }

        /**
         * Set the HttpClient that will be used by HttpPipelines that are created by this Builder.
         * @param httpClient The HttpClient to use.
         * @return This HttpPipeline builder.
         */
        public Builder withHttpClient(HttpClient httpClient) {
            if (options == null) {
                options = new Options();
            }
            options.withHttpClient(httpClient);
            return this;
        }

        /**
         * Set the Logger that will be used for each RequestPolicy within the created HttpPipeline.
         * @param logger The Logger to provide to each RequestPolicy.
         * @return This HttpPipeline options object.
         */
        public Builder withLogger(Logger logger) {
            if (options == null) {
                options = new Options();
            }
            options.withLogger(logger);
            return this;
        }

        /**
         * Add the provided RequestPolicy factory to this HttpPipeline builder.
         * @param requestPolicyFactory The RequestPolicy factory to add to this HttpPipeline builder.
         * @return This HttpPipeline builder.
         */
        public Builder withRequestPolicy(RequestPolicy.Factory requestPolicyFactory) {
            return withRequestPolicy(requestPolicyFactories.size(), requestPolicyFactory);
        }

        /**
         * Add the provided RequestPolicy factory to this HttpPipeline builder
         * at the provided index in the pipeline.
         * @param index The index to insert the provided RequestPolicy factory.
         * @param requestPolicyFactory The RequestPolicy factory to add to this
         *                             HttpPipeline builder.
         * @return This HttpPipeline builder.
         */
        public Builder withRequestPolicy(int index, RequestPolicy.Factory requestPolicyFactory) {
            // The requestPolicyFactories list is in reverse order that the
            // policies will be in. The caller of this method should be
            // providing the index based on the policy list, not the factory
            // list.
            final int insertIndex = requestPolicyFactories.size() - index;
            requestPolicyFactories.add(insertIndex, requestPolicyFactory);
            return this;
        }

        /**
         * Add the provided RequestPolicy factories to this HttpPipeline builder.
         * @param requestPolicyFactories The RequestPolicy factories to add to this
         *                               HttpPipeline builder.
         * @return This HttpPipeline builder.
         */
        public Builder withRequestPolicies(RequestPolicy.Factory... requestPolicyFactories) {
            for (RequestPolicy.Factory factory : requestPolicyFactories) {
                withRequestPolicy(factory);
            }
            return this;
        }

        /**
         * Add the provided RequestPolicy factory to this HttpPipeline builder
         * directly before the first instance of the provided RequestPolicy
         * factory type. If the provided RequestPolicy factory type is not
         * found, then the RequestPolicy factory will be added to the end of the
         * pipeline.
         * @param requestPolicyFactoryType The RequestPolicy factory type to
         *                                 search for.
         * @param requestPolicyFactory The RequestPolicy factory to add.
         * @return This HttpPipeline builder.
         */
        public Builder withRequestPolicyBefore(Class<? extends RequestPolicy.Factory> requestPolicyFactoryType, RequestPolicy.Factory requestPolicyFactory) {
            int searchIndex = 0;
            for (final RequestPolicy.Factory factory : requestPolicyFactories) {
                if (requestPolicyFactoryType.equals(factory.getClass())) {
                    break;
                }
                else {
                    ++searchIndex;
                }
            }
            final int factoryCount = requestPolicyFactories.size();

            if (searchIndex == factoryCount) {
                withRequestPolicy(requestPolicyFactory);
            } else {
                final int insertIndex = searchIndex + 1;
                requestPolicyFactories.add(insertIndex, requestPolicyFactory);
            }

            return this;
        }

        /**
         * Add the provided RequestPolicy factory to this HttpPipeline builder
         * directly after the first instance of the provided RequestPolicy
         * factory type. If the provided RequestPolicy factory type is not
         * found, then the RequestPolicy factory will be added to the end of the
         * pipeline.
         * @param requestPolicyFactoryType The RequestPolicy factory type to
         *                                 search for.
         * @param requestPolicyFactory The RequestPolicy factory to add.
         * @return This HttpPipeline builder.
         */
        public Builder withRequestPolicyAfter(Class<? extends RequestPolicy.Factory> requestPolicyFactoryType, RequestPolicy.Factory requestPolicyFactory) {
            int searchIndex = 0;
            for (final RequestPolicy.Factory factory : requestPolicyFactories) {
                if (requestPolicyFactoryType.equals(factory.getClass())) {
                    break;
                }
                else {
                    ++searchIndex;
                }
            }
            final int factoryCount = requestPolicyFactories.size();

            if (searchIndex == factoryCount) {
                withRequestPolicy(requestPolicyFactory);
            } else {
                requestPolicyFactories.add(searchIndex, requestPolicyFactory);
            }

            return this;
        }

        /**
         * Add a RequestPolicy that will add the providedd UserAgent header to each outgoing
         * HttpRequest.
         * @param userAgent The userAgent header value to add to each outgoing HttpRequest.
         * @return This HttpPipeline builder.
         */
        public Builder withUserAgent(String userAgent) {
            return withRequestPolicy(new UserAgentPolicy.Factory(userAgent));
        }

        /**
         * Create a new HttpPipeline from the RequestPolicy factories that have been added to this
         * HttpPipeline builder.
         * @return The created HttpPipeline.
         */
        public HttpPipeline build() {
            final int requestPolicyCount = requestPolicyFactories.size();
            final RequestPolicy.Factory[] requestPolicyFactoryArray = new RequestPolicy.Factory[requestPolicyCount];
            return new HttpPipeline(requestPolicyFactories.toArray(requestPolicyFactoryArray), options);
        }
    }

    /**
     * A Logger that can be added to an HttpPipeline. This enables each RequestPolicy to log
     * messages that can be used for debugging purposes.
     */
    public interface Logger {
        /**
         * The log level threshold for what logs will be logged.
         * @return The log level threshold for what logs will be logged.
         */
        LogLevel minimumLogLevel();

        /**
         * Log the provided message.
         * @param logLevel The LogLevel associated with this message.
         * @param message The message to log.
         * @param formattedArguments A variadic list of arguments that should be formatted into the
         *                           provided message.
         */
        void log(LogLevel logLevel, String message, Object... formattedArguments);
    }

    /**
     * An abstract Logger for HttpPipeline RequestPolicies that contains functionality that is
     * common to Loggers.
     */
    public abstract static class AbstractLogger implements Logger {
        private HttpPipeline.LogLevel minimumLogLevel = HttpPipeline.LogLevel.INFO;

        /**
         * Set the minimum log level that this logger should log. Anything with a higher log level
         * should be ignored.
         * @param minimumLogLevel The minimum log level to set.
         * @return This Logger.
         */
        public AbstractLogger withMinimumLogLevel(HttpPipeline.LogLevel minimumLogLevel) {
            this.minimumLogLevel = minimumLogLevel;
            return this;
        }

        @Override
        public HttpPipeline.LogLevel minimumLogLevel() {
            return minimumLogLevel;
        }

        protected static String format(String message, Object... formattedMessageArguments) {
            if (formattedMessageArguments != null && formattedMessageArguments.length >= 1) {
                message = String.format(message, formattedMessageArguments);
            }
            return message;
        }
    }

    /**
     * The different levels of logs from HttpPipeline's RequestPolicies.
     */
    public enum LogLevel {
        /**
         * A log level that indicates that no logs will be logged.
         */
        OFF,

        /**
         * An error log.
         */
        ERROR,

        /**
         * A warning log.
         */
        WARNING,

        /**
         * An information log.
         */
        INFO
    }

    /**
     * The optional properties that can be set on an HttpPipeline.
     */
    public static class Options {
        private HttpClient httpClient;
        private Logger logger;

        /**
         * Configure the HttpClient that will be used for the created HttpPipeline. If no HttpClient
         * is set (or if null is set), then a default HttpClient will be created for the
         * HttpPipeline.
         * @param httpClient the HttpClient to use for the created HttpPipeline.
         * @return This HttpPipeline options object.
         */
        public Options withHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Get the HttpClient that was set.
         * @return The HttpClient that was set.
         */
        HttpClient httpClient() {
            return httpClient;
        }

        /**
         * Configure the Logger that will be used for each RequestPolicy within the created
         * HttpPipeline.
         * @param logger The Logger to provide to each RequestPolicy.
         * @return This HttpPipeline options object.
         */
        public Options withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Get the Logger that was set.
         * @return The Logger that was set.
         */
        Logger logger() {
            return logger;
        }
    }
}
