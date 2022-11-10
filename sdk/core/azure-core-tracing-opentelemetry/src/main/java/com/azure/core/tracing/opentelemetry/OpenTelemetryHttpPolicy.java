package com.azure.core.tracing.opentelemetry;

import com.azure.core.http.policy.AfterRetryPolicyProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.InstrumentationPolicy;

public class OpenTelemetryHttpPolicy implements AfterRetryPolicyProvider {

    @Override
    public HttpPipelinePolicy create() {
        return new InstrumentationPolicy("azure-core", null, null, null, null);
    }
}
