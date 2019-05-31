package com.azure.core.util;

public class ContextManager {

    private ContextManager() { }

    public static ThreadLocalContext newContext() {
        // FIXME
        // Two issues: 1) We don't have any values to place here, and 2) Contexts know their parent, but not their child
        return new ThreadLocalContext(null, null, null);
    }
}
