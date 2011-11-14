package com.microsoft.windowsazure.common;


public interface FilterableService<T> {
    T withFilter(ServiceFilter filter);
}
