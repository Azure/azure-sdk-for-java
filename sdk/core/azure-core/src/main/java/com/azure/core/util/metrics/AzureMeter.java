package com.azure.core.util.metrics;

import java.util.Map;

public interface AzureMeter {
    AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes);
    AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes);
    boolean isEnabled();
}
