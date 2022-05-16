package com.azure.core.metrics.micrometer;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.ClientLongHistogram;
import io.micrometer.core.instrument.DistributionSummary;

class MicrometerLongHistogram implements ClientLongHistogram {

    private final DistributionSummary summary;

    MicrometerLongHistogram(DistributionSummary summary) {
        this.summary = summary;
    }

    @Override
    public void record(long value, Context context) {
        summary.record(value);
    }
}
