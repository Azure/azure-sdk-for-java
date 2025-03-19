package io.clientcore.core.http.models;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.ProgressReporter;

public class SdkRequestContext extends RequestContext {
    private InstrumentationContext childInstrumentationContext;

    public SdkRequestContext() {

    }

    private SdkRequestContext(RequestContext options) {
        super(options);

        this.childInstrumentationContext = options == null ? null : options.getInstrumentationContext();
    }

    public static SdkRequestContext fromRequestOptions(RequestContext options) {
        if (options instanceof SdkRequestContext) {
            return (SdkRequestContext) options;
        }

        return new SdkRequestContext(options);
    }

    public SdkRequestContext setInstrumentationContext(InstrumentationContext instrumentationContext) {
        SdkRequestContext cloned = (SdkRequestContext) super.clone();
        cloned.childInstrumentationContext = instrumentationContext;
        return cloned;
    }

    @Override
    public InstrumentationContext getInstrumentationContext() {
        if (childInstrumentationContext != null) {
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
