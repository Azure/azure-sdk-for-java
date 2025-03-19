package io.clientcore.core.http.models;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.utils.ProgressReporter;

public class SdkRequestContext extends RequestOptions {
    private final InstrumentationContext childInstrumentationContext;

    private SdkRequestContext(RequestOptions options, InstrumentationContext instrumentationContext) {
        super(options);

        this.childInstrumentationContext = instrumentationContext;
    }

    public static SdkRequestContext create(RequestOptions options, InstrumentationContext instrumentationContext) {
        if (options instanceof SdkRequestContext && (instrumentationContext == null || !instrumentationContext.isValid())) {
            return (SdkRequestContext) options;
        }

        return new SdkRequestContext(options, instrumentationContext);
    }

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

    public ProgressReporter getProgressReporter() {
        Object progressReporter = super.getData("progressReporter");
        if (progressReporter instanceof ProgressReporter) {
            return (ProgressReporter) progressReporter;
        }

        // TODO log something

        return null;
    }
}
