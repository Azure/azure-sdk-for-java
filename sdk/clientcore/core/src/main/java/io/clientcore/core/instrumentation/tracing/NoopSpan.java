package io.clientcore.core.instrumentation.tracing;

import io.clientcore.core.instrumentation.InstrumentationContext;

class NoopSpan implements Span {
    static final NoopSpan INSTANCE = new NoopSpan();
    private NoopSpan() {
    }
    private static final TracingScope NOOP_SCOPE = () -> {
    };

    @Override
    public Span setAttribute(String key, Object value) {
        return this;
    }

    @Override
    public Span setError(String errorType) {
        return this;
    }

    @Override
    public void end(Throwable throwable) {

    }

    @Override
    public void end() {

    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public TracingScope makeCurrent() {
        return NOOP_SCOPE;
    }

    @Override
    public InstrumentationContext getInstrumentationContext() {
        return NoopInstrumentationContext.INSTANCE;
    }
}
