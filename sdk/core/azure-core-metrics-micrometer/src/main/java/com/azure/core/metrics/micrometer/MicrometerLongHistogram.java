package com.azure.core.metrics.micrometer;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongHistogram;
import io.micrometer.core.instrument.DistributionSummary;

class MicrometerLongHistogram implements AzureLongHistogram {

    private final DistributionSummary summary;

    MicrometerLongHistogram(DistributionSummary summary) {
        this.summary = summary;
    }

    @Override
    public void record(long value, Context context) {
        summary.record(value);
    }
}
