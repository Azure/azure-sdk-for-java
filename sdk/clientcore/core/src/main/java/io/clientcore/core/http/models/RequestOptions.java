// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.Context;
import io.clientcore.core.utils.ProgressReporter;

import java.util.function.Consumer;

/**
 * This class contains the options to customize an {@link HttpRequest}. {@link RequestOptions} can be used to configure
 * the request headers, query params, the request body, or add a callback to modify all aspects of the
 * {@link HttpRequest}.
 *
 * <p>An instance of fully configured {@link RequestOptions} can be passed to a service method that preconfigures known
 * components of the request like URI, path params etc, further modifying both un-configured, or preconfigured
 * components.</p>
 *
 * <p>To demonstrate how this class can be used to construct a request, let's use a Pet Store service as an example.
 * The
 * list of APIs available on this service are <a href="https://petstore.swagger.io/#/pet">documented in the swagger
 * definition.</a></p>
 *
 * <p><strong>Creating an instance of RequestOptions</strong></p>
 * <!-- src_embed io.clientcore.core.http.rest.requestoptions.instantiation -->
 * <pre>
 * RequestOptions options = new RequestOptionsBuilder&#40;&#41;
 *     .setBody&#40;BinaryData.fromString&#40;&quot;&#123;&#92;&quot;name&#92;&quot;:&#92;&quot;Fluffy&#92;&quot;&#125;&quot;&#41;&#41;
 *     .addHeader&#40;new HttpHeader&#40;HttpHeaderName.fromString&#40;&quot;x-ms-pet-version&quot;&#41;, &quot;2021-06-01&quot;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.rest.requestoptions.instantiation -->
 *
 * <p><strong>Configuring the request with JSON body and making a HTTP POST request</strong></p>
 *
 * To <a href="https://petstore.swagger.io/#/pet/addPet">add a new pet to the pet store</a>, an HTTP POST call should be
 * made to the service with the details of the pet that is to be added. The details of the pet are included as the
 * request body in JSON format.
 *
 * The JSON structure for the request is defined as follows:
 *
 * <pre>{@code
 * {
 *   "id": 0,
 *   "category": {
 *     "id": 0,
 *     "name": "string"
 *   },
 *   "name": "doggie",
 *   "photoUris": [
 *     "string"
 *   ],
 *   "tags": [
 *     {
 *       "id": 0,
 *       "name": "string"
 *     }
 *   ],
 *   "status": "available"
 * }
 * }</pre>
 *
 * To create a concrete request, Json builder provided in javax package is used here for demonstration. However, any
 * other JSON building library can be used to achieve similar results.
 *
 * <!-- src_embed io.clientcore.core.http.rest.requestoptions.createjsonrequest -->
 * <pre>
 * JsonArray photoUris = new JsonArray&#40;&#41;
 *     .addElement&#40;&quot;https:&#47;&#47;imgur.com&#47;pet1&quot;&#41;
 *     .addElement&#40;&quot;https:&#47;&#47;imgur.com&#47;pet2&quot;&#41;;
 *
 * JsonArray tags = new JsonArray&#40;&#41;
 *     .addElement&#40;new JsonObject&#40;&#41;
 *         .setProperty&#40;&quot;id&quot;, 0&#41;
 *         .setProperty&#40;&quot;name&quot;, &quot;Labrador&quot;&#41;&#41;
 *     .addElement&#40;new JsonObject&#40;&#41;
 *         .setProperty&#40;&quot;id&quot;, 1&#41;
 *         .setProperty&#40;&quot;name&quot;, &quot;2021&quot;&#41;&#41;;
 *
 * JsonObject requestBody = new JsonObject&#40;&#41;
 *     .setProperty&#40;&quot;id&quot;, 0&#41;
 *     .setProperty&#40;&quot;name&quot;, &quot;foo&quot;&#41;
 *     .setProperty&#40;&quot;status&quot;, &quot;available&quot;&#41;
 *     .setProperty&#40;&quot;category&quot;, new JsonObject&#40;&#41;.setProperty&#40;&quot;id&quot;, 0&#41;.setProperty&#40;&quot;name&quot;, &quot;dog&quot;&#41;&#41;
 *     .setProperty&#40;&quot;photoUris&quot;, photoUris&#41;
 *     .setProperty&#40;&quot;tags&quot;, tags&#41;;
 *
 * BinaryData requestBodyData = BinaryData.fromObject&#40;requestBody&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.rest.requestoptions.createjsonrequest -->
 *
 * Now, this string representation of the JSON request can be set as body of {@link RequestOptions}.
 *
 * <!-- src_embed io.clientcore.core.http.rest.requestoptions.postrequest -->
 * <pre>
 * RequestOptions options = new RequestOptionsBuilder&#40;&#41;
 *     .addRequestCallback&#40;request -&gt; request
 *         &#47;&#47; may already be set if request is created from a client
 *         .setUri&#40;&quot;https:&#47;&#47;petstore.example.com&#47;pet&quot;&#41;
 *         .setMethod&#40;HttpMethod.POST&#41;
 *         .setBody&#40;requestBodyData&#41;
 *         .getHeaders&#40;&#41;.set&#40;HttpHeaderName.CONTENT_TYPE, &quot;application&#47;json&quot;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.http.rest.requestoptions.postrequest -->
 */
public final class RequestOptions {
    private static final RequestOptions NONE = new RequestOptions();
    private static final RequestOptions DESERIALIZE_BODY
        = new RequestOptionsBuilder().setResponseBodyMode(ResponseBodyMode.DESERIALIZE).build();
    private static final RequestOptions IGNORE_BODY
        = new RequestOptionsBuilder().setResponseBodyMode(ResponseBodyMode.IGNORE).build();

    private final Consumer<HttpRequest> requestCallback;
    private final Context context;
    private final ResponseBodyMode responseBodyMode;
    private final ClientLogger logger;
    private final InstrumentationContext instrumentationContext;
    private final ProgressReporter progressReporter;

    private RequestOptions() {
        this.requestCallback = request -> {
            // No-op
        };
        this.context = Context.none();
        this.responseBodyMode = null;
        this.logger = null;
        this.instrumentationContext = null;
        this.progressReporter = null;
    }

    /**
     * Creates a new instance of {@link RequestOptions}.
     */
    RequestOptions(Consumer<HttpRequest> requestCallback, Context context, ResponseBodyMode responseBodyMode,
        ClientLogger logger, InstrumentationContext instrumentationContext, ProgressReporter progressReporter) {
        this.requestCallback = requestCallback;
        this.context = context;
        this.responseBodyMode = responseBodyMode;
        this.logger = logger;
        this.instrumentationContext = instrumentationContext;
        this.progressReporter = progressReporter;
    }

    /**
     * Gets the request callback, applying all the configurations set on this instance of {@link RequestOptions}.
     *
     * @return The request callback.
     */
    public Consumer<HttpRequest> getRequestCallback() {
        return this.requestCallback;
    }

    /**
     * Gets the additional context on the request that is passed during the service call.
     *
     * @return The additional context that is passed during the service call.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the configuration indicating how the body of the resulting HTTP response should be handled.
     *
     * <p>For more information about the options for handling an HTTP response body, see {@link ResponseBodyMode}.</p>
     *
     * @return The configuration indicating how the body of the resulting HTTP response should be handled.
     */
    public ResponseBodyMode getResponseBodyMode() {
        return responseBodyMode;
    }

    /**
     * Gets the {@link ClientLogger} used to log the request and response.
     *
     * @return The {@link ClientLogger} used to log the request and response.
     */
    public ClientLogger getLogger() {
        return logger;
    }

    /**
     * Gets the {@link InstrumentationContext} used to instrument the request.
     *
     * @return The {@link InstrumentationContext} used to instrument the request.
     */
    public InstrumentationContext getInstrumentationContext() {
        return instrumentationContext;
    }

    /**
     * Gets the {@link ProgressReporter} used to track progress of I/O operations of the request.
     *
     * @return The {@link ProgressReporter} used to track progress of I/O operations of the request.
     */
    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }

    /**
     * An empty {@link RequestOptions} used in situations where there is no request-specific
     * configuration to pass into the request.
     *
     * @return The singleton instance of an empty {@link RequestOptions}.
     */
    public static RequestOptions none() {
        return NONE;
    }

    /**
     * {@link RequestOptions} with the response body mode set to {@link ResponseBodyMode#DESERIALIZE}.
     *
     * @return A {@link RequestOptions} with the response body mode set to {@link ResponseBodyMode#DESERIALIZE}.
     */
    public static RequestOptions deserializeResponse() {
        return DESERIALIZE_BODY;
    }

    /**
     * {@link RequestOptions} with the response body mode set to {@link ResponseBodyMode#IGNORE}.
     *
     * @return A {@link RequestOptions} with the response body mode set to {@link ResponseBodyMode#IGNORE}.
     */
    public static RequestOptions ignoreResponse() {
        return IGNORE_BODY;
    }

    /**
     * Creates a new {@link RequestOptionsBuilder} to create a new instance of {@link RequestOptions}.
     *
     * @return A new {@link RequestOptionsBuilder}.
     */
    public RequestOptionsBuilder toBuilder() {
        return new RequestOptionsBuilder().addRequestCallback(requestCallback)
            .setContext(context)
            .setResponseBodyMode(responseBodyMode)
            .setLogger(logger)
            .setInstrumentationContext(instrumentationContext)
            .setProgressReporter(progressReporter);
    }
}
