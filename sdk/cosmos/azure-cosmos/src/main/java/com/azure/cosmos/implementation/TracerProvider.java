// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.models.CosmosAsyncDatabaseResponse;
import com.azure.cosmos.models.CosmosResponse;
import reactor.core.publisher.Signal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TracerProvider {
    private final ClientLogger logger = new ClientLogger(TracerProvider.class);
    private final List<Tracer> tracers = new ArrayList<>();
    public final static String DB_TYPE_VALUE = "cosmosdb";
    public final static String DB_TYPE = "db.type";
    public final static String DB_INSTANCE = "db.instance";
    public final static String DB_URL = "db.url";
    public static final String DB_STATEMENT = "db.statement";
    public static final String ERROR_MSG = "error.msg";
    public static final String ERROR_TYPE = "error.type";
    public static final String ERROR_STACK = "error.stack";
    public static final String MASTER_CALL = "masterCall";
    public static final String NESTED_CALL = "nestedCall";

    public static final Object ATTRIBUTE_MAP = "span-attributes";


    public TracerProvider(Iterable<Tracer> tracers) {
        Objects.requireNonNull(tracers, "'tracers' cannot be null.");
        tracers.forEach(e -> this.tracers.add(e));
    }

    public boolean isEnabled() {
        return tracers.size() > 0;
    }

    /**
     * For each tracer plugged into the SDK a new tracing span is created.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    public Context startSpan(String methodName, Context context, ProcessKind processKind) {
        Context local = Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(processKind, "'processKind' cannot be null.");

        for (Tracer tracer : tracers) {
            local = tracer.start(methodName, local, processKind);
        }

        return local;
    }

    /**
     * Given a context containing the current tracing span the span is marked completed with status info from
     * {@link Signal}.  For each tracer plugged into the SDK the current tracing span is marked as completed.
     * @param context Additional metadata that is passed through the call stack.
     * @param signal The signal indicates the status and contains the metadata we need to end the tracing span.
     */
    public void endSpan(Context context, Signal<CosmosResponse> signal) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(signal, "'signal' cannot be null.");

        switch (signal.getType()) {
            case ON_COMPLETE:
                end("success", null, context);
                break;
            case ON_ERROR:
                String errorCondition = "";
                Throwable throwable = null;
                if (signal.hasError()) {
                    // The last status available is on error, this contains the thrown error.
                    throwable = signal.getThrowable();

                    if (throwable instanceof CosmosClientException) {
                        CosmosClientException exception = (CosmosClientException) throwable;
                        // confirm ?
                        errorCondition =exception.getError().getErrorDetails();
                    }
                }
                end(errorCondition, throwable, context);
                break;
            default:
                // ON_SUBSCRIBE and ON_NEXT don't have the information to end the span so just return.
                break;
        }
    }

    private void end(String statusMessage, Throwable throwable, Context context) {
        for (Tracer tracer : tracers) {
            if(throwable != null) {
                tracer.setAttribute(TracerProvider.ERROR_MSG,  throwable.getMessage(), context);
                tracer.setAttribute(TracerProvider.ERROR_TYPE,  throwable.getClass().getName(), context);
                StringWriter errorStack = new StringWriter();
                throwable.printStackTrace(new PrintWriter(errorStack));
                tracer.setAttribute(TracerProvider.ERROR_STACK,  errorStack.toString(), context);
            }
            tracer.end(statusMessage, throwable, context);
        }
    }
}
