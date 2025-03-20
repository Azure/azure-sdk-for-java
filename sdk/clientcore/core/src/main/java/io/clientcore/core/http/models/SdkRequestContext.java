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
    static final SdkRequestContext NONE = new SdkRequestContext(null, null);
    private static final ClientLogger LOGGER = new ClientLogger(SdkRequestContext.class);
    private final InstrumentationContext childInstrumentationContext;

    private SdkRequestContext(RequestOptions options, InstrumentationContext instrumentationContext) {
        super(options);

        this.childInstrumentationContext = instrumentationContext;
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

        return new SdkRequestContext(options, instrumentationContext);
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

        return new SdkRequestContext(options, null);
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
        throwOnImmutable("Cannot put data");
        return this;
    }

    @Override
    public RequestOptions setLogger(ClientLogger logger) {
        throwOnImmutable("Cannot set logger");
        return this;
    }

    @Override
    public RequestOptions setInstrumentationContext(InstrumentationContext instrumentationContext) {
        throwOnImmutable("Cannot set instrumentation context");
        return this;
    }

    @Override
    public RequestOptions addRequestCallback(Consumer<HttpRequest> requestCallback) {
        throwOnImmutable("Cannot add request callback");
        return this;
    }

    @Override
    public RequestOptions addQueryParam(String name, String value) {
        throwOnImmutable("Cannot add query param");
        return this;
    }

    @Override
    public RequestOptions addQueryParam(String parameterName, String value, boolean encoded) {
        throwOnImmutable("Cannot add query param");
        return this;
    }

    private void throwOnImmutable(String message) {
        throw LOGGER.logThrowableAsError(
            new IllegalStateException(message + ". This instance of RequestOptions is immutable."));
    }
}
