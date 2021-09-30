// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.trace.sleuth;

import com.azure.spring.tracing.sleuth.SleuthTracer;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;

import static com.azure.spring.cloud.autoconfigure.trace.sleuth.AzureApplicationContext.getBean;

public class AzureSpringSleuthTracer extends SleuthTracer {

    public AzureSpringSleuthTracer() {
        super(getBean(Tracer.class),
            getBean(CurrentTraceContext.class),
            getBean(Propagator.class));
    }
}
