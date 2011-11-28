package com.microsoft.windowsazure.services.core;


public interface FilterableService<T> {
    T withFilter(ServiceFilter filter);
}
