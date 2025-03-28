// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.ProgressReporter;

import java.util.function.Consumer;

/**
 * This class contains the request metadata that flows along with the {@link HttpRequest} as it goes through HTTP pipeline.
 * It includes {@link RequestOptions} provided by the user along with metadata provided by client libraries and core components.
 * <p>
 * This class is intended to be used by client library code and HTTP pipeline components. Application developers should
 * configure request metadata using {@link RequestOptions}.
 * <p>
 * This class does not allow any modifications to the request metadata provided by the user.
 */
@Metadata(properties = { MetadataProperties.FLUENT, MetadataProperties.IMMUTABLE })
public final class SdkRequestContext {
    // TODO (limolkova) do we need to make it public and probably move to another class?
    private static final String PROGRESS_REPORTER_KEY = "progressReporter";
    static final SdkRequestContext NONE = new SdkRequestContext(null, null, null);
    private static final ClientLogger LOGGER = new ClientLogger(SdkRequestContext.class);
    private final ProgressReporter childProgressReporter;
    private final RequestOptions requestOptions;

    private SdkRequestContext(RequestOptions options, ProgressReporter progressReporter,
        InstrumentationContext instrumentationContext) {
        this.requestOptions = new RequestOptions(options, this);
        if (instrumentationContext != null) {
            this.requestOptions.setInstrumentationContext(instrumentationContext);
        }
        this.childProgressReporter = progressReporter;
    }

    /**
     * Creates a new instance of {@link SdkRequestContext} with the given options.
     * @param options The {@link RequestOptions} to be used.
     */
    SdkRequestContext(RequestOptions options) {
        this(options, null, null);
    }

    /**
     * Creates a new instance of {@link SdkRequestContext} with the given options.
     * @param options The {@link RequestOptions} to be used.
     * @return The {@link SdkRequestContext} from the given options.
     */
    public static SdkRequestContext from(RequestOptions options) {
        if (options != null && options.getRequestContext() != null) {
            return options.getRequestContext();
        }

        return new SdkRequestContext(options);
    }

    /**
     * Get the {@link RequestOptions} corresponding to this request context.
     * @return The {@link RequestOptions} corresponding to this request context.
     */
    public RequestOptions toRequestOptions() {
        return requestOptions;
    }

    /**
     * An empty {@link SdkRequestContext} used in situations where there is no request-specific
     * configuration to pass into the request.
     *
     * @return The singleton instance of an empty {@link RequestOptions}.
     */
    public static SdkRequestContext none() {
        return NONE;
    }

    /**
     * Get the {@link InstrumentationContext} from the request context.
     * @return The {@link InstrumentationContext} if present, otherwise null.
     */
    public InstrumentationContext getInstrumentationContext() {
        return requestOptions.getInstrumentationContext();
    }

    /**
     * Get the {@link ProgressReporter} from the request context.
     *
     * @return The {@link ProgressReporter} if present, otherwise null.
     */
    public ProgressReporter getProgressReporter() {
        if (childProgressReporter != null) {
            return childProgressReporter;
        }

        Object progressReporter = requestOptions.getMetadata(PROGRESS_REPORTER_KEY);
        if (progressReporter instanceof ProgressReporter) {
            return (ProgressReporter) progressReporter;
        }

        if (progressReporter != null) {
            LOGGER.atVerbose()
                .addKeyValue("actualType", progressReporter.getClass().getCanonicalName())
                .addKeyValue("contextKey", PROGRESS_REPORTER_KEY)
                .log("Unexpected object type. Ignoring it.");
        }

        return null;
    }

    /**
     * Set the {@link ProgressReporter} to be used for this request context.
     *
     * @param progressReporter The {@link ProgressReporter} to be used.
     * @return A new instance of {@link SdkRequestContext} with the given {@link ProgressReporter}.
     */
    public SdkRequestContext setProgressReporter(ProgressReporter progressReporter) {
        return new SdkRequestContext(this.requestOptions, progressReporter, null);
    }

    /**
     * Set the {@link InstrumentationContext} to be used for this request context.
     *
     * @param context The {@link InstrumentationContext} to be used.
     * @return A new instance of {@link SdkRequestContext} with the given {@link InstrumentationContext}.
     */
    public SdkRequestContext setInstrumentationContext(InstrumentationContext context) {
        return new SdkRequestContext(this.requestOptions, this.childProgressReporter, context);
    }

    /**
     * Gets the request callback, applying all the configurations set on this instance of {@link RequestOptions}.
     *
     * @return The request callback.
     */
    public Consumer<HttpRequest> getRequestCallback() {
        return requestOptions.getRequestCallback();
    }

    /**
     * Gets the {@link ClientLogger} used to log the request and response.
     *
     * @return The {@link ClientLogger} used to log the request and response.
     */
    public ClientLogger getLogger() {
        return requestOptions.getLogger();
    }

    /**
     * Gets the metadata associated with this request context.
     *
     * @param key The key of the metadata to retrieve.
     * @return The value of the metadata associated with the given key, or null if not found.
     */
    public Object getMetadata(String key) {
        return requestOptions.getMetadata(key);
    }
}
