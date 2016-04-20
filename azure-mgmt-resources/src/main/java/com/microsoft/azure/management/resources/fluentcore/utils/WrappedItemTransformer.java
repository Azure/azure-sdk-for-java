package com.microsoft.azure.management.resources.fluentcore.utils;

public interface WrappedItemTransformer<SourceT, WrappedT> {
    WrappedT toWrapped(SourceT source);
    SourceT toSource(WrappedT wrapped);
}
