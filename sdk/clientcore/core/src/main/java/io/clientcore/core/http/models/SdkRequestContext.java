// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

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
public final class SdkRequestContext extends RequestOptions {
    // TODO (limolkova) do we need to make it public and probably move to another class?
    private static final String PROGRESS_REPORTER_KEY = "progressReporter";
    static final SdkRequestContext NONE = new SdkRequestContext(null, null, null);
    private static final ClientLogger LOGGER = new ClientLogger(SdkRequestContext.class);
    private final InstrumentationContext childInstrumentationContext;
    private final ProgressReporter childProgressReporter;

    private SdkRequestContext(RequestOptions options, InstrumentationContext instrumentationContext,
        ProgressReporter progressReporter) {
        super(options);

        this.childInstrumentationContext = instrumentationContext;
        this.childProgressReporter = progressReporter;
    }

    /**
     * Creates a new instance of {@link SdkRequestContext} with the given options and instrumentation context.
     *
     * @param options The {@link RequestOptions} to be used.
     * @param instrumentationContext The {@link InstrumentationContext} to be used.
     * @return A new instance of {@link SdkRequestContext} or, when optimization is possible, the existing instance of {@link SdkRequestContext}
     * provided in the {@code options} parameter.
     */
    public static SdkRequestContext create(RequestOptions options, InstrumentationContext instrumentationContext) {
        if (options instanceof SdkRequestContext
            && (instrumentationContext == null || !instrumentationContext.isValid())) {
            return (SdkRequestContext) options;
        }

        return new SdkRequestContext(options, instrumentationContext, null);
    }

    /**
     * Creates a new instance of {@link SdkRequestContext} with the given options and progress reporter.
     *
     * @param options The {@link RequestOptions} to be used.
     * @param progressReporter The {@link ProgressReporter} to be used.
     * @return A new instance of {@link SdkRequestContext} or, when optimization is possible, the existing instance of {@link SdkRequestContext}
     * provided in the {@code options} parameter.
     */
    public static SdkRequestContext create(RequestOptions options, ProgressReporter progressReporter) {
        return new SdkRequestContext(options, null, progressReporter);
    }

    /**
     * Creates a new instance of {@link SdkRequestContext} with the given options.
     * @param options The {@link RequestOptions} to be used.
     * @return A new instance of {@link SdkRequestContext} or, when optimization is possible, the existing instance of {@link SdkRequestContext}
     * provided in the {@code options} parameter.
     */
    public static SdkRequestContext create(RequestOptions options) {
        if (options instanceof SdkRequestContext) {
            return (SdkRequestContext) options;
        }

        return new SdkRequestContext(options, null, null);
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

    @Override
    public InstrumentationContext getInstrumentationContext() {
        if (childInstrumentationContext != null && childInstrumentationContext.isValid()) {
            return childInstrumentationContext;
        }

        return super.getInstrumentationContext();
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

        Object progressReporter = super.getData(PROGRESS_REPORTER_KEY);
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

    @Override
    public RequestOptions putData(String key, Object value) {
        throw throwOnImmutable("Cannot put data");
    }

    @Override
    public RequestOptions setLogger(ClientLogger logger) {
        throw throwOnImmutable("Cannot set logger");
    }

    @Override
    public RequestOptions setInstrumentationContext(InstrumentationContext instrumentationContext) {
        throw throwOnImmutable("Cannot set instrumentation context");
    }

    @Override
    public RequestOptions addRequestCallback(Consumer<HttpRequest> requestCallback) {
        throw throwOnImmutable("Cannot add request callback");
    }

    @Override
    public RequestOptions addQueryParam(String name, String value) {
        throw throwOnImmutable("Cannot add query param");
    }

    @Override
    public RequestOptions addQueryParam(String parameterName, String value, boolean encoded) {
        throw throwOnImmutable("Cannot add query param");
    }

    private IllegalStateException throwOnImmutable(String message) {
        return LOGGER.logThrowableAsError(new IllegalStateException(message + ". This instance is immutable."));
    }
}
