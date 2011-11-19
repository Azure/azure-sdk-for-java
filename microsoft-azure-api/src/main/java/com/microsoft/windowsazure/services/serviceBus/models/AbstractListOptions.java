package com.microsoft.windowsazure.services.serviceBus.models;

public abstract class AbstractListOptions<T> {
    Integer skip;
    Integer top;

    public Integer getSkip() {
        return skip;
    }

    @SuppressWarnings("unchecked")
    public T setSkip(Integer skip) {
        this.skip = skip;
        return (T) this;
    }

    public Integer getTop() {
        return top;
    }

    @SuppressWarnings("unchecked")
    public T setTop(Integer top) {
        this.top = top;
        return (T) this;
    }
}
