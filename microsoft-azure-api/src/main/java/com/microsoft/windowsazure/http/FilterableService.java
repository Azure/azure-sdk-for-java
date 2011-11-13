package com.microsoft.windowsazure.http;

public interface FilterableService<T> {
    T withFilter(ServiceFilter filter);
}
