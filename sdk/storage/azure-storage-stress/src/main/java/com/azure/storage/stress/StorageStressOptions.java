package com.azure.storage.stress;

import com.azure.core.http.HttpClient;
import com.azure.perf.test.core.PerfStressOptions;

public class StorageStressOptions extends PerfStressOptions {
    private HttpClient faultInjectingClient;
    private boolean useFaultInjection;

    public StorageStressOptions enableFaultInjection(boolean enable) {
        this.useFaultInjection = enable;
        return this;
    }

    public boolean isFaultInjectionEnabled() {
        return this.useFaultInjection;
    }
}
