package com.azure.core.metrics.micrometer;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.ClientLongCounter;
import io.micrometer.core.instrument.Counter;

class MicrometerLongCounter implements ClientLongCounter {

    private final Counter counter;

    MicrometerLongCounter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void add(long value, Context context) {
        counter.increment(value);
    }
}
